import test from "node:test";
import assert from "node:assert/strict";
import { createAccountApi, ApiError, loginAccount, logoutAccount } from "../src/api/client.js";
import { credentialsError, registrationError, registrationPayload } from "../src/features/auth/validation.js";
import { createWorkoutDraftStore, workoutDraftKey } from "../src/features/workouts/draft.js";
const csrf = () => ({ ok: true, json: async () => ({ token: "fresh-token", headerName: "X-CSRF-TOKEN" }) });
const accepted = data => ({ ok: true, status: 200, json: async () => data });

test("registration enforces shared profile boundaries without inventing default values", () => {
  const valid = { loginId: "user_1", password: "long-enough-password", name: "", gender: "male", age: "0", bodyWeightKg: "0.01" };
  assert.equal(registrationError(valid), "");
  for (const loginId of ["Aaa", "aa", "has space", "x".repeat(33)]) assert.ok(credentialsError({ ...valid, loginId }));
  for (const password of ["x".repeat(11), "x".repeat(129), null]) assert.ok(credentialsError({ ...valid, password }));
  for (const password of ["x".repeat(12), "朝から筋トレを続けるぞ！", "x".repeat(15), "x".repeat(128)]) {
    assert.equal(credentialsError({ ...valid, password }), "");
    assert.equal(registrationError({ ...valid, password }), "");
  }
  for (const patch of [{ age: "" }, { age: "1.5" }, { gender: "" }, { bodyWeightKg: "" }, { bodyWeightKg: "1000" }, { name: "a".repeat(51) }]) assert.ok(registrationError({ ...valid, ...patch }));
  assert.equal(registrationPayload(valid).bodyWeightKg, 0.01);
  assert.equal(registrationPayload(valid).age, 0);
});

test("account API keeps its original identity and sends no client-selected owner", async () => {
  const original = globalThis.fetch, requests = [];
  try {
    globalThis.fetch = async (url, options) => { requests.push({ url, options }); return url.endsWith("/auth/csrf") ? csrf() : accepted([]); };
    const a = createAccountApi("1:namespace-a"), b = createAccountApi("2:namespace-b");
    await b.getProfile();
    await a.saveWorkout({ userId: 2, clientRequestId: "fixed-uuid", finishedAt: "fixed-time", exercises: [] });
    await a.getWorkoutHistory();
    const post = requests.find(r => r.options.method === "POST");
    assert.equal(post.options.headers["X-Overload-Account"], "1:namespace-a");
    assert.equal(post.options.headers["X-CSRF-TOKEN"], "fresh-token");
    assert.equal(post.options.credentials, "include");
    assert.equal(Object.hasOwn(JSON.parse(post.options.body), "userId"), false);
    assert.equal(requests.at(-1).url, "/api/workouts/history");
    assert.equal(requests.at(-1).options.headers["X-Overload-Account"], "1:namespace-a");
  } finally { globalThis.fetch = original; }
});

test("authentication changes and CSRF rejection notify the owning screen without retrying writes", async () => {
  const original = globalThis.fetch;
  try {
    for (const [status, code, shouldClose] of [[401,"AUTH_REQUIRED",true], [409,"AUTH_SESSION_CHANGED",true], [403,"CSRF_INVALID",true], [409,"REQUEST_CONFLICT",false], [403,"ACCESS_DENIED",false]]) {
      let lost = 0, posts = 0;
      globalThis.fetch = async (url, options) => {
        if (url.endsWith("/auth/csrf")) return csrf();
        if (options.method === "POST") posts++;
        return { ok: false, status, json: async () => ({ code, detail: "入力と下書きを保持" }) };
      };
      await assert.rejects(createAccountApi("1:scope", () => lost++).saveWorkout({}), e => e instanceof ApiError && e.status === status && e.code === code);
      assert.equal(lost, shouldClose ? 1 : 0); assert.equal(posts, 1);
    }
  } finally { globalThis.fetch = original; }
});

test("login and logout refresh CSRF and logout binds the account displayed to the user", async () => {
  const original = globalThis.fetch, calls = [];
  try {
    globalThis.fetch = async (url, options) => { calls.push({ url, options }); return url.endsWith("/auth/csrf") ? csrf() : url.endsWith("/logout") ? { ok: true, status: 204 } : accepted({ id: 1 }); };
    await loginAccount({ loginId: "user_a", password: "long-enough-password" });
    assert.equal(await logoutAccount("1:namespace"), null);
    assert.equal(calls.filter(c => c.url.endsWith("/auth/csrf")).length, 2);
    assert.equal(calls.at(-1).options.headers["X-Overload-Account"], "1:namespace");
  } finally { globalThis.fetch = original; }
});

