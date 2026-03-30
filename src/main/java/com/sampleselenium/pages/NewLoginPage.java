package com.sampleselenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class NewLoginPage extends HomePage {

    private static final String URL="https://the-internet.herokuapp.com/login";

    private static final By USERNAME_INPUT = By.id("username");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.className("radius");
//    private static final String LOGIN_BUTTON = String.valueOf(new By.ByCssSelector("button.radius"));


    public NewLoginPage(WebDriver driver) {
        super(driver);
    }
    public NewLoginPage open() {
        driver.get(URL);
        return this;
    }
    public SecurePage login(String username, String password) {
        waitAndType(USERNAME_INPUT, username);
        waitAndType(PASSWORD_INPUT, password);
        waitAndClick(LOGIN_BUTTON);
        return new SecurePage(driver);
    }


    }
