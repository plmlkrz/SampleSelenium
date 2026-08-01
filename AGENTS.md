# AGENTS.md

Guidance for AI coding agents working in this Selenium test automation codebase.

## Architecture Overview

This is a Maven-based Selenium 4 WebDriver test framework using the Page Object Model (POM) pattern. Core layers:

1. **DriverManager** (`src/main/java/.../driver/`) — ThreadLocal singleton wrapping `WebDriver` for thread-safe parallel execution. Always use `DriverManager.getDriver()` in tests; **never instantiate WebDriver directly** (except in isolated experiments like `MyLoginTest`).
2. **Page Objects** (`src/main/java/.../pages/`) — Domain-specific page classes encapsulating UI interactions. See two supported patterns below.
3. **Tests** (`src/test/java/.../tests/`) — JUnit 5 test classes extending `BaseTest` for automated setup/teardown.

## Two Page Object Patterns (Both Valid)

### Pattern 1: BasePage (Preferred)
Extends `BasePage`, leverages shared `WebDriverWait` (10s default) and helpers. **Use this for new pages.**

Example: `LoginPage`, `InventoryPage`

```java
public class LoginPage extends BasePage {
    private static final By USERNAME_INPUT = By.id("user-name");
    // ...
    public LoginPage login(String username, String password) {
        waitAndType(USERNAME_INPUT, username);
        return new InventoryPage(driver);  // Fluent navigation
    }
}
```

**Key characteristics:**
- Locators declared as `private static final By` constants
- Action methods return next page object (fluent chaining)
- Helpers: `waitAndClick()`, `waitAndType()`, `waitForVisible()`, `getText()`, `isDisplayed()`

### Pattern 2: PageFactory (Legacy)
Uses `@FindBy` annotations and `PageFactory.initElements()`. **Only used in `SetupPage`; avoid for new code.**

```java
public class SetupPage {  // Does NOT extend BasePage
    @FindBy(id = "username")
    private WebElement usernameField;
    // ...
}
```

## Test Execution & Key Commands

All commands run from project root:

```bash
# Run all tests (headless by default when CI, opens window for local dev)
mvn test

# Force headless mode (no browser window — use for CI/CD)
mvn test -Dheadless=true

# Single test class
mvn test -Dtest=LoginTests

# Single test method
mvn test -Dtest=LoginTests#loginWithValidCredentials

# Compile only (no tests)
mvn compile
```

The `headless` system property is read in `BaseTest.setUp()` and passed to `DriverManager.createDriver(headless)`.

## Test Target Sites & Credentials

### Primary: Sauce Demo (Interview Practice)
- **URL:** `https://www.saucedemo.com`
- **All passwords:** `secret_sauce`
- **Test users:** `standard_user`, `locked_out_user`, `problem_user`, `performance_glitch_user`
- Each exhibits different behavior (locked, broken UI, slow responses) — useful for testing error handling.

### Secondary: The Internet
- **URL:** `https://the-internet.herokuapp.com/login`
- **Credentials:** `tomsmith` / `SuperSecretPassword!`

## Test Structure & BaseTest Pattern

All JUnit 5 tests **must extend `BaseTest`**:

```java
class MyTests extends BaseTest {  // Ensures @BeforeEach setup / @AfterEach teardown
    @Test
    void myTest() {
        WebDriver driver = DriverManager.getDriver();  // Initialized in @BeforeEach
        // ... test logic
    }
}
```

**@BeforeEach** calls `DriverManager.createDriver(headless)` — driver is ThreadLocal-bound to test thread.
**@AfterEach** calls `DriverManager.quitDriver()` — cleanly closes browser and removes from ThreadLocal.

**Critical:** Never call `new WebDriver()` or skip teardown; leaks browser processes.

