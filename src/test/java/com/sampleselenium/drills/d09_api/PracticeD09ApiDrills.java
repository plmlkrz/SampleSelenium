package com.sampleselenium.drills.d09_api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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

    @Disabled("delete this line, then write the body from memory")
    @Test
    void getTradeReturns200WithExpectedJsonBody() {
        // TODO: given().accept(JSON).when().get("/api/trades/1001").then()
        //       assert statusCode 200, contentType JSON, and body fields:
        //       tradeId=1001, instrument=AAPL, side=BUY, price=189.50f, status=SETTLED
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void postNewTradeReturns201CreatedWithLocationHeader() {
        // TODO: POST a JSON trade body to /api/trades with header X-API-KEY
        //       assert 201 (NOT 200!), Location header notNullValue(),
        //       extract the Location header and assertEquals("/api/trades/1002", ...)
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void postWithoutApiKeyReturns401() {
        // TODO: same POST but WITHOUT the X-API-KEY header
        //       assert 401 and body "error" containsString("X-API-KEY")
        //       (say out loud: 401 = who are you, 403 = you may not)
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void getUnknownTradeReturns404WithErrorBody() {
        // TODO: GET /api/trades/9999 -> 404, body error="trade not found", path echoes the URI
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void putReturns200WithBodyAndDeleteReturns204Empty() {
        // TODO: PUT /api/trades/1001 with a JSON body -> 200, status=CANCELLED
        //       DELETE /api/trades/1001 -> 204, extract the Response and assert body is ""
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void portfolioValuationJsonRecomputesToItsOwnTotals() {
        // TODO: GET /api/portfolio/valuation -> 200, positions hasSize(3)
        //       extract lists: positions.quantity, positions.price, positions.marketValue
        //       loop: assert marketValue[i] == quantity[i] * price[i] (delta 0.01)
        //       sum them and assert equals totals.grossMarketValue and totals.positionCount
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void valuationEndpointRespondsWithinSla() {
        // TODO: GET the valuation and assert .time(lessThan(2L), TimeUnit.SECONDS)
    }
}
