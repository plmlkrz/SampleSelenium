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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DRILL 10 — PLAYWRIGHT  [SOURCE — read, close, reproduce]
 *
 * ONE-TIME SETUP (downloads browser binaries, needs internet once):
 *   mvn test -Dtest=SourceD10PlaywrightDrills
 * The Java binding downloads Chromium on the first Playwright.create(). If it cannot,
 * every test here SKIPS with a message instead of failing the build (see @BeforeAll).
 *
 * After that install the drills are fully offline — MockPortalApp (JDK HttpServer)
 * serves the page and the JSON API on a random local port before each test.
 *
 * Targets the Builders Mutual JD, which names Playwright FIRST and Selenium second.
 *
 * ======================= THE QUESTION THEY WILL ACTUALLY ASK =======================
 * "You're a Selenium person. Why should we believe you can do Playwright?"
 * The answer is NOT 'the syntax is similar'. It is: the hard parts of automation are
 * design, waiting, test data, isolation, and CI triage — and Playwright gives me for
 * free several things I had to BUILD in the Selenium framework:
 *
 *   Selenium (what we built by hand)        Playwright (built in)
 *   --------------------------------        ---------------------
 *   BasePage + WebDriverWait everywhere     actionability auto-waiting on every action
 *   Custom ExpectedConditions               web-first assertions that retry (assertThat)
 *   findElement returns the FIRST match      strict mode: 2 matches = error, not a coin flip
 *   ThreadLocal<WebDriver> for parallelism  BrowserContext per test, workers in parallel
 *   Screenshot listener on failure          trace viewer: DOM snapshots + network + console
 *   Separate HTTP client for setup          page.request() shares cookies with the browser
 *   Log in through the UI every test        storageState: log in once, reuse the session
 *
 * ======================= AUTO-WAITING, PRECISELY =======================
 * Before every action Playwright retries a set of ACTIONABILITY checks until they pass
 * or the timeout expires: the element is attached to the DOM, visible, STABLE (not
 * animating), enabled, and actually receives pointer events (nothing covering it).
 * That is strictly more than Selenium's elementToBeClickable, and it is why
 * Thread.sleep never appears in a Playwright suite.
 *   - Implicit wait equivalent: none, and you don't want one.
 *   - Explicit wait equivalent: the assertion itself. assertThat(x).isVisible() polls.
 *   - Still needed: waits for things the framework CANNOT see — a queue draining, a
 *     downstream record landing. Those get an explicit expectation with a real timeout.
 * Honest limit to say out loud: auto-waiting removes TIMING boilerplate, not the need
 * to think about state. A locator that matches too early still matches the wrong thing.
 *
 * ======================= LOCATOR PRIORITY (say this order) =======================
 *   1. getByRole(BUTTON, name="Sign in")  — how a user and a screen reader find it
 *   2. getByLabel / getByPlaceholder      — form controls, via their real label wiring
 *   3. getByText                          — content that is genuinely the identity
 *   4. getByTestId("data-testid")         — the explicit contract for volatile markup
 *   5. CSS / XPath                        — last resort, and generated class names never
 * Locators are LAZY: page.getByRole(...) resolves at action time, every time, so there is
 * no StaleElementReferenceException to catch. That whole Selenium failure mode disappears.
 *
 * ======================= STRICT MODE (the differentiator answer) =======================
 * Selenium's findElement silently returns the first of N matches — a bug that hides for
 * months. Playwright locators are strict: resolving to 2+ elements throws. You then say
 * what you MEANT: .first(), .nth(1), or better, a locator that is actually unique.
 *
 * ======================= ISOLATION AND PARALLELISM =======================
 * Browser (expensive, launched once) > BrowserContext (cheap, isolated cookies/storage —
 * this is the unit of test isolation, like a fresh incognito profile) > Page (a tab).
 * One context per test is the Playwright equivalent of our ThreadLocal<WebDriver>, but
 * without launching a browser per thread. The JS runner parallelizes with workers; from
 * JUnit you get the same effect with parallel execution plus a context per test.
 *
 * SECTIONS TO REPRODUCE:
 *   1. Login with role/label locators and ZERO waits
 *   2. Retrying assertion on rows that arrive late
 *   3. Strict mode violation — and the two ways to resolve it
 *   4. getByTestId + text assertion
 *   5. Native dialog handling
 *   6. page.request(): API testing from the same tool, incl. a 401 negative
 *   7. Network interception — stub the API and prove the UI's behavior
 *   8. Tracing — the artifact that root-causes a CI-only failure
 */
class SourceD10PlaywrightDrills {

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
            // Skip rather than fail: a machine without the downloaded browsers should not
            // break `mvn test` for the other nine drill modules.
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
        // A fresh context per test = fresh cookies and storage = real isolation.
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

