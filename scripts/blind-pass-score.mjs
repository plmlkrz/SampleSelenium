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
const fileChecks = [];
const disagreements = [];
const ambiguous = [];
const confidence = { high: 0, medium: 0, low: 0 };
let scored = 0;
let agreed = 0;

for (const file of answerFiles) {
    const fileSaltResults = [];
    let answers;
    try { answers = JSON.parse(fs.readFileSync(file, "utf8")); }
    catch (error) { console.error("could not parse " + file + ": " + error.message); process.exitCode = 1; fileChecks.push({ file, saltItems: 0, caught: 0, valid: false }); continue; }
    if (!Array.isArray(answers)) { console.error("answer file must contain a JSON array: " + file); process.exitCode = 1; fileChecks.push({ file, saltItems: 0, caught: 0, valid: false }); continue; }
    for (const a of answers) {
        if (!a || !Number.isInteger(a.id) || !Number.isInteger(a.pick) || a.pick < -1 || a.pick > 3
            || (a.noneCorrect !== undefined && typeof a.noneCorrect !== "boolean")
            || (a.noneCorrect === true && a.pick !== -1)
            || (a.confidence !== undefined && !["high", "medium", "low"].includes(a.confidence))
            || (a.why !== undefined && typeof a.why !== "string")
            || (a.alsoDefensible !== undefined && !Array.isArray(a.alsoDefensible))) {
            console.error("invalid answer schema in " + file + "; expected {id, pick (-1..3), optional noneCorrect, confidence, why, alsoDefensible}");
            process.exitCode = 1;
            continue;
        }
        const k = key.get(a.id);
        if (!k) { console.error("answer for unknown id " + a.id + " in " + file); process.exitCode = 1; continue; }
        if (seen.has(a.id)) { console.error("duplicate answer for id " + a.id + " in " + file); process.exitCode = 1; continue; }
        seen.add(a.id);
        if (a.confidence) confidence[a.confidence]++;

        const flaggedNone = a.noneCorrect === true || a.pick === -1;
        if (k.kind === "salt") {
            const result = { id: a.id, caught: flaggedNone, question: k.question };
            saltResults.push(result); fileSaltResults.push(result); continue;
        }

        scored++;
        if (flaggedNone) { disagreements.push({ id: a.id, why: "solver says no option is correct", confidence: a.confidence, question: k.question }); }
        else if (a.pick === k.markedAnswer) { agreed++; }
        else { disagreements.push({ id: a.id, why: a.why || "picked a different option", confidence: a.confidence, question: k.question }); }
        if ((a.alsoDefensible || []).length) ambiguous.push({ id: a.id, question: k.question });
    }
    fileChecks.push({ file, saltItems: fileSaltResults.length, caught: fileSaltResults.filter((s) => s.caught).length,
        valid: fileSaltResults.length > 0 && fileSaltResults.every((s) => s.caught) });
}

const saltCaught = saltResults.filter((s) => s.caught).length;
const saltValid = fileChecks.length > 0 && fileChecks.every((check) => check.valid);
const unanswered = [...key.keys()].filter((id) => !seen.has(id));

console.log(JSON.stringify({
    controlCheck: {
        saltItems: saltResults.length,
        caught: saltCaught,
        verdict: saltResults.length === 0 ? "NO SALT PRESENT — run is not interpretable"
            : saltValid ? "instrument discriminates" : "INSTRUMENT UNRELIABLE — agreement below is uninformative",
        missed: saltResults.filter((s) => !s.caught).map((s) => s.question)
    },
    perAnswerFile: fileChecks,
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
