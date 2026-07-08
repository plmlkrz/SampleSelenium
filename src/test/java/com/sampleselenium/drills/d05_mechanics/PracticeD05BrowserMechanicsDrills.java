package com.sampleselenium.drills.d05_mechanics;

import com.sampleselenium.base.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void alertAcceptAndReadResult() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void confirmDismissClicksCancel() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void promptAlertTypeTextThenAccept() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void switchIntoNestedFramesAndReadText() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void switchToNewWindowThenBack() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void openNewTabWithSelenium4() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void selectDropdownOptionThreeWays() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void hoverRevealsHiddenCaption() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void javascriptExecutorScrollAndRead() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void takeScreenshotToFile() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void staleElementAfterDomRemoval() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void readTypedTextWithGetAttributeValue() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void navigateBackForwardRefresh() {
        // TODO
    }
}
