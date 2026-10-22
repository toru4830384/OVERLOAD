import test from "node:test";
import assert from "node:assert/strict";
import {
  parseWorkoutRepetitions,
  workoutRepetitionsError,
  WORKOUT_REPETITIONS_MIN,
  WORKOUT_REPETITIONS_MAX,
} from "../src/features/workouts/validation.js";
import { workoutPayload } from "../src/features/workouts/registration.js";

test("repetitions accept positive integers through the API/DB INT upper boundary", () => {
  for (const [input, expected] of [
    ["1", WORKOUT_REPETITIONS_MIN],
    [1, 1],
    ["10", 10],
    ["10.0", 10],
    ["2147483647", WORKOUT_REPETITIONS_MAX],
    [2147483647, WORKOUT_REPETITIONS_MAX],
  ]) {
    assert.equal(parseWorkoutRepetitions(input), expected);
    assert.equal(workoutRepetitionsError(input), "");
  }
});

test("repetitions reject decimals, partial numbers, absent values and INT overflow", () => {
  for (const input of [
    "10.9", 10.9, "0.1", "1e-2", "0", 0, "-1", -1,
    "2147483648", 2147483648, "1e100", "Infinity", Infinity, NaN,
    "10reps", "1e", "", " ", null, undefined, true, [], [10], {},
  ]) {
    assert.equal(parseWorkoutRepetitions(input), null, String(input));
    assert.notEqual(workoutRepetitionsError(input), "", String(input));
  }
});

test("scientific notation is converted as a whole value instead of a prefix", () => {
  for (const [input, expected] of [["1e2", 100], ["1E+2", 100], ["1e0", 1]]) {
    assert.equal(parseWorkoutRepetitions(input), expected);
    assert.equal(workoutRepetitionsError(input), "");
  }
});

test("a validated scientific-notation count reaches the workout request unchanged", () => {
  const reps = parseWorkoutRepetitions("1e2");
  const session = {
    id: "12345678-1234-1234-1234-123456789abc",
    date: "2026-01-01T10:00:00+09:00",
    bodyWeightKg: 70,
    entries: [{ exerciseId: 1, sets: [{ weight: 50, reps }] }],
  };
  const payload = workoutPayload(session, "2026-01-01T11:00:00+09:00");
  assert.equal(payload.exercises[0].sets[0].reps, 100);
  assert.equal(JSON.parse(JSON.stringify(payload)).exercises[0].sets[0].reps, 100);
});
