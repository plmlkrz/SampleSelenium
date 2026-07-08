package com.sampleselenium.drills.d01_locators;

import com.sampleselenium.base.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void findElementReturnsFirstMatchWhenLocatorMatchesMany() {
        // TODO: open saucedemo, findElement(By.tagName("input")) vs findElements — compare
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void findElementThrowsWhenNothingMatches() {
        // TODO: assertThrows(...) around findElement with a bogus id
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void findElementsReturnsEmptyListWhenNothingMatches() {
        // TODO: findElements with a bogus id — assert not null AND empty
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void sameElementByIdCssAndXpath() {
        // TODO: locate the login button 3 ways, assert they are the same element
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void relativeLocatorFindsPasswordBelowUsername() {
        // TODO: RelativeLocator.with(...).below(...) — assert you got the password field
    }
}
