package com.sampleselenium.drills.support;

import com.sampleselenium.driver.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DELOITTE ROUND 2, QUESTION 4 — "How do you take screenshots for FAILED test cases only in TestNG?"
 *
 * The answer, spoken: "I implement ITestListener and override onTestFailure. Inside it I cast
 * the driver to TakesScreenshot, capture bytes, and write them to a timestamped file named
 * after the failed test. The listener is registered either with @Listeners on the class or
 * in testng.xml, so no test code has to think about screenshots at all."
 *
 * Key details interviewers probe:
 *  - onTestFailure (NOT onTestSuccess/onFinish) is what makes it "failed tests only".
 *  - TakesScreenshot is an interface the driver implements; getScreenshotAs(OutputType.BYTES/FILE).
 *  - Getting the right driver in parallel runs: DriverManager's ThreadLocal hands back the
 *    driver belonging to the thread whose test just failed — this is why the pattern survives
 *    parallel="methods".
 */
public class ScreenshotOnFailureListener implements ITestListener {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = DriverManager.getDriver();
        if (!(driver instanceof TakesScreenshot)) {
            return;
        }
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            String fileName = result.getMethod().getMethodName() + "_" + STAMP.format(LocalDateTime.now()) + ".png";
            Files.write(dir.resolve(fileName), png);
            System.out.println("[ScreenshotOnFailureListener] saved " + dir.resolve(fileName));
        } catch (IOException e) {
            System.err.println("[ScreenshotOnFailureListener] could not save screenshot: " + e.getMessage());
        }
    }
}
