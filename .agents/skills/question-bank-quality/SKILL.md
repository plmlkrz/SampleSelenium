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

Before finishing, run `node --check practice.js`. Audit changed questions for duplicate wording, correct answer indexes, employer-filter reachability, and option-length bias. Report any legacy imbalance outside the requested scope rather than silently changing unrelated questions.
