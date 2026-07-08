package com.sampleselenium.drills.d04_parallel;

import com.sampleselenium.drills.support.TestNgBase;
import org.testng.annotations.Test;

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

    @Test(enabled = false /* TODO: flip to true and write from memory */)
    public void parallelLoginCheckOne() {
        // TODO: print thread id + driver hash, then log in and assert inventory loaded
    }

    @Test(enabled = false /* TODO */)
    public void parallelLoginCheckTwo() {
        // TODO
    }

    @Test(enabled = false /* TODO */)
    public void parallelLoginCheckThree() {
        // TODO
    }
}
