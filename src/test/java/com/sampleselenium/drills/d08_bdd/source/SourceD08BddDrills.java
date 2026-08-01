package com.sampleselenium.drills.d08_bdd.source;

import com.sampleselenium.drills.d08_bdd.OrderBook;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DRILL 08 — CUCUMBER BDD, BROWSER-FREE  [SOURCE — read, close, reproduce in Practice file]
 *
 * Run:  mvn test -Dtest=RunSourceD08BddDrills
 *
 * Covers (real interview questions this answers):
 *   - "Walk me through how a Cucumber project is wired." (feature, glue, runner)
 *   - "How do you pass a table of data into a step?" (DataTable)
 *   - "What is the difference between a Scenario Outline and a data table?"
 *   - "Where do you put setup code in Cucumber?" (hooks, and why not @BeforeEach)
 *   - "How do you share state between steps?" (instance fields, or DI for many classes)
 *   - "How do you test money?" (BigDecimal, compareTo, not double)
 *
 * ONE-LINE DEFINITIONS TO SAY WHILE THESE RUN:
 *   Feature file    — the business rules in Gherkin, readable by someone who cannot code.
 *   Step definition — the Java method a Gherkin line binds to, matched by a Cucumber expression.
 *   Glue            — the package Cucumber scans for step definitions and hooks.
 *   Runner          — the JUnit entry point that names the features, the glue, and the plugins.
 *   Hook            — @Before / @After, which run around every scenario in the glue package.
 *   Tag             — @smoke on a scenario, filtered with cucumber.filter.tags.
 *
 * THE THREE THINGS THAT BITE PEOPLE, and why this drill sits in its own sub-package:
 *   1. Hooks are GLOBAL TO THE GLUE PACKAGE, not to the class. LoginSteps in the parent
 *      package has an untagged @Before that opens a browser. If these steps lived beside
 *      it, every scenario here would launch Chrome for nothing. Isolate, or tag the hook.
 *   2. Step definitions must be UNIQUE across the glue. Two methods matching the same
 *      Gherkin line is a DuplicateStepDefinitionException, not a warning. That is why the
 *      practice half has its own glue package: once you implement it, the two would collide.
 *   3. A runner's @SelectClasspathResource takes a DIRECTORY, and it takes all of it.
 *      RunCucumberDrills points at "features", so these live in "bdd_drills/source".
 *
 * STATE LIVES IN AN INSTANCE FIELD. Cucumber builds a fresh instance of every glue class
 * for each scenario, so the field cannot leak between scenarios. With several glue classes
 * that need to share one object you add a DI module (picocontainer is the usual one) and
 * inject a shared world object through the constructor. This project has no picocontainer,
 * so that is a talk-track answer here, not something you can point at in the code.
 */
public class SourceD08BddDrills {

    private OrderBook order;

    /**
     * Untagged, so it runs for every scenario in this glue package. Add a tag expression
     * to narrow it: @Before("@smoke") runs only on tagged scenarios, and @Before(order = 1)
     * controls sequence when several hooks exist. That is the answer to "how do you run
     * setup for only some scenarios?"
     */
    @Before
    public void announce(Scenario scenario) {
        System.out.println("[d08 source] " + scenario.getName());
    }

    @After
    public void clear() {
        order = null;
    }

    @Given("an empty order")
    public void anEmptyOrder() {
        order = new OrderBook();
    }

    /**
     * DataTable with a header row reads cleanly as a list of maps. The alternative,
     * asMaps() versus asLists() versus asList(Type.class), is a good thing to be able
     * to name: a headerless table is a List, a headed table is a List of Map, and with
     * a registered TableEntryTransformer it becomes a List of your own type.
     */
    @When("the customer adds these items:")
    public void theCustomerAddsTheseItems(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            order.addLine(
                    row.get("item"),
                    Integer.parseInt(row.get("quantity")),
                    new BigDecimal(row.get("unit price")));
        }
    }

    /** {string} takes the quoted text, {bigdecimal} and {int} are built in too. */
    @When("the customer adds {string} at {bigdecimal} each, quantity {int}")
    public void theCustomerAddsOneLine(String item, BigDecimal unitPrice, int quantity) {
        order.addLine(item, quantity, unitPrice);
    }

    @When("the customer applies discount code {string}")
    public void theCustomerAppliesDiscountCode(String code) {
        order.applyDiscountCode(code);
    }

    @Then("the subtotal should be {bigdecimal}")
    public void theSubtotalShouldBe(BigDecimal expected) {
        assertMoney("subtotal", expected, order.subtotal());
    }

    @Then("the discount should be {bigdecimal}")
    public void theDiscountShouldBe(BigDecimal expected) {
        assertMoney("discount", expected, order.discount());
    }

    @Then("the shipping should be {bigdecimal}")
    public void theShippingShouldBe(BigDecimal expected) {
        assertMoney("shipping", expected, order.shipping());
    }

    @Then("the total should be {bigdecimal}")
    public void theTotalShouldBe(BigDecimal expected) {
        assertMoney("total", expected, order.total());
    }

    @Then("the order should contain {int} items")
    public void theOrderShouldContainItems(int expected) {
        assertEquals(expected, order.itemCount(), "item count");
    }

    // ---------- helpers ----------

    /**
     * compareTo, not equals. BigDecimal.equals compares scale as well as value, so
     * new BigDecimal("0.00").equals(BigDecimal.ZERO) is false and an assertEquals on
     * two BigDecimals fails for a reason that has nothing to do with the money.
     */
    private void assertMoney(String label, BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                label + ": expected " + expected + " but was " + actual);
    }
}