## Framework Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 17 | Language |
| Selenium | 4.33.0 | WebDriver automation |
| WebDriverManager | 5.9.3 | Auto-downloads ChromeDriver, GeckoDriver, etc. |
| JUnit 5 | 5.12.2 | Test framework (primary) |
| TestNG | 7.11.0 | On classpath but not preferred (tests use JUnit 5 `@Test` anyway) |
| Maven | Latest | Build tool |

## Key Patterns & Conventions

### Page Action Methods Return Page Objects
Enables fluent test chains. Example:

```java
LoginPage login = new LoginPage(driver).open();
InventoryPage inventory = login.login("user", "pass");
assertTrue(inventory.isLoaded());
```

### Locators as Class Constants
All element locators are `private static final By` in page classes. Never hardcode locators in test methods.

### Error Handling via Page Methods
Pages expose `hasError()` and `getErrorMessage()` — tests never scrape DOM for errors directly.

```java
loginPage.login("user", "wrong");
assertTrue(loginPage.hasError());  // Page object handles visibility check
```

### Fluent Assertions
Tests use JUnit 5's `org.junit.jupiter.api.Assertions`. Example:

```java
assertEquals(6, inventoryPage.getProductCount(), "Should display 6 products");
```

## Adding New Tests or Pages

1. **New Test Class:** Extend `BaseTest`, use JUnit 5 `@Test` annotations.
2. **New Page:** Extend `BasePage` (not `PageFactory`), declare `By` constants, write action methods returning page objects.
3. **Page Navigation:** Return next page object from action method, never call `driver.get()` within tests.

Example:

```java
// Page
public class MyPage extends BasePage {
    private static final By ELEMENT = By.id("foo");
    public MyPage clickFoo() {
        waitAndClick(ELEMENT);
        return this;
    }
}

// Test
class MyTest extends BaseTest {
    @Test
    void testFoo() {
        MyPage page = new MyPage(DriverManager.getDriver());
        page.clickFoo();
    }
}
```

## ThreadLocal & Parallel Execution

`DriverManager` uses `ThreadLocal<WebDriver>` to bind drivers to threads. This enables Maven Surefire to run tests in parallel without driver interference:

```xml
<!-- In pom.xml, if configuring parallel execution -->
<configuration>
    <parallel>methods</parallel>
    <threadCount>4</threadCount>
    <useModulePath>false</useModulePath>  <!-- Required for JUnit 5 -->
</configuration>
```

**Always use `DriverManager.getDriver()`** in tests — do not pass driver as instance variable.

## Common Gotchas

- **Don't instantiate WebDriver directly.** Use `DriverManager.getDriver()` only.
- **Don't skip teardown.** Browser processes leak if `DriverManager.quitDriver()` is not called.
- **Don't mix Page Object patterns.** New pages extend `BasePage`; avoid `PageFactory` unless refactoring legacy code.
- **Headless mode:** Controlled by `-Dheadless=true` system property at test runtime, not hard-coded.
- **Test naming:** Follow `Test` suffix convention; Maven Surefire runs `**/*Tests.java` and `**/*Test.java` by default.

## AI Specialist Skills

Canonical project skills live in `.agents/skills/`; Claude and Codex wrappers point there so their instructions cannot drift. Use `coordinate` for multi-surface work. It routes Java/Selenium changes to `automation-framework-reviewer`, practice-question changes to `question-bank-quality` plus `learning-explanation-author` when post-answer teaching content changes, Maven/CI changes to `ci-reliability-auditor`, and practice-site UI changes to `practice-site-visual-verification`.

Two skills apply to every change rather than a surface. `qa-engineer` gives the risk tier, the
post-conditions worth asserting, and the smallest verification command — consult it before
starting, not as a closing rubber stamp. `subagent-orchestration` decides whether to fan work
out at all and how to shard it without two agents editing `practice.js` at once.

The single rule behind both: **green does not mean applied.** A passing `node --check`, a
successful-looking applier, and a subagent reporting success have each accompanied an edit that
never reached its target here. Assert the post-condition against freshly re-read state.

