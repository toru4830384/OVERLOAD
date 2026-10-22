import test from "node:test";
import assert from "node:assert/strict";
import { createWorkoutDraftStore, workoutDraftKey } from "../src/features/workouts/draft.js";

const session = () => ({ id: "12345678-1234-1234-1234-123456789abc", date: "2026-01-01T10:00:00+09:00", bodyWeightKg: 80,
  entries: [{ exerciseId: 1, exerciseName: "Bench", note: "メモ", draftWeight: "55.25", draftReps: "10.9", sets: [{ id: "set-1", weight: 50, reps: 10 }] }] });
function memoryStorage() {
  const values = new Map();
  const locks = new Map();
  const withLock = (key, action) => {
    const task = (locks.get(key) || Promise.resolve()).then(action);
    locks.set(key, task.catch(() => {}));
    return task;
  };
  return { values, withLock, getItem: key => values.get(key) ?? null, setItem: (key, value) => values.set(key, value), removeItem: key => values.delete(key) };
}

const createStore = (storage, userId = 1) => createWorkoutDraftStore(userId, () => storage, storage.withLock);

test("start, sets, note, start weight and unfinished inputs survive immediate reload", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession({ ...session(), entries: [] });
  assert.deepEqual(createStore(storage).getSnapshot().session.entries, []);
  await store.setSession(session());
  await store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: "変更したメモ" })) }));
  const restored = createStore(storage).getSnapshot();
  assert.deepEqual(restored.session, { ...session(), entries: [{ ...session().entries[0], note: "変更したメモ" }] });
  assert.equal(restored.session.entries[0].draftReps, "10.9"); // Keep invalid draft input to let the user correct it.
  assert.equal(restored.pendingPayload, null);
});

test("refresh while POST or history read fails preserves exact retry UUID, finish time and payload", async () => {
  const storage = memoryStorage();
  const first = createStore(storage);
  await first.setSession(session());
  const request = await first.freezeForSave("2026-01-01T11:00:00+09:00");
  const afterReload = createStore(storage);
  assert.deepEqual(await afterReload.freezeForSave("2026-01-01T12:00:00+09:00"), request);
  await afterReload.setSession(previous => ({ ...previous, bodyWeightKg: 90 }));
  assert.equal(afterReload.getSnapshot().session.bodyWeightKg, 80);
  assert.deepEqual(createStore(storage).getSnapshot().pendingPayload, request);
  await afterReload.complete(); // Only after API save and canonical history are confirmed.
  assert.equal(storage.getItem(workoutDraftKey(1)), null);
  assert.equal(createStore(storage).getSnapshot().session, null);
});

test("initialization and StrictMode-style duplicate reads never erase or rewrite a saved draft", async () => {
  const storage = memoryStorage();
  await createStore(storage).setSession(session());
  const original = storage.getItem(workoutDraftKey(1));
  storage.setItem = () => { throw new Error("unexpected startup write"); };
  for (let count = 0; count < 2; count++) {
    assert.deepEqual(createStore(storage).getSnapshot().session, session());
  }
  assert.equal(storage.getItem(workoutDraftKey(1)), original);
});

test("corrupt or mismatched-user drafts are retained and cannot be overwritten by starting", async () => {
  const storage = memoryStorage();
  for (const value of ["not JSON", JSON.stringify({ version: 1, userId: 2, session: session(), pendingPayload: null })]) {
    storage.setItem(workoutDraftKey(1), value);
    const store = createStore(storage);
    assert.equal(store.getSnapshot().errorKind, "load");
    await store.setSession(session());
    assert.equal(storage.getItem(workoutDraftKey(1)), value);
    await store.discard();
    assert.equal(storage.getItem(workoutDraftKey(1)), null);
  }
});

test("storage quota failure keeps edits in memory, reports the failure and allows retry", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  const write = storage.setItem;
  storage.setItem = () => { throw new Error("quota"); };
  await store.setSession(session());
  assert.equal(store.getSnapshot().errorKind, "save");
  assert.deepEqual(store.getSnapshot().session, session());
  storage.setItem = write;
  await store.retry();
  assert.equal(store.getSnapshot().error, "");
  assert.deepEqual(createStore(storage).getSnapshot().session, session());
});

