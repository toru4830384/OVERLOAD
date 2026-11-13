import test from "node:test";
import assert from "node:assert/strict";
import { addWorkoutSet, removeWorkoutEntry, removeWorkoutSet, updateWorkoutDraft } from "../src/features/workouts/sessionActions.js";
import { workoutPayload } from "../src/features/workouts/registration.js";

function workout() {
  return {
    id: "12345678-1234-1234-1234-123456789abc",
    date: "2026-10-22T10:00:00+09:00",
    bodyWeightKg: 80,
    entries: [
      { exerciseId: 101, exerciseName: "ベンチプレス", note: "胸", draftWeight: "50", draftReps: "10", sets: [{ id: "first", weight: 40, reps: 8 }] },
      { exerciseId: 202, exerciseName: "スクワット", note: "脚", draftWeight: "80", draftReps: "12", sets: [{ id: "second", weight: 70, reps: 8 }] },
    ],
  };
}

test("deleting the first exercise then editing another uses its stable id", () => {
  const original = workout();
  const remaining = removeWorkoutEntry(original, 101);
  const changed = updateWorkoutDraft(remaining, 202, "note", "削除後も脚のメモ");
  assert.equal(changed.entries.length, 1);
  assert.equal(changed.entries[0].exerciseId, 202);
  assert.equal(changed.entries[0].note, "削除後も脚のメモ");
  assert.equal(original.entries[1].note, "脚");
  assert.equal(changed.entries[0].sets, original.entries[1].sets);
});

test("stale edit, set-add, set-remove and exercise-remove events are no-ops", () => {
  const remaining = removeWorkoutEntry(workout(), 101);
  assert.equal(updateWorkoutDraft(remaining, 101, "note", "削除済み"), remaining);
  assert.equal(removeWorkoutSet(remaining, 101, "first"), remaining);
  assert.equal(removeWorkoutEntry(remaining, 101), remaining);
  const result = addWorkoutSet(remaining, 101, { setId: "stale" });
  assert.equal(result.session, remaining);
  assert.equal(result.added, false);
  assert.equal(result.nextSet, null);
  assert.deepEqual(result.errors, { weight: "", reps: "" });
  assert.deepEqual(remaining.entries.map(entry => entry.exerciseId), [202]);
});

test("input and set mutations do not change another exercise or the original snapshot", () => {
  const original = workout();
  const edited = updateWorkoutDraft(original, 202, "draftReps", "1e2");
  const result = addWorkoutSet(edited, 202, { setId: "new" });
  assert.equal(result.added, true);
  assert.equal(result.session.entries[0], original.entries[0]);
  assert.deepEqual(result.session.entries[1].sets[1], { id: "new", weight: 80, reps: 100 });
  assert.deepEqual(result.nextSet, { exerciseId: 202, exerciseName: "スクワット", setNumber: 3 });
  assert.equal(original.entries[1].sets.length, 1);
  const removed = removeWorkoutSet(result.session, 202, "second");
  assert.deepEqual(removed.entries[1].sets.map(set => set.id), ["new"]);
  assert.equal(removed.entries[0], original.entries[0]);
  assert.equal(removeWorkoutSet(removed, 202, "second"), removed);
  assert.equal(removeWorkoutSet(removed, 101, "new"), removed);
  assert.equal(workoutPayload(result.session).exercises[1].sets[1].reps, 100);
});

test("invalid set input reports only the requested exercise and never truncates reps", () => {
  const original = workout();
  const invalid = updateWorkoutDraft(updateWorkoutDraft(original, 101, "draftWeight", ""), 101, "draftReps", "10.9");
  const rejected = addWorkoutSet(invalid, 101, { setId: "invalid" });
  assert.equal(rejected.session, invalid);
  assert.equal(rejected.added, false);
  assert.notEqual(rejected.errors.weight, "");
  assert.notEqual(rejected.errors.reps, "");
  assert.equal(invalid.entries[0].draftReps, "10.9");
  assert.equal(invalid.entries[0].sets.length, 1);
  const other = addWorkoutSet(invalid, 202, { setId: "valid-other" });
  assert.equal(other.added, true);
  assert.deepEqual(other.errors, { weight: "", reps: "" });
  assert.notEqual(rejected.errors.reps, "");
  assert.equal(other.session.entries[0], invalid.entries[0]);
});

test("only supported raw-string draft fields are updated", () => {
  const original = workout();
  assert.equal(updateWorkoutDraft(original, 101, "exerciseId", "202"), original);
  assert.equal(updateWorkoutDraft(original, 101, "sets", "invalid"), original);
  assert.equal(updateWorkoutDraft(original, 101, "draftReps", 10), original);
  assert.equal(updateWorkoutDraft(original, 101, "note", "胸"), original);
  assert.equal(addWorkoutSet(original, 101, { setId: "first" }).session, original);
});

test("events queued after workout completion do not recreate a session", () => {
  assert.equal(updateWorkoutDraft(null, 101, "draftWeight", "50"), null);
  assert.equal(removeWorkoutEntry(null, 101), null);
  assert.equal(removeWorkoutSet(null, 101, "first"), null);
  assert.deepEqual(addWorkoutSet(null, 101, { setId: "new" }), { session: null, added: false, errors: { weight: "", reps: "" }, nextSet: null });
});
