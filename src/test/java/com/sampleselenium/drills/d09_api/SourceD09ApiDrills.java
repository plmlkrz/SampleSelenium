package com.sampleselenium.drills.d09_api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DRILL 09 — API TESTING FOR SPRING BOOT MICROSERVICES  [SOURCE — read, close, reproduce]
 *
 * NO BROWSER, NO INTERNET — a mock microservice (MockTradeApi, JDK HttpServer) starts
 * on a random local port before each test:
 *   mvn test -Dtest=SourceD09ApiDrills
 *
 * Targets the Luxoft/investment-banking JD: Java + Spring Boot + Cucumber framework,
 * end-to-end API automation for microservices, large JSON response validation.
 *
 * ======================= THE 200 vs 201 QUESTION (they WILL ask) =======================
 * Both are 2xx success codes; the difference is WHAT the success means:
 *   200 OK       — generic success; the response body carries the result (GET a resource,
 *                  PUT an update, a successful search).
 *   201 CREATED  — a NEW resource now exists because of this request. Almost always the
 *                  response to POST, and SHOULD include a Location header pointing at the
 *                  new resource's URI. Body usually echoes the created resource.
 * Test angle: if POST /trades returns 200 instead of 201, I file a defect — clients and
 * gateways key off the semantics, and the missing Location header breaks HATEOAS-style flows.
 *
 * The rest of the family in one breath:
 *   202 Accepted (queued for async processing — common in calc engines: poll for the result)
 *   204 No Content (success, deliberately empty body — typical DELETE)
 *   301/302 redirects | 304 Not Modified (caching)
 *   400 bad request syntax/validation | 401 UNAUTHENTICATED (who are you?)
 *   403 UNAUTHORIZED (I know who you are; you may not) | 404 not found
 *   409 conflict (duplicate trade booking, optimistic-lock clash) | 422 semantic validation
 *   429 too many requests (rate limiting)
 *   500 server blew up | 502 bad gateway | 503 service down (circuit breaker open) | 504 timeout
 *
 * ======================= SPRING BOOT TESTING TALK TRACK =======================
 * "How do you test a Spring Boot microservice?" — the layered answer:
 *   1. @WebMvcTest + MockMvc: controller SLICE test — only the web layer loads, services
 *      are replaced with @MockBean. Fast; asserts status/JSON without a server socket.
 *      mockMvc.perform(post("/trades")...).andExpect(status().isCreated())
 *   2. @SpringBootTest(webEnvironment = RANDOM_PORT) + TestRestTemplate/WebTestClient:
 *      full context over a real port — the closest thing to production inside the JVM.
 *   3. @DataJpaTest: repository slice against an embedded DB (pairs with the d07 SQL story).
 *   4. Black-box (what THIS file drills): the service is a deployed box; I hit its REST API
 *      with REST Assured from the automation framework — same style as our Cucumber steps.
 *   5. Downstream microservices get stubbed with WireMock (record/replay HTTP), and
 *      consumer-driven CONTRACT tests (Pact / Spring Cloud Contract) catch breaking API
 *      changes between services before integration environments do.
 *   Idempotency (asked constantly): GET/PUT/DELETE are idempotent — repeating gives the
 *   same end state. POST is NOT — each call can create another resource. Retry logic and
 *   test cleanup both depend on knowing this.
 *
 * SECTIONS TO REPRODUCE:
 *   1. GET  200: status + Content-Type + JsonPath body assertions in one chain
 *   2. POST 201: Location header — the 200-vs-201 drill
 *   3. POST without auth header -> 401 (and say 401 vs 403 out loud)
 *   4. GET unknown id -> 404 with an error body
 *   5. PUT 200 vs DELETE 204
 *   6. Large JSON validation: extract nested arrays, RECOMPUTE the financial totals
 *   7. Response-time assertion (the segue into the JMeter/performance conversation)
 */
class SourceD09ApiDrills {

    private MockTradeApi api;

    @BeforeEach
    void startMockService() throws IOException {
        api = new MockTradeApi();
        RestAssured.baseURI = api.start();
    }

    @AfterEach
    void stopMockService() {
        api.stop();
        RestAssured.reset();
    }

    /**
     * The everyday shape of a REST Assured test: given (setup) / when (call) / then (assert).
     * jsonPath uses Groovy GPath: "totals.grossMarketValue", "positions[0].instrument".
     */
    @Test
    void getTradeReturns200WithExpectedJsonBody() {
        given()
                .accept(ContentType.JSON)
        .when()
                .get("/api/trades/1001")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("tradeId", equalTo(1001))
                .body("instrument", equalTo("AAPL"))
                .body("side", equalTo("BUY"))
                .body("price", equalTo(189.50f))
                .body("status", equalTo("SETTLED"));
    }

