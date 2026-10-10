import test from "node:test";
import assert from "node:assert/strict";
import { createProfileSaveQueue } from "../src/features/profile/saveQueue.js";
import { profileError, profilePayload } from "../src/features/profile/validation.js";
import { restoreHistory } from "../src/features/workouts/history.js";
import { workoutPayload } from "../src/features/workouts/registration.js";

function deferred() {
  let resolve, reject;
  const promise = new Promise((yes, no) => { resolve = yes; reject = no; });
  return { promise, resolve, reject };
}
async function until(predicate) {
  for (let n = 0; n < 100; n++) {
    if (predicate()) return;
    await new Promise(resolve => setTimeout(resolve, 2));
  }
  assert.fail("Expected asynchronous state was not reached");
}

test("profile saves are serialized and intermediate edits collapse to the latest", async () => {
  const calls = [], events = [], jobs = [];
  const queue = createProfileSaveQueue(payload => {
    calls.push(payload);
    const job = deferred(); jobs.push(job); return job.promise;
  }, event => events.push(event), 0);
  queue.schedule({ name: "A" });
  await until(() => calls.length === 1);
  queue.schedule({ name: "B" });
  queue.schedule({ name: "C" });
  await new Promise(resolve => setTimeout(resolve, 5));
  assert.deepEqual(calls, [{ name: "A" }]);
  jobs[0].resolve();
  await until(() => calls.length === 2);
  assert.deepEqual(calls[1], { name: "C" });
  jobs[1].resolve();
  await until(() => events.some(event => event.state === "saved"));
  assert.deepEqual(events.filter(event => event.state === "saved").map(event => event.payload), [{ name: "C" }]);
  queue.dispose();
});

test("failed profile save can retry; invalid input cancels pending work", async () => {
  let calls = 0;
  const events = [];
  const queue = createProfileSaveQueue(async () => {
    calls++;
    if (calls === 1) throw new Error("offline");
  }, event => events.push(event), 0);
  queue.schedule({ name: "A" });
  await until(() => events.some(event => event.state === "error"));
  queue.schedule({ name: "A" }, true);
  await until(() => events.some(event => event.state === "saved"));
  queue.schedule({ name: "B" });
  queue.invalidate();
  await new Promise(resolve => setTimeout(resolve, 5));
  assert.equal(calls, 2);
  queue.dispose();
});

test("reverting a field during an in-flight save persists the reverted value last", async () => {
  const calls = [], jobs = [];
  const queue = createProfileSaveQueue(payload => {
    calls.push(payload); const job = deferred(); jobs.push(job); return job.promise;
  }, () => {}, 0);
  queue.schedule({ name: "Changed" });
  await until(() => calls.length === 1);
  assert.equal(queue.hasWork(), true);
  queue.schedule({ name: "Original" });
  jobs[0].resolve();
  await until(() => calls.length === 2);
  assert.deepEqual(calls[1], { name: "Original" });
  jobs[1].resolve();
  await until(() => !queue.hasWork());
  queue.dispose();
});

test("profile validation agrees with required fields and DB precision", () => {
  const good = { name: "Test", gender: "female", age: 0, bodyWeightKg: "70.25" };
  assert.equal(profileError(good), "");
  assert.deepEqual(profilePayload(good), { ...good, bodyWeightKg: 70.25 });
  for (const change of [{ gender: "none" }, { age: "" }, { age: -1 }, { bodyWeightKg: "" }, { bodyWeightKg: 0 }, { bodyWeightKg: "70.001" }, { bodyWeightKg: 1000 }, { name: "x".repeat(51) }]) {
    assert.notEqual(profileError({ ...good, ...change }), "");
  }
});

test("history restores recorded calories without recalculating from current weight", () => {
  const rows = [{ sessionId: 1, startedAt: "2026-01-01T10:00:00+09:00", finishedAt: "2026-01-01T11:00:00+09:00", bodyWeightKg: "70.00", totalCalories: "150.31", exerciseId: 1, sets: [{ weightKg: 50, reps: 10 }] }];
  assert.equal(restoreHistory(rows)[0].totalCalories, 150.31);
  assert.equal(restoreHistory(rows)[0].bodyWeightKg, 70);
  assert.equal(restoreHistory([{ ...rows[0], totalCalories: null }])[0].totalCalories, null);
});

test("workout request freezes completion time and preserves retry identifier and start weight", () => {
  const session = { id: "12345678-1234-1234-1234-123456789abc", date: "2026-01-01T10:00:00+09:00", bodyWeightKg: 70, entries: [{ exerciseId: 1, sets: [{ weight: 50, reps: 10 }] }] };
  const payload = workoutPayload(session, "2026-01-01T11:00:00+09:00");
  session.entries[0].sets[0].reps = 20;
  assert.equal(payload.exercises[0].sets[0].reps, 10);
  assert.equal(payload.bodyWeightKg, 70);
  assert.equal(payload.clientRequestId, session.id);
  assert.equal(payload.finishedAt, "2026-01-01T11:00:00+09:00");
});

