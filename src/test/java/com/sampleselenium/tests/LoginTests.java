package com.sampleselenium.tests;

import com.sampleselenium.base.BaseTest;
import com.sampleselenium.driver.DriverManager;
import com.sampleselenium.pages.InventoryPage;
import com.sampleselenium.pages.LoginPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Sauce Demo login page.
 *
 * Valid credentials for testing:
 *   standard_user     / secret_sauce  — normal user
 *   locked_out_user   / secret_sauce  — blocked by admin
 *   problem_user      / secret_sauce  — images are broken
 *   performance_glitch_user / secret_sauce — slow app responses
 */
class LoginTests extends BaseTest {

    @Test
    void loginWithValidCredentials() {
        LoginPage loginPage = new LoginPage(DriverManager.getDriver()).open();

        InventoryPage inventoryPage = loginPage.login("standard_user", "secret_sauce");

        assertTrue(inventoryPage.isLoaded(), "Should land on inventory page after valid login");
        assertEquals("Products", inventoryPage.getPageTitle());
        assertEquals(6, inventoryPage.getProductCount(), "Should display 6 products");
    }

    @Test
    void loginWithInvalidCredentials() {
        LoginPage loginPage = new LoginPage(DriverManager.getDriver()).open();

        loginPage.login("standard_user", "wrong_password");

        assertTrue(loginPage.hasError(), "Error message should be displayed");
        assertTrue(
            loginPage.getErrorMessage().contains("Username and password do not match"),
            "Error should mention invalid credentials"
        );
    }

    @Test
    void loginWithLockedOutUser() {
        LoginPage loginPage = new LoginPage(DriverManager.getDriver()).open();

        loginPage.login("locked_out_user", "secret_sauce");

        assertTrue(loginPage.hasError(), "Error message should be displayed for locked user");
        assertTrue(
            loginPage.getErrorMessage().contains("Sorry, this user has been locked out"),
            "Error should mention locked out"
        );
    }
}
