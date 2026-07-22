package com.sampleselenium.drills.d11_resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * A tiny "pricing service client" — the code a Spring Boot service would use to call a
 * downstream microservice, minus the Spring wiring. Each public method decorates the SAME
 * raw HTTP call with a DIFFERENT resilience policy, on purpose: in a real interview you
 * test each policy in isolation before trusting them composed together (see
 * {@link #callFullyProtected}, which is the one that ships).
 *
 * Three policies, three separate concerns:
 *   TIMEOUT   — java.net.http's own per-request budget (HttpRequest.timeout()). No library
 *               needed; this IS the timeout. Resilience4j's TimeLimiter exists for the
 *               async/CompletableFuture world (WebClient); a blocking client doesn't need it.
 *   RETRY     — bounded re-attempts on a transient failure, with backoff. Only ever wraps
 *               calls that are safe to repeat (idempotent reads).
 *   CIRCUIT BREAKER — stops calling a downstream that is already failing, so retries don't
 *               pile onto (and worsen) an outage. Opens on a failure-rate threshold over a
 *               sliding window, then probes recovery through a bounded HALF_OPEN trial.
 */
final class DownstreamClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String baseUrl;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    DownstreamClient(String baseUrl) {
        this.baseUrl = baseUrl;

        this.retry = Retry.of("downstream-retry", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(50))
                .retryExceptions(DownstreamException.class)
                .build());

        this.circuitBreaker = CircuitBreaker.of("downstream-breaker", CircuitBreakerConfig.custom()
                .slidingWindowSize(4)
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofMillis(500))
                .permittedNumberOfCallsInHalfOpenState(2)
                .recordException(t -> t instanceof DownstreamException)
                .build());
    }

    /** Timeout ONLY — no retry, no circuit breaker. Fails fast at the latency budget. */
    String callWithTimeoutOnly(String path) {
        return rawGet(path, Duration.ofMillis(300));
    }

    /** Retry ONLY — up to 3 attempts, 50ms apart, on ANY DownstreamException. */
    String callWithRetry(String path) {
        Supplier<String> call = () -> rawGet(path, Duration.ofMillis(300));
        return Retry.decorateSupplier(retry, call).get();
    }

    /** Circuit breaker ONLY — single attempt per call; the breaker decides whether to allow it. */
    String callWithCircuitBreaker(String path) {
        Supplier<String> call = () -> rawGet(path, Duration.ofMillis(300));
        return CircuitBreaker.decorateSupplier(circuitBreaker, call).get();
    }

    /** THE PRODUCTION METHOD: circuit breaker wraps retry, which wraps the timeout-bounded call.
     *  Order matters — the breaker sees the OUTCOME of a full retry cycle as one call, so a
     *  flaky-but-recovering downstream doesn't trip the breaker on transient blips. */
    String callFullyProtected(String path) {
        Supplier<String> call = () -> rawGet(path, Duration.ofMillis(300));
        Supplier<String> retried = Retry.decorateSupplier(retry, call);
        return CircuitBreaker.decorateSupplier(circuitBreaker, retried).get();
    }

    /** Deliberately UNDECORATED — no retry. Booking a trade is not idempotent: retrying a
     *  timed-out POST risks creating a second trade if the first one actually landed. */
    int createTradeReturningStatus(String path, String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofMillis(300))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new DownstreamException("create-trade call failed", e);
        }
    }

    CircuitBreaker.State circuitBreakerState() {
        return circuitBreaker.getState();
    }

    private String rawGet(String path, Duration timeout) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(timeout)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500) {
                throw new DownstreamException("downstream returned " + response.statusCode());
            }
            return response.body();
        } catch (HttpTimeoutException e) {
            throw new DownstreamException("downstream timed out past " + timeout, e);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new DownstreamException("downstream call failed", e);
        }
    }
}
