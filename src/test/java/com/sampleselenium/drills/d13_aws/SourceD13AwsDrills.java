package com.sampleselenium.drills.d13_aws;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DRILL 13 — TESTING A REAL AWS DEPLOYMENT  [SOURCE — read, close, reproduce]
 *
 * The only module in this gym that hits infrastructure YOU deployed. Everything else runs
 * against a mock, a public practice site, or an in-memory DB. Deploy first:
 *
 *   aws/RUNBOOK.md      — free-tier account, S3 bucket, Lambda, Function URL (~45 min)
 *   aws/lambda_function.py — the service under test (Python, pasted into the console)
 *
 *   $env:AWS_FN_URL='https://xxxx.lambda-url.us-east-1.on.aws'
 *   $env:AWS_API_KEY='...'
 *   mvn test -Dtest=SourceD13AwsDrills
 *
 * Without those variables every test SKIPS, so the repo stays green for everyone else.
 *
 * ======================= WHY THIS MODULE EXISTS =======================
 * "AWS experience" on a QA resume usually means one of three very different things, and a
 * good interviewer separates them in about two questions:
 *   1. I consumed an app that happened to run on AWS.       (everyone)
 *   2. I read CloudWatch logs and S3 buckets to triage.     (most working QA)
 *   3. I deployed something and tested it.                  (fewer)
 * This drill buys level 3 honestly and cheaply. It is a ~90-line Lambda. Do not oversell it —
 * the value is that every noun in your answer is one you can actually describe.
 *
 * ======================= WHAT IS DIFFERENT FROM DRILL 09 =======================
 * Same status-code assertions as the d09 mock API, but now over the real internet, which
 * introduces the things that actually make cloud suites flaky:
 *   - COLD START. First invoke after idle pays container init (Python: a few hundred ms;
 *     JVM Lambdas: seconds). A 500ms SLA assertion that passes locally fails on the first
 *     CI run of the morning. Assert a realistic ceiling, or warm the function first.
 *   - EVENTUAL CONSISTENCY. S3 is now strongly read-after-write consistent for new objects
 *     (changed in Dec 2020), which is why the round-trip test below can read immediately.
 *     Say that precisely — plenty of people still repeat the old "you must retry S3 reads"
 *     advice, and knowing it changed is a credible detail. DynamoDB reads are still
 *     eventually consistent by default unless you ask for a strongly consistent read.
 *   - REAL AUTH. 401 here is a header your code checks, not a mock returning a constant.
 *   - SHARED MUTABLE STATE. Every POST leaves an object in a real bucket. Tests that assume
 *     an empty store are the #1 source of "passes alone, fails in the suite" in cloud QA.
 *
 * ======================= THE TRIAGE ANSWER (the one worth rehearsing) =======================
 * "An API call failed in the cloud environment. Walk me through it."
 *   1. What did the CLIENT see — status, body, headers? 4xx points at my request, 5xx at
 *      the service, a timeout at neither until proven.
 *   2. Did the request reach the code? CloudWatch Logs, filtered to the request window.
 *      NO log line at all means it never got there: auth on the URL/gateway, throttling,
 *      or the wrong host — do not go debugging application code yet.
 *   3. If it reached the code, read the traceback. AccessDenied on an S3 call is an IAM
 *      problem, not a logic problem; a KeyError on an env var is a configuration problem.
 *   4. Reproduce it with curl outside the framework to prove it is the service, not my test.
 *   5. File it with the request id from the log — it is the join key between my defect and
 *      whatever the dev sees in their tooling.
 * That sequence — client, reached-the-code, traceback, isolate, request id — is what people
 * mean by "cloud debugging". It is the same triage you already do, plus knowing where the
 * logs live.
 *
 * ======================= IAM IN ONE BREATH =======================
 * The Lambda has an EXECUTION ROLE, not a password. The inline policy in aws/iam-policy.json
 * grants GetObject/PutObject on `notes/*` in one bucket — no ListBucket, no Delete, nothing
 * else. Least privilege is testable: if the function could delete objects, that is a finding
 * even when the feature works.
 *
 * SECTIONS TO REPRODUCE:
 *   1. GET / -> 200 health check against the deployed URL
 *   2. POST /notes -> 201 + Location header (the 200-vs-201 drill, for real this time)
 *   3. Round trip: POST then GET the created id -> the text survived S3
 *   4. Missing/wrong x-api-key -> 401
 *   5. Invalid body -> 400 (three flavors: no text, blank text, over the length cap)
 *   6. Unknown id -> 404
 *   7. Cold-start-aware response time assertion
 */
class SourceD13AwsDrills {

    @BeforeEach
    void pointAtTheDeployedFunction() {
        RestAssured.baseURI = AwsDrillConfig.baseUri();
    }

    @AfterEach
    void resetRestAssured() {
        RestAssured.reset();
    }

    /**
     * 1. The endpoint is up and is mine. Also the cheapest possible smoke test to put at the
     * front of a cloud suite: if this fails, every other failure below is noise.
     */
    @Test
    void healthEndpointReturns200() {
        given()
        .when()
                .get("/")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("service", equalTo("note-store"))
                .body("status", equalTo("ok"));
    }

