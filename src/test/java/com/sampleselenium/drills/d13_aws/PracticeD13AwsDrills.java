package com.sampleselenium.drills.d13_aws;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 13 — PRACTICE FILE
 *
 * PREREQUISITE: your own deployed endpoint. See aws/RUNBOOK.md.
 *
 *   $env:AWS_FN_URL='https://xxxx.lambda-url.us-east-1.on.aws'
 *   $env:AWS_API_KEY='...'
 *
 * 1. Read SourceD13AwsDrills.java carefully. Then CLOSE it — no peeking.
 * 2. Pick a test below, delete its @Disabled line, and write the body from memory.
 * 3. Run:  mvn test -Dtest=PracticeD13AwsDrills
 * 4. Compare with the source. Note what you missed. Repeat until clean.
 *
 * KEEP THIS FILE COMPILING — a syntax error here blocks the whole project.
 * Quick syntax check:  mvn test-compile
 *
 * SAY OUT LOUD WHILE TYPING (the answers live in the source file's comments):
 *   - 401 vs 403, and why the service checks auth BEFORE parsing the body
 *   - What a 201 plus a Location header is claiming about the world
 *   - Cold start: why a threshold that passes at 2pm fails at 9am
 *   - S3 read-after-write consistency, and what changed in Dec 2020
 *   - The five-step cloud triage: client, did-it-reach-the-code, traceback, isolate, request id
 *   - What the execution role can and cannot do, from memory, without opening the policy
 *
 * SECTIONS TO REPRODUCE:
 *   1. GET / -> 200 health check
 *   2. POST /notes -> 201 + Location header matching /notes/{id}
 *   3. Round trip: POST, then GET the created id, assert the text survived
 *   4. Missing key -> 401; wrong key -> 401
 *   5. Invalid body -> 400 (missing text, blank text, over the 500-char cap)
 *   6. Unknown id -> 404
 *   7. Warm-up call, then a response-time assertion
 */
class PracticeD13AwsDrills {

    @BeforeEach
    void pointAtTheDeployedFunction() {
        RestAssured.baseURI = AwsDrillConfig.baseUri();
    }

    @AfterEach
    void resetRestAssured() {
        RestAssured.reset();
    }

    @Test
    @Disabled("PRACTICE: delete this line and write the body from memory")
    void healthEndpointReturns200() {
        // GET "/" -> 200, JSON content type, service == "note-store", status == "ok"
    }

    @Test
    @Disabled("PRACTICE: delete this line and write the body from memory")
    void postNoteReturns201WithLocationHeader() {
        // POST "/notes" with the x-api-key header and {"text": "..."}
        // -> 201, body has id/text/createdAt, and Location equals "/notes/" + id
    }

    @Test
    @Disabled("PRACTICE: delete this line and write the body from memory")
    void createdNoteCanBeReadBackFromS3() {
        // POST a note with a unique text, extract the id,
        // then GET "/notes/{id}" -> 200 and the same text came back out of S3
    }

    @Test
    @Disabled("PRACTICE: delete this line and write the body from memory")
    void postWithoutApiKeyReturns401() {
        // POST with no x-api-key -> 401, error == "unauthenticated"
    }

    @Test
    @Disabled("PRACTICE: delete this line and write the body from memory")
    void postWithWrongApiKeyReturns401() {
        // POST with a junk x-api-key -> 401 (NOT 403 — be able to say why)
    }

    @Test
    @Disabled("PRACTICE: delete this line and write the body from memory")
    void invalidBodyReturns400() {
        // With a VALID key: {} -> 400, {"text":"   "} -> 400, 501 chars -> 400
    }

    @Test
    @Disabled("PRACTICE: delete this line and write the body from memory")
    void unknownNoteIdReturns404() {
        // GET "/notes/{random uuid}" -> 404, error == "not_found"
    }

    @Test
    @Disabled("PRACTICE: delete this line and write the body from memory")
    void warmInvocationRespondsWithinSla() {
        // One warm-up GET "/" with no timing assertion, THEN a GET "/" asserting
        // .time(lessThan(3L), TimeUnit.SECONDS) — and know why the warm-up is there
    }
}
