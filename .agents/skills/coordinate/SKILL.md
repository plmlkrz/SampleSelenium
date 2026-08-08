---
# Last audited: 2026-08-07
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
| Any change, before starting and before closing | `qa-engineer` for the risk tier, the post-conditions to assert, and the minimal verification command |
| Deciding whether to fan work out, and how to shard it | `subagent-orchestration` |

Consult `qa-engineer` early, not as a final rubber stamp. Its risk tier decides how much
verification the change warrants, and its pre-flight checks exist because the expensive failures
here were cheap to detect beforehand and costly to discover afterwards.

Use specialists inline for a narrow task. Delegate independent reviews only when the task warrants it and the user has authorized parallel work. Do not ask several specialists to repeat the same general review.

Most of this project's content lives in one file, `practice.js`, so parallel specialists
cannot each edit their own slice of it. When fanning out work that lands in a shared file,
have every specialist return structured data to a path of its own and let the coordinator
apply it. Keep the applier separate from the author: validate each returned item against
mechanically checkable constraints, apply only what passes, and report what was rejected and
why. Specialists that self-report success are not evidence — verify their output yourself.

Give specialists constraints a script can check, not adjectives. A rejected batch should come
back with the specific lever that fixes it and the measurements that prove the failure, then
be re-sent to the same specialist so its context survives.

Return one report: **Scope**, **Specialists consulted**, then merged findings under **Must fix**, **Should fix**, and **Consider**. Include exact files/lines where possible, the smallest relevant verification command, and any documentation that must be updated.

Before closing, check configuration drift: if a new skill, agent, command, module, test profile, or question-bank rule was added, update the relevant routing table and project guidance in the same change.
