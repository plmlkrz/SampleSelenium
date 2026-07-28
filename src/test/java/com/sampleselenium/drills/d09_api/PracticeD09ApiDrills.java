package com.sampleselenium.drills.d09_api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 09 — PRACTICE FILE
 *
 * 1. Read SourceD09ApiDrills.java carefully. Then CLOSE it — no peeking.
 * 2. Pick a test below, delete its @Disabled line, and write the body from memory.
 * 3. Run:  mvn test -Dtest=PracticeD09ApiDrills          (no browser needed — runs in ~1s)
 * 4. Compare with the source. Note what you missed. Repeat until clean.
 *
 * KEEP THIS FILE COMPILING — a syntax error here blocks the whole project.
 * Quick syntax check:  mvn test-compile
 *
 * SAY OUT LOUD WHILE TYPING (the answers live in the source file's comments):
 *   - 200 vs 201, and why 201 must carry a Location header
 *   - 401 vs 403
 *   - Which methods are idempotent and why POST is not
 *   - The Spring Boot layers: @WebMvcTest+MockMvc vs @SpringBootTest vs black-box REST Assured
 *
 * SECTIONS TO REPRODUCE:
 *   1. GET  200: statusCode + contentType + body jsonPath assertions in one chain
 *   2. POST 201 Created + Location header (the headline drill)
 *   3. POST without X-API-KEY -> 401
 *   4. GET unknown id -> 404 + error body
 *   5. PUT 200 with body, DELETE 204 with EMPTY body
 *   6. Valuation JSON: recompute quantity*price per position and the grand total
 *   7. Response time under an SLA
 */
class PracticeD09ApiDrills {

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


    @Test
    void getTradeReturns200WithExpectedJsonBody() {
        // TODO: given().accept(JSON).when().get("/api/trades/1001").then()
        //       assert statusCode 200, contentType JSON, and body fields:
        //       tradeId=1001, instrument=AAPL, side=BUY, price=189.50f, status=SETTLED
        given()
                .accept(ContentType.JSON)
        .when()
                .get("api/trades/1001")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("tradeId", equalTo(1001))
                .body("instrument", equalTo("AAPL"))
                .body("side", equalTo("BUY"))
                .body("price", equalTo(189.50f))
                .body("status", equalTo("SETTLED"));
    }


    @Test
    void postNewTradeReturns201CreatedWithLocationHeader() {
        // TODO: POST a JSON trade body to /api/trades with header X-API-KEY
        //       assert 201 (NOT 200!), Location header notNullValue(),
        //       extract the Location header and assertEquals("/api/trades/1002", ...)
        String locationOfNewTrade =
        given()
                .header("X-API-KEY", "drill-key")
                .contentType(ContentType.JSON)
                .body("""
                          {"instrument": "MSFT", "side": "SELL","quantity": 50, "price": 425.10}""")
        .when()
                .post("/api/trades")
        .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("tradeId", equalTo(1002))
                .body("status", equalTo("NEW"))
                .extract().header("Location");
        assertEquals("/api/trades/1002", locationOfNewTrade);


    }


    @Test
    void postWithoutApiKeyReturns401() {
        // TODO: same POST but WITHOUT the X-API-KEY header
        //       assert 401 and body "error" containsString("X-API-KEY")
        //       (say out loud: 401 = who are you, 403 = you may not)
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"instrument": "MSFT", "side": "SELL","quantity": 50, "price": 425.10}""")
        .when()
                .post("/api/trades")
        .then()
                .statusCode(401)
                .body("error", containsString("X-API-KEY"));
    }


    @Test
    void getUnknownTradeReturns404WithErrorBody() {
        // TODO: GET /api/trades/9999 -> 404, body error="trade not found", path echoes the URI
        given()
        .when()
                .get("api/trades/9999")
        .then()
                .statusCode(404)
                .body("error", equalTo("trade not found"))
                .body("path", equalTo("/api/trades/9999"));
    }


    @Test
    void putReturns200WithBodyAndDeleteReturns204Empty() {
        // TODO: PUT /api/trades/1001 with a JSON body -> 200, status=CANCELLED
        //       DELETE /api/trades/1001 -> 204, extract the Response and assert body is ""
        given()
                .contentType(ContentType.JSON)
                .body("""
                      {"status": "CANCELLED"}""")
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
    }


    @Test
    void portfolioValuationJsonRecomputesToItsOwnTotals() {
        // TODO: GET /api/portfolio/valuation -> 200, positions hasSize(3)
        //       extract lists: positions.quantity, positions.price, positions.marketValue
        //       loop: assert marketValue[i] == quantity[i] * price[i] (delta 0.01)
        //       sum them and assert equals totals.grossMarketValue and totals.positionCount
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
                    "position" + i + ": marketValue must equal quantity * price");
            recomputedTotal += expected;
        }
        double reportedTotal = response.jsonPath().getDouble("totals.grossMarketValue");
        assertEquals(recomputedTotal, reportedTotal, 0.01,
                "grossMarketValue must equal the sum of position market values");
        assertEquals(quantities.size(), response.jsonPath().getInt("totals.positionCount"));
        }




    @Test
    void valuationEndpointRespondsWithinSla() {
        // TODO: GET the valuation and assert .time(lessThan(2L), TimeUnit.SECONDS)
        given()
        .when()
                .get("/api/portfolio/valuation")
        .then()
                .statusCode(200)
                .time(lessThan(2L), TimeUnit.SECONDS);
    }

//    @Test
//    void postAndDeserializeTrade(){
//        TradeRequest request = new TradeRequest("AAPL", "BUY", "10", new BigDecimal("158.50"));
//        TradeResult result = given()
//                .header("X-API-KEY", "drill-key")
//                .contentType(ContentType.JSON)
//                .body(request)
//        .when()
//                .post("/api/trades")
//        .then()
//                .statusCode(201)
//                .header("Location", notNullValue())
//                .contentType(ContentType.JSON)
//                .extract().as(Trade.class);
//        assertEquals(0, new BigDecimal("158.50").compareTo(result.getPrice()));
//        assertEquals("NEW", result.getStatus());
//    }
}
