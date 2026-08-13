# Java SDET Interview Drill Gym — Infosys 149552BR (and Deloitte-style loops)

Same system as the PythonData project: every module has a `Source*` file (complete,
runnable, with the interview answer written in the comments) and a `Practice*` file
(skeleton you re-type FROM MEMORY). Repetition is the point.

## The Loop

1. Open the `Source*` file for a module. Run it. Read it until you can explain every line.
2. **Close it.** Open the matching `Practice*` file.
3. Delete one `@Disabled` line (JUnit) or flip `enabled = false` to `true` (TestNG).
4. Write the body from memory. Run it. Compare against the source. Note what you missed.
5. Repeat until you can write it clean AND say the talking point out loud while typing.

**Java-specific rule the Python project didn't have:** Maven compiles EVERY test file
before running ANY test. If your practice file has a syntax error, everything is blocked.
Quick syntax check without launching a browser: `mvn test-compile`. If you get stuck,
comment out the broken code, recompile, keep moving.

## One-Time Setup Note (this machine)

Norton antivirus intercepts HTTPS, so Java can't verify Maven Central's certificate when
downloading NEW dependencies. Fix (one time, then restart the terminal):

```powershell
setx MAVEN_OPTS "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
```

This tells Java to trust what Windows trusts (which includes Norton's certificate).
Nothing is disabled — it's the same trust store your browser uses. Until it's set, prefix
runs with: `$env:MAVEN_OPTS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'; mvn test ...`

## Modules

| # | Topic | Source file | Run with |
|---|---|---|---|
| 1 | Locators, findElement vs findElements | `drills/d01_locators/SourceD01LocatorDrills.java` | `mvn test -Dtest=SourceD01LocatorDrills -Dheadless=true` |
| 2 | Waits: implicit / explicit / fluent | `drills/d02_waits/SourceD02WaitDrills.java` | `mvn test -Dtest=SourceD02WaitDrills -Dheadless=true` |
| 3 | TestNG core: annotations, DataProvider, groups, SoftAssert | `drills/d03_testng/SourceD03TestNgDrills.java` | `mvn test -Ptestng-drills -Dheadless=true` |
| 4 | **Parallel execution** (testng.xml + ThreadLocal) | `drills/d04_parallel/SourceD04ParallelDrills.java` | `mvn test -Pparallel -Dheadless=true` |
| 5 | Browser mechanics: alerts, frames, windows, dropdowns, JS, screenshots, stale elements | `drills/d05_mechanics/SourceD05BrowserMechanicsDrills.java` | `mvn test -Dtest=SourceD05BrowserMechanicsDrills -Dheadless=true` |
| 6 | Java core: string/collection/OOP notepad questions | `drills/d06_java_core/SourceD06JavaCoreDrills.java` | `mvn test -Dtest=SourceD06JavaCoreDrills` |
| 7 | SQL validation (H2 in-memory) | `drills/d07_sql/SourceD07SqlDrills.java` | `mvn test -Dtest=SourceD07SqlDrills` |
| 8 | Cucumber BDD: feature / steps / runner / hooks / tags, over a browser | `drills/d08_bdd/` + `resources/features/` | `mvn test -Dtest=RunCucumberDrills -Dheadless=true` |
| 8b | Cucumber BDD **browser-free**: DataTable, Scenario Outline, Background, money assertions | `drills/d08_bdd/source/` + `resources/bdd_drills/source/` | `mvn test -Dtest=RunSourceD08BddDrills` |
| 9 | **API testing**: REST Assured vs local mock microservice, 200 vs 201, JSON calc validation, Spring Boot testing talk track | `drills/d09_api/SourceD09ApiDrills.java` | `mvn test -Dtest=SourceD09ApiDrills` |
| 10 | **Playwright**: auto-waiting/actionability, getByRole/getByLabel/getByTestId, strict mode, context isolation, dialogs, `page.request()` API calls, network stubbing, tracing — vs the Selenium equivalents we hand-built | `drills/d10_playwright/SourceD10PlaywrightDrills.java` | `mvn test -Dtest=SourceD10PlaywrightDrills` |
| 11 | **Resilience**: timeout/retry/circuit-breaker against a WireMock-stubbed downstream | `drills/d11_resilience/SourceD11ResilienceDrills.java` | `mvn test -Dtest=SourceD11ResilienceDrills` |
| 12 | **Promoting Postman to coded suites**: read an exported collection with JsonPath, port its happy-path requests into REST Assured, then add the negative/boundary cases exploratory clicking skipped | `drills/d12_postman_promotion/SourceD12PostmanPromotionDrills.java` | `mvn test -Dtest=SourceD12PostmanPromotionDrills` |
| 13 | **AWS for real**: deploy your own Lambda + S3 behind a Function URL, then test the live endpoint — 201/Location, 401, 400, 404, S3 round trip, cold-start-aware SLA, CloudWatch triage | `aws/RUNBOOK.md` then `drills/d13_aws/SourceD13AwsDrills.java` | `mvn test -Dtest=SourceD13AwsDrills` (skips until `AWS_FN_URL` is set) |
| 14 | CI/CD: GitHub Actions + Jenkins | `.github/workflows/selenium-tests.yml`, `Jenkinsfile` | read + narrate; runs on push to GitHub |

Modules 6, 7, 8b, 9, 11 and 12 need **no browser and no network** — they run in about a second.
Perfect for high-repetition days and for warming up the morning of the interview.
Module 10 does drive a real browser, but against a local mock app, so it is offline too
(~12s once Chromium is cached — see the setup note below). Modules 1, 2, 5 and 8 are the
only ones that hit the public practice sites, so they are the ones that break when Sauce
Demo or the-internet.herokuapp.com is slow or down — a failure there is usually the
internet, not your code.

Run one single test method: `mvn test -Dtest="SourceD05BrowserMechanicsDrills#alertAcceptAndReadResult" -Dheadless=true`

**Module 13 is the odd one out** — it is the only module that talks to infrastructure you
deployed yourself, so it needs an AWS account and the internet. Follow [aws/RUNBOOK.md](aws/RUNBOOK.md)
once (~45 min of console clicking, inside the free tier), then:

```powershell
$env:AWS_FN_URL='https://xxxx.lambda-url.us-east-1.on.aws'   # no trailing slash
$env:AWS_API_KEY='...'
mvn test -Dtest=SourceD13AwsDrills
```

Until those two variables are set the module **skips** rather than fails, so `mvn test` stays
green on a clean checkout. The endpoint is a ~90-line Python Lambda that writes JSON notes to a
private S3 bucket; the Java side is REST Assured against the deployed URL. The runbook ends with
the teardown steps and — importantly — a list of what this does and does not entitle you to
claim in an interview. Read that section before you talk about it.

**Module 10 one-time setup:** the first `mvn test -Dtest=SourceD10PlaywrightDrills` downloads
Chromium (~1 minute, needs internet once). After that the drills are fully offline — like d09
they drive a local mock app (`MockPortalApp`, JDK HttpServer) on a random port, so there is no
Sauce Demo dependency. If the browsers are missing the tests **skip** rather than fail, so a
clean checkout never breaks `mvn test` for the other modules. Portal login: `adjuster` /
`secret_sauce`. Traces land in `target/traces/` and open at trace.playwright.dev.

## Daily Field Test (practice.html)

The recall side of the gym: the drills build muscle memory for WRITING the code; the
field test checks you can PRODUCE the answers cold. Open `practice.html` via a local
server (`py -m http.server 8080`, then <http://localhost:8080/practice.html>).

- **306 questions** (219 multiple-choice + 87 written prompts) covering the full Infosys
  top-100 bank, the Deloitte rounds, the Luxoft high-probability 15 (Spring Boot,
  REST Assured, financial JSON validation, JMeter, release-risk communication), the
  Barclays QA automation rounds (exception handling, cross-browser, test data
  and config management, maintainability, Git workflow), and a Lead Software QA /
  Maximus JD field test (test planning/estimation, defect triage and RCA, risk escalation,
  automation strategy at program scale, AI-assisted testing, ETL/SQL reconciliation,
  AWS fundamentals, Unix/Linux operations, stakeholder demos/UAT/status reporting,
  Agile/SAFe execution oversight, and white-/gray-box testing), plus Luxoft investment-
  banking topics (Spring Boot/Cucumber, microservices, financial JSON, SQL, JMeter,
  Windows UI, Git/Bitbucket, code review, and JIRA defect management),
  plus Builders Mutual API/integration topics (multi-system data flow and reconciliation,
  eventual consistency, idempotency/duplicate delivery, Swagger/OpenAPI contract testing,
  Postman/Newman vs coded suites, API test-plan documentation, integration root-cause
  analysis, stubbing unavailable partners, Workato/middleware, the sole-QA operating model,
  Playwright specifics — auto-waiting/actionability, role-based locators and strict mode,
  trace viewer, APIRequestContext, and the honest Selenium-to-Playwright ramp answer —
  and AWS testing depth — SNS/SQS/Lambda event flows, CloudWatch Logs Insights triage,
  S3 file ingestion, API Gateway 401/403/502/504 status triage, IAM and secrets),
  plus Inn-Flow QA Automation Engineer topics (AI-assisted QA tooling and how to adopt
  it safely, Cypress vs Selenium architecture, React SPA testing, multi-tenant SaaS
  tenant isolation, SQL Server/database testing depth, testing .NET APIs from a
  non-.NET stack, Azure environments and Azure DevOps pipeline stages, JMeter vs k6,
  shift-left defect prevention, mentoring and quality culture, defensible quality
  metrics, and continuous testing in CI/CD),
  plus Relias Software Developer in Test topics (Cypress reliability and Selenium trade-offs,
  .NET/xUnit service testing, microservices integration, SpecFlow/Reqnroll/Cucumber BDD,
  Azure DevOps/Jenkins continuous testing, JMeter performance analysis, healthcare workflow
  quality, initiative, mentorship, and release-risk communication),
  plus a 50-question scenario bank (test strategy and prioritization, release
  readiness and quality metrics, scenario-design prompts for login/payment/search/
  cart/file upload, distributed-system testing — pagination, eventual consistency,
  dynamic responses, DB migration — and QA leadership situations).
- **20 questions per session, no repeats** — sampled without replacement.
- **Answer positions shuffle on every render**, so "it's always B" can't be memorized.
- **Tracks 01–06** slice by topic (Selenium, Java, Framework, TestNG+SQL, Interview
  voice, API + Spring Boot); **Mixed Signal** pulls from everything.
- **Employer filter** narrows the session to ALL, Infosys, Deloitte, Barclays, Luxoft,
  Maximus, Builders Mutual, Inn-Flow, Relias, or general cross-company questions. It can be combined with a topic track;
  Maximus currently emphasizes the Lead Software QA JD plus the Selenium/Java,
  framework, SQL, API, CI/CD, and leadership questions most likely to come up.
- **Track 07 "Written answers"** holds all typed prompts separately — say the answer
  out loud, type it, then reveal the coach note. These map to the talk tracks in the
  drill modules (e.g. the 200-vs-201 written answer uses the same wording as the
  Module 9 javadoc — one phrasing in both places for faster recall).
- Coding drills (reverse-preserving-whitespace, palindrome, second-highest salary,
  Labcorp-style name letters, settled-trade aggregation, and REST Assured POST/POJO
  deserialization) sit below the question panel with hint/solution reveals.
- **Framework blueprints** below the coding drill: five collapsible architecture
  diagrams (Spring Boot API framework, Selenium UI framework, Selenium BDD framework,
  API integration and reconciliation for Builders Mutual, and multi-tenant SaaS continuous
  testing for Inn-Flow)
  with talk-track notes — practice sketching each while narrating top to bottom.
- Streak, best score, and session count persist in browser localStorage. Best score
  resets when it first sees a 20-question session (old /10 bests don't compare).

Each question's "source signal" anchor points back to the drill module or question
bank it came from — miss a question, run that module.

## Question-Bank Coverage Map

### Infosys Top-100 list → module

| Infosys bucket | Where it's drilled |
|---|---|
| Selenium Q1–6, 16–17 (components, locators, XPath vs CSS) | Module 1 |
| Selenium Q7 (findElement vs findElements) | **Module 1 — the headline drill** |
| Selenium Q8–10 (waits) | Module 2 |
| Selenium Q11–14, 19–24 (alerts, frames, windows, stale, actions, close/quit, screenshots, dropdowns, JSExecutor) | Module 5 |
| Selenium Q15 (dynamic elements), Q25 (captcha — say NO and explain) | Modules 2, 5 (comments) |
| Java Q1–20 | Module 6 (runnable) + comments for the pure-definition ones |
| TestNG Q1–10 | Module 3 (+ Module 4 for parallel) |
| Framework Q1–15 | The repo itself — see "Explain your framework" below; Maven/Jenkins in Module 10 |
| API Testing Q1–10 | **Module 9** — runnable REST Assured drills + status-code semantics; open with the Railinc SOAP UI story, then the 200-vs-201 answer |
| SQL Q1–10 | Module 7 |
| Manual Q1–10 | Pure talk — STLC, severity vs priority, smoke vs sanity; no code needed |

### Deloitte 3-round list → module

| Deloitte question | Where |
|---|---|
| R1 Q2: reverse string preserving whitespace | Module 6 (`reverseStringPreservingWhitespacePositions`) |
| R1 Q3: second-largest salary | Module 7 — **both** solutions, know both |
| R1 Q4: joins | Module 7 |
| R1 Q5: LinkedHashMap | Module 6 (`mapOrderingHashVsLinkedVsTree`) |
| R1 Q7/Q13: exception vs error, checked vs unchecked | Module 6 (`exceptionsAndFinallyOrder` + comments) |
| R1 Q8/Q14: findElement vs findElements, multi-match XPath | Module 1 |
| R1 Q9: implicit vs explicit wait | Module 2 |
| R1 Q11: fetch text from a text box | Module 5 (`readTypedTextWithGetAttributeValue`) — getText() is EMPTY for inputs! |
| R1 Q12: enter text in an alert | Module 5 (`promptAlertTypeTextThenAccept`) |
| R1 Q16: relative locators | Module 1 (`relativeLocatorFindsPasswordBelowUsername`) |
| R2 Q1–2: explain your framework / 100 pages = 100 page objects? | Talk tracks below |
| R2 Q3: element click intercepted | Module 5 class comment — recite the fix ladder |
| R2 Q4: screenshots for failed tests only | `drills/support/ScreenshotOnFailureListener.java` — walk through it |
| R2 Q8: why Cucumber BDD | Module 8 feature-file comments (incl. the honest trade-off) |
| R2 Q9–11: static vs dynamic binding, overload vs override, Comparable vs Comparator | Module 6 |
| R2 Q17: when fluent waits | Module 2 (`fluentWaitForElementAddedToDom`) |
| R2 Q18: open a new tab | Module 5 (`openNewTabWithSelenium4`) |

## "Explain Your Framework" — the 90-second answer, using THIS repo

> "It's a Maven project, Java 17, Selenium 4. Page Object Model: every page is a class
> holding `By` locators and action methods, extending a `BasePage` that centralizes
> explicit waits — `waitAndClick`, `waitAndType` — so no test ever calls `Thread.sleep`.
> Driver lifecycle is a `DriverManager` with a `ThreadLocal<WebDriver>`, so the same
> suite runs serially or parallel — TestNG's `parallel="methods"` in the suite XML with
> a thread count of three. Tests are data-driven with TestNG DataProviders, and business-
> facing flows are Cucumber features running on the JUnit platform. On failure, an
> `ITestListener` captures screenshots automatically. CI is GitHub Actions — every push
> runs the whole suite headless on a Linux runner and publishes surefire reports and
> failure screenshots as artifacts; the same pipeline exists as a Jenkinsfile."

Follow-up they always ask — **"100 pages, do you make 100 page objects?"**:
> "No — page objects model distinct page STRUCTURES, not URLs. Shared components
> (header, nav, grids) become reusable component classes; templated pages (100 product
> pages with one layout) share ONE page object. You end up with tens, not hundreds."

## Honest-Ramp Lines for JD Gaps (don't overclaim)

- **AWS**: "My cloud exposure is from the QA side — validating deployed environments,
  logs, and data movement rather than administering AWS. The testing patterns are the
  same; I'm actively closing the cloud-services vocabulary gap."
- **Spring Boot / Angular**: "My production Java is automation-focused — frameworks,
  Selenium, API and data validation — not Spring Boot application development. I read
  application code comfortably in code reviews; I don't want to overstate hands-on
  Spring depth."
- **BDD Cucumber**: fully claimable — Metabolon framework ownership. Module 8 keeps the
  mechanics fresh.

## Live-Interview Reminders (from the Hard Technical Prep Protocol)

- Narrate before you type. "I'm checking whether the captured value is actually asserted
  on" beats silent scanning.
- The most common planted bug in "review this code" questions: a `getText()` /
  `isDisplayed()` / `System.out.println` whose value never reaches an assertion.
  Dead capture, not a check. Scan for it FIRST.
- Blank on syntax? "I'd need to check the exact method name, but the pattern is—" and
  keep going. That's a senior answer.
- You're not proving you memorized Java. You're reasoning about a testing problem that
  happens to be written in Java.

## Suggested 6-Day Plan

- **Day 1:** Module 1 + Module 2 (the exact questions that hurt last time). Say each
  30-second answer out loud while the tests run.
- **Day 2:** Module 3 + Module 4. Run serial, run parallel, watch the thread ids. Write
  the `<suite parallel="methods" thread-count="3">` line on paper five times.
- **Day 3:** Module 6 + Module 7 (no browser — go for volume; 3+ reps each).
- **Day 4:** Module 5 (biggest module — split over two sessions if needed).
- **Day 5:** Module 8 + Module 10. Uncomment a practice scenario, let Cucumber print the
  missing-step snippets, implement them. Narrate the GitHub Actions file top to bottom.
- **Day 5.5 (or fold into Day 3):** Module 9 — no browser, seconds per run. Say the
  200-vs-201 answer and the Spring Boot layers (@WebMvcTest / @SpringBootTest / black-box)
  out loud until they're automatic.
- **Day 6 (day before):** Cold run — practice files only, 90 minutes, no peeking. Then
  the framework talk track and honest-ramp lines out loud.
