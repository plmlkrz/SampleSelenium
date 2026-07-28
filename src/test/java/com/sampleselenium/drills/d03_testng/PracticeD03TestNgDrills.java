package com.sampleselenium.drills.d03_testng;

import com.sampleselenium.drills.support.ScreenshotOnFailureListener;
import com.sampleselenium.drills.support.TestNgBase;
import com.sampleselenium.driver.DriverManager;
import com.sampleselenium.pages.InventoryPage;
import com.sampleselenium.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

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
@Listeners (ScreenshotOnFailureListener.class)
public class PracticeD03TestNgDrills extends TestNgBase {

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() { System.out.println("[lifecycle] @BeforeSuite — once per suite run");}

    @BeforeTest(alwaysRun = true)
    public void beforeTest() { System.out.println("[lifecycle] @BeforeTest — once per <test> tag in testng.xml");}

    @BeforeClass(alwaysRun = true)
    public void beforeClass() { System.out.println("[lifecycle] @BeforeClass — once per class");}




    @Test(priority = 1, groups = "smoke")
    public void loginSucceedsForStandardUser() {
        InventoryPage inventory = new LoginPage(DriverManager.getDriver()).open().login("standard_user", "secret_sauce");
        Assert.assertTrue(inventory.isLoaded());
    }

    @Test(priority = 2, groups = "smoke")
    public void inventoryShowsSixProductsAfterLogin() {
        InventoryPage inventory = new LoginPage(DriverManager.getDriver()).open().login("standard_user", "secret_sauce");
        Assert.assertEquals(inventory.getProductCount(), 6, "Sauce Demo lists 6 products");
    }

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
        InventoryPage inventory = new LoginPage(DriverManager.getDriver()).open().login(username, password);

        if(expectSuccess) {
            Assert.assertTrue(inventory.isLoaded(), "Expected successful login for " + username);
        }else{
            Assert.assertTrue(loginPage.getErrorMessage().contains(expectedError), "Expected successful login for " + username);
        }
    }

    @Test(groups = "regression")
    public void softAssertChecksSeveralThingsInOnePass() {
        InventoryPage inventory = new LoginPage(DriverManager.getDriver()).open().login("standard_user", "secret_sauce");
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(inventory.isLoaded(), "should be on inventory page");
        softAssert.assertEquals(inventory.getPageTitle(), "Products", "page header");
        softAssert.assertEquals(inventory.getProductCount(), 6, "Product count");
        softAssert.assertAll();
    }
}