    /** 1. Login with user-facing locators. Note what is NOT here: any wait, any sleep. */
    @Test
    void signsInUsingRoleAndLabelLocatorsWithNoExplicitWaits() {
        page.getByLabel("Username").fill("adjuster");
        page.getByLabel("Password").fill("secret_sauce");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in")).click();

        // The dashboard is 600ms away behind a spinner. This assertion RETRIES until the
        // heading is visible or the timeout expires — the explicit wait is the assertion.
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Policy Dashboard")))
                .isVisible();
    }

    /** 2. Rows arrive by fetch AFTER the dashboard renders. Count assertions retry too. */
    @Test
    void waitsForTableRowsThatArriveAfterTheDashboardRenders() {
        signIn();

        // Reading getByRole(ROW).count() ONCE here would return 0 — the fetch has not
        // resolved. hasCount polls, which is the whole point.
        assertThat(page.locator("#policy-rows tr")).hasCount(3);
        assertThat(page.locator("#policy-rows")).containsText("Cardinal Roofing");
    }

    /** 3. Two buttons say "Details". Selenium would click the first one and say nothing. */
    @Test
    void strictModeFailsLoudlyWhenALocatorMatchesTwoElements() {
        signIn();

        var ambiguous = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Details"));
        assertThat(ambiguous).hasCount(2);

        PlaywrightException violation = assertThrows(PlaywrightException.class, ambiguous::click);
        assertTrue(violation.getMessage().contains("strict mode violation"),
                "expected a strict mode violation, got: " + violation.getMessage());

        // Resolve it by SAYING WHAT YOU MEANT — either an index...
        ambiguous.first().click();
        // ...or, better, a locator that is genuinely unique.
        page.locator("#details-1002").click();
    }

    /** 4. getByTestId is the explicit contract for markup that is expected to churn. */
    @Test
    void readsTheWelcomeBannerByTestId() {
        signIn();

        assertThat(page.getByTestId("welcome-banner")).containsText("adjuster");
    }

    /** 5. Dialogs: register the handler BEFORE the action that triggers it. */
    @Test
    void acceptsANativeConfirmDialog() {
        signIn();

        page.onDialog(dialog -> {
            assertEquals("Cancel this policy?", dialog.message());
            dialog.accept();
        });
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel policy")).click();

        assertThat(page.locator("#cancel-result")).isVisible();
    }

    /**
     * 6. The JD is API-focused: Playwright is an HTTP client too. page.request() shares
     * the browser context's cookies, so an authenticated UI session can call the API
     * directly — the fastest way to set up or verify data around a UI journey.
     */
    @Test
    void callsTheApiDirectlyThroughTheBrowserContext() {
        APIResponse list = page.request().get(baseUrl + "/api/policies");
        assertEquals(200, list.status());
        assertTrue(list.text().contains("PL-1002"));

        APIResponse unauthorized = page.request().post(baseUrl + "/api/policies",
                RequestOptions.create().setData("{\"holder\":\"Riverbend Framing\"}"));
        assertEquals(401, unauthorized.status(), "missing X-API-KEY must be 401, not 403");

        APIResponse created = page.request().post(baseUrl + "/api/policies",
                RequestOptions.create()
                        .setHeader("X-API-KEY", "drill-key")
                        .setData("{\"holder\":\"Riverbend Framing\"}"));
        assertEquals(201, created.status());
        assertEquals("/api/policies/PL-3003", created.headers().get("location"));
    }

    /**
     * 7. Network interception: stub the integration at the browser boundary. This is the
     * UI-side equivalent of WireMock, and it is how you test what the page does when a
     * downstream system returns something ugly — without waiting for that system to break.
     */
    @Test
    void stubsTheApiToProveTheUiHandlesTheResponse() {
        page.route("**/api/policies", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("""
                        [{"policyId":"PL-9999","holder":"Stubbed Contractor",
                          "premium":100.00,"status":"ACTIVE"}]""")));

        signIn();

        assertThat(page.locator("#policy-rows tr")).hasCount(1);
        assertThat(page.locator("#policy-rows")).containsText("Stubbed Contractor");
    }

    /**
     * 8. Tracing: the answer to "how do you debug a test that only fails in CI?"
     * In the JS runner this is config (trace: 'on-first-retry'); in Java it is explicit.
     * The zip opens at trace.playwright.dev — DOM snapshot per action, network, console.
     */
    @Test
    void recordsATraceForFailureAnalysis() {
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        signIn();
        assertThat(page.getByTestId("welcome-banner")).isVisible();

        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("target/traces/d10-signin-trace.zip")));
    }

    /** Shared setup step. In a real suite this is a fixture or storageState, not a helper. */
    private void signIn() {
        page.getByLabel("Username").fill("adjuster");
        page.getByLabel("Password").fill("secret_sauce");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in")).click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Policy Dashboard")))
                .isVisible();
    }
}
