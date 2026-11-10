import test from "node:test";
import assert from "node:assert/strict";
import { passwordChangeError } from "../src/features/auth/securityValidation.js";
import { googleRegistrationError, googleRegistrationPayload } from "../src/features/auth/validation.js";
import { createAccountApi } from "../src/api/client.js";
test("password change validates confirmation, boundaries and reuse without altering input", () => {
  const valid = { currentPassword: "old-password-123", newPassword: "朝から筋トレを続けるぞ！", confirmation: "朝から筋トレを続けるぞ！" };
  assert.equal(passwordChangeError(valid), "");
  for (const patch of [{ currentPassword: "" }, { newPassword: "x".repeat(11) }, { newPassword: "x".repeat(129) }, { confirmation: "different" }, { newPassword: valid.currentPassword, confirmation: valid.currentPassword }]) assert.ok(passwordChangeError({ ...valid, ...patch }));
});
test("Google registration keeps mandatory profile fields but sends no invented password", () => {
  const input = { loginId: "google_user", name: "本人", gender: "male", age: "30", bodyWeightKg: "65" };
  assert.equal(googleRegistrationError(input), "");
  for (const patch of [{ loginId: "BAD" }, { age: "" }, { bodyWeightKg: "" }, { gender: "" }]) assert.ok(googleRegistrationError({ ...input, ...patch }));
  assert.deepEqual(googleRegistrationPayload({ ...input, password: "must-not-send" }), { loginId: "google_user", name: "本人", gender: "male", age: 30, bodyWeightKg: 65 });
});
test("security writes keep account boundary, CSRF and do not retry credential mutations", async () => {
  const original = globalThis.fetch, calls = [];
  try {
    globalThis.fetch = async (url, options) => {
      calls.push({ url, options });
      if (url.endsWith("/csrf")) return { ok: true, json: async () => ({ token: "csrf", headerName: "X-CSRF-TOKEN" }) };
      return { ok: true, status: 204 };
    };
    await createAccountApi("1:namespace").changePassword({ currentPassword: "old-password-123", newPassword: "new-password-123" });
    const writes = calls.filter(c => c.options.method === "POST"); assert.equal(writes.length, 1);
    assert.equal(writes[0].options.headers["X-Overload-Account"], "1:namespace");assert.equal(writes[0].options.headers["X-CSRF-TOKEN"], "csrf");
  } finally { globalThis.fetch = original; }
});
