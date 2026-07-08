package com.sampleselenium.drills.d05_mechanics;

import com.sampleselenium.base.BaseTest;
import com.sampleselenium.driver.DriverManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 05 — BROWSER MECHANICS  [SOURCE — read, close, reproduce in Practice file]
 *
 * Run all:      mvn test -Dtest=SourceD05BrowserMechanicsDrills -Dheadless=true
 * Run one:      mvn test -Dtest="SourceD05BrowserMechanicsDrills#alertAcceptAndReadResult" -Dheadless=true
 *
 * Covers: alerts (incl. typing into a prompt — Deloitte R1 Q12), frames, window/tab switching
 * (incl. Selenium 4 newWindow — Deloitte R2 Q18), close vs quit, dropdowns, Actions hover,
 * JavascriptExecutor, screenshots, StaleElementReferenceException, getText vs getAttribute
 * for text boxes (Deloitte R1 Q11), get vs navigate.
 *
 * "Element click intercepted" (Deloitte R2 Q3) — the spoken answer:
 *   The element was found but something else (overlay, cookie banner, sticky header) would
 *   receive the click. Fixes in order of preference: wait for the overlay to disappear
 *   (invisibilityOfElementLocated), wait elementToBeClickable, scrollIntoView first, and as
 *   a last resort click via JavascriptExecutor — noting JS clicks bypass the real user event
 *   pipeline, so they can hide genuine bugs.
 *
 * "Can Selenium automate captcha?" — No, by design; captchas exist to block automation.
 *   Real answer: test environments disable it, use a test bypass token, or stub the service.
 */
class SourceD05BrowserMechanicsDrills extends BaseTest {

