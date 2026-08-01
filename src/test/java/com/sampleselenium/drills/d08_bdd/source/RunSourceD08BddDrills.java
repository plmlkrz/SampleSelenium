package com.sampleselenium.drills.d08_bdd.source;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * DRILL 08 — SOURCE RUNNER (browser-free).
 *
 * Run:  mvn test -Dtest=RunSourceD08BddDrills
 *
 * THE THREE THINGS EVERY RUNNER MUST DECLARE, and they are all here:
 *   features — @SelectClasspathResource("bdd_drills/source"), a classpath DIRECTORY.
 *   glue     — the package Cucumber scans for steps and hooks.
 *   plugins  — reporters. "pretty" prints to the console; html:target/... and
 *              json:target/... are what a CI job archives.
 *
 * Note the glue is this package, NOT com.sampleselenium.drills.d08_bdd. Pointing it at
 * the parent would drag in LoginSteps and its untagged @Before, and every scenario in
 * this suite would open a browser it does not need.
 *
 * The legacy JUnit 4 spelling, still what a lot of interviewers picture:
 *   @RunWith(Cucumber.class)
 *   @CucumberOptions(features = "src/test/resources/bdd_drills/source",
 *                    glue = "com.sampleselenium.drills.d08_bdd.source",
 *                    plugin = {"pretty"})
 *
 * To run only tagged scenarios:
 *   mvn test -Dtest=RunSourceD08BddDrills -Dcucumber.filter.tags="@smoke"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("bdd_drills/source")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.sampleselenium.drills.d08_bdd.source")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
public class RunSourceD08BddDrills {
}
