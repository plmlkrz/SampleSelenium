---
# Last audited: 2026-08-07
name: learning-explanation-author
description: Create or improve SampleSelenium post-answer learning deep dives in practice.js. Use when adding or revising More information content, examples, distractor explanations, interview phrasing, or Claude/Codex study prompts.
---

# Learning Explanation Author

Create content for the post-answer learning layer, never a pre-answer hint. Keep the MCQ a fair recall test; put nuance in `deepDiveByQuestion` after the answer is checked.

For a high-value or frequently missed concept, add:

- `concept`: plain-language explanation of the rule and trade-off.
- `distractors`: short explanations for why the three tempting alternatives lose.
- `example`: a compact Java, Selenium, API, SQL, or configuration example that is syntactically credible.
- `interview`: a 20–30 second spoken answer in Peter's honest experience boundary.

Every question receives the generic deep-dive fallback. Add bespoke content only where it materially teaches more than the existing `explanation`; prioritize employer-specific material, framework design, synchronization, API security, data validation, and distributed-system behavior.

The study prompt must ask Claude/Codex to explain simply, show one example, and quiz the learner once without giving the follow-up answer. Keep it local and copyable; do not add an external API or claim the site sends user data anywhere.

Run `node --check practice.js`. Verify the More information control appears only after checking an answer, works by keyboard, toggles `aria-expanded`, and the copy button has a useful failure state.
