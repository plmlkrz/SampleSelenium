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
 * DRILL 01 — PRACTICE FILE
 *
 * 1. Read SourceD01LocatorDrills.java carefully. Then CLOSE it — no peeking.
 * 2. Pick a test below, delete its @Disabled line, and write the body from memory.
 * 3. Run:  mvn test -Dtest=PracticeD01LocatorDrills -Dheadless=true
 * 4. Compare with the source. Note what you missed. Repeat until clean.
 *
 * KEEP THIS FILE COMPILING — Java compiles every test file before running any test,
 * so a syntax error here blocks the whole project. Quick syntax check without
 * running anything:  mvn test-compile
 *
 * SECTIONS TO REPRODUCE:
 *   1. findElement on a many-match locator -> returns FIRST match (no exception)
 *   2. findElement on a no-match locator   -> assertThrows NoSuchElementException
 *   3. findElements on a no-match locator  -> empty list (the presence-check idiom)
 *   4. Same element via By.id, By.cssSelector, By.xpath — assert all equal
 *   5. RelativeLocator: input below user-name -> the password field
 */
class PracticeD01LocatorDrills extends BaseTest {

    private static final String SAUCE_URL = "https://www.saucedemo.com";

    private WebDriver driver;
    @BeforeEach
    void openSite() {
        driver = DriverManager.getDriver();
        driver.get(SAUCE_URL);
    }

    @Test
    void findElementReturnsFirstMatchWhenLocatorMatchesMany() {
        // TODO: open saucedemo, findElement(By.tagName("input")) vs findElements — compare
        WebElement first = driver.findElement(By.tagName("input"));
        List<WebElement> all = driver.findElements(By.tagName("input"));

        assertTrue(all.size() > 1, "There should be at least one input element");
        assertEquals(all.get(0), first, "findElement returns the first of the findElements list");

    }


    @Test
    void findElementThrowsWhenNothingMatches() {
        // TODO: assertThrows(...) around findElement with a bogus id
        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.id("does-not-exist-anywhere"));
        });
    }

    @Test
    void findElementsReturnsEmptyListWhenNothingMatches() {
        // TODO: findElements with a bogus id — assert not null AND empty
        List<WebElement> none = driver.findElements(By.id("does-not-exist-anywhere"));
        assertNotNull(none, "findElements never returns null");
        assertTrue(none.isEmpty(), "No match means empty list — NOT an exception");
    }

    @Test
    void sameElementByIdCssAndXpath() {
        // TODO: locate the login button 3 ways, assert they are the same element
        WebElement byId = driver.findElement(By.id("login-button"));
        WebElement byCss = driver.findElement(By.cssSelector("#login-button"));
        WebElement byXpath = driver.findElement(By.xpath("//input[@id='login-button']"));

        assertEquals(byId, byCss);
        assertEquals(byId, byXpath);
    }


    @Test
    void relativeLocatorFindsPasswordBelowUsername() {
        // TODO: RelativeLocator.with(...).below(...) — assert you got the password field
        WebElement password = driver.findElement(
                RelativeLocator.with(By.tagName("input")).below(By.id("user-name")));

        assertEquals("password", password.getDomAttribute("id"), "The input directly below the username field should be the password field");
    }
}
