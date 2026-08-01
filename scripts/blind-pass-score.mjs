// Step 2 of the blind verification pass: score solver answers against the withheld key.
//
// The salt is scored FIRST and gates everything else. If the solver did not catch the planted
// falsehoods, its agreement on the real questions carries no information and this script
// refuses to report a pass — a perfect score from an instrument that cannot fail is not
// evidence.
//
//   node scripts/blind-pass-score.mjs <dir-containing-blind-key.json> <answers1.json> [...]
import fs from "node:fs";
import path from "node:path";

const dir = process.argv[2];
const answerFiles = process.argv.slice(3);
if (!dir || !answerFiles.length) {
    console.error("usage: node scripts/blind-pass-score.mjs <dir> <answers1.json> [answers2.json ...]");
    process.exit(1);
}

const key = new Map(JSON.parse(fs.readFileSync(path.join(dir, "blind-key.json"), "utf8")).map((k) => [k.id, k]));
const seen = new Set();
const saltResults = [];
const disagreements = [];
const ambiguous = [];
const confidence = { high: 0, medium: 0, low: 0 };
let scored = 0;
let agreed = 0;

for (const file of answerFiles) {
    for (const a of JSON.parse(fs.readFileSync(file, "utf8"))) {
        const k = key.get(a.id);
        if (!k) { console.error("answer for unknown id " + a.id + " in " + file); process.exitCode = 1; continue; }
        if (seen.has(a.id)) { console.error("duplicate answer for id " + a.id + " in " + file); process.exitCode = 1; continue; }
        seen.add(a.id);
        confidence[a.confidence] = (confidence[a.confidence] || 0) + 1;

        const flaggedNone = a.noneCorrect === true || a.pick === -1;
        if (k.kind === "salt") { saltResults.push({ id: a.id, caught: flaggedNone, question: k.question }); continue; }

        scored++;
        if (flaggedNone) { disagreements.push({ id: a.id, why: "solver says no option is correct", confidence: a.confidence, question: k.question }); }
        else if (a.pick === k.markedAnswer) { agreed++; }
        else { disagreements.push({ id: a.id, why: a.why || "picked a different option", confidence: a.confidence, question: k.question }); }
        if ((a.alsoDefensible || []).length) ambiguous.push({ id: a.id, question: k.question });
    }
}

const saltCaught = saltResults.filter((s) => s.caught).length;
const saltValid = saltResults.length > 0 && saltCaught === saltResults.length;
const unanswered = [...key.keys()].filter((id) => !seen.has(id));

console.log(JSON.stringify({
    controlCheck: {
        saltItems: saltResults.length,
        caught: saltCaught,
        verdict: saltResults.length === 0 ? "NO SALT PRESENT — run is not interpretable"
            : saltValid ? "instrument discriminates" : "INSTRUMENT UNRELIABLE — agreement below is uninformative",
        missed: saltResults.filter((s) => !s.caught).map((s) => s.question)
    },
    realQuestions: { scored, agreed, agreementPct: scored ? Number((100 * agreed / scored).toFixed(1)) : null },
    disagreements,
    ambiguousQuestions: ambiguous,
    confidence,
    unansweredIds: unanswered
}, null, 2));

if (!saltValid) {
    console.error("\nBlind pass VOID: the solver did not catch every planted falsehood, so its agreement proves nothing about the bank.");
    process.exitCode = 1;
} else if (unanswered.length) {
    console.error("\nBlind pass incomplete: " + unanswered.length + " item(s) unanswered.");
    process.exitCode = 1;
} else if (disagreements.length) {
    console.error("\n" + disagreements.length + " disagreement(s) to adjudicate. Disagreement is a flag, not a verdict — a judgment question may have a defensible second answer.");
    process.exitCode = 1;
}
