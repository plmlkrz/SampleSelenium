package com.sampleselenium.drills.d10_playwright;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * A tiny fake "policy portal" — one HTML page plus a JSON API, served by the JDK's own
 * HttpServer. No internet, no Sauce Demo, starts in milliseconds. The DRILLS are the
 * Playwright side; this class just gives them something real to drive. You do NOT need
 * to reproduce this file from memory.
 *
 * The page is deliberately built to exercise the things interviewers ask about:
 *   - a login form with proper <label for=...> wiring   -> getByLabel
 *   - a submit button with an accessible name           -> getByRole
 *   - a 600ms spinner before the dashboard renders      -> auto-waiting (no sleeps)
 *   - a table populated by fetch() after another delay  -> retrying assertions
 *   - TWO buttons that both say "Details"               -> strict mode violation
 *   - a data-testid on a volatile-looking element       -> getByTestId
 *   - a native confirm() dialog                         -> dialog handling
 *
 * Endpoints:
 *   GET  /                 -> the portal page
 *   GET  /api/policies     -> 200 + JSON array of policies
 *   POST /api/policies     -> 201 + Location header (401 when X-API-KEY is missing)
 */
final class MockPortalApp {

    private HttpServer server;

    /** Starts on a random free port; returns the base URL, e.g. http://localhost:54321 */
    String start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/policies", this::handlePolicies);
        server.createContext("/", this::handlePage);
        server.start();
        return "http://localhost:" + server.getAddress().getPort();
    }

    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handlePolicies(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            if (exchange.getRequestHeaders().getFirst("X-API-KEY") == null) {
                send(exchange, 401, "application/json", "{\"error\":\"missing X-API-KEY header\"}");
                return;
            }
            exchange.getResponseHeaders().add("Location", "/api/policies/PL-3003");
            send(exchange, 201, "application/json",
                    "{\"policyId\":\"PL-3003\",\"holder\":\"Riverbend Framing\",\"premium\":2450.00,\"status\":\"NEW\"}");
            return;
        }
        send(exchange, 200, "application/json", """
                [
                  {"policyId":"PL-1001","holder":"Ashwood Builders","premium":1875.00,"status":"ACTIVE"},
                  {"policyId":"PL-1002","holder":"Cardinal Roofing","premium":3120.50,"status":"ACTIVE"},
                  {"policyId":"PL-1003","holder":"Delta Site Works","premium":990.25,"status":"LAPSED"}
                ]""");
    }

    private void handlePage(HttpExchange exchange) throws IOException {
        send(exchange, 200, "text/html; charset=utf-8", """
                <!doctype html>
                <html lang="en">
                <head><meta charset="utf-8"><title>Policy Portal</title></head>
                <body>
                  <h1>Policy Portal</h1>

                  <section id="login">
                    <form id="login-form">
                      <label for="username">Username</label>
                      <input id="username" name="username" type="text">
                      <label for="password">Password</label>
                      <input id="password" name="password" type="password">
                      <button type="submit">Sign in</button>
                    </form>
                    <p id="login-error" hidden role="alert">Invalid credentials</p>
                  </section>

                  <section id="spinner" hidden><p>Loading dashboard...</p></section>

                  <section id="dashboard" hidden>
                    <h2>Policy Dashboard</h2>
                    <p data-testid="welcome-banner">Signed in as <span id="who"></span></p>

                    <table id="policy-table">
                      <thead><tr><th>Policy</th><th>Holder</th><th>Premium</th><th>Status</th></tr></thead>
                      <tbody id="policy-rows"></tbody>
                    </table>

                    <!-- Two buttons with the SAME accessible name: the strict-mode drill -->
                    <button type="button" id="details-1001">Details</button>
                    <button type="button" id="details-1002">Details</button>

                    <button type="button" id="cancel-policy">Cancel policy</button>
                    <p id="cancel-result" hidden>Policy cancelled</p>
                  </section>

                  <script>
                    const $ = (id) => document.getElementById(id);

                    $("login-form").addEventListener("submit", (event) => {
                      event.preventDefault();
                      const user = $("username").value;
                      const pass = $("password").value;
                      if (user !== "adjuster" || pass !== "secret_sauce") {
                        $("login-error").hidden = false;
                        return;
                      }
                      $("login-error").hidden = true;
                      $("login").hidden = true;
                      $("spinner").hidden = false;
                      // 600ms of nothing: a fixed sleep would be guessing, auto-waiting is not
                      setTimeout(() => {
                        $("spinner").hidden = true;
                        $("dashboard").hidden = false;
                        $("who").textContent = user;
                        loadPolicies();
                      }, 600);
                    });

                    // Rows arrive AFTER the dashboard renders — the table is briefly empty.
                    // This is why you assert on the row count instead of reading it once.
                    async function loadPolicies() {
                      const response = await fetch("/api/policies");
                      const policies = await response.json();
                      await new Promise((resolve) => setTimeout(resolve, 400));
                      $("policy-rows").innerHTML = policies.map((policy) =>
                        "<tr><td>" + policy.policyId + "</td><td>" + policy.holder +
                        "</td><td>" + policy.premium.toFixed(2) + "</td><td>" +
                        policy.status + "</td></tr>").join("");
                    }

                    $("cancel-policy").addEventListener("click", () => {
                      if (window.confirm("Cancel this policy?")) {
                        $("cancel-result").hidden = false;
                      }
                    });
                  </script>
                </body>
                </html>""");
    }

    private void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream stream = exchange.getResponseBody()) {
            stream.write(bytes);
        }
        exchange.close();
    }
}
