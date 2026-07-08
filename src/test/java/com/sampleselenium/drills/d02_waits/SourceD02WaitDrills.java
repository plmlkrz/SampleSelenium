package com.sampleselenium.drills.d02_waits;

import com.sampleselenium.base.BaseTest;
import com.sampleselenium.driver.DriverManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 02 — WAITS  [SOURCE — read, close, reproduce in Practice file]
 *
 * Run:  mvn test -Dtest=SourceD02WaitDrills -Dheadless=true
 *
 * Interview questions this drill answers:
 *  - Infosys Q8/Q9/Q10, Deloitte R1 Q9, R2 Q17: waits, implicit vs explicit, fluent wait
 *  - "Why is Thread.sleep bad?" — every large firm asks some form of this
 *
 * THE 30-SECOND ANSWER:
 *   Implicit wait is a global setting on the driver: every findElement retries up to the
 *   timeout before throwing. Explicit wait (WebDriverWait + ExpectedConditions) targets ONE
 *   condition on ONE element — visibility, clickability, text present — and returns as soon
 *   as the condition is true. Fluent wait is the general form of explicit wait where you also
 *   control the POLLING INTERVAL and which exceptions to IGNORE while polling.
 *   Thread.sleep always burns the full duration and still fails when the app is slower than
 *   your guess — explicit waits are both faster and more reliable.
 *
 * WATCH-OUT they love to probe: don't MIX implicit and explicit waits — the timeouts
 * interact unpredictably (the implicit wait applies inside every poll of the explicit wait).
 * Pick explicit waits and leave implicit at zero. This project's BasePage does exactly that.
 */
class SourceD02WaitDrills extends BaseTest {

    // the-internet's dynamic loading pages exist precisely to practice waits:
    // /dynamic_loading/1 — element is present in the DOM but hidden until "Start" is clicked
    // /dynamic_loading/2 — element is ADDED to the DOM only after "Start" is clicked
    private static final String HIDDEN_ELEMENT_URL = "https://the-internet.herokuapp.com/dynamic_loading/1";
    private static final String ADDED_ELEMENT_URL = "https://the-internet.herokuapp.com/dynamic_loading/2";

    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By FINISH_TEXT = By.id("finish");

    private WebDriver driver;

    @BeforeEach
    void grabDriver() {
        driver = DriverManager.getDriver();
    }

    /** Explicit wait: WebDriverWait + ExpectedConditions — the everyday workhorse. */
    @Test
    void explicitWaitForHiddenElementToBecomeVisible() {
        driver.get(HIDDEN_ELEMENT_URL);
        driver.findElement(START_BUTTON).click();

        // Element already in the DOM, just invisible -> visibilityOfElementLocated
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement finish = wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_TEXT));

        assertEquals("Hello World!", finish.getText());
    }

    /**
     * Fluent wait: same idea, but YOU choose polling interval and ignored exceptions.
     * Needed here because on /dynamic_loading/2 the element does not exist at all until
     * loading finishes — polls in between would throw NoSuchElementException.
     */
    @Test
    void fluentWaitForElementAddedToDom() {
        driver.get(ADDED_ELEMENT_URL);
        driver.findElement(START_BUTTON).click();

        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class);

        WebElement finish = wait.until(d -> d.findElement(FINISH_TEXT));

        assertEquals("Hello World!", finish.getText());
    }

    /**
     * Implicit wait: a driver-wide retry budget for every findElement call.
     * Demonstrated, then RESET TO ZERO — see the class comment for why mixing is dangerous.
     */
    @Test
    void implicitWaitRetriesEveryFindElement() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        try {
            driver.get(ADDED_ELEMENT_URL);
            driver.findElement(START_BUTTON).click();

            // No explicit wait here — the implicit wait keeps retrying this findElement
            // until the element appears (or 10s passes).
            WebElement finish = driver.findElement(FINISH_TEXT);
            assertEquals("Hello World!", finish.getText());
        } finally {
            // Always reset so this global setting can't leak into other tests.
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        }
    }

    /**
     * Why Thread.sleep is the anti-pattern: the explicit wait returns the moment the
     * condition is met; sleep would burn its full duration every single time — and would
     * STILL fail on the day the app is slower than the hardcoded number.
     */
    @Test
    void explicitWaitReturnsEarlyUnlikeSleep() {
        driver.get(HIDDEN_ELEMENT_URL);
        driver.findElement(START_BUTTON).click();

        long start = System.currentTimeMillis();
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.visibilityOfElementLocated(FINISH_TEXT));
        long elapsed = System.currentTimeMillis() - start;

        // The page's loader takes ~5s. A Thread.sleep(30_000) "equivalent" would always
        // cost 30s; the explicit wait cost only as long as the app actually needed.
        assertTrue(elapsed < 30_000, "Explicit wait should return as soon as the element shows, not at timeout");
        System.out.println("[waits drill] explicit wait returned after " + elapsed + " ms (timeout was 30s)");
    }
}
