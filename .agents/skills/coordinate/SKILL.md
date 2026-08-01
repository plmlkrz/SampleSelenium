---
name: coordinate
description: Coordinate a SampleSelenium change or review that spans framework code, the interview question bank, CI, or the practice-site UI. Use when a task needs two or more specialist perspectives or the user asks for a unified review.
---

# Coordinate

Read `AGENTS.md` and inspect the user-requested scope plus `git status --short` and `git diff --name-only`.

Route work as follows:

| Scope | Specialists |
|---|---|
| `src/main/java`, `src/test/java`, WebDriver, POM, waits, parallel tests | `automation-framework-reviewer` |
| `practice.js`, employer filters, MCQs, written prompts | `question-bank-quality`; add `learning-explanation-author` for post-answer teaching content |
| `pom.xml`, `Jenkinsfile`, `.github/workflows` | `ci-reliability-auditor` |
| `practice.html`, `practice.css`, responsive or accessibility changes | `practice-site-visual-verification` |

Use specialists inline for a narrow task. Delegate independent reviews only when the task warrants it and the user has authorized parallel work. Do not ask several specialists to repeat the same general review.

Return one report: **Scope**, **Specialists consulted**, then merged findings under **Must fix**, **Should fix**, and **Consider**. Include exact files/lines where possible, the smallest relevant verification command, and any documentation that must be updated.

Before closing, check configuration drift: if a new skill, agent, command, module, test profile, or question-bank rule was added, update the relevant routing table and project guidance in the same change.
