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

console.log(JSON.stringify({
    total: bank.length, mcqs: mcqs.length, written: written.length,
    invalidMcqs: invalid.length, duplicateQuestions: duplicateQuestions.length,
    bespokeDeepDives: Object.keys(context.deep).length,
    optionLength: { uniqueLongest, spreadAtLeast55: spread55, flaggedQuestions: lengthViolations.length, baseline: { uniqueLongest: 187, spreadAtLeast55: 75 } },
    pairedCoverage: { buildersMutual: pairedCoverage("Builders Mutual"), innFlow: pairedCoverage("Inn-Flow") }
}, null, 2));

const strict = process.argv.includes("--strict");
const baselineRegressed = uniqueLongest > 187 || spread55 > 75;
if (invalid.length || duplicateQuestions.length || baselineRegressed || (strict && lengthViolations.length)) {
    console.error("Question-bank audit failed: fix invalid data, duplicates, or a regression beyond the committed length-bias baseline.");
    process.exitCode = 1;
}
