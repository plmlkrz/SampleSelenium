# SampleSelenium

Practice project for Selenium WebDriver tests — built for interview preparation.

## Daily interview field test

Open `practice.html` in a browser for a randomized daily practice session based on the Deloitte interview rounds and Infosys top-100 question bank.

The field test includes:

- 20 randomized questions per session with no repeats and shuffled answer positions
- 132-question bank covering the Infosys top-100, Deloitte rounds, Barclays QA rounds, Luxoft-style API/Spring Boot questions, the full HTTP status-code map (2xx/3xx/4xx/5xx), and Spring IoC / REST Assured specification / BDD design-depth questions
- Focus tracks for Selenium, Java, framework design, TestNG/SQL, interview communication, and API + Spring Boot
- Multiple-choice questions with immediate explanations and source signals
- A dedicated written-answers track with coach notes, kept separate from the multiple-choice mix
- Java and SQL coding drills, including the Labcorp-style "print every letter of my name" prompt
- Local streak, best score, and session tracking through browser local storage
- Responsive desktop and mobile layouts with no backend dependency

To run it from a local URL:

~~~powershell
cd C:\Users\peter\IdeaProjects\SampleSelenium
py -m http.server 8080
~~~

Then open <http://localhost:8080/practice.html>.

For the best practice loop, answer out loud before revealing the coach note, then type the coding solution from memory before revealing the sample.

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
