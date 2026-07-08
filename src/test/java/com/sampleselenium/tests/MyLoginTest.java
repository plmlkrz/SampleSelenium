package com.sampleselenium.tests;

import com.sampleselenium.pages.MainPage;
import com.sampleselenium.pages.SetupPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MyLoginTest {
    private WebDriver driver;
    private static final String URL = "https://the-internet.herokuapp.com/login";

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.get(URL);
    }

    @Test
    public void validLoginTest() {
        SetupPage setupPage = new SetupPage(driver);
        MainPage mainPage = setupPage.login("tomsmith", "SuperSecretPassword!");

        String Message = mainPage.getFlashMessage();
        Assert.assertTrue(Message.contains("You logged into a secure area!"), "Expected success message but got: " + Message);

    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
