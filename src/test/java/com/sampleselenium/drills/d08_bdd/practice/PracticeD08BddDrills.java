package com.sampleselenium.drills.d08_bdd.practice;

/**
 * DRILL 08 — PRACTICE FILE  (no browser, no network)
 *
 * 1. Read d08_orders.feature and SourceD08BddDrills.java. Close them, no peeking.
 * 2. Uncomment one scenario in d08_orders_practice.feature.
 * 3. Run:  mvn test -Dtest=RunPracticeD08BddDrills
 *    Cucumber prints a snippet for every undefined step. Do NOT paste the snippet.
 *    Type the method from memory, then compare.
 * 4. Green? Next scenario.
 *
 * WHY THIS CLASS IS ITS OWN PACKAGE: step definitions must be unique across a glue
 * package. The source steps bind the same Gherkin lines, so if these two classes
 * shared a package Cucumber would throw DuplicateStepDefinitionException the moment
 * you implemented your first step. Separate glue package, separate runner, no clash.
 *
 * TO REPRODUCE FROM MEMORY (write the signatures on paper first):
 *   1. A field to hold the OrderBook, plus @Before / @After. Remember hooks are
 *      package-scoped, not class-scoped, and remember @After still runs after a
 *      failed step whereas the remaining steps do not.
 *   2. @Given("an empty order")
 *   3. @When("the customer adds these items:") taking a DataTable.
 *      asMaps(String.class, String.class) for a headed table. Know what asLists()
 *      would give you instead, and when you would register a TableEntryTransformer.
 *   4. @When("the customer adds {string} at {bigdecimal} each, quantity {int}")
 *      Name the built-in Cucumber expression types: {int} {float} {word} {string}
 *      {bigdecimal} {byte} {short} {long} {biginteger} {} (anything).
 *   5. @When("the customer applies discount code {string}")
 *   6. Four Then steps: subtotal, discount, shipping, total. All {bigdecimal}.
 *   7. @Then("the order should contain {int} items")
 *   8. A money assertion helper. compareTo, NOT equals — BigDecimal.equals compares
 *      scale too, so 0.00 does not equal 0. Getting this wrong is the classic
 *      intermittent-failure story, and it is worth telling if a panel asks how you
 *      validate financial calculations.
 *
 * QUESTIONS TO ANSWER OUT LOUD WHILE YOU TYPE:
 *   - What does the runner have to declare, and what breaks if the glue is wrong?
 *   - Scenario Outline versus DataTable: when do you reach for each?
 *   - How do you run only the @smoke scenarios from the command line?
 *   - Where does Background help, and where does it hide state and hurt readability?
 *   - Two glue classes need the same object. How? (DI container, picocontainer or
 *     spring; this project has neither installed, so say it, do not fake it.)
 */
public class PracticeD08BddDrills {

    // TODO: private OrderBook order;

    // TODO: @Before  — create a fresh OrderBook.
    // TODO: @After   — release it. Try adding a Scenario parameter and printing
    //                  scenario.getName() and scenario.isFailed().

    // TODO: @Given("an empty order")

    // TODO: @When("the customer adds these items:")  -- DataTable parameter

    // TODO: @When("the customer adds {string} at {bigdecimal} each, quantity {int}")

    // TODO: @When("the customer applies discount code {string}")

    // TODO: @Then("the subtotal should be {bigdecimal}")

    // TODO: @Then("the discount should be {bigdecimal}")

    // TODO: @Then("the shipping should be {bigdecimal}")

    // TODO: @Then("the total should be {bigdecimal}")

    // TODO: @Then("the order should contain {int} items")

    // ---------- helpers ----------

    // TODO: private void assertMoney(String label, BigDecimal expected, BigDecimal actual)
    //       using assertEquals(0, expected.compareTo(actual), message)
}
