package com.sampleselenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * Page Object for the Sauce Demo inventory (products) page.
 * URL: https://www.saucedemo.com/inventory.html
 */
public class InventoryPage extends BasePage {

    private static final String EXPECTED_URL_FRAGMENT = "/inventory.html";

    private static final By PAGE_TITLE    = By.cssSelector(".title");
    private static final By PRODUCT_ITEMS = By.cssSelector(".inventory_item");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Returns true if the browser is currently on the inventory page.
     */
    public boolean isLoaded() {
        return getCurrentUrl().contains(EXPECTED_URL_FRAGMENT);
    }

    /**
     * Returns the page header title text (e.g. "Products").
     */
    public String getPageTitle() {
        return getText(PAGE_TITLE);
    }

    /**
     * Returns the number of product items currently displayed on the page.
     */
    public int getProductCount() {
        return driver.findElements(PRODUCT_ITEMS).size();
    }
}
