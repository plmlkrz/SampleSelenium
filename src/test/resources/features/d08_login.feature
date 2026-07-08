# DRILL 08 — CUCUMBER BDD  [SOURCE feature file]
#
# Run:  mvn test -Dtest=RunCucumberDrills -Dheadless=true
#
# Interview vocabulary, in the order they ask it:
#   Feature file (this) -> written in GHERKIN (Given/When/Then business language)
#   Step definitions    -> LoginSteps.java glues each Gherkin line to Selenium code
#   Runner              -> RunCucumberDrills.java tells the JUnit platform where both live
#   Hooks               -> @Before/@After in LoginSteps manage the browser per scenario
#   Tags (@smoke below) -> filter which scenarios run, like TestNG groups
#   Scenario Outline    -> data-driven scenarios; the Examples table is Cucumber's DataProvider
#
# "Why Cucumber?" — the honest answer: living documentation the business can read,
# a shared vocabulary between BA/dev/QA, and reuse of step definitions across features.
# The honest trade-off: an extra abstraction layer to maintain — overkill when no
# non-technical stakeholder ever reads the features.

Feature: Sauce Demo login
  As a Sauce Demo shopper
  I want to log in with my credentials
  So that I can see the product inventory

  @smoke
  Scenario: Standard user logs in successfully
    Given I am on the Sauce Demo login page
    When I log in as "standard_user" with password "secret_sauce"
    Then I should see the inventory page with 6 products

  Scenario: Locked out user is rejected
    Given I am on the Sauce Demo login page
    When I log in as "locked_out_user" with password "secret_sauce"
    Then I should see a login error containing "Sorry, this user has been locked out"

  # Scenario Outline = one scenario body, many Examples rows (Cucumber's DataProvider)
  Scenario Outline: Invalid credential combinations are rejected
    Given I am on the Sauce Demo login page
    When I log in as "<username>" with password "<password>"
    Then I should see a login error containing "<error>"

    Examples:
      | username      | password       | error                              |
      | standard_user | wrong_password | Username and password do not match |
      | wrong_user    | secret_sauce   | Username and password do not match |
