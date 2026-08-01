// Can someone score on this bank WITHOUT knowing the subject?
//
// audit-question-bank.mjs measures one tell: whether the correct answer is the longest
// option. That is not the strongest one. Correct answers here are written as comprehensive
// enumerations while distractors state a single idea, and every surface symptom of that --
// length, clause count, vocabulary size -- is separately exploitable. Fixing length alone
// moves the bias into whichever symptom is left.
//
// This script scores the whole family. It only uses what a test-taker can see before
// answering: the stem and the four options. Answer POSITION is deliberately not tested,
// because shuffleChoices() randomizes it at render time.
import fs from "node:fs";
import vm from "node:vm";

const src = fs.readFileSync(new URL("../practice.js", import.meta.url), "utf8");
const cut = src.indexOf("const state =");
const context = {};
vm.createContext(context);
vm.runInContext(src.slice(0, cut) + ";applyOptionHardening();this.bank=questionBank;", context);
const mcqs = context.bank.filter((q) => q.type === "mcq");

const HEDGE = /\b(usually|generally|typically|often|depends|both|combine|balance|prefer|primarily|mostly|when|while|then)\b/i;
const ABSOLUTE = /\b(never|always|only|all|none|every|cannot|impossible|any|must)\b/i;
const words = (s) => new Set(String(s).toLowerCase().match(/[a-z0-9]+/g) || []);
const clauses = (s) => (String(s).match(/[,;]/g) || []).length + 1;

const strategies = {
    "most clauses (commas/semicolons)": (q) => q.options.map(clauses),
    "longest option": (q) => q.options.map((o) => o.length),
    "largest vocabulary": (q) => q.options.map((o) => words(o).size),
    "avoids absolute words": (q) => q.options.map((o) => (ABSOLUTE.test(o) ? 0 : 1)),
    "uses hedging language": (q) => q.options.map((o) => (HEDGE.test(o) ? 1 : 0)),
    "most stem-word overlap": (q) => { const s = words(q.question); return q.options.map((o) => [...words(o)].filter((w) => s.has(w)).length); },
    "shortest option": (q) => q.options.map((o) => -o.length)
};

// A tie among N winners is a 1/N coin flip, not a hit -- otherwise a strategy that ties
// everything would look perfect.
const score = (fn) => {
    let hits = 0;
    for (const q of mcqs) {
        const scores = fn(q);
        const max = Math.max(...scores);
        const winners = scores.reduce((acc, s, i) => (s === max ? [...acc, i] : acc), []);
        if (winners.includes(q.answer)) hits += 1 / winners.length;
    }
    return 100 * hits / mcqs.length;
};

const table = Object.entries(strategies)
    .map(([strategy, fn]) => ({ strategy, hitRate: Number(score(fn).toFixed(1)) }))
    .sort((a, b) => b.hitRate - a.hitRate);

const best = table[0];
const correctClauses = mcqs.reduce((n, q) => n + clauses(q.options[q.answer]), 0) / mcqs.length;
const distractorClauses = mcqs.reduce((n, q) => n + q.options.filter((_, i) => i !== q.answer).reduce((m, o) => m + clauses(o), 0) / 3, 0) / mcqs.length;

console.log(JSON.stringify({
    mcqs: mcqs.length,
    randomBaseline: 25,
    bestFixedStrategy: best,
    allStrategies: table,
    structuralParallelism: {
        avgClausesCorrect: Number(correctClauses.toFixed(2)),
        avgClausesDistractor: Number(distractorClauses.toFixed(2)),
        note: "Parity means distractors argue at the same shape and depth as the correct answer."
    }
}, null, 2));

// Ratchet, matching audit-question-bank.mjs: regressing fails, and so does improving without
// writing the better number back.
const BASELINE_BEST_HIT_RATE = 28.9;
const EPSILON = 0.05;
if (best.hitRate > BASELINE_BEST_HIT_RATE + EPSILON) {
    console.error("Guessability audit failed: a subject-blind strategy now scores " + best.hitRate
        + "% (\"" + best.strategy + "\"), worse than the committed " + BASELINE_BEST_HIT_RATE + "%.");
    process.exitCode = 1;
} else if (best.hitRate < BASELINE_BEST_HIT_RATE - EPSILON) {
    console.error("Guessability audit failed: the bank improved to " + best.hitRate
        + "%. Lower BASELINE_BEST_HIT_RATE in this file to " + best.hitRate + " so the gain cannot be given back.");
    process.exitCode = 1;
}
