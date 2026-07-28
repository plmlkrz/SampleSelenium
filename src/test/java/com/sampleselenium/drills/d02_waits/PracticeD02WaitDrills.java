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
 * DRILL 02 — PRACTICE FILE
 *
 * 1. Read SourceD02WaitDrills.java. Close it — no peeking.
 * 2. Delete one @Disabled line, write the body from memory.
 * 3. Run:  mvn test -Dtest=PracticeD02WaitDrills -Dheadless=true
 * 4. Compare with the source. Repeat until clean.
 *
 * SECTIONS TO REPRODUCE:
 *   1. Explicit wait: /dynamic_loading/1, click Start, WebDriverWait +
 *      ExpectedConditions.visibilityOfElementLocated, assert "Hello World!"
 *   2. Fluent wait: /dynamic_loading/2, FluentWait with withTimeout / pollingEvery /
 *      ignoring(NoSuchElementException.class), lambda findElement
 *   3. Implicit wait: manage().timeouts().implicitlyWait(...), then RESET to ZERO in finally
 *   4. Timing proof: explicit wait with a 30s timeout returns in ~5s (measure elapsed ms)
 *
 * SAY OUT LOUD WHILE DRILLING: implicit = global retry on findElement; explicit = one
 * condition, one element, returns early; fluent = explicit + polling interval + ignored
 * exceptions; Thread.sleep = always full cost, still flaky.
 */
class PracticeD02WaitDrills extends BaseTest {

    private static final String HIDDEN_ELEMENT_URL = "https://the-internet.herokuapp.com/dynamic_loading/1";
    private static final String ADDED_ELEMENT_URL = "https://the-internet.herokuapp.com/dynamic_loading/2";

    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By FINISH_TEXT = By.id("finish");

    private WebDriver driver;
    @BeforeEach
    void grabDriver() {
        driver = DriverManager.getDriver(); }


    @Test
    void explicitWaitForHiddenElementToBecomeVisible() {
        // TODO
        driver.get(HIDDEN_ELEMENT_URL);
        driver.findElement(START_BUTTON).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement finish = wait.until(ExpectedConditions.visibilityOfElementLocated(FINISH_TEXT));

        assertEquals("Hello World!", finish.getText());
    }


    @Test
    void fluentWaitForElementAddedToDom() {
        // TODO
        driver.get(ADDED_ELEMENT_URL);
        driver.findElement(START_BUTTON).click();

        Wait<WebDriver> wait = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(10)).pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class);

        WebElement finish = wait.until(d -> driver.findElement(FINISH_TEXT));
        assertEquals("Hello World!", finish.getText());
    }


    @Test
    void implicitWaitRetriesEveryFindElement() {
        // TODO — and don't forget the finally block that resets it to Duration.ZERO
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        try {
            driver.get(ADDED_ELEMENT_URL);
            driver.findElement(START_BUTTON).click();

            WebElement finish = driver.findElement(FINISH_TEXT);
            assertEquals("Hello World!", finish.getText());
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        }
    }


    @Test
    void explicitWaitReturnsEarlyUnlikeSleep() {
        // TODO
        driver.get(HIDDEN_ELEMENT_URL);
        driver.findElement(START_BUTTON).click();

        long start = System.currentTimeMillis();
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.visibilityOfElementLocated(FINISH_TEXT));
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 30_000);
        System.out.println("[waits drill] explicit wait returned after " + elapsed + " ms (timeout was 30s)");

    }
}
