import test from "node:test";
import assert from "node:assert/strict";
import {
  REST_SECONDS_MIN, REST_SECONDS_MAX, completeRestTimer, createRestTimer, formatRestTime,
  isValidRestTimer, parseRestSeconds, pauseRestTimer, remainingRestMilliseconds,
  restSecondsError, resumeRestTimer,
} from "../src/features/workouts/restTimer.js";
import { createWorkoutDraftStore } from "../src/features/workouts/draft.js";

const nextSet = { exerciseId: 1, exerciseName: "ベンチプレス", setNumber: 2 };
const now = Date.parse("2026-01-01T10:00:00Z");

test("rest input keeps raw text and accepts only positive integer seconds through one day", () => {
  for (const [value, seconds] of [["1", REST_SECONDS_MIN], ["86400", REST_SECONDS_MAX], ["90", 90], ["1e2", 100]]) {
    assert.equal(parseRestSeconds(value), seconds);
    assert.equal(restSecondsError(value), "");
    assert.equal(createRestTimer(value, nextSet, now).durationSeconds, seconds);
  }
  for (const value of ["", " ", "0", "-1", "1.5", "90seconds", "Infinity", "1e999", "86401", Infinity, NaN, null, true]) {
    assert.equal(parseRestSeconds(value), null, String(value));
    assert.notEqual(restSecondsError(value), "", String(value));
    assert.equal(createRestTimer(value, nextSet, now), null);
  }
});

test("deadline catches up with elapsed wall time even when no ticks run in the background", () => {
  const timer = createRestTimer("90", nextSet, now);
  assert.equal(remainingRestMilliseconds(timer, now + 15500), 74500);
  assert.equal(remainingRestMilliseconds(timer, now + 95000), 0);
  assert.deepEqual(completeRestTimer(timer, timer, now + 95000), {
    ...timer, remainingMs: 0, deadlineAt: null,
  });
});

test("running and paused rest survive draft persistence and browser reload", async () => {
  let raw = null;
  const storage = { getItem: () => raw, setItem: (_key, value) => { raw = value; }, removeItem: () => { raw = null; } };
  const lock = (_key, action) => Promise.resolve(action());
  const first = createWorkoutDraftStore(1, () => storage, lock);
  await first.setSession({ id: "12345678-1234-1234-1234-123456789abc", date: new Date(now).toISOString(),
    bodyWeightKg: 80, entries: [], restSeconds: "90", restTimer: createRestTimer("90", nextSet, now) });
  const reloaded = createWorkoutDraftStore(1, () => storage, lock);
  assert.equal(reloaded.getSnapshot().session.restSeconds, "90");
  assert.equal(remainingRestMilliseconds(reloaded.getSnapshot().session.restTimer, now + 20000), 70000);
  await reloaded.setSession(session => ({ ...session, restTimer: pauseRestTimer(session.restTimer, now + 20500) }));
  const afterPauseReload = createWorkoutDraftStore(1, () => storage, lock);
  const paused = afterPauseReload.getSnapshot().session.restTimer;
  assert.equal(paused.deadlineAt, null);
  assert.equal(remainingRestMilliseconds(paused, now + 3600000), 69500);
  assert.deepEqual(paused.nextSet, nextSet);
  const resumed = resumeRestTimer(paused, now + 3600000);
  assert.equal(remainingRestMilliseconds(resumed, now + 3600000 + 500), 69000);
});

test("cleared, replaced and paused timers ignore callbacks from an old running timer", () => {
  const old = createRestTimer("10", nextSet, now);
  assert.equal(completeRestTimer(null, old, now + 20000), null);
  const replacement = createRestTimer("10", { ...nextSet, setNumber: 3 }, now);
  assert.equal(completeRestTimer(replacement, old, now + 20000), replacement);
  const paused = pauseRestTimer(old, now + 5000);
  assert.equal(completeRestTimer(paused, old, now + 20000), paused);
});

test("a deadline completes once across repeated observers and completed reloads cannot restart", () => {
  const running = createRestTimer("1", nextSet, now);
  let current = running;
  let completions = 0;
  for (let observer = 0; observer < 2; observer++) {
    const completed = completeRestTimer(current, running, now + 1000);
    if (completed !== current) completions++;
    current = completed;
  }
  assert.equal(completions, 1);
  const reloaded = JSON.parse(JSON.stringify(current));
  assert.equal(completeRestTimer(reloaded, reloaded, now + 10000), reloaded);
  assert.equal(resumeRestTimer(reloaded, now + 10000), reloaded);
  assert.equal(remainingRestMilliseconds(reloaded, now + 10000), 0);
});

test("invalid stored timer fields are rejected and displayed time has whole seconds", () => {
  const valid = createRestTimer("90", nextSet, now);
  assert.equal(isValidRestTimer(valid), true);
  for (const patch of [{ durationSeconds: 0 }, { durationSeconds: 1.5 }, { durationSeconds: 86401 },
    { remainingMs: -1 }, { remainingMs: 90001 }, { deadlineAt: Infinity },
    { deadlineAt: 8640000000000001 }, { nextSet: null }, { nextSet: { ...nextSet, exerciseId: 0 } }]) {
    assert.equal(isValidRestTimer({ ...valid, ...patch }), false);
  }
  assert.equal(formatRestTime(90), "1:30");
  assert.equal(formatRestTime(1.5), "0:02");
  assert.equal(formatRestTime(0), "0:00");
  assert.equal(formatRestTime(86400), "1440:00");
});
