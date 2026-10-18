/** 重量をDBのDECIMAL(6,2)で保存できる値に限定する。 */
export function workoutWeightError(value) {
  const weight = Number(value);
  if (!/^\d+(\.\d{1,2})?$/.test(String(value)) || !Number.isFinite(weight) || weight < 0.01 || weight > 9999.99) {
    return "重量は0.01kg以上9999.99kg以下、小数第2位まで入力してください";
  }
  return "";
}

/** APIのIntegerとDBのINTで保存できる回数の範囲。 */
export const WORKOUT_REPETITIONS_MIN = 1;
export const WORKOUT_REPETITIONS_MAX = 2147483647;

/**
 * 入力全体を数値へ変換し、正の整数として保存できる場合だけ返す。
 * 小数を切り捨てず、指数表記（1e2など）は元の数値として扱う。
 * @returns {number|null} 検証済みの回数。不正な入力はnull。
 */
export function parseWorkoutRepetitions(value) {
  if (typeof value !== "string" && typeof value !== "number") return null;
  if (typeof value === "string" && value.trim() === "") return null;
  const repetitions = Number(value);
  return Number.isInteger(repetitions)
    && repetitions >= WORKOUT_REPETITIONS_MIN
    && repetitions <= WORKOUT_REPETITIONS_MAX
    ? repetitions
    : null;
}

export function workoutRepetitionsError(value) {
  return parseWorkoutRepetitions(value) === null
    ? `回数は${WORKOUT_REPETITIONS_MIN}以上${WORKOUT_REPETITIONS_MAX}以下の整数で入力してください`
    : "";
}
