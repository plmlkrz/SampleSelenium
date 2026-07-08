package com.sampleselenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MainPage {
    private final WebDriver driver;
    private final By flashMessage = By.id("flash");

    public MainPage(WebDriver driver) {
        this.driver = driver;
    }

    public String getFlashMessage() {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(flashMessage))
                .getText();
    }

}
