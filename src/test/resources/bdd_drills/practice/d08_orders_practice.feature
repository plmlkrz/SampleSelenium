# DRILL 08 — PRACTICE FEATURE FILE  (browser-free)
#
# HOW TO USE THIS FILE
# 1. Read d08_orders.feature and SourceD08BddDrills.java. Close them. No peeking.
# 2. Uncomment ONE scenario below by deleting the leading "# " from its lines.
# 3. Run:  mvn test -Dtest=RunPracticeD08BddDrills
#    Cucumber prints an undefined-step snippet for every line it cannot bind.
# 4. Implement those steps in PracticeD08BddDrills.java from memory.
# 5. Green? Uncomment the next scenario. Repeat until the file is live.
#
# Everything is commented out on purpose so the file is inert until you start.
# Note that RunPracticeD08BddDrills is NOT in the surefire include list, so a plain
# "mvn test" will not run it and cannot be broken by a half-finished drill. If you
# run it with nothing uncommented, the suite finds no scenarios and complains — that
# is expected, not a bug. Uncomment something first.
#
# WRITE THE GHERKIN YOURSELF WHERE IT SAYS SO. Reproducing a feature file from a
# prompt is the actual interview task: you get a rule in a sentence and have to turn
# it into Given/When/Then that a product owner would sign off on.
#
# ---------------------------------------------------------------------------
#
# Feature: Order totals, discounts, and shipping
#
#   Background:
#     Given an empty order
#
#   # SCENARIO 1 — data table. Two lines, then assert the subtotal and the item count.
#   @smoke
#   Scenario: Subtotal is the sum of every line
#     When the customer adds these items:
#       | item             | quantity | unit price |
#       | Sauce Labs Bike  | 2        | 12.50      |
#       | Sauce Labs Shirt | 1        | 15.99      |
#     Then the subtotal should be 40.99
#     And the order should contain 3 items
#
#   # SCENARIO 2 — Scenario Outline. Fill in the Examples rows yourself. The rule:
#   # shipping is 7.95 below 50.00 and free at 50.00 or above. Include the boundary.
#   Scenario Outline: Free shipping applies at and above the threshold
#     When the customer adds "<item>" at <price> each, quantity 1
#     Then the shipping should be <shipping>
#
#     Examples:
#       | item | price | shipping |
#       |      |       |          |
#
#   # SCENARIO 3 — write this one from the rule alone, no template.
#   # RULE: the code SAVE10 takes ten percent off the subtotal. Any other code is
#   # worth nothing. Cover both halves of that sentence, one scenario each.
#
#   # SCENARIO 4 — the one that matters. Write the scenario that pins down what
#   # happens to a 52.00 order when SAVE10 drops it under the free-shipping
#   # threshold. Before you write it, decide out loud whether the behaviour is
#   # correct or a defect, and who you would ask. That conversation is the point
#   # of BDD, and it is the answer to "what value does BDD actually add?"