    private static final String THE_INTERNET = "https://the-internet.herokuapp.com";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void grabDriver() {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // ---------- ALERTS ----------

    /** Plain alert: switchTo().alert(), read text, accept. */
    @Test
    void alertAcceptAndReadResult() {
        driver.get(THE_INTERNET + "/javascript_alerts");
        driver.findElement(By.cssSelector("button[onclick='jsAlert()']")).click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("I am a JS Alert", alert.getText());
        alert.accept();

        assertEquals("You successfully clicked an alert", driver.findElement(By.id("result")).getText());
    }

    /** Confirm box: dismiss() clicks Cancel (accept() would click OK). */
    @Test
    void confirmDismissClicksCancel() {
        driver.get(THE_INTERNET + "/javascript_alerts");
        driver.findElement(By.cssSelector("button[onclick='jsConfirm()']")).click();

        wait.until(ExpectedConditions.alertIsPresent()).dismiss();

        assertEquals("You clicked: Cancel", driver.findElement(By.id("result")).getText());
    }

    /** DELOITTE R1 Q12 — "How do you enter text in an alert?" -> only prompts take text: alert.sendKeys(). */
    @Test
    void promptAlertTypeTextThenAccept() {
        driver.get(THE_INTERNET + "/javascript_alerts");
        driver.findElement(By.cssSelector("button[onclick='jsPrompt()']")).click();

        Alert prompt = wait.until(ExpectedConditions.alertIsPresent());
        prompt.sendKeys("Peter");
        prompt.accept();

        assertEquals("You entered: Peter", driver.findElement(By.id("result")).getText());
    }

    // ---------- FRAMES ----------

    /**
     * You CANNOT touch elements inside an <iframe> until you switch into it.
     * switchTo().frame(nameOrIdOrIndexOrElement) to enter, switchTo().defaultContent()
     * to come all the way back out (parentFrame() goes up just one level).
     */
    @Test
    void switchIntoNestedFramesAndReadText() {
        driver.get(THE_INTERNET + "/nested_frames");

        driver.switchTo().frame("frame-top");      // enter top frame...
        driver.switchTo().frame("frame-middle");   // ...then the middle frame inside it
        assertEquals("MIDDLE", driver.findElement(By.id("content")).getText());

        driver.switchTo().defaultContent();        // all the way back to the main document
        driver.switchTo().frame("frame-bottom");
        assertEquals("BOTTOM", driver.findElement(By.tagName("body")).getText());
    }

    // ---------- WINDOWS AND TABS ----------

    /**
     * Window switching: getWindowHandle() = current, getWindowHandles() = all (a Set).
     * driver.close() closes ONLY the current window; driver.quit() ends the whole session
     * (all windows + the driver process) — the classic close-vs-quit question.
     */
    @Test
    void switchToNewWindowThenBack() {
        driver.get(THE_INTERNET + "/windows");
        String original = driver.getWindowHandle();

        driver.findElement(By.linkText("Click Here")).click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Find the handle that isn't ours and switch to it
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        handles.remove(original);
        driver.switchTo().window(handles.get(0));
        assertEquals("New Window", driver.findElement(By.tagName("h3")).getText());

        driver.close();                            // close only the child window
        driver.switchTo().window(original);        // ALWAYS switch back after close
        assertTrue(driver.getCurrentUrl().contains("/windows"));
    }

    /** DELOITTE R2 Q18 — Selenium 4 opens a fresh tab/window natively: switchTo().newWindow(). */
    @Test
    void openNewTabWithSelenium4() {
        driver.get("https://www.saucedemo.com");
        String original = driver.getWindowHandle();

        driver.switchTo().newWindow(WindowType.TAB);   // WindowType.WINDOW for a new window
        driver.get(THE_INTERNET);
        assertTrue(driver.getTitle().contains("The Internet"));

        driver.close();
        driver.switchTo().window(original);
        assertEquals("Swag Labs", driver.getTitle());
    }

    // ---------- DROPDOWNS ----------

    /** Real <select> elements use the Select helper: byVisibleText / byValue / byIndex. */
    @Test
    void selectDropdownOptionThreeWays() {
        driver.get(THE_INTERNET + "/dropdown");
        Select dropdown = new Select(driver.findElement(By.id("dropdown")));

        dropdown.selectByVisibleText("Option 1");
        assertEquals("Option 1", dropdown.getFirstSelectedOption().getText());

        dropdown.selectByValue("2");
        assertEquals("Option 2", dropdown.getFirstSelectedOption().getText());
        // Watch-out: Select only works on a real <select> tag. Custom JS dropdowns
        // (divs styled as dropdowns) need click + click on the option instead.
    }

    // ---------- ACTIONS (mouse) ----------

    /** Actions class: moveToElement (hover). Same class does dragAndDrop, contextClick, doubleClick, keyDown. */
    @Test
    void hoverRevealsHiddenCaption() {
        driver.get(THE_INTERNET + "/hovers");
        WebElement firstAvatar = driver.findElement(By.cssSelector(".figure"));

        new Actions(driver).moveToElement(firstAvatar).perform();   // perform() actually executes

        WebElement caption = firstAvatar.findElement(By.cssSelector(".figcaption h5"));
        assertTrue(caption.isDisplayed(), "Caption should appear on hover");
        assertEquals("name: user1", caption.getText());
    }

    // ---------- JAVASCRIPT EXECUTOR ----------

    /** JavascriptExecutor: scrolling, reading page state, last-resort clicks. */
    @Test
    void javascriptExecutorScrollAndRead() {
        driver.get(THE_INTERNET + "/large");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement deepRow = driver.findElement(By.id("large-table"));
        js.executeScript("arguments[0].scrollIntoView(true);", deepRow);

        String title = (String) js.executeScript("return document.title;");
        assertEquals("The Internet", title);

        Long scrollY = (Long) js.executeScript("return Math.round(window.pageYOffset);");
        assertTrue(scrollY > 0, "Page should have scrolled down");
    }

    // ---------- SCREENSHOTS ----------

    /** Manual screenshot: cast to TakesScreenshot. (Failed-only screenshots: see ScreenshotOnFailureListener.) */
    @Test
    void takeScreenshotToFile() throws Exception {
        driver.get("https://www.saucedemo.com");

        byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Path dir = Path.of("target", "screenshots");
        Files.createDirectories(dir);
        Path file = dir.resolve("manual_demo.png");
        Files.write(file, png);

        assertTrue(Files.size(file) > 0, "Screenshot file should not be empty");
    }

    // ---------- STALE ELEMENTS ----------

    /**
     * StaleElementReferenceException = you hold a reference to an element that is no longer
     * attached to the DOM (removed or re-rendered). Fix: RE-FIND the element after the DOM
     * changes — never cache WebElements across page updates. (This is also why this project
     * stores By locators in page objects, not WebElement fields.)
     */
    @Test
    void staleElementAfterDomRemoval() {
        driver.get(THE_INTERNET + "/dynamic_controls");
        WebElement checkbox = driver.findElement(By.cssSelector("#checkbox input"));

        driver.findElement(By.cssSelector("#checkbox-example button")).click();  // "Remove"
        wait.until(ExpectedConditions.textToBe(By.id("message"), "It's gone!"));

        // The old reference is now stale...
        assertThrows(StaleElementReferenceException.class, checkbox::isSelected);
        // ...and the re-find pattern proves the element is really gone (findElements idiom!)
        assertTrue(driver.findElements(By.cssSelector("#checkbox input")).isEmpty());
    }

    // ---------- READING INPUT VALUES ----------

    /** DELOITTE R1 Q11 — getText() does NOT read what's typed in a text box; use getAttribute("value"). */
    @Test
    void readTypedTextWithGetAttributeValue() {
        driver.get(THE_INTERNET + "/inputs");
        WebElement input = driver.findElement(By.tagName("input"));
        input.sendKeys("42");

        assertEquals("", input.getText(), "getText() is empty for input elements");
        assertEquals("42", input.getDomProperty("value"),
                "The typed value lives in the element's value property");
        // Legacy API you must still recognize in interviews: input.getAttribute("value").
        // Selenium 4 splits it into getDomAttribute (HTML) vs getDomProperty (live DOM).
    }

    // ---------- NAVIGATION ----------

    /** get() vs navigate(): both load a URL; navigate() adds back/forward/refresh history control. */
    @Test
    void navigateBackForwardRefresh() {
        driver.get("https://www.saucedemo.com");
        driver.navigate().to(THE_INTERNET + "/login");
        assertTrue(driver.getCurrentUrl().contains("the-internet"));

        driver.navigate().back();
        assertTrue(driver.getCurrentUrl().contains("saucedemo"));

        driver.navigate().forward();
        assertTrue(driver.getCurrentUrl().contains("the-internet"));

        driver.navigate().refresh();
        assertTrue(driver.getCurrentUrl().contains("the-internet"));
    }
}
