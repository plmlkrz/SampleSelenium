package com.sampleselenium.drills.d11_resilience;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 11 — TIMEOUT / RETRY / CIRCUIT BREAKER FOR A SPRING BOOT SERVICE'S DOWNSTREAM CALL
 * [SOURCE — read, close, reproduce]
 *
 * NO BROWSER, NO INTERNET, NO SPRING BOOT — WireMock stands in for "a downstream
 * microservice that's slow or flaky"; DownstreamClient stands in for "the resilience
 * wiring a Spring Boot @Service would have" (Resilience4j has a Spring Boot starter that
 * applies these same configs via @Retry/@CircuitBreaker annotations — same policies,
 * annotation-driven instead of hand-wired):
 *   mvn test -Dtest=SourceD11ResilienceDrills
 *
 * Answers the interview question directly: "How would you verify timeout, retry, and
 * circuit-breaker behavior when a Spring Boot service calls a slow downstream service?"
 *
 * ======================= THE MODEL ANSWER, MADE RUNNABLE =======================
 * "Use WireMock to inject delays/errors, assert the public response and latency budget,
 *  verify the bounded retry count, and confirm no duplicate side effects; then verify the
 *  circuit opens and later recovers."
 *   1. TIMEOUT   — stub a fixed delay past the client's budget; assert the call fails
 *                  FAST (bounded elapsed time), not that it hangs.
 *   2. RETRY     — stub a WireMock Scenario: fail twice, then succeed; assert the final
 *                  call SUCCEEDS and that exactly maxAttempts requests hit the wire — a
 *                  retry policy with no upper bound is a self-inflicted DDoS.
 *   3. CIRCUIT BREAKER — drive it to OPEN with real failures, prove the NEXT call
 *                  short-circuits (CallNotPermittedException, wire count does NOT
 *                  increase), then let the wait duration elapse, heal the stub, and
 *                  prove HALF_OPEN trial calls close it again.
 *   4. NO DUPLICATE SIDE EFFECTS — a non-idempotent POST (booking a trade) is never
 *                  wrapped in retry. Verify it: exactly ONE request reaches the wire even
 *                  when it fails, because retrying a maybe-it-landed write is how you get
 *                  two trades from one click.
 *
 * SECTIONS TO REPRODUCE:
 *   1. Timeout alone: fixed delay > budget -> DownstreamException, elapsed bounded, 1 request
 *   2. Retry alone: WireMock Scenario 500,500,200 -> succeeds, exactly 3 requests
 *   3. Circuit breaker: 4 real failures -> OPEN -> 5th short-circuits -> wait -> heal -> CLOSED
 *   4. POST is never retried: always 500 -> exactly 1 request, status 500 returned as-is
 */
class SourceD11ResilienceDrills {

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

    /**
     * TIMEOUT DRILL. The downstream takes 1000ms; the client's budget is 300ms.
     * The assertion that matters isn't just "it throws" — it's that the ELAPSED time is
     * bounded near the budget, proving we didn't just get lucky waiting out a slow response.
     */
    @Test
    void timeoutBudgetIsEnforcedWhenDownstreamIsSlow() {
        wireMock.stubFor(get(urlEqualTo("/api/pricing/AAPL"))
                .willReturn(aResponse().withFixedDelay(1000).withStatus(200).withBody("{}")));

        long start = System.nanoTime();
        assertThrows(DownstreamException.class, () -> client.callWithTimeoutOnly("/api/pricing/AAPL"));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMs < 900, "call should fail near the 300ms budget, not wait out the full delay");
        wireMock.verify(exactly(1), getRequestedFor(urlEqualTo("/api/pricing/AAPL")));
    }

    /**
     * RETRY DRILL. WireMock's Scenario state machine simulates "fails twice, then recovers" —
     * a real transient blip. maxAttempts(3) is a CEILING, not a suggestion: verify the exact
     * request count so nobody quietly changes it to "retry forever."
     */
    @Test
    void retrySucceedsAfterBoundedTransientFailures() {
        String path = "/api/pricing/MSFT";
        wireMock.stubFor(get(urlEqualTo(path)).inScenario("retry-recovery")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("one failure logged"));
        wireMock.stubFor(get(urlEqualTo(path)).inScenario("retry-recovery")
                .whenScenarioStateIs("one failure logged")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("two failures logged"));
        wireMock.stubFor(get(urlEqualTo(path)).inScenario("retry-recovery")
                .whenScenarioStateIs("two failures logged")
                .willReturn(aResponse().withStatus(200).withBody("{\"price\":425.10}")));

        String body = client.callWithRetry(path);

        assertEquals("{\"price\":425.10}", body);
        wireMock.verify(exactly(3), getRequestedFor(urlEqualTo(path)));
    }

    /**
     * CIRCUIT BREAKER DRILL — the full arc: OPEN, short-circuit, then recovery.
     * Config: slidingWindowSize=4, minimumNumberOfCalls=4, failureRateThreshold=50%,
     * waitDurationInOpenState=500ms, permittedNumberOfCallsInHalfOpenState=2.
     */
    @Test
    void circuitBreakerOpensThenRecoversThroughHalfOpen() throws InterruptedException {
        String path = "/api/pricing/GOOG";
        wireMock.stubFor(get(urlEqualTo(path)).willReturn(aResponse().withStatus(500)));

        // 4 real failures -> failure rate 100% >= 50% threshold -> breaker OPENS.
        for (int i = 0; i < 4; i++) {
            assertThrows(DownstreamException.class, () -> client.callWithCircuitBreaker(path));
        }
        assertEquals(CircuitBreaker.State.OPEN, client.circuitBreakerState());

        // The 5th call must NOT reach the wire — it fails fast on the breaker itself.
        assertThrows(CallNotPermittedException.class, () -> client.callWithCircuitBreaker(path));
        wireMock.verify(exactly(4), getRequestedFor(urlEqualTo(path)));

        // Let the open-state window elapse, then heal the downstream.
        Thread.sleep(600);
        wireMock.resetAll();
        wireMock.stubFor(get(urlEqualTo(path)).willReturn(aResponse().withStatus(200).withBody("{\"price\":176.40}")));

        // First call after the wait flips OPEN -> HALF_OPEN and is permitted as a trial.
        assertEquals("{\"price\":176.40}", client.callWithCircuitBreaker(path));
        // Second trial call (permittedNumberOfCallsInHalfOpenState=2) -> both healthy -> CLOSED.
        assertEquals("{\"price\":176.40}", client.callWithCircuitBreaker(path));
        assertEquals(CircuitBreaker.State.CLOSED, client.circuitBreakerState());
    }

    /**
     * NO DUPLICATE SIDE EFFECTS. Booking a trade is a POST — not idempotent. Even though
     * this call fails, it must reach the wire EXACTLY ONCE: a retry here could book the
     * same trade twice if the first attempt actually succeeded before the response was lost.
     */
    @Test
    void createTradeIsNeverBlindlyRetried() {
        String path = "/api/trades";
        wireMock.stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(500)));

        int status = client.createTradeReturningStatus(path,
                "{\"instrument\":\"AAPL\",\"side\":\"BUY\",\"quantity\":10,\"price\":189.50}");

        assertEquals(500, status, "the failure is returned as-is, not swallowed by a retry");
        wireMock.verify(exactly(1), postRequestedFor(urlEqualTo(path)));
    }
}