test("failed cleanup is explicit and blocks a new workout until deletion is retried", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  await store.freezeForSave("2026-01-01T11:00:00+09:00");
  const remove = storage.removeItem;
  storage.removeItem = () => { throw new Error("remove failed"); };
  await store.complete();
  assert.equal(store.getSnapshot().errorKind, "clear");
  await store.setSession(session());
  assert.equal(store.getSnapshot().session, null);
  assert.notEqual(storage.getItem(workoutDraftKey(1)), null);
  storage.removeItem = remove;
  await store.retry();
  assert.equal(store.getSnapshot().error, "");
  assert.equal(createStore(storage).getSnapshot().session, null);
});

test("drafts are isolated per user and malformed retry payloads remain recoverable", async () => {
  const storage = memoryStorage();
  const store = createStore(storage, 2);
  await store.setSession(session());
  assert.equal((await store.freezeForSave("2026-01-01T11:00:00+09:00")).userId, 2);
  assert.equal(createStore(storage).getSnapshot().session, null);
  const data = JSON.parse(storage.getItem(workoutDraftKey(2)));
  data.pendingPayload.exercises[0].sets[0].reps = 99;
  storage.setItem(workoutDraftKey(2), JSON.stringify(data));
  assert.equal(createStore(storage, 2).getSnapshot().errorKind, "load");
  assert.equal(JSON.parse(storage.getItem(workoutDraftKey(2))).session.entries[0].sets[0].reps, 10);
});

test("stale tabs cannot overwrite or delete a newer draft and can explicitly reload it", async () => {
  const storage = memoryStorage();
  const tabA = createStore(storage);
  await tabA.setSession(session());
  const tabB = createStore(storage);
  await tabA.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({
    ...entry, note: "別タブで追加済み", sets: [...entry.sets, { id: "set-2", weight: 55, reps: 12 }],
  })) }));
  const latest = storage.getItem(workoutDraftKey(1));
  await assert.rejects(tabB.freezeForSave("2026-01-01T11:00:00+09:00"), /別のタブ/);
  assert.equal(tabB.getSnapshot().errorKind, "conflict");
  assert.equal(await tabB.complete(), false);
  assert.equal(await tabB.retry(), false);
  assert.equal(storage.getItem(workoutDraftKey(1)), latest);
  assert.equal(await tabB.reload(), true); // The UI must ask before replacing this tab's unsaved inputs.
  assert.equal(tabB.getSnapshot().session.entries[0].sets.length, 2);
  assert.equal(tabB.getSnapshot().session.entries[0].note, "別タブで追加済み");
});

test("simultaneous tabs acquire one atomic lock before comparing and writing", async () => {
  const storage = memoryStorage();
  const tabA = createStore(storage);
  const tabB = createStore(storage);
  const second = { ...session(), id: "22345678-1234-1234-1234-123456789abc" };
  const outcomes = await Promise.all([tabA.setSession(session()), tabB.setSession(second)]);
  assert.deepEqual(outcomes, [true, false]);
  assert.equal(createStore(storage).getSnapshot().session.id, session().id);
  assert.equal(tabB.getSnapshot().errorKind, "conflict");
  assert.equal(tabB.getSnapshot().session.id, second.id); // The losing tab keeps its own input for recovery.
});

test("same-tab functional updates run in input order after acquiring the lock", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  const append = text => previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: entry.note + text })) });
  await Promise.all([store.setSession(append("A")), store.setSession(append("B")), store.setSession(append("C"))]);
  assert.equal(createStore(storage).getSnapshot().session.entries[0].note, "メモABC");
});

