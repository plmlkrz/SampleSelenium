---
name: subagent-orchestration
description: Decide whether to delegate SampleSelenium work to subagents, how to shard it, and which model tier to use. Use when asked to parallelize, swarm, or spawn agents, when coordinate selects multiple specialists, or when a large batch of question-bank or drill work might be cheaper handled inline.
---

# Subagent Orchestration

Optimize for completed work per unit of usage, not the largest possible swarm. The parent owns
scope, integration, verification, and the final answer.

## Authorization gate

Spawn only when the user or repository instructions authorize delegation. This skill is not
itself that permission. Preserve the task's mutation and external-action boundaries — a worker
inherits the parent's limits, it does not widen them.

## Decide whether to delegate

Delegate when at least one holds:

- Two or more workstreams are genuinely independent.
- A bounded scout can absorb bulky reading and return a compact map.
- Repetitive, well-specified work can be sharded without overlapping files.
- A specialist checklist is meaningfully independent of the parent's reasoning.

Keep it inline when the subtask is small, sequential, coupled to the parent's judgment, or
cheaper than reconstructing the context elsewhere. Do not spawn to answer a question.

## Model tier

- **Opus** — orchestration, ambiguous debugging, cross-domain trade-offs, final judgment, and
  content review where being wrong is expensive.
- **Sonnet** — scoped authoring, code search, bounded verification, checklist execution. This is
  the right default for question-bank and deep-dive batches.
- **Haiku** — bulk mechanical work with no judgment.

Reserve Opus workers for subtasks that genuinely need planning the parent cannot retain.

## The pattern that fits this repo

Nearly all content lives in one file, `practice.js`. Concurrent editors therefore collide by
default. Use **fan-out for authoring, single-writer for applying**:

1. Shard the input into disjoint batches, one file per worker under the scratchpad.
2. Workers **return structured data to their own output path** and are explicitly forbidden from
   editing `practice.js`.
3. The parent validates every returned item against mechanically checkable constraints and applies
   only what passes.

Cap the swarm at 3-6 workers. More has not shortened wall-clock here; it has only widened the
surface the parent must validate.

## Write contracts a script can grade

The single biggest source of wasted work in this project has been constraints workers cannot
check themselves. "Make the options similar in length" produced 42 rejections out of 60. "At
least one distractor must be strictly longer than the correct option" produced zero.

Every worker prompt should carry:

1. One concrete objective and exact scope.
2. The input path and the output path, both absolute.
3. Whether edits are allowed, and which files the worker owns.
4. Constraints stated so a validator can accept or reject each item.
5. A strict return contract, and a reminder that self-reported success is not evidence.

Prefer file paths over pasted bodies. Ask for terse reports — every word re-enters the parent's
context.

## Verify, then re-send rather than respawn

Run the parent's validator over every returned batch. When items fail, send the **specific failing
items plus the measurements that prove the failure** back to the same worker; its context is still
warm and a fresh agent would rediscover the problem from scratch. Respawn only when a clean
evaluation context is the point — a blind verification pass, for instance, must not be sent to the
agent that authored the content.

## Prevent collisions and waste

- Concurrent editors get disjoint file ownership; otherwise one writer and read-only reviewers.
- Do not poll running agents; continue useful parent work and wait only when results are required.
- Workers write only to their own scratchpad path. A worker that writes into the repository can
  leak scratch files into a commit — this has happened here.
- Before dispatching a large batch, prove the apply mechanism on one item. Discovering the target
  was wrong after 74 edits is the most expensive mistake available in this repo.
