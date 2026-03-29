package com.sampleselenium.base;

import com.sampleselenium.driver.DriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for all test classes.
 * Handles WebDriver creation before each test and cleanup after each test.
 *
 * To run tests headless (no browser window), pass: -Dheadless=true
 */
public abstract class BaseTest {

    @BeforeEach
    public void setUp() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        DriverManager.createDriver(headless);
    }

    @AfterEach
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
