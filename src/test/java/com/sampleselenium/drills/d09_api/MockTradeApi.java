package com.sampleselenium.drills.d09_api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * A tiny fake "trade service" microservice built on the JDK's own HttpServer —
 * no Spring Boot, no internet, starts in milliseconds. The DRILLS are the client
 * side (REST Assured); this class just exists so the drills have something real
 * to hit. You do NOT need to reproduce this file from memory.
 *
 * Endpoints (all JSON, investment-banking flavored):
 *   GET    /api/trades/1001            -> 200 + trade body
 *   GET    /api/trades/{anything else} -> 404 + error body
 *   POST   /api/trades                 -> 201 + Location header + created body
 *                                         (401 if X-API-KEY header is missing)
 *   PUT    /api/trades/1001            -> 200 + updated body
 *   DELETE /api/trades/1001            -> 204, empty body
 *   GET    /api/portfolio/valuation    -> 200 + nested calculation-heavy JSON
 */
final class MockTradeApi {

    private HttpServer server;

    /** Starts on a random free port; returns the base URI, e.g. http://localhost:54321 */
    String start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/trades", this::handleTrades);
        server.createContext("/api/portfolio/valuation", this::handleValuation);
        server.start();
        return "http://localhost:" + server.getAddress().getPort();
    }

    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleTrades(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET" -> {
                if (path.equals("/api/trades/1001")) {
                    send(exchange, 200, """
                            {"tradeId":1001,"instrument":"AAPL","side":"BUY",
                             "quantity":100,"price":189.50,"status":"SETTLED",
                             "currency":"USD"}""");
                } else {
                    send(exchange, 404, """
                            {"error":"trade not found","path":"%s"}""".formatted(path));
                }
            }
            case "POST" -> {
                if (exchange.getRequestHeaders().getFirst("X-API-KEY") == null) {
                    send(exchange, 401, """
                            {"error":"missing X-API-KEY header"}""");
                    return;
                }
                exchange.getResponseHeaders().add("Location", "/api/trades/1002");
                send(exchange, 201, """
                        {"tradeId":1002,"instrument":"MSFT","side":"SELL",
                         "quantity":50,"price":425.10,"status":"NEW",
                         "currency":"USD"}""");
            }
            case "PUT" -> send(exchange, 200, """
                    {"tradeId":1001,"instrument":"AAPL","side":"BUY",
                     "quantity":100,"price":189.50,"status":"CANCELLED",
                     "currency":"USD"}""");
            case "DELETE" -> {
                // 204 No Content: success, deliberately empty body
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            }
            default -> send(exchange, 405, """
                    {"error":"method not allowed"}""");
        }
    }

    /** Calculation-heavy nested JSON: marketValue = quantity * price for each position. */
    private void handleValuation(HttpExchange exchange) throws IOException {
        send(exchange, 200, """
                {
                  "portfolioId": "PB-7788",
                  "asOf": "2026-07-14",
                  "baseCurrency": "USD",
                  "positions": [
                    {"instrument":"AAPL","quantity":100,"price":189.50,"marketValue":18950.00},
                    {"instrument":"MSFT","quantity":50,"price":425.10,"marketValue":21255.00},
                    {"instrument":"GOOG","quantity":25,"price":176.40,"marketValue":4410.00}
                  ],
                  "totals": {"grossMarketValue":44615.00,"positionCount":3}
                }""");
    }

    private void send(HttpExchange exchange, int status, String jsonBody) throws IOException {
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(bytes);
        }
        exchange.close();
    }
}
