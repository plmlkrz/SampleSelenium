# SampleSelenium

Practice project for Selenium WebDriver tests — built for interview preparation.

## Tech Stack

- Java 17
- Selenium 4.33.0
- WebDriverManager 5.9.3 (auto-manages ChromeDriver)
- JUnit 5.12.2
- Maven

## Project Structure

```
src/
├── main/java/com/sampleselenium/
│   ├── driver/DriverManager.java   — ThreadLocal WebDriver factory
│   └── pages/
│       ├── BasePage.java           — Shared wait helpers (waitAndClick, waitAndType, etc.)
│       ├── LoginPage.java          — saucedemo.com login page
│       └── InventoryPage.java      — saucedemo.com products page
└── test/java/com/sampleselenium/
    ├── base/BaseTest.java          — @BeforeEach setup / @AfterEach teardown
    └── tests/LoginTests.java       — Login test scenarios
```

## Running Tests

```bash
# Run all tests (opens a Chrome window)
mvn test

# Run all tests headless
mvn test -Dheadless=true

# Run a single test class
mvn test -Dtest=LoginTests

# Run a single test method
mvn test -Dtest=LoginTests#loginWithValidCredentials
```

## Test Site

Tests run against [Sauce Demo](https://www.saucedemo.com) — a purpose-built Selenium practice application.

| Username | Password | Behavior |
|---|---|---|
| `standard_user` | `secret_sauce` | Normal login |
| `locked_out_user` | `secret_sauce` | Locked out error |
| `problem_user` | `secret_sauce` | Broken images |
| `performance_glitch_user` | `secret_sauce` | Slow responses |
