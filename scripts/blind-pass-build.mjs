// Step 1 of the blind verification pass: build batches an independent solver can answer
// without ever seeing the answer key.
//
// Why blind rather than "review this answer": showing the marked answer anchors the reviewer,
// which turns the exercise into "is this defensible?" instead of "what is true?". Answering
// cold is a genuine independent attempt, and disagreement then means something.
//
// Every batch is SALTED with known-false control items. That is not optional. A blind pass
// that returns 100% agreement is indistinguishable from a solver that rubber-stamps whatever
// it is shown, and the only way to tell those apart is to check whether it catches planted
// falsehoods in the same run. If the salt is not caught, the run is void.
//
//   node scripts/blind-pass-build.mjs <outputDir> [batches]
import fs from "node:fs";
import path from "node:path";
import vm from "node:vm";

const outDir = process.argv[2];
const batchCount = Number(process.argv[3] || 4);
if (!outDir) { console.error("usage: node scripts/blind-pass-build.mjs <outputDir> [batches]"); process.exit(1); }
fs.mkdirSync(outDir, { recursive: true });

const src = fs.readFileSync(new URL("../practice.js", import.meta.url), "utf8");
const cut = src.indexOf("const state =");
const context = {};
vm.createContext(context);
vm.runInContext(src.slice(0, cut) + ";applyOptionHardening();this.bank=questionBank;", context);
const mcqs = context.bank.filter((q) => q.type === "mcq");

// Reversed statements of facts with a definite answer. Each makes the marked option FALSE,
// so no option in that item is correct and a real solver must say so.
// Keyed by question prefix; entries whose question is gone are reported, never silently dropped.
const SALT = [
    ["What is the difference between HTTP 401 and 403", "401 means authenticated but forbidden (you may not); 403 means unauthenticated (who are you?)"],
    ["Which HTTP methods are idempotent", "POST and PATCH — repeating them leaves the same end state"],
    ["String vs StringBuilder", "String is mutable; StringBuilder is immutable; StringBuffer removes synchronization."],
    ["Array vs ArrayList", "An array grows dynamically; ArrayList has a fixed length set at construction."],
    ["List vs Set", "List enforces uniqueness; Set preserves insertion order and allows duplicates."],
    ["Primary key vs foreign key", "The foreign key identifies a row; the primary key enforces table relationships."],
    ["What is the difference between 301, 302, and 304", "301 is temporary, 302 permanent, and 304 signals a server error."]
];

let seed = Number(process.env.BLIND_SEED || 20260801);
const rnd = () => { seed = (seed * 1103515245 + 12345) & 0x7fffffff; return seed / 0x7fffffff; };
const shuffleIndices = (n) => {
    const idx = [...Array(n).keys()];
    for (let a = n - 1; a > 0; a--) { const b = Math.floor(rnd() * (a + 1)); [idx[a], idx[b]] = [idx[b], idx[a]]; }
    return idx;
};

const items = [];
const key = [];

mcqs.forEach((q, i) => {
    const order = shuffleIndices(q.options.length);
    items.push({ id: i, question: q.question, options: order.map((k) => q.options[k]) });
    key.push({ id: i, kind: "real", markedAnswer: order.indexOf(q.answer), question: q.question });
});

const missing = [];
SALT.forEach(([prefix, falsified], n) => {
    const q = mcqs.find((x) => x.question.startsWith(prefix));
    if (!q) { missing.push(prefix); return; }
    const options = [...q.options];
    options[q.answer] = falsified;
    const order = shuffleIndices(options.length);
    const id = 100000 + n;
    items.push({ id, question: q.question, options: order.map((k) => options[k]) });
    key.push({ id, kind: "salt", question: q.question, note: "correct option was falsified; no option is correct" });
});

if (missing.length) {
    console.error("Salt items no longer match any question — fix these before trusting a run:");
    for (const m of missing) console.error("  " + m);
    process.exitCode = 1;
}

// Interleave so salt is not clustered, then renumber. Renumbering matters: if salt kept a
// distinct id range the solver could spot the planted items by their ids alone and the
// control would measure nothing.
const order = shuffleIndices(items.length);
const shuffled = order.map((i) => items[i]);
const keyById = new Map(key.map((k) => [k.id, k]));
const renumberedKey = [];
shuffled.forEach((item, n) => {
    const original = keyById.get(item.id);
    item.id = n;
    renumberedKey.push({ ...original, id: n });
});
key.length = 0;
key.push(...renumberedKey);

const per = Math.ceil(shuffled.length / batchCount);
for (let b = 0; b < batchCount; b++) {
    const slice = shuffled.slice(b * per, (b + 1) * per);
    if (slice.length) fs.writeFileSync(path.join(outDir, "blind" + (b + 1) + ".json"), JSON.stringify(slice, null, 1));
}
fs.writeFileSync(path.join(outDir, "blind-key.json"), JSON.stringify(key, null, 1));

// The batches must not leak the key. Only these three fields may appear on an item.
const allowed = new Set(["id", "question", "options"]);
let leaked = false;
for (let b = 1; b <= batchCount; b++) {
    const f = path.join(outDir, "blind" + b + ".json");
    if (!fs.existsSync(f)) continue;
    for (const item of JSON.parse(fs.readFileSync(f, "utf8"))) {
        const extra = Object.keys(item).filter((k) => !allowed.has(k));
        if (extra.length) { console.error("LEAK in blind" + b + ".json: unexpected field(s) " + extra.join(", ")); leaked = true; }
    }
}
if (leaked) process.exitCode = 1;

console.log(JSON.stringify({
    realQuestions: mcqs.length,
    saltItems: SALT.length - missing.length,
    totalItems: shuffled.length,
    batches: batchCount,
    keyWrittenTo: path.join(outDir, "blind-key.json"),
    leakCheck: leaked ? "FAILED" : "clean",
    seed
}, null, 2));