test("freeze failure never releases a POST payload and retries persist the original finish time", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  const write = storage.setItem;
  storage.setItem = () => { throw new Error("quota"); };
  let posts = 0;
  await assert.rejects(store.freezeForSave("2026-01-01T11:00:00+09:00").then(() => { posts++; }), /途中保存/);
  assert.equal(posts, 0);
  assert.equal(JSON.parse(storage.getItem(workoutDraftKey(1))).pendingPayload, null);
  assert.equal(store.getSnapshot().pendingPayload.finishedAt, "2026-01-01T11:00:00+09:00");
  storage.setItem = write;
  const payload = await store.freezeForSave("2026-01-01T12:00:00+09:00");
  assert.equal(payload.finishedAt, "2026-01-01T11:00:00+09:00");
  assert.deepEqual(createStore(storage).getSnapshot().pendingPayload, payload);
});

test("a definite rejection can resume editing without changing UUID or losing sets", async () => {
  for (const status of [400, 404, 422]) {
    const storage = memoryStorage();
    const store = createStore(storage);
    await store.setSession(session());
    const original = await store.freezeForSave("2026-01-01T11:00:00+09:00");
    assert.equal(await store.resumeAfterRejection(status), true);
    assert.equal(store.getSnapshot().pendingPayload, null);
    assert.equal(createStore(storage).getSnapshot().pendingPayload, null);
    await store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: "修正後" })) }));
    const corrected = await store.freezeForSave("2026-01-01T12:00:00+09:00");
    assert.equal(corrected.clientRequestId, original.clientRequestId);
    assert.equal(corrected.startedAt, original.startedAt);
    assert.equal(corrected.finishedAt, "2026-01-01T12:00:00+09:00");
    assert.deepEqual(corrected.exercises[0].sets, original.exercises[0].sets);
    assert.equal(corrected.exercises[0].note, "修正後");
  }
});

test("conflict, server errors and unknown outcomes never unlock or replace a retry UUID", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  const original = await store.freezeForSave("2026-01-01T11:00:00+09:00");
  for (const status of [409, 500, undefined]) assert.equal(await store.resumeAfterRejection(status), false);
  assert.equal(await store.setSession(previous => ({ ...previous, entries: [] })), false);
  assert.deepEqual(await store.freezeForSave("2026-01-01T12:00:00+09:00"), original);
});

test("pending writes are visible before a browser lock is granted", async () => {
  const storage = memoryStorage();
  let release;
  const gate = new Promise(resolve => { release = resolve; });
  const store = createWorkoutDraftStore(1, () => storage, (_key, action) => gate.then(action));
  const save = store.setSession(session());
  assert.equal(store.getSnapshot().isPersisting, true); // useWorkoutDraft warns on beforeunload during this interval.
  release();
  await save;
  assert.equal(store.getSnapshot().isPersisting, false);
  assert.deepEqual(createStore(storage).getSnapshot().session, session());
});

test("default persistence uses the browser lock and never falls back to an unsafe write", async () => {
  const storage = memoryStorage();
  const descriptor = Object.getOwnPropertyDescriptor(globalThis, "navigator");
  const calls = [];
  try {
    Object.defineProperty(globalThis, "navigator", { configurable: true, value: { locks: {
      request(name, options, action) { calls.push({ name, mode: options.mode }); return storage.withLock(name, action); },
    } } });
    const store = createWorkoutDraftStore(1, () => storage);
    await store.setSession(session());
    assert.deepEqual(calls, [{ name: workoutDraftKey(1), mode: "exclusive" }]);
    Object.defineProperty(globalThis, "navigator", { configurable: true, value: {} });
    const unsupported = createWorkoutDraftStore(2, () => storage);
    assert.equal(await unsupported.setSession(session()), false);
    assert.equal(storage.getItem(workoutDraftKey(2)), null);
    assert.match(unsupported.getSnapshot().error, /安全な途中保存/);
  } finally {
    if (descriptor) Object.defineProperty(globalThis, "navigator", descriptor);
    else delete globalThis.navigator;
  }
});


