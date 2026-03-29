package com.sampleselenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the Sauce Demo login page.
 * URL: https://www.saucedemo.com
 */
public class LoginPage extends BasePage {

    private static final String URL = "https://www.saucedemo.com";

    private static final By USERNAME_INPUT   = By.id("user-name");
    private static final By PASSWORD_INPUT   = By.id("password");
    private static final By LOGIN_BUTTON     = By.id("login-button");
    private static final By ERROR_MESSAGE    = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates to the login page.
     */
    public LoginPage open() {
        driver.get(URL);
        return this;
    }

    /**
     * Fills in credentials and clicks Login.
     * Returns an InventoryPage — the caller should verify it loaded if needed.
     */
    public InventoryPage login(String username, String password) {
        waitAndType(USERNAME_INPUT, username);
        waitAndType(PASSWORD_INPUT, password);
        waitAndClick(LOGIN_BUTTON);
        return new InventoryPage(driver);
    }

    /**
     * Returns the text of the error message banner, or an empty string if not visible.
     */
    public String getErrorMessage() {
        if (isDisplayed(ERROR_MESSAGE)) {
            return getText(ERROR_MESSAGE);
        }
        return "";
    }

    /**
     * Returns true if the error banner is currently displayed.
     */
    public boolean hasError() {
        return isDisplayed(ERROR_MESSAGE);
    }
}
