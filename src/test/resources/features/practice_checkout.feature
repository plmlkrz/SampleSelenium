# DRILL 08 — PRACTICE FEATURE FILE
#
# Everything below is commented out so it doesn't run until you're ready.
# The drill:
#   1. Uncomment ONE scenario.
#   2. Run:  mvn test -Dtest=RunCucumberDrills -Dheadless=true
#      Cucumber will fail with "undefined step" and PRINT THE SNIPPET for the missing
#      step definition — copy its shape, implement it yourself in a new CheckoutSteps.java
#      (same package as LoginSteps so the glue finds it).
#   3. Make it pass. Then uncomment the next scenario.
#
# This mirrors real Cucumber workflow: feature first, snippets, then glue code —
# which is exactly how you should DESCRIBE the workflow in the interview.
#
# Feature: Sauce Demo checkout
#
#   Scenario: Add an item to the cart
#     Given I am logged in as a standard user
#     When I add "Sauce Labs Backpack" to the cart
#     Then the cart badge should show 1
#
#   Scenario: Remove an item from the cart
#     Given I am logged in as a standard user
#     And I add "Sauce Labs Backpack" to the cart
#     When I remove "Sauce Labs Backpack" from the cart
#     Then the cart badge should be empty
