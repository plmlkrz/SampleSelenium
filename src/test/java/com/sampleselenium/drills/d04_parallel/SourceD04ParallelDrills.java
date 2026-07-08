package com.sampleselenium.drills.d04_parallel;

import com.sampleselenium.driver.DriverManager;
import com.sampleselenium.drills.support.TestNgBase;
import com.sampleselenium.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DRILL 04 — PARALLEL EXECUTION  [SOURCE]
 *
 * Run in PARALLEL (3 threads):   mvn test -Pparallel -Dheadless=true
 * Run the same tests serially:   mvn test -Ptestng-drills -Dheadless=true
 * Watch the console: in the parallel run you'll see 3 different thread ids and
 * 3 different driver identity hashes, all running at the same time.
 *
 * ======================= THE COMPLETE INTERVIEW ANSWER =======================
 * "How do you set up parallel test execution?"  (say it in this order)
 *
 * 1. CONFIGURED IN testng.xml, AT THE SUITE LEVEL — not in the test code:
 *        <suite name="Parallel Drills" parallel="methods" thread-count="3">
 *    parallel= can be:
 *      - "methods"   each @Test method on its own thread (finest grain — used here)
 *      - "classes"   each test class on its own thread
 *      - "tests"     each <test> tag in the suite file on its own thread
 *      - "instances" each test-class INSTANCE on its own thread (factory-created classes)
 *    thread-count caps how many run concurrently.
 *
 * 2. THE DRIVER MUST BE THREAD-SAFE — this is the part most candidates miss.
 *    A plain `static WebDriver driver` is ONE shared browser: with 3 threads, every test
 *    would drive the same window and stomp on each other mid-click. The fix is
 *    ThreadLocal<WebDriver> (see DriverManager): each thread gets its own isolated driver,
 *    and getDriver() transparently returns "my thread's browser".
 *
 * 3. EVERYTHING ELSE MUST BE THREAD-SAFE TOO: no shared mutable page objects, no static
 *    counters, thread-safe collections for any cross-test bookkeeping (like the map below),
 *    and listeners must fetch the driver through the ThreadLocal (see
 *    ScreenshotOnFailureListener — that's why screenshot-on-failure still works in parallel).
 *
 * 4. Maven side: surefire's <suiteXmlFiles> points at the testng.xml (see the `parallel`
 *    profile in pom.xml). Alternative for JUnit-only shops: surefire's own
 *    <parallel>/<forkCount>, or junit-platform.properties for JUnit 5.
 * =============================================================================
 */
public class SourceD04ParallelDrills extends TestNgBase {

    /** threadId -> identity hash of that thread's WebDriver. ConcurrentHashMap because
     *  three threads write to it at once — a plain HashMap here would be its own bug. */
    private static final Map<Long, Integer> DRIVER_PER_THREAD = new ConcurrentHashMap<>();

    @Test
    public void parallelLoginCheckOne() {
        proveThreadIsolation("one");
    }

    @Test
    public void parallelLoginCheckTwo() {
        proveThreadIsolation("two");
    }

    @Test
    public void parallelLoginCheckThree() {
        proveThreadIsolation("three");
    }

    private void proveThreadIsolation(String label) {
        long threadId = Thread.currentThread().getId();
        int driverHash = System.identityHashCode(DriverManager.getDriver());

        System.out.printf("[parallel drill %s] thread=%d driver=%h%n", label, threadId, driverHash);

        // Each thread must have its own driver: if another thread already registered this
        // exact driver instance, ThreadLocal isolation is broken.
        Integer previous = DRIVER_PER_THREAD.put(threadId, driverHash);
        DRIVER_PER_THREAD.forEach((otherThread, otherHash) -> {
            if (otherThread != threadId) {
                Assert.assertNotEquals(otherHash.intValue(), driverHash,
                        "Two threads are sharing one WebDriver — ThreadLocal isolation failed");
            }
        });

        // And the driver actually works: quick real interaction per thread.
        boolean loaded = new LoginPage(DriverManager.getDriver())
                .open()
                .login("standard_user", "secret_sauce")
                .isLoaded();
        Assert.assertTrue(loaded, "Each parallel thread should complete its own login");
    }
}
