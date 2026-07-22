package com.sampleselenium.drills.d11_resilience;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 11 — PRACTICE FILE
 *
 * 1. Read SourceD11ResilienceDrills.java carefully. Then CLOSE it — no peeking.
 * 2. Pick a test below, delete its @Disabled line, and write the body from memory.
 * 3. Run:  mvn test -Dtest=PracticeD11ResilienceDrills   (no browser needed — runs in ~1-2s)
 * 4. Compare with the source. Note what you missed. Repeat until clean.
 *
 * KEEP THIS FILE COMPILING — a syntax error here blocks the whole project.
 * Quick syntax check:  mvn test-compile
 *
 * SAY OUT LOUD WHILE TYPING (the answers live in the source file's comments):
 *   - Why the elapsed-time assertion matters more than "it threw" for the timeout case
 *   - Why you verify the EXACT retry count, not just "it eventually succeeded"
 *   - Circuit breaker state machine: CLOSED -> OPEN -> HALF_OPEN -> CLOSED (or back to OPEN)
 *   - Why POST /api/trades is never wrapped in retry
 *
 * SECTIONS TO REPRODUCE:
 *   1. Timeout alone: fixed delay > budget -> DownstreamException, bounded elapsed, 1 request
 *   2. Retry alone: WireMock Scenario 500,500,200 -> succeeds, exactly 3 requests
 *   3. Circuit breaker: 4 real failures -> OPEN -> 5th short-circuits -> wait -> heal -> CLOSED
 *   4. POST is never retried: always 500 -> exactly 1 request, status 500 returned as-is
 */
class PracticeD11ResilienceDrills {

    private WireMockServer wireMock;
    private DownstreamClient client;

    @BeforeEach
    void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        client = new DownstreamClient(wireMock.baseUrl());
    }

    @AfterEach
    void stopWireMock() {
        wireMock.stop();
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void timeoutBudgetIsEnforcedWhenDownstreamIsSlow() {
        // TODO: stub GET /api/pricing/AAPL with withFixedDelay(1000)
        //       time the call, assertThrows(DownstreamException.class, () -> client.callWithTimeoutOnly(...))
        //       assert elapsed millis < 900 (bounded near the 300ms budget, not the full delay)
        //       wireMock.verify(exactly(1), getRequestedFor(urlEqualTo(...))) -- no retry happened
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void retrySucceedsAfterBoundedTransientFailures() {
        // TODO: build a WireMock Scenario for /api/pricing/MSFT:
        //       STARTED -> 500, set state "one failure logged"
        //       "one failure logged" -> 500, set state "two failures logged"
        //       "two failures logged" -> 200 with a JSON body
        //       call client.callWithRetry(path), assert the body equals the 200 response
        //       wireMock.verify(exactly(3), getRequestedFor(urlEqualTo(path)))
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void circuitBreakerOpensThenRecoversThroughHalfOpen() throws InterruptedException {
        // TODO: stub /api/pricing/GOOG to always 500
        //       loop 4x: assertThrows(DownstreamException.class, () -> client.callWithCircuitBreaker(path))
        //       assert client.circuitBreakerState() == CircuitBreaker.State.OPEN
        //       assertThrows(CallNotPermittedException.class, () -> client.callWithCircuitBreaker(path))
        //       wireMock.verify(exactly(4), getRequestedFor(...)) -- 5th call never hit the wire
        //       Thread.sleep(600); wireMock.resetAll(); re-stub 200 healthy
        //       call twice more (half-open trial calls), assert both succeed
        //       assert client.circuitBreakerState() == CircuitBreaker.State.CLOSED
    }

    @Disabled("delete this line, then write the body from memory")
    @Test
    void createTradeIsNeverBlindlyRetried() {
        // TODO: stub POST /api/trades to always 500
        //       call client.createTradeReturningStatus(path, jsonBody)
        //       assert the returned status is 500 (not swallowed/retried)
        //       wireMock.verify(exactly(1), postRequestedFor(urlEqualTo(path))) -- exactly ONE attempt
    }
}
