package com.sampleselenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class HomePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    protected HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
    }

    protected void waitAndClick(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void waitAndType(By locator, String text) {
        WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected String getText (By locator) {
        return waitForVisible(locator).getText().trim();
    }

}
