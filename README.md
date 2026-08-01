# SampleSelenium

Practice project for Selenium WebDriver tests — built for interview preparation.

## Daily interview field test

Open `practice.html` in a browser for a randomized daily practice session based on the Deloitte interview rounds, Infosys top-100 question bank, and targeted employer preparation sets.

The field test includes:

- 20 randomized questions per session with no repeats and shuffled answer positions
- 234-question bank covering the Infosys top-100, Deloitte rounds, Barclays QA rounds, Luxoft investment-banking automation topics, Maximus Lead Software QA topics, Builders Mutual API/integration QA topics, Inn-Flow automation-first QA topics (AI-assisted QA, Cypress/Playwright, React, .NET APIs, SQL Server, multi-tenant SaaS on Azure, JMeter/k6, CI/CD continuous testing), the full HTTP status-code map (2xx/3xx/4xx/5xx), and Spring IoC / REST Assured specification / BDD design-depth questions
- Employer filter for ALL, Infosys, Deloitte, Barclays, Luxoft, Maximus, Builders Mutual, Inn-Flow, and general cross-company questions; the employer filter can be combined with any focus track
- Focus tracks for Selenium, Java, framework design, TestNG/SQL, interview communication, and API + Spring Boot
- Multiple-choice questions with immediate explanations and source signals
- A dedicated written-answers track with coach notes, kept separate from the multiple-choice mix
- Java and SQL coding drills, including the Labcorp-style "print every letter of my name" prompt
- Five whiteboard-ready framework blueprint diagrams: Spring Boot API, Selenium UI, Selenium BDD, Builders Mutual-style API integration and reconciliation, and Inn-Flow-style multi-tenant SaaS continuous testing
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

## Auditing the practice bank

```powershell
node scripts/audit-question-bank.mjs
```

Use `--strict` when the answer-length backlog has reached zero; it then rejects every
multiple-choice question with a long-answer cue or a 55+ character option spread.

The audit checks question structure, duplicate wording, employer written-to-MCQ coverage, bespoke deep-dive count, and option-length bias. It fails if structural issues occur or the committed answer-length baseline gets worse; reduce that baseline deliberately as legacy questions are rebalanced.

This runs in CI as the `question-bank-audit` job. It needs no JDK, browser, or network, so it
is the one job that stays trustworthy when the Selenium suites go red because Sauce Demo or
the-internet.herokuapp.com is unreachable.

```powershell
node scripts/audit-guessability.mjs
```

Scores the bank as a test-taker who knows nothing about the subject would: always pick the
wordiest option, the longest, the one that avoids "never" and "always". Anything well above
the 25% random baseline means the bank is partly measuring writing style instead of
knowledge. Answer position is not tested, because the page shuffles it on every render.

Both scripts ratchet: they fail on a regression past the committed baseline **and** on an
improvement that was not written back, so a gain cannot quietly erode.

### What these prove, and what they do not

Guessability is fully decidable — it is a property of the data, so the number above is
measured, not estimated. **Answer correctness is not.** No script knows whether the marked
answer is true. That gap is covered, in descending order of independence, by: executing the
claim as a drill under `src/test/java/com/sampleselenium/drills/` where it is executable at
all; citing primary documentation; a review by a different model family than the one that
wrote the question; and last and weakest, a same-family review, which shares the blind spots
of whatever produced the content.

## Test Site

Tests run against [Sauce Demo](https://www.saucedemo.com) — a purpose-built Selenium practice application.

| Username | Password | Behavior |
|---|---|---|
| `standard_user` | `secret_sauce` | Normal login |
| `locked_out_user` | `secret_sauce` | Locked out error |
| `problem_user` | `secret_sauce` | Broken images |
| `performance_glitch_user` | `secret_sauce` | Slow responses |
