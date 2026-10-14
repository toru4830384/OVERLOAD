import test from "node:test";
import assert from "node:assert/strict";
import { profileWeightError } from "../src/features/profile/weightValidation.js";
test("profile body weight follows required DECIMAL(5,2) contract", () => {
  for (const value of [null, "", 0, "1000", "70.001"]) assert.notEqual(profileWeightError(value), "");
  for (const value of ["0.01", "999.99", "70.00"]) assert.equal(profileWeightError(value), "");
});

import { profileNameError } from "../src/features/profile/nameValidation.js";
test("profile name respects null and 50-character boundaries", () => {
  for (const name of [null, "a".repeat(51)]) assert.notEqual(profileNameError(name), "");
  for (const name of ["", "a".repeat(50)]) assert.equal(profileNameError(name), "");
});

import { workoutWeightError } from "../src/features/workouts/validation.js";
test("set weights are validated before adding to the workout", () => {
  for (const weight of ["", "10000", "50.001", "0.001", "50oops"]) assert.notEqual(workoutWeightError(weight), "");
  for (const weight of ["0.01", "9999.99", "50.00"]) assert.equal(workoutWeightError(weight), "");
});
