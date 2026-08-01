import fs from "node:fs";
import vm from "node:vm";

const source = fs.readFileSync(new URL("../practice.js", import.meta.url), "utf8");
const cutoff = source.indexOf("const state =");
const context = {};
vm.createContext(context);
vm.runInContext(source.slice(0, cutoff)
    + ";applyOptionHardening();this.bank=questionBank;this.deep=deepDiveByQuestion;this.concise=conciseCorrectOptionOverrides;", context);

// A repeated key in an object literal is legal JavaScript -- the last one silently wins and
// every earlier copy is dead text. That is invisible to `node --check` and to the bank at
// runtime, so an edit applied to the shadowed copy appears to do nothing for no visible
// reason. Nine of these had accumulated across the override maps before this check existed.
const duplicateOverrideKeys = ["balancedOptionOverrides", "conciseCorrectOptionOverrides"].flatMap((name) => {
    const start = source.indexOf("const " + name + " = {");
    if (start < 0) return [{ object: name, key: "(object not found)" }];
    let i = source.indexOf("{", start);
    let depth = 0, inString = false, escaped = false, end = -1;
    for (; i < source.length; i++) {
        const ch = source[i];
        if (inString) {
            if (escaped) escaped = false; else if (ch === "\\") escaped = true; else if (ch === '"') inString = false;
            continue;
        }
        if (ch === '"') { inString = true; continue; }
        if (ch === "{") depth++;
        else if (ch === "}") { depth--; if (depth === 0) { end = i; break; } }
    }
    const keys = [...source.slice(start, end).matchAll(/^ {4}"((?:[^"\\]|\\.)*)":/gm)].map((m) => JSON.parse('"' + m[1] + '"'));
    const counts = new Map();
    for (const k of keys) counts.set(k, (counts.get(k) || 0) + 1);
    return [...counts].filter(([, n]) => n > 1).map(([key]) => ({ object: name, key }));
});

const bank = context.bank;
const mcqs = bank.filter((item) => item.type === "mcq");
const written = bank.filter((item) => item.type === "written");
const invalid = mcqs.filter((item) => item.options?.length !== 4 || new Set(item.options).size !== 4
    || item.options.some((option) => typeof option !== "string" || !option.trim())
    || !Number.isInteger(item.answer) || item.answer < 0 || item.answer > 3 || !item.explanation || !item.anchor);
const duplicateQuestions = [...bank.reduce((counts, item) => counts.set(item.question, (counts.get(item.question) || 0) + 1), new Map())]
    .filter(([, count]) => count > 1).map(([question]) => question);
const lengthStats = mcqs.map((item) => {
    const lengths = item.options.map((option) => option.length);
    const max = Math.max(...lengths);
    return { item, spread: max - Math.min(...lengths), correctIsLongest: lengths[item.answer] === max && lengths.filter((length) => length === max).length === 1 };
});
const uniqueLongest = lengthStats.filter((item) => item.correctIsLongest).length;
const spread55 = lengthStats.filter((item) => item.spread >= 55).length;
const lengthViolations = lengthStats.filter((item) => item.correctIsLongest || item.spread >= 55);
const pairedCoverage = (employer) => {
    const writtenCount = written.filter((item) => item.anchor?.includes(`${employer} JD gap`)).length;
    const pairedCount = mcqs.filter((item) => item.anchor?.includes(`paired recall`) && item.anchor?.includes(employer)).length;
    return { writtenCount, pairedCount };
};
const conciseOverrideIssues = Object.entries(context.concise).flatMap(([prefix, expectedAnswer]) => {
    const matches = mcqs.filter((item) => item.question.startsWith(prefix));
    if (matches.length !== 1) return [{ prefix, matches: matches.length, reason: "prefix must identify exactly one MCQ" }];
    return matches[0].options[matches[0].answer] === expectedAnswer
        ? [] : [{ prefix, matches: 1, reason: "reviewed answer was not applied" }];
});

// A ratchet, not a threshold: going past the baseline fails, and so does improving on it
// without writing the better numbers back here. Otherwise slack accumulates silently and the
// gate stops meaning anything.
const BASELINE = { uniqueLongest: 32, spreadAtLeast55: 0 };
// With four parallel choices the correct answer should be uniquely longest about 25% of
// the time. Strict mode allows a small sampling margin instead of demanding zero and
// accidentally teaching learners that the longest option is always wrong.
const STRICT_MAX_UNIQUE_LONGEST = Math.ceil(mcqs.length * 0.30);

console.log(JSON.stringify({
    total: bank.length, mcqs: mcqs.length, written: written.length,
    invalidMcqs: invalid.length, duplicateQuestions: duplicateQuestions.length,
    shadowedOverrideKeys: duplicateOverrideKeys,
    bespokeDeepDives: Object.keys(context.deep).length,
    conciseAnswerOverrides: { total: Object.keys(context.concise).length, issues: conciseOverrideIssues.length },
    optionLength: {
        uniqueLongest, spreadAtLeast55: spread55, flaggedQuestions: lengthViolations.length,
        baseline: BASELINE, strictMaxUniqueLongest: STRICT_MAX_UNIQUE_LONGEST
    },
    pairedCoverage: { buildersMutual: pairedCoverage("Builders Mutual"), innFlow: pairedCoverage("Inn-Flow") }
}, null, 2));

const strict = process.argv.includes("--strict");
const baselineRegressed = uniqueLongest > BASELINE.uniqueLongest || spread55 > BASELINE.spreadAtLeast55;
const baselineStale = uniqueLongest < BASELINE.uniqueLongest || spread55 < BASELINE.spreadAtLeast55;
const strictLengthBias = uniqueLongest > STRICT_MAX_UNIQUE_LONGEST || spread55 > 0;
if (baselineStale && !baselineRegressed) {
    console.error("Question-bank audit failed: the bank improved past the committed baseline. Lower BASELINE in this file to { uniqueLongest: "
        + uniqueLongest + ", spreadAtLeast55: " + spread55 + " } so the gain cannot be given back.");
    process.exitCode = 1;
}
if (duplicateOverrideKeys.length) {
    console.error("Question-bank audit failed: " + duplicateOverrideKeys.length
        + " override key(s) are duplicated and therefore shadowed. Keep the last copy and delete the earlier ones:");
    for (const d of duplicateOverrideKeys) console.error("  " + d.object + " -> " + d.key.slice(0, 70));
    process.exitCode = 1;
}
if (invalid.length || duplicateQuestions.length || conciseOverrideIssues.length || baselineRegressed || (strict && strictLengthBias)) {
    console.error("Question-bank audit failed: fix invalid data, duplicates, or a regression beyond the committed length-bias baseline.");
    process.exitCode = 1;
}
