import fs from "node:fs";
import vm from "node:vm";

const source = fs.readFileSync(new URL("../practice.js", import.meta.url), "utf8");
const cutoff = source.indexOf("const state =");
const context = {};
vm.createContext(context);
vm.runInContext(source.slice(0, cutoff) + ";applyOptionHardening();this.bank=questionBank;this.deep=deepDiveByQuestion;", context);

const bank = context.bank;
const mcqs = bank.filter((item) => item.type === "mcq");
const written = bank.filter((item) => item.type === "written");
const invalid = mcqs.filter((item) => item.options?.length !== 4 || !Number.isInteger(item.answer)
    || item.answer < 0 || item.answer > 3 || !item.explanation || !item.anchor);
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

// A ratchet, not a threshold: going past the baseline fails, and so does improving on it
// without writing the better numbers back here. Otherwise slack accumulates silently and the
// gate stops meaning anything.
const BASELINE = { uniqueLongest: 127, spreadAtLeast55: 15 };

console.log(JSON.stringify({
    total: bank.length, mcqs: mcqs.length, written: written.length,
    invalidMcqs: invalid.length, duplicateQuestions: duplicateQuestions.length,
    bespokeDeepDives: Object.keys(context.deep).length,
    optionLength: { uniqueLongest, spreadAtLeast55: spread55, flaggedQuestions: lengthViolations.length, baseline: BASELINE },
    pairedCoverage: { buildersMutual: pairedCoverage("Builders Mutual"), innFlow: pairedCoverage("Inn-Flow") }
}, null, 2));

const strict = process.argv.includes("--strict");
const baselineRegressed = uniqueLongest > BASELINE.uniqueLongest || spread55 > BASELINE.spreadAtLeast55;
const baselineStale = uniqueLongest < BASELINE.uniqueLongest || spread55 < BASELINE.spreadAtLeast55;
if (baselineStale && !baselineRegressed) {
    console.error("Question-bank audit failed: the bank improved past the committed baseline. Lower BASELINE in this file to { uniqueLongest: "
        + uniqueLongest + ", spreadAtLeast55: " + spread55 + " } so the gain cannot be given back.");
    process.exitCode = 1;
}
if (invalid.length || duplicateQuestions.length || baselineRegressed || (strict && lengthViolations.length)) {
    console.error("Question-bank audit failed: fix invalid data, duplicates, or a regression beyond the committed length-bias baseline.");
    process.exitCode = 1;
}
