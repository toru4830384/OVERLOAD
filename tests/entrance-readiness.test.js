import test from "node:test";
import assert from "node:assert/strict";
import { waitForReadiness, ReadinessRejectedError, abortableDelay } from "../entrance/readiness.js";

const response = (status, body, type = "application/json") => new Response(body, { status, headers: { "content-type": type } });
function scenario(responses, extra = {}) {
  let time = 0;
  const requests = [];
  return { requests, run: () => waitForReadiness({ url: "https://app.example/api/health", signal: new AbortController().signal,
    now: () => time, maxWaitMs: 30, intervalMs: 10,
    pause: async ms => { time += ms; },
    fetchImpl: async (url, options) => { requests.push({ url, options }); const next = responses.shift(); if (next instanceof Error) throw next; return next; }, ...extra }) };
}
test("readiness requires actual up JSON; cold-start HTML and unavailable replies cannot redirect", async () => {
  const s = scenario([response(200, "<html>starting</html>", "text/html"), response(503, '{"status":"down"}'), response(200, '{"status":"up"}')]);
  await s.run();
  assert.equal(s.requests.length, 3);
  for (const { options } of s.requests) {
    assert.equal(options.method, "GET"); assert.equal(options.credentials, "omit");
    assert.equal(options.cache, "no-store"); assert.equal(options.redirect, "error");
    assert.equal(options.body, undefined);
  }
});
test("persistent offline state stops at deadline instead of keeping the free server awake", async () => {
  const s = scenario(Array.from({ length: 3 }, () => new TypeError("network")));
  await assert.rejects(s.run(), /接続確認を終了/);
  assert.equal(s.requests.length, 3);
});
test("a successful HTTP status with wrong JSON is not readiness", async () => {
  const s = scenario(Array.from({ length: 3 }, () => response(200, '{"status":"down"}')));
  await assert.rejects(s.run(), /準備未完了/);
});
test("configuration rejection stops immediately without retry", async () => {
  const s = scenario([response(403, '{}')]);
  await assert.rejects(s.run(), ReadinessRejectedError);
  assert.equal(s.requests.length, 1);
});
test("navigation cancellation stops the in-flight request and further checks", async () => {
  const controller = new AbortController();
  const reason = new Error("page hidden");
  const s = scenario([], { signal: controller.signal, fetchImpl: async (_, options) => {
    controller.abort(reason); assert.equal(options.signal.aborted, true); throw reason;
  } });
  await assert.rejects(s.run(), error => error === reason);
});
test("success after the total deadline is not accepted", async () => {
  let time = 0;
  const s = scenario([], { now: () => time, fetchImpl: async () => { time = 100; return response(200, '{"status":"up"}'); } });
  await assert.rejects(s.run(), /接続確認を終了/);
});
test("cancel interrupts a scheduled wait", async () => {
  const controller = new AbortController();
  const wait = abortableDelay(60_000, controller.signal);
  controller.abort(new Error("cancel"));
  await assert.rejects(wait, /cancel/);
});
