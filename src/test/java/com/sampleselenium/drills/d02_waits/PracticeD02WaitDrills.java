package com.sampleselenium.drills.d02_waits;

import com.sampleselenium.base.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void explicitWaitForHiddenElementToBecomeVisible() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void fluentWaitForElementAddedToDom() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void implicitWaitRetriesEveryFindElement() {
        // TODO — and don't forget the finally block that resets it to Duration.ZERO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void explicitWaitReturnsEarlyUnlikeSleep() {
        // TODO
    }
}
