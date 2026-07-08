package com.sampleselenium.drills.support;

import com.sampleselenium.driver.DriverManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * TestNG counterpart of the JUnit 5 {@code BaseTest}.
 *
 * INTERVIEW TALKING POINT — "What is @BeforeMethod vs @BeforeEach?"
 * Same idea, different framework: TestNG's @BeforeMethod runs before every @Test method,
 * exactly like JUnit 5's @BeforeEach. TestNG adds coarser-grained hooks too:
 * @BeforeSuite (once per suite) -> @BeforeTest (once per &lt;test&gt; tag in testng.xml)
 * -> @BeforeClass (once per class) -> @BeforeMethod (before every test method).
 *
 * alwaysRun = true means these configuration methods still run for tests in
 * groups-filtered runs — without it, a grouped run can silently skip your driver setup.
 */
public abstract class TestNgBase {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        DriverManager.createDriver(headless);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
