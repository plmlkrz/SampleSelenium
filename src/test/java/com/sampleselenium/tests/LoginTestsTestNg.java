package com.sampleselenium.tests;

import com.sampleselenium.driver.DriverManager;
import com.sampleselenium.pages.NewLoginPage;
import com.sampleselenium.pages.SecurePage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginTestsTestNg {

    @BeforeEach
    public void setUp() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        DriverManager.createDriver(headless);
    }

    @Test
    void loginTestWithValidCredentials() {
        System.out.println("Running loginWithValidCredentials test");
        NewLoginPage loginPage = new NewLoginPage(DriverManager.getDriver()).open();
        SecurePage securePage = loginPage.login("tomsmith", "SuperSecretPassword!");
        assertTrue (securePage.isLoaded(), "Secure page should be loaded after valid login");
        assertEquals("Welcome to the Secure Area. When you are done click logout below.", securePage.getPageMessage(), "Secure page should display welcome message");

    }

    @AfterEach
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
