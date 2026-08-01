package com.sampleselenium.drills.d12_postman_promotion;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DRILL 12 — PROMOTING AN EXPLORATORY POSTMAN COLLECTION INTO THE CODED SUITE
 * [SOURCE — read, close, reproduce]
 *
 * NO BROWSER, NO INTERNET — a mock quotes service (MockQuoteApi, JDK HttpServer) starts
 * on a random local port before each test:
 *   mvn test -Dtest=SourceD12PostmanPromotionDrills
 *
 * Answers the interview question directly: "You have a large Postman collection for
 * exploratory API work — how should it relate to the automated suite?" (Builders Mutual
 * JD gap: Postman/Newman vs coded suites.)
 *
 * ======================= THE MODEL ANSWER, MADE RUNNABLE =======================
 * Postman stays the exploration layer; the coded suite is the regression layer. A request
 * graduates from one to the other when it clears a checklist — this file proves each item:
 *   1. CONTRACT-STABLE  — the endpoint's shape isn't still changing week to week.
 *   2. AUTH SCRIPTABLE  — no manual token copy-paste; credentials come from env/config,
 *                          never hardcoded (see how MockQuoteApi needs none here — when a
 *                          real endpoint does, that value is an env var, not a literal).
 *   3. NEGATIVE CASES KNOWN — not just happy path. THE POINT OF THIS DRILL: the fixture
 *                          collection below only has the two requests a human clicked
 *                          "Send" on. Section 3 adds the negative/boundary cases that
 *                          collection never exercised.
 *   4. SCHEMA STABLE    — status, headers, and JSON shape are asserted, not eyeballed.
 *   5. TEST DATA REPRODUCIBLE — hits a local mock, not a shared live environment, so the
 *                          suite is safe to run on every PR.
 *   6. EARNS ITS PLACE IN THE FAST SUITE — no sleeps, no external network; runs in ~1s,
 *                          so it belongs in the PR-blocking tier, not a slow nightly one.
 * A collection that fails item 1 or 2 stays in Postman as exploration — that's fine, not
 * everything needs to graduate yet.
 *
 * SECTIONS TO REPRODUCE:
 *   1. Load the exported collection with JsonPath and assert what it actually contains —
 *      "read what Postman already proved" before porting anything.
 *   2. Port the two happy-path requests 1:1 into REST Assured (same status/body Postman saw).
 *   3. Add the negative/boundary cases the collection never had — the real payoff of promotion.
 */
class SourceD12PostmanPromotionDrills {

    private static final String COLLECTION_PATH = "/postman/quotes-exploration.postman_collection.json";

    private MockQuoteApi api;

    @BeforeEach
    void startMockService() throws IOException {
        api = new MockQuoteApi();
        RestAssured.baseURI = api.start();
    }

    @AfterEach
    void stopMockService() {
        api.stop();
        RestAssured.reset();
    }

    /**
     * SECTION 1 — read the exported collection before touching any code. JsonPath (bundled
     * with REST Assured) parses the raw Postman export directly; no separate JSON library,
     * no POJO mapping needed just to inspect it.
     */
    @Test
    void exportedCollectionOnlyCoversTheTwoHappyPathRequests() {
        // JsonPath parses lazily on first access, so the source must be read to a String
        // (or otherwise kept open) before the stream backing it is closed.
        String json;
        try (InputStream in = getClass().getResourceAsStream(COLLECTION_PATH)) {
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JsonPath collection = new JsonPath(json);

        List<String> requestNames = collection.getList("item.name");
        assertEquals(List.of("Get Quote by Symbol", "Create Quote"), requestNames,
                "the collection is exploratory: two clicks in Postman, happy path only");
        assertEquals("GET", collection.getString("item[0].request.method"));
        assertEquals("POST", collection.getString("item[1].request.method"));
        // Nothing in the export exercises a 4xx — that gap is exactly what section 3 closes.
    }

    /**
     * SECTION 2 — port "Get Quote by Symbol" out of Postman verbatim: same method, same
     * path, same expected status. This is the boring, mechanical half of promotion.
     */
    @Test
    void getQuoteReturns200MatchingWhatPostmanExplored() {
        given()
                .accept(ContentType.JSON)
        .when()
                .get("/api/quotes/AAPL")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("symbol", equalTo("AAPL"))
                .body("price", equalTo(189.50f));
    }

    /**
     * SECTION 2 (cont.) — port "Create Quote" out of Postman verbatim. Still worth a real
     * assertion, not just "200-ish": a created resource is 201 with a Location header —
     * the same 200-vs-201 point from the d09 API drills.
     */
    @Test
    void createQuoteReturns201WithLocationHeaderMatchingWhatPostmanExplored() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"symbol":"MSFT","price":425.10}""")
        .when()
                .post("/api/quotes")
        .then()
                .statusCode(201)
                .header("Location", equalTo("/api/quotes/MSFT"))
                .body("symbol", equalTo("MSFT"));
    }

    /**
     * SECTION 3 — THE PAYOFF. Nobody clicked "Send" on this in Postman: an unknown symbol.
     * Promotion means adding the case exploration skipped, not just replaying what it did.
     */
    @Test
    void getUnknownQuoteReturns404WithErrorBodyPostmanNeverTried() {
        given()
        .when()
                .get("/api/quotes/ZZZZ")
        .then()
                .statusCode(404)
                .body("error", equalTo("quote not found"))
                .body("path", equalTo("/api/quotes/ZZZZ"));
    }

    /**
     * SECTION 3 (cont.) — a malformed/incomplete body is the other negative case exploratory
     * clicking rarely bothers with. Promotion asserts the API rejects it correctly.
     */
    @Test
    void createQuoteMissingPriceReturns400WithValidationErrorPostmanNeverTried() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"symbol":"MSFT"}""")
        .when()
                .post("/api/quotes")
        .then()
                .statusCode(400)
                .body("error", containsString("price"));
    }
}
