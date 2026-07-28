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
 * DRILL 05 — PRACTICE FILE
 *
 * 1. Read SourceD05BrowserMechanicsDrills.java. Close it — no peeking.
 * 2. Delete one @Disabled line, write the body from memory.
 * 3. Run one test at a time:
 *      mvn test -Dtest="PracticeD05BrowserMechanicsDrills#alertAcceptAndReadResult" -Dheadless=true
 *
 * SECTIONS TO REPRODUCE (each one is a stock interview question):
 *   1. Alert: click, switchTo().alert() via ExpectedConditions.alertIsPresent, getText, accept
 *   2. Confirm: dismiss() -> "You clicked: Cancel"
 *   3. Prompt: sendKeys into the alert, accept, verify result
 *   4. Nested frames: frame("frame-top") -> frame("frame-middle") -> read -> defaultContent()
 *   5. New window: getWindowHandles, switch, close vs quit, switch back
 *   6. Selenium 4 new tab: switchTo().newWindow(WindowType.TAB)
 *   7. Dropdown: Select — byVisibleText, byValue, getFirstSelectedOption
 *   8. Hover: Actions().moveToElement(...).perform()
 *   9. JavascriptExecutor: scrollIntoView + return document.title
 *  10. Screenshot: TakesScreenshot -> OutputType.BYTES -> write file
 *  11. Stale element: remove checkbox, assertThrows(StaleElementReferenceException),
 *      then the findElements-isEmpty re-check
 *  12. Text box value: getText() vs getDomProperty("value") / getAttribute("value")
 *  13. get() vs navigate(): back / forward / refresh
 */
class PracticeD05BrowserMechanicsDrills extends BaseTest {
    private static final String THE_INTERNET = "https://the-internet.herokuapp.com";
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void grabDriver() {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

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

    /** Plain alert: switchTo().alert(), read text, accept. */
    @Test
    void confirmDismissClicksCancel() {
        driver.get(THE_INTERNET + "/javascript_alerts");
        driver.findElement(By.cssSelector("button[onclick='jsConfirm()']")).click();
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
        assertEquals("You clicked: Cancel", driver.findElement(By.id("result")).getText());
    }


    @Test
    void promptAlertTypeTextThenAccept() {
        driver.get(THE_INTERNET + "/javascript_alerts");
        driver.findElement(By.cssSelector("button[onclick='jsConfirm()']")).click();
        Alert prompt = wait.until(ExpectedConditions.alertIsPresent());
        prompt.sendKeys("Peter");
        prompt.accept();
        assertEquals("You Entered: Peter", driver.findElement(By.id("result")).getText());
    }


    @Test
    void switchIntoNestedFramesAndReadText() {
        driver.get(THE_INTERNET + "/nested_frames");
        driver.switchTo().frame("frame-top");
        driver.switchTo().frame("frame-middle");
        assertEquals("MIDDLE", driver.findElement(By.id("content")).getText());
        driver.switchTo().defaultContent();
        driver.switchTo().frame("frame-bottom");
        assertEquals("BOTTOM", driver.findElement(By.tagName("body")).getText());
    }


    @Test
    void switchToNewWindowThenBack() {
        driver.get(THE_INTERNET + "/windows");
        String original = driver.getWindowHandle();
        driver.findElement(By.linkText("Click Here")).click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        handles.remove(original);
        driver.switchTo().window(handles.get(0));
        assertEquals("New Window", driver.findElement(By.tagName("h3")).getText());
        driver.close();
        driver.switchTo().window(original);
        assertTrue(driver.getCurrentUrl().contains("/windows"));
    }


    @Test
    void openNewTabWithSelenium4() {
        driver.get("https://www.saucedemo.com");
        String original = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(THE_INTERNET);
        assertTrue(driver.getTitle().contains("The Internet"));
        driver.close();
        driver.switchTo().window(original);
        assertEquals("Swag Labs",  driver.getTitle());
    }


    @Test
    void selectDropdownOptionThreeWays() {
        driver.get(THE_INTERNET + "/dropdown");
        Select dropdown = new Select(driver.findElement(By.id("dropdown")));
        dropdown.selectByVisibleText("Option 1");
        assertEquals("Option 1", dropdown.getFirstSelectedOption().getText());
        dropdown.selectByValue("2");
        assertEquals("Option 2", dropdown.getFirstSelectedOption().getText());
    }


    @Test
    void hoverRevealsHiddenCaption() {
        driver.get(THE_INTERNET + "/hovers");
        WebElement firstAvatar = driver.findElement(By.cssSelector(".figure"));
        new Actions(driver).moveToElement(firstAvatar).perform();
        WebElement caption = firstAvatar.findElement(By.cssSelector(".figcaption h5"));
        assertTrue(caption.isDisplayed(), "Caption should appear on hover");
        assertEquals("name: user1", caption.getText());
    }


    @Test
    void javascriptExecutorScrollAndRead() {
        driver.get(THE_INTERNET + "/large");
        JavascriptExecutor js = (JavascriptExecutor)  driver;
        WebElement deepRow = driver.findElement(By.id("large-table"));
        js.executeScript("arguments[0].scrollIntoView(true);", deepRow);
        String title = (String) js.executeScript("return document.title;");
        assertEquals("The Internet", title);
        Long scrollY = (Long) js.executeScript("return Math.round(window.pageYOffset);");
        assertTrue(scrollY > 0, "Page should have scrolled down");
    }


    @Test
    void takeScreenshotToFile()throws Exception {
        driver.get("https://www.saucedemo.com");
        byte[]png = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
        Path dir = Path.of("target", "screenshots");
        Files.createDirectories(dir);
        Path file = dir.resolve("manual_demo.png");
        Files.write(file, png);
        assertTrue(Files.size(file) > 0, "Screenshot file should not be empty");
    }


    @Test
    void staleElementAfterDomRemoval() {
        driver.get(THE_INTERNET + "/dynamic_controls");
        WebElement checkbox = driver.findElement(By.cssSelector("#checkbox input"));
        driver.findElement(By.cssSelector("#checkbox-example button")).click();
        wait.until(ExpectedConditions.textToBe(By.id("message"), "It's gone!"));
        assertThrows(StaleElementReferenceException.class, checkbox::isSelected);
        assertTrue (driver.findElements(By.cssSelector("#checkbox input")).isEmpty());
    }


    @Test
    void readTypedTextWithGetAttributeValue() {
        driver.get(THE_INTERNET + "/inputs");
        WebElement input = driver.findElement(By.tagName("input"));
        input.sendKeys("42");
        assertEquals("", input.getText(), "getText() is empty for input elements");
        assertEquals("42", input.getDomAttribute("value"),  "The typed value lives in the element's value property");

    }


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
