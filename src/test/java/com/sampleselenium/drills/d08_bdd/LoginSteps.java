package com.sampleselenium.drills.d08_bdd;

import com.sampleselenium.driver.DriverManager;
import com.sampleselenium.pages.InventoryPage;
import com.sampleselenium.pages.LoginPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 08 — STEP DEFINITIONS: the glue between Gherkin lines and Selenium code.
 *
 * How matching works: Cucumber scans the "glue" package (named in RunCucumberDrills),
 * and matches each feature line against these expressions. {string} and {int} are
 * Cucumber expression parameters — they capture the quoted/numeric values as arguments.
 *
 * Cucumber HOOKS vs TestNG/JUnit hooks: @Before/@After here are io.cucumber.java
 * annotations and run around each SCENARIO — the browser lifecycle lives here, so
 * scenarios stay pure business language. Order across classes is controlled with
 * @Before(order = n); tagged hooks like @Before("@smoke") run only for tagged scenarios.
 *
 * State between steps: steps in one scenario share this class instance, so the
 * loginPage/inventoryPage fields carry state Given -> When -> Then. In bigger suites
 * you'd use dependency injection (picocontainer) to share state across step classes.
 */
public class LoginSteps {

    private LoginPage loginPage;
    private InventoryPage inventoryPage;

    @Before
    public void startBrowser() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        DriverManager.createDriver(headless);
    }

    @After
    public void stopBrowser() {
        DriverManager.quitDriver();
    }

    @Given("I am on the Sauce Demo login page")
    public void iAmOnTheLoginPage() {
        loginPage = new LoginPage(DriverManager.getDriver()).open();
    }

    @When("I log in as {string} with password {string}")
    public void iLogInAs(String username, String password) {
        inventoryPage = loginPage.login(username, password);
    }

    @Then("I should see the inventory page with {int} products")
    public void iShouldSeeTheInventoryPage(int expectedProducts) {
        assertTrue(inventoryPage.isLoaded(), "Should land on the inventory page");
        assertEquals(expectedProducts, inventoryPage.getProductCount());
    }

    @Then("I should see a login error containing {string}")
    public void iShouldSeeALoginError(String expectedError) {
        assertTrue(loginPage.hasError(), "Error banner should be displayed");
        assertTrue(loginPage.getErrorMessage().contains(expectedError),
                "Expected error containing: " + expectedError + " but got: " + loginPage.getErrorMessage());
    }
}