test("API errors expose their message and retries reuse the exact workout payload", async () => {
  const { saveWorkout } = await import("../src/api/client.js");
  const original = globalThis.fetch;
  const requests = [];
  try {
    globalThis.fetch = async (url, options) => {
      requests.push({ url, body: options.body });
      if (requests.length === 1) return { ok: false, json: async () => ({ detail: "保存できませんでした" }) };
      return { ok: true, json: async () => [{ sessionId: 1 }] };
    };
    const payload = { clientRequestId: "12345678-1234-1234-1234-123456789abc", finishedAt: "2026-01-01T11:00:00Z" };
    await assert.rejects(() => saveWorkout(payload), /保存できませんでした/);
    assert.deepEqual(await saveWorkout(payload), [{ sessionId: 1 }]);
    assert.equal(requests[0].body, requests[1].body);
  } finally {
    globalThis.fetch = original;
  }
});


test("profile name follows null and 50-character API constraints", () => {
  const profile = { name: "Test", gender: "male", age: 30, bodyWeightKg: "70" };
  for (const name of [null, "a".repeat(51)]) assert.notEqual(profileError({ ...profile, name }), "");
  for (const name of ["", "a".repeat(50)]) assert.equal(profileError({ ...profile, name }), "");
});

test("workout weight is rejected before adding a set if it cannot be stored", async () => {
  const { workoutWeightError } = await import("../src/features/workouts/validation.js");
  for (const weight of ["", "10000", "50.001", "0.001", "50oops", "Infinity"]) assert.notEqual(workoutWeightError(weight), "");
  for (const weight of ["0.01", "9999.99", "50.00"]) assert.equal(workoutWeightError(weight), "");
});


test("successful in-flight writes remain known after invalid input cancels their UI revision", async () => {
  const calls = [], jobs = [], persisted = [];
  const queue = createProfileSaveQueue(payload => {
    calls.push(payload); const job = deferred(); jobs.push(job); return job.promise;
  }, event => { if (event.state === "persisted") persisted.push(event.payload); }, 0);
  queue.schedule({ name: "Changed" });
  await until(() => calls.length === 1);
  queue.invalidate();
  jobs[0].resolve();
  await until(() => !queue.hasWork());
  assert.deepEqual(persisted, [{ name: "Changed" }]);
  // The hook now knows that the DB differs from the valid draft reverted to Original.
  queue.schedule({ name: "Original" });
  await until(() => calls.length === 2);
  jobs[1].resolve();
  await until(() => !queue.hasWork());
  assert.deepEqual(persisted, [{ name: "Changed" }, { name: "Original" }]);
  queue.dispose();
});

test("workout requires loaded, valid and successfully saved profile and freezes its start weight", async () => {
  const { workoutStartError } = await import("../src/features/workouts/start.js");
  const profile = { name: "Test", gender: "male", age: 30, bodyWeightKg: "70" };
  const state = { profile, loadState: "ready", saveState: "saved" };
  const data = { dataReady: true, dataError: "" };
  assert.equal(workoutStartError(state, data), "");
  for (const weight of ["", 0, "70.001", "1000"]) {
    assert.notEqual(workoutStartError({ ...state, profile: { ...profile, bodyWeightKg: weight } }, data), "");
  }
  for (const saveState of ["pending", "saving", "error"]) assert.notEqual(workoutStartError({ ...state, saveState }, data), "");
  for (const loadState of ["loading", "error"]) assert.notEqual(workoutStartError({ ...state, loadState }, data), "");
  assert.notEqual(workoutStartError(state, { dataReady: false, dataError: "offline" }), "");
  const session = { id: "12345678-1234-1234-1234-123456789abc", date: "2026-01-01T10:00:00Z", bodyWeightKg: Number(profile.bodyWeightKg), entries: [{ exerciseId: 1, sets: [{ weight: 50, reps: 10 }] }] };
  profile.bodyWeightKg = "90";
  assert.equal(workoutPayload(session, "2026-01-01T11:00:00Z").bodyWeightKg, 70);
});

test("profile read and save errors reach the caller and can retry with valid data", async () => {
  const { getProfile, saveProfile } = await import("../src/api/client.js");
  const original = globalThis.fetch;
  const profile = { name: "Test", gender: "male", age: 30, bodyWeightKg: 70 };
  let calls = 0;
  try {
    globalThis.fetch = async () => {
      if (++calls <= 2) return { ok: false, json: async () => ({ detail: "プロフィール通信に失敗" }) };
      return { ok: true, json: async () => profile };
    };
    await assert.rejects(() => getProfile(1), /プロフィール通信に失敗/);
    await assert.rejects(() => saveProfile(1, profile), /プロフィール通信に失敗/);
    assert.deepEqual(await getProfile(1), profile);
    assert.deepEqual(await saveProfile(1, profile), profile);
  } finally { globalThis.fetch = original; }
});