test("completion waits for queued set additions before deciding whether the workout is empty", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession({ ...session(), entries: [] });
  let release;
  const held = new Promise(resolve => { release = resolve; });
  const holdingLock = storage.withLock(workoutDraftKey(1), () => held);
  const addingSet = store.setSession(previous => ({ ...previous, entries: session().entries }));
  const completing = store.freezeForSave("2026-01-01T11:00:00+09:00");
  release();
  await holdingLock;
  await addingSet;
  const payload = await completing;
  assert.equal(payload.exercises.length, 1);
  assert.equal(payload.exercises[0].sets[0].reps, 10);
  assert.equal(createStore(storage).getSnapshot().pendingPayload.exercises.length, 1);
});

test("controlled inputs publish synchronously while a browser lock is held", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  let release;
  const held = new Promise(resolve => { release = resolve; });
  const holding = storage.withLock(workoutDraftKey(1), () => held);
  const first = store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, draftWeight: "5" })) }));
  assert.equal(store.getSnapshot().session.entries[0].draftWeight, "5");
  const second = store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, draftWeight: entry.draftWeight + "5" })) }));
  assert.equal(store.getSnapshot().session.entries[0].draftWeight, "55");
  const weights = [];
  const unsubscribe = store.subscribe(() => weights.push(store.getSnapshot().session.entries[0].draftWeight));
  release();
  await holding;
  await Promise.all([first, second]);
  unsubscribe();
  assert.ok(weights.every(weight => weight === "55")); // An old successful write must not render "5" again.
  assert.equal(createStore(storage).getSnapshot().session.entries[0].draftWeight, "55");
  assert.equal(store.getSnapshot().hasUnsavedChanges, false);
});

test("an older failed write cannot roll back the newer optimistic input", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  const write = storage.setItem;
  storage.setItem = (key, raw) => {
    if (JSON.parse(raw).session.entries[0].note === "older") throw new Error("quota for the older write");
    return write(key, raw);
  };
  const older = store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: "older" })) }));
  const newer = store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: "newest" })) }));
  assert.equal(store.getSnapshot().session.entries[0].note, "newest");
  const notes = [];
  const unsubscribe = store.subscribe(() => notes.push(store.getSnapshot().session.entries[0].note));
  assert.equal(await older, false);
  assert.equal(await newer, true);
  unsubscribe();
  assert.ok(notes.every(note => note === "newest"));
  assert.equal(createStore(storage).getSnapshot().session.entries[0].note, "newest");
  assert.equal(store.getSnapshot().error, "");
});

test("persistent quota failures preserve the newest synchronous input for retry", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  const write = storage.setItem;
  storage.setItem = () => { throw new Error("quota"); };
  const first = store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, draftReps: "1" })) }));
  const second = store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, draftReps: entry.draftReps + "00" })) }));
  assert.equal(store.getSnapshot().session.entries[0].draftReps, "100");
  assert.deepEqual(await Promise.all([first, second]), [false, false]);
  assert.equal(store.getSnapshot().session.entries[0].draftReps, "100");
  assert.equal(store.getSnapshot().hasUnsavedChanges, true);
  storage.setItem = write;
  assert.equal(await store.retry(), true);
  assert.equal(createStore(storage).getSnapshot().session.entries[0].draftReps, "100");
  assert.equal(store.getSnapshot().hasUnsavedChanges, false);
});

test("invalid and throwing updaters never publish or mutate the last valid snapshot", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  const raw = storage.getItem(workoutDraftKey(1));
  const original = structuredClone(store.getSnapshot().session);
  const snapshots = [];
  const unsubscribe = store.subscribe(() => snapshots.push(store.getSnapshot().session));
  for (const update of [
    previous => { previous.entries[0].sets = null; return previous; },
    previous => { previous.bodyWeightKg = 0; throw new Error("broken updater"); },
    () => { throw undefined; },
    () => undefined,
    previous => ({ ...previous, entries: [null] }),
  ]) {
    const result = store.setSession(update);
    assert.deepEqual(store.getSnapshot().session, original);
    assert.equal(store.getSnapshot().errorKind, "validation");
    assert.equal(await result, false);
    assert.equal(storage.getItem(workoutDraftKey(1)), raw);
  }
  unsubscribe();
  assert.ok(snapshots.every(value => Array.isArray(value.entries[0].sets)));
  await assert.rejects(store.freezeForSave("2026-01-01T11:00:00+09:00"), /更新できません/);
  assert.equal(await store.setSession(previous => previous), true);
  assert.equal(store.getSnapshot().error, "");
});

