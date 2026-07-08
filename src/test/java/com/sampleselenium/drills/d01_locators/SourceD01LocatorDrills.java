package com.sampleselenium.drills.d01_locators;

import com.sampleselenium.base.BaseTest;
import com.sampleselenium.driver.DriverManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 01 — LOCATORS, findElement vs findElements  [SOURCE — read, close, reproduce in Practice file]
 *
 * Run:  mvn test -Dtest=SourceD01LocatorDrills -Dheadless=true
 *
 * Interview questions this drill answers:
 *  - Infosys Selenium Q7 / Deloitte R1 Q8: difference between findElement() and findElements()
 *  - Deloitte R1 Q14: if an XPath matches two elements, does findElement throw?
 *  - Infosys Q5/Q6/Q16/Q17: locators, fastest locator, XPath vs CSS, absolute vs relative XPath
 *  - Deloitte R1 Q16: relative locators (Selenium 4)
 *
 * THE 30-SECOND ANSWER (say this out loud while the tests run):
 *   findElement returns a single WebElement and THROWS NoSuchElementException when nothing
 *   matches. findElements returns a List&lt;WebElement&gt; and returns an EMPTY LIST — no
 *   exception — when nothing matches. So for a presence check I use
 *   !findElements(locator).isEmpty() instead of try/catch around findElement:
 *   exception-based control flow is slower and an anti-pattern.
 */
class SourceD01LocatorDrills extends BaseTest {

    private static final String SAUCE_URL = "https://www.saucedemo.com";

    private WebDriver driver;

    @BeforeEach
    void openSite() {
        driver = DriverManager.getDriver();
        driver.get(SAUCE_URL);
    }

    /** findElement on a locator matching MANY elements: no exception — you get the FIRST match. */
    @Test
    void findElementReturnsFirstMatchWhenLocatorMatchesMany() {
        // The login page has several <input> elements (username, password, submit button).
        WebElement first = driver.findElement(By.tagName("input"));
        List<WebElement> all = driver.findElements(By.tagName("input"));

        assertTrue(all.size() > 1, "Locator should match multiple inputs");
        // findElement == the first element findElements would have returned
        assertEquals(all.get(0), first, "findElement returns the first of the findElements list");
    }

    /** findElement on a locator matching NOTHING: throws NoSuchElementException. */
    @Test
    void findElementThrowsWhenNothingMatches() {
        assertThrows(NoSuchElementException.class,
                () -> driver.findElement(By.id("does-not-exist-anywhere")));
    }

    /** findElements on a locator matching NOTHING: empty list, no exception — the presence-check idiom. */
    @Test
    void findElementsReturnsEmptyListWhenNothingMatches() {
        List<WebElement> none = driver.findElements(By.id("does-not-exist-anywhere"));

        assertNotNull(none, "findElements never returns null");
        assertTrue(none.isEmpty(), "No match means empty list — NOT an exception");
        // This is why BasePage.isDisplayed() could be rewritten as
        // return !driver.findElements(locator).isEmpty();  — no try/catch needed.
    }

    /**
     * Same element, three locator strategies. Talking points:
     *  - By.id is the fastest and most stable (browsers index by id natively).
     *  - CSS is generally faster than XPath and is the go-to when there's no id.
     *  - XPath is the only one that can traverse UPWARD (parent/ancestor) and match by text().
     *  - Absolute XPath (/html/body/div...) breaks on any DOM change; relative XPath (//tag[@attr])
     *    survives layout changes — never use absolute in real suites.
     */
    @Test
    void sameElementByIdCssAndXpath() {
        WebElement byId = driver.findElement(By.id("login-button"));
        WebElement byCss = driver.findElement(By.cssSelector("input#login-button"));
        WebElement byXpath = driver.findElement(By.xpath("//input[@id='login-button']"));

        assertEquals(byId, byCss);
        assertEquals(byId, byXpath);
    }

    /**
     * Selenium 4 relative locators (a.k.a. friendly locators): above / below / toLeftOf /
     * toRightOf / near. Deloitte asked for these by name.
     */
    @Test
    void relativeLocatorFindsPasswordBelowUsername() {
        WebElement password = driver.findElement(
                RelativeLocator.with(By.tagName("input")).below(By.id("user-name")));

        assertEquals("password", password.getDomAttribute("id"),
                "The input directly below the username field should be the password field");
    }
}
