package com.sampleselenium.drills.d04_parallel;

import com.sampleselenium.drills.support.TestNgBase;
import com.sampleselenium.driver.DriverManager;
import com.sampleselenium.pages.LoginPage;
import org.junit.jupiter.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * DRILL 04 — PRACTICE FILE
 *
 * 1. Read SourceD04ParallelDrills.java AND src/test/resources/testng-parallel.xml. Close them.
 * 2. Flip a test to enabled = true, write it from memory.
 * 3. Run:  mvn test -Pparallel -Dheadless=true
 *
 * REPRODUCE FROM MEMORY:
 *   1. Three @Test methods that each print thread id + driver identity hash and do a login.
 *   2. The suite line that makes it parallel (write it out on paper, no peeking):
 *        <suite name="..." parallel="methods" thread-count="3">
 *   3. Say out loud the four parallel= values and when you'd use each:
 *        methods / classes / tests / instances
 *   4. Say out loud WHY DriverManager uses ThreadLocal and what a plain static
 *      WebDriver would do to a parallel run.
 */

public class PracticeD04ParallelDrills extends TestNgBase {

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
        Integer previous = DRIVER_PER_THREAD.put(threadId, driverHash);
        DRIVER_PER_THREAD.forEach((otherThread, otherHash) -> {
            if (otherThread != threadId) {
                Assert.assertNotEquals(otherHash.intValue(), driverHash, "Two threads are sharing one WebDriver — ThreadLocal isolation failed");
            }
        });
        boolean loaded = new LoginPage(DriverManager.getDriver()).open().login("standard_user", "secret_sauce").isLoaded();
        Assert.assertTrue(loaded, "Each parallel thread should complete its own login");

    }
}