test("a stale tab retains its newest optimistic input when its oldest queued write conflicts", async () => {
  const storage = memoryStorage();
  const original = createStore(storage);
  await original.setSession(session());
  const stale = createStore(storage);
  await original.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: "remote" })) }));
  const remote = storage.getItem(workoutDraftKey(1));
  const first = stale.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: "local-1" })) }));
  const second = stale.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: "local-latest" })) }));
  assert.equal(stale.getSnapshot().session.entries[0].note, "local-latest");
  assert.deepEqual(await Promise.all([first, second]), [false, false]);
  assert.equal(stale.getSnapshot().session.entries[0].note, "local-latest");
  assert.equal(stale.getSnapshot().errorKind, "conflict");
  assert.equal(storage.getItem(workoutDraftKey(1)), remote);
  let called = false;
  assert.equal(await stale.setSession(() => { called = true; return session(); }), false);
  assert.equal(called, false);
});

test("freeze reserves pending immediately and waits for all optimistic writes before releasing a payload", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  let release;
  const held = new Promise(resolve => { release = resolve; });
  const holding = storage.withLock(workoutDraftKey(1), () => held);
  const edited = store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: "latest before finish" })) }));
  const completing = store.freezeForSave("2026-01-01T11:00:00+09:00");
  let returned = false;
  completing.then(() => { returned = true; });
  assert.ok(store.getSnapshot().pendingPayload);
  let called = false;
  assert.equal(await store.setSession(() => { called = true; return session(); }), false);
  assert.equal(called, false);
  assert.equal(returned, false);
  release();
  await holding;
  await edited;
  const payload = await completing;
  assert.equal(payload.exercises[0].note, "latest before finish");
  assert.deepEqual(createStore(storage).getSnapshot().pendingPayload, payload);
});

test("same-value input is a synchronous no-op without a needless storage write", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  storage.setItem = () => { throw new Error("same input must not be written again"); };
  let called = false;
  const result = store.setSession(previous => { called = true; return previous; });
  assert.equal(called, true);
  assert.equal(await result, true);
  assert.equal(store.getSnapshot().isPersisting, false);
  assert.equal(store.getSnapshot().error, "");
});

test("rest timer state and raw setting survive reload while legacy v1 drafts remain valid", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  assert.deepEqual(createStore(storage).getSnapshot().session, session());
  const timer = { durationSeconds: 90, remainingMs: 45000, deadlineAt: null,
    nextSet: { exerciseId: 1, exerciseName: "Bench", setNumber: 2 } };
  await store.setSession(previous => ({ ...previous, restSeconds: "", restTimer: timer }));
  let restored = createStore(storage).getSnapshot().session;
  assert.equal(restored.restSeconds, "");
  assert.deepEqual(restored.restTimer, timer);
  await store.setSession(previous => ({ ...previous, restSeconds: "10.9", restTimer: { ...timer, deadlineAt: 1800000000000 } }));
  restored = createStore(storage).getSnapshot().session;
  assert.equal(restored.restSeconds, "10.9");
  assert.equal(restored.restTimer.deadlineAt, 1800000000000);
  for (const restTimer of [{ ...timer, remainingMs: -1 }, { ...timer, durationSeconds: 1.5 }, { ...timer, deadlineAt: Infinity }]) {
    assert.equal(await store.setSession(previous => ({ ...previous, restTimer })), false);
    assert.equal(store.getSnapshot().session.restTimer.deadlineAt, 1800000000000);
  }
});