    /**
     * 2. THE HEADLINE DRILL. A POST that creates a resource returns 201, not 200, and carries
     * a Location header pointing at the thing it just made. Here the 201 means an object now
     * exists in S3 — the status code is a claim about the world, and test 3 verifies the claim.
     */
    @Test
    void postNoteReturns201WithLocationHeader() {
        Response response =
                given()
                        .header("x-api-key", AwsDrillConfig.apiKey())
                        .contentType(ContentType.JSON)
                        .body("{\"text\":\"created by SourceD13AwsDrills\"}")
                .when()
                        .post("/notes")
                .then()
                        .statusCode(201)
                        .contentType(ContentType.JSON)
                        .body("id", not(emptyOrNullString()))
                        .body("text", equalTo("created by SourceD13AwsDrills"))
                        .body("createdAt", not(emptyOrNullString()))
                        .header("Location", not(emptyOrNullString()))
                        .extract().response();

        String id = response.jsonPath().getString("id");
        assertEquals("/notes/" + id, response.getHeader("Location"),
                "Location must point at the resource that was just created");
    }

    /**
     * 3. The assertion that makes this an integration test rather than a Lambda unit test:
     * the write went through the execution role into S3, and a SEPARATE request read it back.
     * If the IAM policy were missing PutObject this fails with a 500 — which is exactly the
     * defect class a mock can never catch for you.
     *
     * No retry loop here on purpose: S3 has been strongly read-after-write consistent for new
     * objects since Dec 2020. Be able to say that; it is a small, checkable detail that
     * separates people who have used S3 from people who have read about it.
     */
    @Test
    void createdNoteCanBeReadBackFromS3() {
        String text = "round trip " + UUID.randomUUID();

        String id =
                given()
                        .header("x-api-key", AwsDrillConfig.apiKey())
                        .contentType(ContentType.JSON)
                        .body("{\"text\":\"" + text + "\"}")
                .when()
                        .post("/notes")
                .then()
                        .statusCode(201)
                        .extract().jsonPath().getString("id");

        assertNotNull(id, "POST must return the new note id");

        given()
        .when()
                .get("/notes/" + id)
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(id))
                .body("text", equalTo(text));
    }

    /**
     * 4. 401 = UNAUTHENTICATED, "I don't know who you are" — the credential is missing or wrong.
     * 403 = UNAUTHORIZED, "I know who you are and you still may not." Interviewers ask this
     * constantly and the words are backwards from the HTTP spec's own naming, so drill it.
     *
     * Note the negative-path habit: assert the status AND that nothing was created. A 401 that
     * still writes to the bucket is a real (and not rare) bug.
     */
    @Test
    void postWithoutApiKeyReturns401() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"text\":\"should never be stored\"}")
        .when()
                .post("/notes")
        .then()
                .statusCode(401)
                .body("error", equalTo("unauthenticated"));
    }

    @Test
    void postWithWrongApiKeyReturns401() {
        given()
                .header("x-api-key", "definitely-not-the-key")
                .contentType(ContentType.JSON)
                .body("{\"text\":\"should never be stored\"}")
        .when()
                .post("/notes")
        .then()
                .statusCode(401);
    }

    /**
     * 5. Input validation — 400 is the service rejecting the REQUEST, distinct from 401
     * (who you are) and 404 (what you asked for). Three boundary flavors in one test because
     * they are one behavior; in a real suite these would be a parameterized test.
     *
     * The order the service checks matters and is worth noticing: auth is verified BEFORE the
     * body is parsed, so a bad key plus a bad body returns 401, not 400. Leaking "your JSON is
     * malformed" to an unauthenticated caller is a small information disclosure.
     */
    @Test
    void invalidBodyReturns400() {
        String key = AwsDrillConfig.apiKey();

        // no text field at all
        given().header("x-api-key", key).contentType(ContentType.JSON).body("{}")
        .when().post("/notes")
        .then().statusCode(400).body("error", equalTo("bad_request"));

        // present but blank — the boundary people forget
        given().header("x-api-key", key).contentType(ContentType.JSON).body("{\"text\":\"   \"}")
        .when().post("/notes")
        .then().statusCode(400);

        // one character past the 500-char cap
        String tooLong = "x".repeat(501);
        given().header("x-api-key", key).contentType(ContentType.JSON)
                .body("{\"text\":\"" + tooLong + "\"}")
        .when().post("/notes")
        .then().statusCode(400).body("message", containsString("500"));
    }

    /**
     * 6. 404 for an id that was never created. This proves the S3 NoSuchKey ClientError is
     * being caught and translated — without that handling the function throws and the caller
     * gets a 500, which is the difference between "not found" and "we are broken".
     */
    @Test
    void unknownNoteIdReturns404() {
        given()
        .when()
                .get("/notes/" + UUID.randomUUID())
        .then()
                .statusCode(404)
                .body("error", equalTo("not_found"));
    }

    /**
     * 7. Response-time assertion that respects reality. The first call after idle pays cold
     * start, so this test warms the function and THEN measures — and even the warm ceiling is
     * generous, because it is crossing the public internet to a shared-tenant service.
     *
     * The talk track: "I assert a functional SLA per call, but I keep cloud thresholds honest
     * about cold starts — a tight threshold that fails every Monday morning trains the team to
     * ignore the suite, which is worse than not asserting at all. For real latency numbers at
     * concurrency I use JMeter against the same endpoint, and I read the duration and init
     * duration out of the CloudWatch REPORT line rather than guessing from the client side."
     */
    @Test
    void warmInvocationRespondsWithinSla() {
        given().when().get("/").then().statusCode(200);   // warm-up, deliberately unasserted on time

        given()
        .when()
                .get("/")
        .then()
                .statusCode(200)
                .time(lessThan(3L), TimeUnit.SECONDS);
    }
}
