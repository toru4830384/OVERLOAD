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


test("a late failed initial request cannot replace a successful newer request status", async () => {
  let rejectOld;
  const old = new Promise((_resolve, reject) => { rejectOld = reject; });
  const requests = [old, Promise.resolve([row(2)])];
  const statuses = [];
  const shown = [];
  const load = createHistoryLoader(() => requests.shift(), history => shown.push(history), status => statuses.push(status));
  const first = load().catch(error => error.message);
  await load();
  rejectOld(new Error("old failure"));
  assert.equal(await first, "old failure");
  assert.deepEqual(statuses.at(-1), { state: "ready", error: "" });
  assert.equal(shown[0][0].id, 2);
});

test("the latest failed history request exposes its error without discarding displayed rows", async () => {
  const statuses = [];
  const shown = [];
  const load = createHistoryLoader(async () => { throw new Error("通信失敗"); }, history => shown.push(history), status => statuses.push(status));
  await assert.rejects(load(), /通信失敗/);
  assert.deepEqual(statuses.at(-1), { state: "error", error: "通信失敗" });
  assert.deepEqual(shown, []);
});
