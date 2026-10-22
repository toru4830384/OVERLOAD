import test from "node:test";
import assert from "node:assert/strict";
import { createHistoryLoader } from "../src/features/workouts/historyLoader.js";

const deferred = () => { let resolve; const promise = new Promise(done => { resolve = done; }); return { promise, resolve }; };
const row = id => ({ sessionId: id, startedAt: "2026-01-01T10:00:00+09:00", totalCalories: 170.1, exerciseId: 1, sets: [] });

test("late initial response cannot replace history fetched after saving", async () => {
  const initial = deferred();
  const saved = deferred();
  const requests = [initial.promise, saved.promise];
  const shown = [];
  const load = createHistoryLoader(() => requests.shift(), history => shown.push(history));
  const first = load();
  const second = load();
  saved.resolve([row(2), row(1)]);
  await second;
  initial.resolve([row(1)]);
  await first;
  assert.equal(shown.length, 1);
  assert.deepEqual(shown[0].map(session => session.id), [2, 1]);
});

test("aborted loads do not change the displayed history", async () => {
  const response = deferred();
  const controller = new AbortController();
  const shown = [];
  const load = createHistoryLoader(() => response.promise, history => shown.push(history));
  const task = load(controller.signal);
  controller.abort();
  response.resolve([row(1)]);
  await task;
  assert.deepEqual(shown, []);
});
