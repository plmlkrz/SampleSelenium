package com.sampleselenium.drills.d09_api;

/**
 * Standalone launcher so the JMeter drill in /jmeter has a live target.
 * Starts MockTradeApi on a fixed port (default 8099) and blocks until Ctrl+C.
 *
 * Run it with:
 *   mvn test-compile exec:java -Dexec.classpathScope=test \
 *       -Dexec.mainClass=com.sampleselenium.drills.d09_api.RunMockTradeApi
 *
 * Override the port with -Dmock.port=9000 (and pass -Jport=9000 to JMeter).
 */
public final class RunMockTradeApi {

    public static void main(String[] args) throws Exception {
        int port = Integer.getInteger("mock.port", 8099);
        MockTradeApi api = new MockTradeApi();
        String baseUri = api.start(port);
        System.out.println("MockTradeApi listening on " + baseUri + " — Ctrl+C to stop.");
        System.out.println("Try it: curl " + baseUri + "/api/trades/1001");
        Runtime.getRuntime().addShutdownHook(new Thread(api::stop));
        Thread.currentThread().join();
    }

    private RunMockTradeApi() {
    }
}