test("new account namespace never adopts an anonymous user-1 draft", () => {
  const items = new Map([[workoutDraftKey(1), "old-anonymous-data"]]);
  const storage = { getItem: key => items.get(key) ?? null, setItem: (k,v) => items.set(k,v), removeItem: key => items.delete(key) };
  const store = createWorkoutDraftStore("1:new-namespace", () => storage, async (_key, action) => action());
  assert.equal(store.getSnapshot().session, null); assert.equal(store.getSnapshot().error, "");
  assert.equal(items.get(workoutDraftKey(1)), "old-anonymous-data");
});

test("unknown network failure is never treated as a confirmed rejection or retried automatically", async () => {
  const original = globalThis.fetch; let writes = 0, lost = 0;
  try {
    globalThis.fetch = async (url) => { if (url.endsWith("/auth/csrf")) return csrf(); writes++; throw new TypeError("Failed to fetch"); };
    await assert.rejects(createAccountApi("1:namespace", () => lost++).saveWorkout({}), e => !(e instanceof ApiError));
    assert.equal(writes, 1); assert.equal(lost, 0);
  } finally { globalThis.fetch = original; }
});

test("request deadline stops a stalled save once and keeps the outcome unknown", async t => {
  const { RequestTimeoutError } = await import("../src/api/client.js");
  const original = globalThis.fetch;
  const controller = new AbortController();
  t.mock.method(AbortSignal, "timeout", ms => { assert.equal(ms, 20_000); return controller.signal; });
  let posts = 0, lost = 0;
  try {
    globalThis.fetch = async (url, options) => {
      if (url.endsWith("/auth/csrf")) return csrf();
      posts++;
      return new Promise((_, reject) => {
        options.signal.addEventListener("abort", () => reject(options.signal.reason), { once: true });
        queueMicrotask(() => controller.abort(new DOMException("deadline", "TimeoutError")));
      });
    };
    await assert.rejects(createAccountApi("1:scope", () => lost++).saveWorkout({}), error => {
      assert.ok(error instanceof RequestTimeoutError);
      assert.ok(!(error instanceof ApiError));
      assert.match(error.message, /保存結果は未確認/);
      return true;
    });
    assert.equal(posts, 1); assert.equal(lost, 0);
  } finally { globalThis.fetch = original; }
});

test("stalled CSRF request expires before any workout POST is sent", async t => {
  const original = globalThis.fetch, controller = new AbortController();
  t.mock.method(AbortSignal, "timeout", () => controller.signal);
  let posts = 0;
  try {
    globalThis.fetch = (url, options) => {
      if (options.method === "POST") posts++;
      return new Promise((_, reject) => {
        options.signal.addEventListener("abort", () => reject(options.signal.reason), { once: true });
        queueMicrotask(() => controller.abort(new DOMException("deadline", "TimeoutError")));
      });
    };
    await assert.rejects(createAccountApi("1:scope").saveWorkout({}), /保存結果は未確認/);
    assert.equal(posts, 0);
  } finally { globalThis.fetch = original; }
});

test("save timeout preserves the persisted draft and frozen UUID, finish time and payload on reload", async t => {
  const original = globalThis.fetch, controller = new AbortController(), values = new Map();
  const storage = { getItem: k => values.get(k) ?? null, setItem: (k,v) => values.set(k,v), removeItem: k => values.delete(k) };
  const restoredStore = () => createWorkoutDraftStore("1:scope", () => storage, async (_key, action) => action());
  const draft = restoredStore();
  const session = { id: "12345678-1234-1234-1234-123456789abc", date: "2026-01-01T10:00:00+09:00", bodyWeightKg: 80,
    entries: [{ exerciseId: 1, exerciseName: "Bench", note: "保持するメモ", draftWeight: "55", draftReps: "8", sets: [{ id: "set-1", weight: 55, reps: 8 }] }] };
  await draft.setSession(session);
  const payload = await draft.freezeForSave("2026-01-01T11:00:00+09:00");
  const persisted = storage.getItem(workoutDraftKey("1:scope"));
  t.mock.method(AbortSignal, "timeout", () => controller.signal);
  let posts = 0;
  try {
    globalThis.fetch = async (url, options) => {
      if (url.endsWith("/auth/csrf")) return csrf();
      posts++;
      return new Promise((_, reject) => {
        options.signal.addEventListener("abort", () => reject(options.signal.reason), { once: true });
        queueMicrotask(() => controller.abort(new DOMException("deadline", "TimeoutError")));
      });
    };
    await assert.rejects(createAccountApi("1:scope").saveWorkout(payload), /保存結果は未確認/);
    assert.equal(posts, 1);
    assert.equal(storage.getItem(workoutDraftKey("1:scope")), persisted);
    assert.deepEqual(restoredStore().getSnapshot().pendingPayload, payload);
    assert.deepEqual(restoredStore().getSnapshot().session, session);
  } finally { globalThis.fetch = original; }
});
