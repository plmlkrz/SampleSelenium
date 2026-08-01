package com.sampleselenium.drills.d12_postman_promotion;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * A tiny fake "quotes service" built on the JDK's own HttpServer — stands in for
 * whatever real service the Postman collection in this module was exploring by hand.
 * You do NOT need to reproduce this file from memory.
 *
 * Endpoints:
 *   GET  /api/quotes/AAPL            -> 200 + quote body (the happy path Postman covered)
 *   GET  /api/quotes/{unknown}       -> 404 + error body (Postman never tried this)
 *   POST /api/quotes                 -> 201 + Location header (the happy path Postman covered)
 *   POST /api/quotes {missing price} -> 400 + validation error (Postman never tried this)
 */
final class MockQuoteApi {

    private HttpServer server;

    String start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/quotes", this::handleQuotes);
        server.start();
        return "http://localhost:" + server.getAddress().getPort();
    }

    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleQuotes(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET" -> {
                if (path.equals("/api/quotes/AAPL")) {
                    send(exchange, 200, """
                            {"symbol":"AAPL","price":189.50,"currency":"USD"}""");
                } else {
                    send(exchange, 404, """
                            {"error":"quote not found","path":"%s"}""".formatted(path));
                }
            }
            case "POST" -> {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (!body.contains("\"price\"")) {
                    send(exchange, 400, """
                            {"error":"price is required"}""");
                    return;
                }
                exchange.getResponseHeaders().add("Location", "/api/quotes/MSFT");
                send(exchange, 201, """
                        {"symbol":"MSFT","price":425.10,"currency":"USD"}""");
            }
            default -> send(exchange, 405, """
                    {"error":"method not allowed"}""");
        }
    }

    private void send(HttpExchange exchange, int status, String jsonBody) throws IOException {
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(bytes);
        }
        exchange.close();
    }
}
