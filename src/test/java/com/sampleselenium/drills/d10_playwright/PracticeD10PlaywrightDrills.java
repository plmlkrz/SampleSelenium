package com.sampleselenium.drills.d10_playwright;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DRILL 10 — PRACTICE FILE
 *
 * 1. Read SourceD10PlaywrightDrills.java carefully. Then CLOSE it — no peeking.
 * 2. Pick a test below, delete its @Disabled line, and write the body from memory.
 * 3. Run:  mvn test -Dtest=PracticeD10PlaywrightDrills
 * 4. Compare with the source. Note what you missed. Repeat until clean.
 *
 * KEEP THIS FILE COMPILING — a syntax error here blocks the whole project.
 * Quick syntax check:  mvn test-compile
 *
 * SAY OUT LOUD WHILE TYPING (the answers live in the source file's comments):
 *   - The five actionability checks Playwright retries before every action
 *   - Why there is no StaleElementReferenceException in Playwright
 *   - What strict mode does that Selenium's findElement does not
 *   - Browser vs BrowserContext vs Page, and which one is the unit of test isolation
 *   - The locator priority order, and why getByRole comes first
 *   - Your honest Selenium-to-Playwright ramp answer, in about 45 seconds
 *
 * THE TRAP: if you find yourself reaching for a wait or a sleep, stop and ask what
 * assertion you actually meant. In this tool the assertion IS the wait.
 *
 * SECTIONS TO REPRODUCE:
 *   1. Sign in with getByLabel + getByRole, assert the dashboard, ZERO waits
 *   2. Assert the 3 table rows that arrive late via fetch (a one-shot count returns 0)
 *   3. Trigger a strict mode violation on "Details", then resolve it two ways
 *   4. getByTestId("welcome-banner") contains the signed-in user
 *   5. Accept the native confirm dialog — handler registered BEFORE the click
 *   6. page.request(): GET 200, POST without X-API-KEY -> 401, POST with it -> 201 + Location
 *   7. page.route(): stub /api/policies, prove the UI renders exactly the stubbed row
 *   8. Record a trace zip around the sign-in flow
 *
 * Credentials for the mock portal: adjuster / secret_sauce
 */
class PracticeD10PlaywrightDrills {

    private static Playwright playwright;
    private static Browser browser;

    private MockPortalApp app;
    private String baseUrl;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        } catch (Exception problem) {
            Assumptions.abort("Playwright browsers unavailable — run once with internet access. Cause: "
                    + problem.getMessage());
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void startAppAndContext() throws IOException {
        app = new MockPortalApp();
        baseUrl = app.start();
        context = browser.newContext();
        page = context.newPage();
        page.navigate(baseUrl);
    }

    @AfterEach
    void closeContextAndApp() {
        if (context != null) {
            context.close();
        }
        app.stop();
    }

    /** 1. Fill Username and Password by LABEL, click the "Sign in" button by ROLE,
     *     then assert the "Policy Dashboard" heading is visible. No waits. */
    @Test
    @Disabled("Practice: write from memory, then delete this line")
    void signsInUsingRoleAndLabelLocatorsWithNoExplicitWaits() {
    }

    /** 2. Sign in, then assert #policy-rows has exactly 3 tr elements and contains
     *     "Cardinal Roofing". Ask yourself why a plain .count() call would return 0. */
    @Test
    @Disabled("Practice: write from memory, then delete this line")
    void waitsForTableRowsThatArriveAfterTheDashboardRenders() {
    }

    /** 3. Sign in. Build the "Details" button locator, assert hasCount(2), assert that
     *     click() throws PlaywrightException mentioning "strict mode violation",
     *     then resolve it with .first() and with a unique locator. */
    @Test
    @Disabled("Practice: write from memory, then delete this line")
    void strictModeFailsLoudlyWhenALocatorMatchesTwoElements() {
    }

    /** 4. Sign in, then assert the test-id banner contains the signed-in username. */
    @Test
    @Disabled("Practice: write from memory, then delete this line")
    void readsTheWelcomeBannerByTestId() {
    }

    /** 5. Sign in, register a dialog handler that asserts the message and accepts,
     *     click "Cancel policy", assert #cancel-result becomes visible. */
    @Test
    @Disabled("Practice: write from memory, then delete this line")
    void acceptsANativeConfirmDialog() {
    }

    /** 6. No browser interaction needed. GET /api/policies -> 200 containing PL-1002.
     *     POST with no X-API-KEY -> 401 (say 401 vs 403 out loud).
     *     POST with the header -> 201 and a Location header. */
    @Test
    @Disabled("Practice: write from memory, then delete this line")
    void callsTheApiDirectlyThroughTheBrowserContext() {
    }

    /** 7. Route the policies endpoint (glob it) and fulfill it with a single stubbed
     *     policy, THEN sign in. Assert exactly 1 row containing the stubbed holder name. */
    @Test
    @Disabled("Practice: write from memory, then delete this line")
    void stubsTheApiToProveTheUiHandlesTheResponse() {
    }

    /** 8. Start tracing with screenshots and snapshots, sign in, stop tracing to
     *     target/traces/. Then say what the trace viewer shows you that a stack trace does not. */
    @Test
    @Disabled("Practice: write from memory, then delete this line")
    void recordsATraceForFailureAnalysis() {
    }
}
