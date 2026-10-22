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
