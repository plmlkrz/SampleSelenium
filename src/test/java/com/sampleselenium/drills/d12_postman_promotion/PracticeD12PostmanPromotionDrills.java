package com.sampleselenium.drills.d12_postman_promotion;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DRILL 12 — PRACTICE FILE
 *
 * 1. Read SourceD12PostmanPromotionDrills.java carefully. Then CLOSE it — no peeking.
 * 2. Pick a test below, delete its @Disabled line, and write the body from memory.
 * 3. Run:  mvn test -Dtest=PracticeD12PostmanPromotionDrills   (no browser needed — runs in ~1s)
 * 4. Compare with the source. Note what you missed. Repeat until clean.
 *
 * KEEP THIS FILE COMPILING — a syntax error here blocks the whole project.
 * Quick syntax check:  mvn test-compile
 *
 * SAY OUT LOUD WHILE TYPING (the answers live in the source file's comments):
 *   - Postman = exploration layer, coded suite = regression layer — say why duplication
 *     is worth it (versioned, CI-enforced, reviewable) vs. Newman-in-CI (no porting, but
 *     assertions stay JSON, harder to review/refactor)
 *   - The promotion checklist: contract-stable, auth scriptable, negative cases known,
 *     schema stable, reproducible test data, earns its place in the fast suite
 *   - Why section 1 reads the collection BEFORE porting anything — you assert what
 *     exploration actually covered, not what you assume it covered
 *   - Why section 3 (unknown symbol, missing price) is the real payoff, not section 2
 *
 * SECTIONS TO REPRODUCE:
 *   1. Load the exported collection with JsonPath, assert it only has the two happy-path
 *      requests ("Get Quote by Symbol" GET, "Create Quote" POST) — no negative cases.
 *   2. Port both happy-path requests 1:1: GET AAPL -> 200 + body, POST -> 201 + Location.
 *   3. Add the negative/boundary cases the collection never had: GET unknown symbol -> 404,
 *      POST missing price -> 400.
 */
class PracticeD12PostmanPromotionDrills {

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

    @Disabled("delete this line, then write the body from memory")
    @Test
    void exportedCollectionOnlyCoversTheTwoHappyPathRequests() {
        // TODO: open the classpath resource at COLLECTION_PATH, read it fully to a String
        //       (JsonPath parses lazily on first access, so the stream must already be
        //       closed and its bytes captured — reading to a String first avoids that trap)
        //       wrap it: new JsonPath(jsonString)
        //       assert collection.getList("item.name") equals ["Get Quote by Symbol", "Create Quote"]
        //       assert collection.getString("item[0].request.method") equals "GET"
        //       assert collection.getString("item[1].request.method") equals "POST"
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void getQuoteReturns200MatchingWhatPostmanExplored() {
        // TODO: GET /api/quotes/AAPL -> statusCode(200), contentType JSON
        //       body("symbol", equalTo("AAPL")), body("price", equalTo(189.50f))
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void createQuoteReturns201WithLocationHeaderMatchingWhatPostmanExplored() {
        // TODO: POST /api/quotes with body {"symbol":"MSFT","price":425.10}
        //       -> statusCode(201), header("Location", equalTo("/api/quotes/MSFT"))
        //       body("symbol", equalTo("MSFT"))
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void getUnknownQuoteReturns404WithErrorBodyPostmanNeverTried() {
        // TODO: GET /api/quotes/ZZZZ -> statusCode(404)
        //       body("error", equalTo("quote not found")), body("path", equalTo("/api/quotes/ZZZZ"))
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void createQuoteMissingPriceReturns400WithValidationErrorPostmanNeverTried() {
        // TODO: POST /api/quotes with body {"symbol":"MSFT"} (no price)
        //       -> statusCode(400), body("error", containsString("price"))
    }
}
