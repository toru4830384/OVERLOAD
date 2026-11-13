

// 消費カロリーの概算。METs(運動強度の指標)を使った一般的な計算式:
// 消費カロリー(kcal) = MET × 3.5 × 体重(kg) / 200 × 運動時間(分)
// ウェイトトレーニング(挙上中、一般的な強度)は MET 5.0、セット間の休憩中は MET 2.0(立って休む程度)を採用。
// 1レップあたりの挙上時間は目安として3秒と仮定して算出(あくまで概算値)。
export const WEIGHT_TRAINING_MET = 5.0;

export const REST_MET = 2.0;

export const SECONDS_PER_REP = 3;

export function caloriesForMinutes(met, bodyWeightKg, minutes) {
  if (!bodyWeightKg || minutes <= 0) return 0;
  return ((met * 3.5 * bodyWeightKg) / 200) * minutes;
}

export function estimateSetCalories(reps, bodyWeightKg) {
  if (!bodyWeightKg || !reps) return 0;
  const minutes = (reps * SECONDS_PER_REP) / 60;
  return caloriesForMinutes(WEIGHT_TRAINING_MET, bodyWeightKg, minutes);
}
