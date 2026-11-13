import test from "node:test";
import assert from "node:assert/strict";
import { ApiError, createAccountApi } from "../src/api/client.js";

const { saveWorkout } = createAccountApi("test:namespace");

test("confirmed API rejection preserves status/code and network failures stay uncertain", async () => {
  const original = globalThis.fetch;
  try {
    for (const status of [400, 404, 409, 422]) {
      globalThis.fetch = async () => ({ ok: false, status, json: async () => ({ detail: "修正してください", code: "WORKOUT_CALORIES_OUT_OF_RANGE" }) });
      await assert.rejects(saveWorkout({}), error => error instanceof ApiError && error.status === status && error.code === "WORKOUT_CALORIES_OUT_OF_RANGE" && error.message === "修正してください");
    }
    globalThis.fetch = async () => { throw new TypeError("Failed to fetch"); };
    await assert.rejects(saveWorkout({}), error => !(error instanceof ApiError) && error.status == null);
  } finally { globalThis.fetch = original; }
});
