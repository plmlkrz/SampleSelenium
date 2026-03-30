package com.sampleselenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;



public class SecurePage extends HomePage {
    private static final String EXPECTED_URL_FRAGMENT = "/secure";
    private static final By GET_TITLE = By.cssSelector(".title");

    public SecurePage (WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return getCurrentUrl().contains(EXPECTED_URL_FRAGMENT);
    }

    public static String getExpectedUrlFragment() {
        return EXPECTED_URL_FRAGMENT;
    }
    public String getPageTitle() {
        return getText(GET_TITLE);
    }
    public String getPageMessage()  {
        return getText(By.className("subheader"));
    }
}
