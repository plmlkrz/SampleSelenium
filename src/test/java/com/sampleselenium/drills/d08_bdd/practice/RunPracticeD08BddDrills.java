package com.sampleselenium.drills.d08_bdd.practice;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * DRILL 08 — PRACTICE RUNNER (browser-free).
 *
 * Run:  mvn test -Dtest=RunPracticeD08BddDrills
 *
 * This runner is deliberately NOT in the surefire include list in pom.xml, so a plain
 * "mvn test" ignores it and a half-finished drill cannot turn the whole build red.
 * Same reason the practice feature file ships fully commented out.
 *
 * If you run it before uncommenting anything, the suite finds no scenarios and says so.
 * That is the expected message, not a broken runner.
 *
 * Try this once the first scenario passes:
 *   mvn test -Dtest=RunPracticeD08BddDrills -Dcucumber.filter.tags="@smoke"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("bdd_drills/practice")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.sampleselenium.drills.d08_bdd.practice")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
public class RunPracticeD08BddDrills {
}
