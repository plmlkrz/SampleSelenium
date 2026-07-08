package com.sampleselenium.drills.d03_testng;

import com.sampleselenium.drills.support.TestNgBase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * DRILL 03 — PRACTICE FILE
 *
 * 1. Read SourceD03TestNgDrills.java. Close it — no peeking.
 * 2. Flip ONE test from enabled = false to enabled = true, write the body from memory.
 * 3. Run:  mvn test -Ptestng-drills -Dheadless=true
 * 4. Compare with the source. Repeat until clean.
 *
 * SECTIONS TO REPRODUCE:
 *   1. Lifecycle hooks: @BeforeSuite / @BeforeTest / @BeforeClass prints (know the order!)
 *   2. priority + groups("smoke") login test
 *   3. dependsOnMethods test (what happens when the dependency FAILS? skipped, not failed)
 *   4. @DataProvider with 3 login rows + the data-driven test that consumes it
 *   5. SoftAssert with 3 checks + assertAll() (what breaks if you forget assertAll?)
 *   6. BONUS, say out loud: how to rerun only failures (testng-failed.xml), where reports
 *      land (target/surefire-reports), how to register a screenshot-on-failure listener
 */
public class PracticeD03TestNgDrills extends TestNgBase {

    @Test(enabled = false /* TODO: flip to true and write from memory */, priority = 1, groups = "smoke")
    public void loginSucceedsForStandardUser() {
        // TODO
    }

    @Test(enabled = false /* TODO */, priority = 2, groups = "smoke")
    public void inventoryShowsSixProductsAfterLogin() {
        // TODO — and add the dependsOnMethods attribute from memory
    }

    @DataProvider(name = "loginScenarios")
    public Object[][] loginScenarios() {
        // TODO: three rows — standard_user OK, locked_out_user error, wrong password error
        return new Object[][]{};
    }

    @Test(enabled = false /* TODO */, dataProvider = "loginScenarios", groups = "regression")
    public void loginDataDriven(String username, String password, boolean expectSuccess, String expectedError) {
        // TODO
    }

    @Test(enabled = false /* TODO */, groups = "regression")
    public void softAssertChecksSeveralThingsInOnePass() {
        // TODO — SoftAssert, three checks, and the one line everyone forgets
    }
}
