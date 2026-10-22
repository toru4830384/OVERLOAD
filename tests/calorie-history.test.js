import test from "node:test";
import assert from "node:assert/strict";
import { getWorkoutHistory } from "../src/api/client.js";
import { restoreHistory, summarizeCalories } from "../src/features/workouts/history.js";
import { workoutPayload } from "../src/features/workouts/registration.js";

const canonicalRow = {
  sessionId: 1,
  startedAt: "2026-01-15T10:00:00+09:00",
  finishedAt: "2026-01-15T11:00:00+09:00",
  bodyWeightKg: "80.00",
  totalCalories: "170.10",
  exerciseId: 1,
  note: "休憩を含む60分",
  sets: [{ weightKg: 50, reps: 10 }],
};

test("80kg/60min/10-rep snapshot survives API reload and profile changes", async () => {
  const profile = { bodyWeightKg: 80 };
  const session = {
    id: "12345678-1234-1234-1234-123456789abc",
    date: canonicalRow.startedAt,
    bodyWeightKg: profile.bodyWeightKg,
    entries: [{ exerciseId: 1, note: canonicalRow.note, sets: [{ weight: 50, reps: 10 }] }],
  };
  const payload = workoutPayload(session, canonicalRow.finishedAt);
  profile.bodyWeightKg = 90;
  assert.equal(payload.bodyWeightKg, 80);
  assert.equal(payload.finishedAt, canonicalRow.finishedAt);
  const originalFetch = globalThis.fetch;
  try {
    globalThis.fetch = async () => ({ ok: true, json: async () => structuredClone([canonicalRow]) });
    // 保存直後とページを再読込した際に同じ履歴APIから確定値を復元する。
    const immediatelyAfterSave = restoreHistory(await getWorkoutHistory(1));
    const afterReload = restoreHistory(await getWorkoutHistory(1));
    assert.deepEqual(afterReload, immediatelyAfterSave);
    assert.equal(afterReload[0].bodyWeightKg, 80);
    assert.equal(afterReload[0].totalCalories, 170.1);
    assert.equal(afterReload[0].finishedAt, canonicalRow.finishedAt);
    assert.equal(afterReload[0].entries[0].note, canonicalRow.note);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("day and month totals count saved calories once per session after reload", () => {
  const rows = [canonicalRow, { ...canonicalRow, exerciseId: 2 }, {
    ...canonicalRow, sessionId: 2, startedAt: "2026-01-16T10:00:00+09:00", totalCalories: "34.02",
  }];
  const beforeReload = restoreHistory(rows);
  const afterReload = restoreHistory(JSON.parse(JSON.stringify(rows)));
  const firstDay = session => new Date(session.date).toDateString() === new Date(canonicalRow.startedAt).toDateString();
  const january = session => new Date(session.date).getMonth() === 0;
  assert.equal(beforeReload.find(session => session.id === 1).entries.length, 2);
  assert.deepEqual(summarizeCalories(afterReload.filter(firstDay)), { totalCalories: 170.1, hasUnknownCalories: false });
  assert.deepEqual(summarizeCalories(afterReload.filter(january)), { totalCalories: 204.12, hasUnknownCalories: false });
  assert.deepEqual(summarizeCalories(afterReload.filter(firstDay)), summarizeCalories(beforeReload.filter(firstDay)));
  assert.deepEqual(summarizeCalories(afterReload.filter(january)), summarizeCalories(beforeReload.filter(january)));
});

test("old unknown calories stay unknown in both daily and monthly totals", () => {
  const old = { ...canonicalRow, sessionId: 2, bodyWeightKg: null, totalCalories: null, finishedAt: null };
  const restored = restoreHistory([old]);
  assert.equal(restored[0].totalCalories, null);
  assert.equal(restored[0].bodyWeightKg, null);
  assert.deepEqual(summarizeCalories(restored), { totalCalories: null, hasUnknownCalories: true });
  assert.deepEqual(summarizeCalories(restoreHistory([canonicalRow, old])), { totalCalories: 170.1, hasUnknownCalories: true });
  assert.deepEqual(summarizeCalories([]), { totalCalories: 0, hasUnknownCalories: false });
});

test("calorie totals preserve stored hundredths without floating point drift", () => {
  assert.deepEqual(summarizeCalories([{ totalCalories: 0.1 }, { totalCalories: 0.2 }]), {
    totalCalories: 0.3, hasUnknownCalories: false,
  });
  assert.deepEqual(summarizeCalories([{ totalCalories: 0 }]), { totalCalories: 0, hasUnknownCalories: false });
});

test("history keeps API exercise names independently of catalog availability", () => {
  const rows = [{ ...canonicalRow, exerciseId: 176, exerciseName: "種目一覧未取得の種目" }, {
    ...canonicalRow, exerciseId: 177, exerciseName: null,
  }];
  const restored = restoreHistory(JSON.parse(JSON.stringify(rows)));
  assert.equal(restored[0].entries[0].exerciseName, "種目一覧未取得の種目");
  assert.equal(restored[0].entries[1].exerciseName, "");
  assert.equal(restored[0].entries[1].exerciseId, 177);
  assert.equal(restored[0].totalCalories, 170.1);
  assert.equal(restored[0].entries.length, 2);
});
