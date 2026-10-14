import test from "node:test";
import assert from "node:assert/strict";
import { profileWeightError } from "../src/features/profile/weightValidation.js";
test("profile body weight follows required DECIMAL(5,2) contract", () => {
  for (const value of [null, "", 0, "1000", "70.001"]) assert.notEqual(profileWeightError(value), "");
  for (const value of ["0.01", "999.99", "70.00"]) assert.equal(profileWeightError(value), "");
});
