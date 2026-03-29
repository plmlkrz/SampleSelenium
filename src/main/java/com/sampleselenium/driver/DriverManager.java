package com.sampleselenium.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Manages WebDriver instances using ThreadLocal for thread-safety.
 * This pattern allows parallel test execution without drivers interfering with each other.
 */
public class DriverManager {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    private DriverManager() {
        // Utility class — do not instantiate
    }

    /**
     * Creates and stores a new ChromeDriver for the current thread.
     *
     * @param headless if true, runs Chrome without a visible browser window
     */
    public static void createDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
        options.addArguments("--disable-notifications");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver.set(new ChromeDriver(options));
        getDriver().manage().window().maximize();
    }

    /**
     * Returns the WebDriver for the current thread.
     */
    public static WebDriver getDriver() {
        return driver.get();
    }

    /**
     * Quits the current thread's WebDriver and removes it from ThreadLocal.
     * Always call this in test teardown to prevent browser leaks.
     */
    public static void quitDriver() {
        WebDriver currentDriver = driver.get();
        if (currentDriver != null) {
            currentDriver.quit();
            driver.remove();
        }
    }
}
