---
name: automation-framework-reviewer
description: Review SampleSelenium Java/Selenium framework changes for Page Object Model boundaries, wait strategy, WebDriver lifecycle, test isolation, parallel execution, and targeted verification. Use for changes under src/main/java or src/test/java.
---

# Automation Framework Reviewer

Read `AGENTS.md`, then inspect only the changed Java and test files.

Flag these as must-fix unless the change is an intentionally isolated drill:

- Direct `new WebDriver()` in a normal test; use `DriverManager.getDriver()` and `BaseTest`.
- `Thread.sleep`, broad implicit waits, or wait logic hidden in test bodies.
- Locators in tests or page actions that do not return the next page/current page appropriately.
- Shared mutable test data, static WebDriver state, or teardown paths that can leak browsers.
- New JUnit tests that do not extend `BaseTest`; new production pages that do not extend `BasePage`.

Check that locators are `private static final By`, actions live in page objects, assertions prove behavior, and tests can run independently. For affected test behavior, recommend the smallest command: `mvn test -Dtest=<class>` or a relevant drill profile; use `-Dheadless=true` for CI parity.

Return **Risk**, **Findings**, **Coverage gap**, and **Verification**. Do not rewrite legacy PageFactory code unless the user requested refactoring.
