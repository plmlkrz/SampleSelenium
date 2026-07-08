package com.sampleselenium.drills.d03_testng;

import com.sampleselenium.driver.DriverManager;
import com.sampleselenium.drills.support.ScreenshotOnFailureListener;
import com.sampleselenium.drills.support.TestNgBase;
import com.sampleselenium.pages.InventoryPage;
import com.sampleselenium.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * DRILL 03 — TESTNG CORE  [SOURCE — read, close, reproduce in Practice file]
 *
 * Run:  mvn test -Ptestng-drills -Dheadless=true
 * (TestNG drills run through testng-drills.xml, NOT through a plain `mvn test` —
 *  that separation is itself an interview talking point: suite files control TestNG runs.)
 *
 * Interview questions this drill answers:
 *  - Infosys TestNG Q1-Q10: annotations, @BeforeSuite vs @BeforeTest, priority,
 *    dependsOnMethods, DataProvider, groups, reports, Assert vs Verify
 *  - Deloitte R2 Q4: screenshots for failed tests only (see the @Listeners line + listener class)
 *
 * ANNOTATION LIFECYCLE (watch the console output prove it):
 *   @BeforeSuite  — once for the whole testng.xml suite
 *   @BeforeTest   — once per <test> tag in the suite file (NOT before each test method!)
 *   @BeforeClass  — once per test class
 *   @BeforeMethod — before EVERY @Test method (lives in TestNgBase; creates the driver)
 *   ...mirrored by @AfterMethod/@AfterClass/@AfterTest/@AfterSuite in reverse order.
 *
 * "How do you rerun only failed tests?" — after a run, TestNG writes
 * target/surefire-reports/testng-failed.xml; run that file as the suite and only the
 * failures execute. Reports: TestNG writes emailable-report.html and index.html under
 * target/surefire-reports by default; most teams layer Allure or ExtentReports on top.
 */
@Listeners(ScreenshotOnFailureListener.class)
public class SourceD03TestNgDrills extends TestNgBase {

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        System.out.println("[lifecycle] @BeforeSuite — once per suite run");
    }

    @BeforeTest(alwaysRun = true)
    public void beforeTest() {
        System.out.println("[lifecycle] @BeforeTest — once per <test> tag in testng.xml");
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        System.out.println("[lifecycle] @BeforeClass — once per class");
    }

    /**
     * priority: lower number runs first (default 0). Without priority, TestNG runs methods
     * in a deterministic-but-surprising order (roughly alphabetical) — priority makes intent
     * explicit. NOTE: each @Test still gets a FRESH browser from @BeforeMethod, so priority
     * controls ORDER, it must never be used to share state between tests.
     */
    @Test(priority = 1, groups = "smoke")
    public void loginSucceedsForStandardUser() {
        InventoryPage inventory = new LoginPage(DriverManager.getDriver())
                .open()
                .login("standard_user", "secret_sauce");

        Assert.assertTrue(inventory.isLoaded(), "Standard user should reach the inventory page");
    }

    /**
     * dependsOnMethods: if the method we depend on FAILS, this test is SKIPPED (not failed).
     * That keeps one root cause from producing a wall of misleading red — a favorite
     * "why would you use this" interview probe.
     */
    @Test(priority = 2, groups = "smoke", dependsOnMethods = "loginSucceedsForStandardUser")
    public void inventoryShowsSixProductsAfterLogin() {
        InventoryPage inventory = new LoginPage(DriverManager.getDriver())
                .open()
                .login("standard_user", "secret_sauce");

        Assert.assertEquals(inventory.getProductCount(), 6, "Sauce Demo lists 6 products");
    }

    /**
     * @DataProvider = TestNG's data-driven engine: one test body, many rows.
     * Each Object[] row maps positionally onto the test method's parameters.
     * (JUnit 5 equivalent: @ParameterizedTest + @MethodSource. Cucumber equivalent:
     * Scenario Outline + Examples — see drill 08.)
     */
    @DataProvider(name = "loginScenarios")
    public Object[][] loginScenarios() {
        return new Object[][]{
                // username,        password,        expectSuccess, expectedErrorFragment
                {"standard_user",   "secret_sauce",  true,          ""},
                {"locked_out_user", "secret_sauce",  false,         "Sorry, this user has been locked out"},
                {"standard_user",   "wrong_password", false,        "Username and password do not match"},
        };
    }

    @Test(dataProvider = "loginScenarios", groups = "regression")
    public void loginDataDriven(String username, String password, boolean expectSuccess, String expectedError) {
        LoginPage loginPage = new LoginPage(DriverManager.getDriver()).open();
        InventoryPage inventory = loginPage.login(username, password);

        if (expectSuccess) {
            Assert.assertTrue(inventory.isLoaded(), "Expected successful login for " + username);
        } else {
            Assert.assertTrue(loginPage.getErrorMessage().contains(expectedError),
                    "Expected error containing: " + expectedError);
        }
    }

    /**
     * "Difference between Assert and Verify" (Infosys TestNG Q10):
     * TestNG has no literal Verify keyword — the real answer is HARD vs SOFT assertions.
     * Hard assert (org.testng.Assert) stops the test at the first failure.
     * SoftAssert records failures and keeps going; nothing fails until assertAll().
     * Classic bug they test for: forgetting assertAll() — then the test can never fail.
     */
    @Test(groups = "regression")
    public void softAssertChecksSeveralThingsInOnePass() {
        InventoryPage inventory = new LoginPage(DriverManager.getDriver())
                .open()
                .login("standard_user", "secret_sauce");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(inventory.isLoaded(), "should be on inventory page");
        softly.assertEquals(inventory.getPageTitle(), "Products", "page header");
        softly.assertEquals(inventory.getProductCount(), 6, "product count");
        softly.assertAll(); // <- without this line, the soft assertions are silently ignored
    }
}
