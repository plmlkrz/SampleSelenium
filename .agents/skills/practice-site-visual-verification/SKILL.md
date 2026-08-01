---
name: practice-site-visual-verification
description: Verify the SampleSelenium practice site for visual layout, responsive behavior, keyboard accessibility, and browser-console errors. Use after changes to practice.html, practice.css, practice.js UI behavior, coding drills, or blueprint diagrams.
---

# Practice Site Visual Verification

Read the changed HTML/CSS/JS and start the static site with `py -m http.server 8080` when browser verification is needed. Use browser automation or inspection to test both a desktop viewport and a 375px-wide viewport.

Check:

- Question start, answer selection, feedback, next-question flow, written-answer reveal, and filters.
- No horizontal overflow; controls remain visible and usable on narrow screens.
- Keyboard access, visible focus, semantic buttons/labels, and meaningful SVG `role`/`aria-label` values.
- Collapsible blueprint cards work and SVG text stays readable without clipping.
- Browser console is free of runtime errors and localStorage-backed statistics still update.

Report exact viewport, steps, expected vs actual result, and whether the problem is functional, visual, or accessibility-related. Do not claim visual verification without opening the site.