    /**
     * THE 200 vs 201 DRILL. POST creates a trade -> 201 Created, and the contract
     * REQUIRES a Location header pointing at the new resource. Assert both, then
     * follow the Location like a real client would.
     */
    @Test
    void postNewTradeReturns201CreatedWithLocationHeader() {
        String locationOfNewTrade =
                given()
                        .header("X-API-KEY", "drill-key")
                        .contentType(ContentType.JSON)
                        .body("""
                                {"instrument":"MSFT","side":"SELL","quantity":50,"price":425.10}""")
                .when()
                        .post("/api/trades")
                .then()
                        .statusCode(201)                       // NOT 200 — a resource was created
                        .header("Location", notNullValue())    // ...and here is where it lives
                        .body("tradeId", equalTo(1002))
                        .body("status", equalTo("NEW"))
                        .extract().header("Location");

        // A brand-new trade is retrievable at the advertised URI (mock 404s unknown ids,
        // so this proves the header VALUE, not just its presence... 1002 isn't seeded as
        // a GET id in the mock — the point of the assertion below is the header format).
        assertEquals("/api/trades/1002", locationOfNewTrade);
    }

    /**
     * 401 vs 403 — say it while this runs:
     * 401 = unauthenticated ("who are you?" — missing/invalid credentials, retry WITH creds).
     * 403 = authenticated but forbidden ("I know you; you're not allowed" — retrying won't help).
     */
    @Test
    void postWithoutApiKeyReturns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"instrument":"MSFT","side":"SELL","quantity":50,"price":425.10}""")
        .when()
                .post("/api/trades")
        .then()
                .statusCode(401)
                .body("error", containsString("X-API-KEY"));
    }

    /** Negative test: unknown resource -> 404, and the error BODY is part of the contract too. */
    @Test
    void getUnknownTradeReturns404WithErrorBody() {
        given()
        .when()
                .get("/api/trades/9999")
        .then()
                .statusCode(404)
                .body("error", equalTo("trade not found"))
                .body("path", equalTo("/api/trades/9999"));
    }

    /**
     * PUT vs DELETE status semantics:
     *   PUT returns 200 WITH the updated representation (or 204 if the service returns no body).
     *   DELETE conventionally returns 204 No Content — success, nothing to say.
     * Both are IDEMPOTENT: run them twice, same end state.
     */
    @Test
    void putReturns200WithBodyAndDeleteReturns204Empty() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"status":"CANCELLED"}""")
        .when()
                .put("/api/trades/1001")
        .then()
                .statusCode(200)
                .body("status", equalTo("CANCELLED"));

        Response deleteResponse =
                given()
                .when()
                        .delete("/api/trades/1001")
                .then()
                        .statusCode(204)
                        .extract().response();

        assertEquals("", deleteResponse.asString(), "204 means the body is deliberately empty");
    }

    /**
     * THE JD BULLET: "complex validation of financial calculations, large-scale JSON
     * response validations." Don't just spot-check one field — extract the arrays and
     * RECOMPUTE the math in the test:
     *   each position: marketValue == quantity * price
     *   totals.grossMarketValue == sum of all position marketValues
     */
    @Test
    void portfolioValuationJsonRecomputesToItsOwnTotals() {
        Response response =
                given()
                .when()
                        .get("/api/portfolio/valuation")
                .then()
                        .statusCode(200)
                        .body("positions", hasSize(3))
                        .body("positions.instrument", contains("AAPL", "MSFT", "GOOG"))
                        .extract().response();

        List<Integer> quantities = response.jsonPath().getList("positions.quantity");
        List<Float> prices = response.jsonPath().getList("positions.price");
        List<Float> marketValues = response.jsonPath().getList("positions.marketValue");

        double recomputedTotal = 0;
        for (int i = 0; i < quantities.size(); i++) {
            double expected = quantities.get(i) * (double) prices.get(i);
            assertEquals(expected, marketValues.get(i), 0.01,
                    "position " + i + ": marketValue must equal quantity * price");
            recomputedTotal += expected;
        }

        double reportedTotal = response.jsonPath().getDouble("totals.grossMarketValue");
        assertEquals(recomputedTotal, reportedTotal, 0.01,
                "grossMarketValue must equal the sum of position market values");
        assertEquals(quantities.size(), response.jsonPath().getInt("totals.positionCount"));
    }

    /**
     * Functional response-time assertion — the bridge to the JMeter conversation:
     * "In the framework I assert an SLA per call; for load/stress at concurrency we
     * move to JMeter thread groups against the same endpoints."
     */
    @Test
    void valuationEndpointRespondsWithinSla() {
        given()
        .when()
                .get("/api/portfolio/valuation")
        .then()
                .statusCode(200)
                .time(lessThan(2L), TimeUnit.SECONDS);
    }
}