test("duplicate exercise and React set keys reject corrupt drafts without overwriting them", async () => {
  const duplicateExercise = session();
  duplicateExercise.entries.push({ ...duplicateExercise.entries[0], note: "重複した種目" });
  const duplicateSet = session();
  duplicateSet.entries[0].sets.push({ ...duplicateSet.entries[0].sets[0], weight: 55 });
  const mixedSetKeys = session();
  mixedSetKeys.entries[0].sets = [{ id: 1, weight: 50, reps: 10 }, { id: "1", weight: 55, reps: 8 }];
  for (const invalid of [duplicateExercise, duplicateSet, mixedSetKeys]) {
    const storage = memoryStorage();
    const raw = JSON.stringify({ version: 1, userId: 1, session: invalid, pendingPayload: null });
    storage.setItem(workoutDraftKey(1), raw);
    const store = createStore(storage);
    assert.equal(store.getSnapshot().errorKind, "load");
    assert.equal(store.getSnapshot().session, null);
    assert.equal(await store.setSession(session()), false);
    assert.equal(storage.getItem(workoutDraftKey(1)), raw);
  }
});

test("duplicate keys in an updater retain the prior valid state and numeric legacy IDs remain valid", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  const legacy = session();
  legacy.entries[0].sets = [{ id: 1, weight: 50, reps: 10 }, { id: 2, weight: 55, reps: 8 }];
  assert.equal(await store.setSession(legacy), true);
  assert.deepEqual(createStore(storage).getSnapshot().session, legacy);
  const raw = storage.getItem(workoutDraftKey(1));
  assert.equal(await store.setSession(previous => ({ ...previous, entries: [...previous.entries, previous.entries[0]] })), false);
  assert.deepEqual(store.getSnapshot().session, legacy);
  assert.equal(store.getSnapshot().errorKind, "validation");
  assert.equal(await store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({
    ...entry, sets: [...entry.sets, { id: "1", weight: 60, reps: 5 }],
  })) })), false);
  assert.deepEqual(store.getSnapshot().session, legacy);
  assert.equal(store.getSnapshot().errorKind, "validation");
  assert.equal(storage.getItem(workoutDraftKey(1)), raw);
});


test("explicit retry recovers the retained valid state after validation blocked completion", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  let release;
  const held = new Promise(resolve => { release = resolve; });
  const holding = storage.withLock(workoutDraftKey(1), () => held);
  const queued = store.setSession(previous => ({ ...previous, entries: previous.entries.map(entry => ({ ...entry, note: "最後の正常な入力" })) }));
  assert.equal(await store.setSession(previous => ({ ...previous, entries: [null] })), false);
  assert.equal(store.getSnapshot().errorKind, "validation");
  release();
  await holding;
  await queued;
  assert.equal(store.getSnapshot().errorKind, "validation"); // The old pending write cannot dismiss this notice.
  await assert.rejects(store.freezeForSave("2026-01-01T11:00:00+09:00"), /更新できません/);
  assert.equal(await store.retry(), true);
  assert.equal(store.getSnapshot().error, "");
  const payload = await store.freezeForSave("2026-01-01T11:00:00+09:00");
  assert.equal(payload.exercises[0].note, "最後の正常な入力");
  assert.deepEqual(createStore(storage).getSnapshot().pendingPayload, payload);
});

test("a pending explicit retry cannot dismiss a newer invalid updater", async () => {
  const storage = memoryStorage();
  const store = createStore(storage);
  await store.setSession(session());
  assert.equal(await store.setSession(() => undefined), false);
  let release;
  const held = new Promise(resolve => { release = resolve; });
  const holding = storage.withLock(workoutDraftKey(1), () => held);
  const retry = store.retry();
  assert.equal(await store.setSession(() => { throw new Error("new invalid update"); }), false);
  release();
  await holding;
  assert.equal(await retry, true);
  assert.equal(store.getSnapshot().errorKind, "validation");
  assert.match(store.getSnapshot().error, /new invalid update/);
  assert.deepEqual(store.getSnapshot().session, session());
  assert.equal(await store.retry(), true);
  assert.equal(store.getSnapshot().error, "");
});
