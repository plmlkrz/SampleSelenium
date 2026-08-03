package com.sampleselenium.drills.d13_aws;

import org.junit.jupiter.api.Assumptions;

/**
 * Reads the deployed endpoint out of the environment so nothing about YOUR account is
 * committed to this repo. Never hardcode the function URL or the key in a test file —
 * a Lambda URL plus its shared secret in git history is a real (small) incident, and
 * "how do you handle secrets in the framework?" is asked in most QA interviews.
 *
 *   $env:AWS_FN_URL='https://xxxx.lambda-url.us-east-1.on.aws'
 *   $env:AWS_API_KEY='...'
 *
 * System properties win over environment variables, so CI can pass -DawsFnUrl=... instead.
 *
 * If neither is set the drills SKIP rather than fail — a clean checkout of this repo must
 * still go green with `mvn test`, exactly like the Playwright module when browsers are missing.
 */
final class AwsDrillConfig {

    private AwsDrillConfig() {
    }

    static String baseUri() {
        String url = read("awsFnUrl", "AWS_FN_URL");
        Assumptions.assumeTrue(url != null && !url.isBlank(),
                "AWS_FN_URL not set — skipping drill 13 (see aws/RUNBOOK.md to deploy one)");
        // A trailing slash turns REST Assured's "/notes" into "//notes", which the Function
        // URL routes as a path this Lambda does not know: a surprise 404 that looks like a
        // broken deployment. Normalize instead of debugging it twice.
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    static String apiKey() {
        String key = read("awsApiKey", "AWS_API_KEY");
        Assumptions.assumeTrue(key != null && !key.isBlank(),
                "AWS_API_KEY not set — skipping drill 13 (see aws/RUNBOOK.md)");
        return key;
    }

    private static String read(String systemProperty, String environmentVariable) {
        String value = System.getProperty(systemProperty);
        return (value != null && !value.isBlank()) ? value : System.getenv(environmentVariable);
    }
}
