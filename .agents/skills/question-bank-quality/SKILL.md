---
name: question-bank-quality
description: Create, edit, or audit SampleSelenium interview-practice questions in practice.js. Use for MCQs, written prompts, employer tagging, answer explanations, distractor quality, coverage pairing, or question-bank validation.
---

# Question Bank Quality

Treat `practice.js` as both a learning tool and an assessment. Keep MCQs and written prompts separate: MCQs test one decision principle; written prompts build a spoken interview answer.

For a post-answer explanation, route rich examples, distractor breakdowns, and study prompts to `learning-explanation-author`; do not turn the question itself into a hint.

For each employer-specific written prompt, ensure at least one employer-tagged MCQ covers its underlying principle. Do not tag a question to an employer when its source signal belongs exclusively to another employer.

MCQ rules:

- Use four options and one unambiguous answer index.
- Make all options plausible and similar in word count; never make the correct answer consistently more detailed.
- Put detail, nuance, and trade-offs in `explanation`, not one option.
- Avoid all/none, joke distractors, and factual traps unrelated to the stated learning objective.
- Preserve randomized answer position and the written-track separation.

Before finishing, run `node --check practice.js` and `node scripts/audit-question-bank.mjs`. Audit changed questions for duplicate wording, correct answer indexes, employer-filter reachability, and option-length bias. The script blocks structural errors and regressions beyond the committed length-bias baseline; lower that baseline whenever a batch is rebalanced.

## Full-Bank Audit Mode

Use this mode when the user asks to audit the practice test, question bank, or interview-prep content as a whole. Do not create a second audit skill; this skill owns both targeted edits and whole-bank review.

1. Run `node --check practice.js` and `node scripts/audit-question-bank.mjs`.
2. Report the audit totals and categorize findings as **Must fix**, **Should fix**, and **Consider**.
3. Review answer-length findings in bounded batches. Rebalance only the options under review, keep the correct index valid, and lower the committed baseline after each verified batch.
4. Sample questions across topics, employers, and tracks for ambiguity, plausible distractors, one learning objective, correct employer tagging, and explanation quality. Flag factual uncertainty rather than guessing.
5. Confirm every employer-specific written prompt has a matching employer-tagged MCQ, then inspect the post-answer deep dive for the most important missed concepts.
6. When UI behavior changes, route responsive, accessibility, and interaction verification to `practice-site-visual-verification` before closing.

The audit is complete only when automated checks pass, all must-fix findings are resolved, and remaining should-fix or consider work is explicitly recorded as a bounded follow-up rather than silently deferred.
