# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run all tests (opens a Chrome window)
mvn test

# Run all tests headless
mvn test -Dheadless=true

# Run a single test class
mvn test -Dtest=LoginTests

# Run a single test method
mvn test -Dtest=LoginTests#loginWithValidCredentials

# Compile without running tests
mvn compile

# TestNG drill suites (interview-prep drills; forced TestNG provider via profile)
mvn test -Ptestng-drills -Dheadless=true   # sequential, via testng-drills.xml
mvn test -Pparallel -Dheadless=true        # parallel="methods" thread-count=3, via testng-parallel.xml
```

**Machine quirk:** Norton AV intercepts HTTPS on this machine, so downloading *new* Maven
dependencies fails with a PKIX error unless Java uses the Windows trust store. If `MAVEN_OPTS`
isn't already set user-wide, prefix commands with
`$env:MAVEN_OPTS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'` (PowerShell).

**Surefire gotchas encoded in pom.xml:** default includes are extended so `Source*`/`Practice*`
drill classes are discovered; TestNG drill packages (`drills/d03_testng`, `drills/d04_parallel`)
are excluded from default runs and only execute via the two profiles, which force the
`surefire-testng` provider (auto-detection would pick the JUnit Platform and silently ignore
`suiteXmlFiles`).

## Architecture

This is a Maven Selenium 4 practice project targeting two sites:

- **Sauce Demo** (`https://www.saucedemo.com`) — primary site with username/password combos: `standard_user`, `locked_out_user`, `problem_user`, `performance_glitch_user` (all use `secret_sauce`)
- **The Internet** (`https://the-internet.herokuapp.com/login`) — secondary site using `tomsmith` / `SuperSecretPassword!`

### Page Object Model

All page classes live in `src/main/java/com/sampleselenium/pages/`. Two styles coexist:

1. **`BasePage` pattern** (preferred): Page classes extend `BasePage`, which holds a shared `WebDriverWait` (10s default) and helpers — `waitAndClick`, `waitAndType`, `waitForVisible`, `getText`, `isDisplayed`. Locators are declared as `private static final By` constants. Page action methods return the next page object (fluent navigation).

2. **`PageFactory` pattern** (used in `SetupPage`): Uses `@FindBy` annotations and `PageFactory.initElements()`. Does not extend `BasePage`.

### Driver Management

`DriverManager` wraps a `ThreadLocal<WebDriver>` for thread-safe parallel execution. Always use `DriverManager.getDriver()` in tests; never instantiate `WebDriver` directly (except in `MyLoginTest`, which is a standalone experiment that bypasses this pattern).

### Test Base Classes

- **`BaseTest`** (`src/test/java/.../base/`): JUnit 5 base class — `@BeforeEach` calls `DriverManager.createDriver(headless)`, `@AfterEach` calls `DriverManager.quitDriver()`. Reads the `headless` system property.
- Test classes that don't extend `BaseTest` manage their own setup/teardown inline (e.g., `LoginTestsTestNg`, `MyLoginTest`).

### Dual Framework Note

The project has both JUnit 5 and TestNG on the classpath. `LoginTestsTestNg` uses JUnit 5 `@Test` annotations despite its name; `MyLoginTest` is a real TestNG test (TestNG annotations + `Assert`) that runs on the JUnit platform via the `testng-engine` dependency. New tests should use JUnit 5 consistently and extend `BaseTest` — except TestNG drill classes, which extend `drills/support/TestNgBase` and run via the suite-file profiles.

### Interview Drill Gym

`src/test/java/com/sampleselenium/drills/` contains numbered interview-prep modules (d01–d13), each with a complete `Source*` file and a `Practice*` skeleton (disabled tests Peter re-types from memory). [STUDY_GUIDE.md](STUDY_GUIDE.md) is the map: module table, run commands, Infosys/Deloitte question coverage, and talk tracks. Cucumber features live in `src/test/resources/features/`. CI lives in `.github/workflows/selenium-tests.yml` and `Jenkinsfile` (both are also drill material). Don't "fix" the `@Disabled`/`enabled=false` practice stubs — being empty is their job.

**Facts about Peter live in one place, and it is not this repo.** Background, work history, stories, metrics, compensation, and honesty boundaries come from `C:\Users\peter\IdeaProjects\JobSearch\PROJECT_CONTEXT.md`, read fresh. The interview-prep workflow that drives this gym is specified in that repo at `job_search_team\04_interview_prep.md`, under `## Coding drill gyms`; its sibling gym is `C:\Users\peter\IdeaProjects\PythonData`. Never restate a fact about Peter in this repo's docs or drill comments. Cue the story and let the interview packet supply the detail. Every copy of those facts has drifted out of date.

## Project AI Specialists

The canonical instructions are in `.agents/skills/`; `.claude/skills`, `.claude/agents`, and `.codex/agents` are thin wrappers.

Use `qa-engineer` before and after any non-trivial change: it carries this project's risk tiers and the pre-flight checks that catch a mis-targeted edit before a batch of work is spent on it. Use `subagent-orchestration` to decide whether to delegate and how to shard.

**Green does not mean applied.** `node --check` has passed on an edit written into the wrong object, an applier has reported 74 successful edits to a map nobody wanted changed, and a subagent has reported success on a batch a validator then rejected outright. Assert the post-condition against freshly re-read state; an exit code is not evidence. Use `coordinate` for work spanning multiple surfaces. It routes framework code to `automation-framework-reviewer`, `practice.js` content to `question-bank-quality` plus `learning-explanation-author` for deep-dive learning content, CI files to `ci-reliability-auditor`, and practice-site UI changes to `practice-site-visual-verification`.

