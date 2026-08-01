# DRILL 08 — CUCUMBER BDD, BROWSER-FREE  [SOURCE feature file]
#
# Run:  mvn test -Dtest=RunSourceD08BddDrills
# No browser, no network, about a second — same as modules 6, 7, 9 and 11.
#
# WHY THIS LIVES OUTSIDE src/test/resources/features:
# RunCucumberDrills selects the whole "features" folder as its classpath resource
# and glues it to the d08_bdd package. Dropping a new feature in there would make
# that runner try to execute these scenarios with the login step definitions and
# fail on undefined steps. Separate resource folder, separate glue package, no
# collision. That is worth saying in an interview: the runner's three declarations
# (features, glue, plugins) are what scope a Cucumber suite, and getting them wrong
# is the most common way a BDD project starts fighting itself.
#
# WHAT THIS FEATURE DRILLS THAT THE LOGIN FEATURE DOES NOT:
#   Data tables      -> a table under a step, read as List<Map<String,String>>
#   Background       -> steps that run before EVERY scenario in the file
#   Doc strings      -> not used here; know that """ blocks pass multi-line text
#   Boundary values  -> the free-shipping threshold, tested at and either side of it
#   Business rules   -> a real ambiguity settled in writing (see the last scenario)
#
# GHERKIN DISCIPLINE, the thing that separates good feature files from bad:
# write WHAT the business rule is, never HOW the UI does it. "When I place the
# order" is a business step. "When I click the button with id checkout" is a
# click-by-click script that belongs in a page object, not in a file a product
# owner is supposed to read.

Feature: Order totals, discounts, and shipping
  As the business owner of the storefront
  I want order totals calculated to the cent
  So that customers are charged correctly and shipping promises are honoured

  Background:
    Given an empty order

  @smoke
  Scenario: Subtotal is the sum of every line
    When the customer adds these items:
      | item             | quantity | unit price |
      | Sauce Labs Bike  | 2        | 12.50      |
      | Sauce Labs Shirt | 1        | 15.99      |
    Then the subtotal should be 40.99
    And the order should contain 3 items

  Scenario: Small orders pay flat shipping
    When the customer adds these items:
      | item            | quantity | unit price |
      | Sauce Labs Sock | 1        | 9.99       |
    Then the shipping should be 7.95
    And the total should be 17.94

  # Scenario Outline is Cucumber's DataProvider. One body, many rows, and the
  # boundary is deliberately included: at exactly 50.00 shipping is free.
  Scenario Outline: Free shipping applies at and above the threshold
    When the customer adds "<item>" at <price> each, quantity 1
    Then the shipping should be <shipping>

    Examples:
      | item      | price | shipping |
      | Just under | 49.99 | 7.95     |
      | Exactly on | 50.00 | 0.00     |
      | Well over  | 75.00 | 0.00     |

  Scenario: SAVE10 takes ten percent off the subtotal
    When the customer adds "Sauce Labs Backpack" at 100.00 each, quantity 1
    And the customer applies discount code "SAVE10"
    Then the discount should be 10.00
    And the total should be 90.00

  Scenario: An unrecognised discount code is worth nothing
    When the customer adds "Sauce Labs Backpack" at 100.00 each, quantity 1
    And the customer applies discount code "NOTAREALCODE"
    Then the discount should be 0.00
    And the total should be 100.00

  # THE SCENARIO THAT EARNS ITS KEEP. A $52 order qualifies for free shipping
  # until SAVE10 drops it to $46.80, and then shipping comes back. Whether that
  # is correct is a business question, not a testing one. Writing it down as a
  # scenario is how the question gets asked before the code ships, which is the
  # entire argument for BDD and is a far better answer than "living documentation".
  Scenario: A discount can push an order back under the free-shipping threshold
    When the customer adds "Sauce Labs Fleece" at 52.00 each, quantity 1
    And the customer applies discount code "SAVE10"
    Then the discount should be 5.20
    And the shipping should be 7.95
    And the total should be 54.75
