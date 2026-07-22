package com.sampleselenium.drills.d11_resilience;

/**
 * Unchecked wrapper for any downstream-call failure (timeout, connection error, 5xx).
 * Resilience4j's Retry/CircuitBreaker predicates key off exception TYPE, so collapsing
 * every failure mode into one unchecked type keeps the decorator wiring simple.
 */
class DownstreamException extends RuntimeException {

    DownstreamException(String message) {
        super(message);
    }

    DownstreamException(String message, Throwable cause) {
        super(message, cause);
    }
}
