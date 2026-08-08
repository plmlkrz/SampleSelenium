---
# Last audited: 2026-08-07
name: question-bank-quality
description: Create, edit, or audit SampleSelenium interview-practice questions in practice.js. Use for MCQs, written prompts, employer tagging, answer explanations, distractor quality, coverage pairing, or question-bank validation.
---

# Question Bank Quality

Treat `practice.js` as both a learning tool and an assessment. Keep MCQs and written prompts separate: MCQs test one decision principle; written prompts build a spoken interview answer.

For a post-answer explanation, route rich examples, distractor breakdowns, and study prompts to `learning-explanation-author`; do not turn the question itself into a hint.

For each employer-specific written prompt, ensure at least one employer-tagged MCQ covers its underlying principle. Do not tag a question to an employer when its source signal belongs exclusively to another employer.

MCQ rules:

- Use four options and one unambiguous answer index.
- Put detail, nuance, and trade-offs in `explanation`, not one option.
- Avoid all/none, joke distractors, and factual traps unrelated to the stated learning objective.
- Preserve randomized answer position and the written-track separation.

Answer length is the bias that actually shows up here, and it survives answer shuffling — a
learner who always clicks the longest option should not beat guessing. "Keep the options a
similar length" is too soft to act on; it was already in this file while 93% of the bank had
the correct answer as its longest option. Use the mechanical form instead:

- **At least one distractor must be strictly longer than the correct option.** Aim for the
  correct option to be second or third longest of the four.
- Keep `max(len) - min(len)` under 55 characters.
- **Match the clause count.** Length is only a symptom; the underlying tell is that correct
  answers enumerate ("do A, then B, and check C") while distractors state one idea. Across
  this bank the correct answer averages 2.55 clauses against 1.16 for distractors, which is
  why "pick the option with the most commas" outscores "pick the longest". A distractor must
  argue at the same shape and depth as the correct answer — same number of clauses, same
  enumerative form — or the bias just moves from length into structure.
- Trim the correct option to its essential claim *and* fill the distractors out to full,
  genuinely tempting wrong answers. Doing only one of the two leaves the ranking unchanged —
  shaving the correct answer alone just shrinks every option together.
- A longer distractor still has to be a plausible wrong answer, never padding.

Rebalanced options go in `balancedOptionOverrides`, keyed by a question prefix. An entry is
either a bare array of four options, which keeps the question's original `answer` index, or
`{ options, answer }` when the rebalance reordered the choices. Use the second shape whenever
the correct option moves — replacing options alone leaves `answer` pointing at a distractor,
and nothing surfaces that until a learner is marked wrong on a correct choice.

Before finishing, run `node --check practice.js` and `node scripts/audit-question-bank.mjs`. Audit changed questions for duplicate wording, correct answer indexes, employer-filter reachability, and option-length bias. The script blocks structural errors and regressions beyond the committed length-bias baseline, and also fails when the bank has improved past the baseline without the baseline being lowered — that is what keeps the ratchet tightening. Both commands also run as the `question-bank-audit` job in `.github/workflows/selenium-tests.yml`, which is the only CI job that cannot fail because a public practice site is down.

To check whether marked answers are actually true, run the blind pass
(`scripts/blind-pass-build.mjs` then `scripts/blind-pass-score.mjs`; see the README). Give the
solver the questions without the key or explanations, tell it some items may have no correct
option, and never show it the marked answer first — seeing the answer turns the task into "is
this defensible?" instead of "what is true?", and it will almost always say yes.

Treat a clean result as evidence only if the salted control items were caught in that same
run. A pass with no salt, or one where planted falsehoods slipped through, is uninterpretable
no matter how high the agreement. Note also what the control does and does not cover: it
reverses crisp facts, so it validates the method on the roughly one-fifth of the bank that is
factual, and says little about the judgment questions that make up most of it. Agreement from
a solver that shares the question author's training data can confirm a shared misconception,
so blind agreement raises confidence without establishing truth.

After any rebalance, prove the correct answer survived rather than assuming it. Load the bank,
run `applyOptionHardening()`, and assert that `item.options[item.answer]` is still the option
you meant — a rewrite that reorders choices without updating `answer` marks a correct choice
wrong, and no syntax check or page load reveals it. Verify against the text of the intended
answer, not the index.

## Full-Bank Audit Mode

Use this mode when the user asks to audit the practice test, question bank, or interview-prep content as a whole. Do not create a second audit skill; this skill owns both targeted edits and whole-bank review.

1. Run `node --check practice.js` and `node scripts/audit-question-bank.mjs`.
2. Report the audit totals and categorize findings as **Must fix**, **Should fix**, and **Consider**.
3. Review answer-length findings in bounded batches. Rebalance only the options under review, keep the correct index valid, and lower the committed baseline after each verified batch.
4. Sample questions across topics, employers, and tracks for ambiguity, plausible distractors, one learning objective, correct employer tagging, and explanation quality. Flag factual uncertainty rather than guessing.
5. Confirm every employer-specific written prompt has a matching employer-tagged MCQ, then inspect the post-answer deep dive for the most important missed concepts.
6. When UI behavior changes, route responsive, accessibility, and interaction verification to `practice-site-visual-verification` before closing.

The audit is complete only when automated checks pass, all must-fix findings are resolved, and remaining should-fix or consider work is explicitly recorded as a bounded follow-up rather than silently deferred.
