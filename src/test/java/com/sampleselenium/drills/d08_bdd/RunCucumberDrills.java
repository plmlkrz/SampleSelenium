package com.sampleselenium.drills.d08_bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * DRILL 08 — THE RUNNER: wires feature files to step definitions.
 *
 * Run all scenarios:   mvn test -Dtest=RunCucumberDrills -Dheadless=true
 * Run only @smoke:     mvn test -Dtest=RunCucumberDrills -Dheadless=true -Dcucumber.filter.tags=@smoke
 *
 * The three things every runner must declare (say these in the interview):
 *   1. WHERE the feature files are  -> @SelectClasspathResource("features")
 *   2. WHERE the step defs (glue) are -> the "glue" configuration parameter
 *   3. WHICH plugins/reports to use  -> "pretty" console output here; teams add
 *      "html:target/cucumber.html" or Allure in real projects.
 *
 * (Legacy equivalent you should still recognize: @RunWith(Cucumber.class) +
 * @CucumberOptions(features=..., glue=..., plugin=...) on JUnit 4. This project uses
 * the modern JUnit 5 platform engine instead.)
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.sampleselenium.drills.d08_bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
public class RunCucumberDrills {
}
