/** 1回の休憩は1秒〜1日。setTimeoutの約24.8日上限より十分小さく保つ。 */
export const REST_SECONDS_MIN = 1;
export const REST_SECONDS_MAX = 86400;
export const DEFAULT_REST_SECONDS = "90";

const MAX_DATE_MILLISECONDS = 8640000000000000;
const validTimestamp = value => Number.isSafeInteger(value) && Math.abs(value) <= MAX_DATE_MILLISECONDS;
const positiveInteger = value => Number.isSafeInteger(value) && value > 0;
const validNextSet = value => value != null && positiveInteger(value.exerciseId)
  && positiveInteger(value.setNumber) && typeof value.exerciseName === "string";

/** 入力全体が保存可能な秒数か調べる。入力途中の文字列自体は変更しない。 */
export function parseRestSeconds(value) {
  if (!["string", "number"].includes(typeof value) || String(value).trim() === "") return null;
  const seconds = Number(value);
  return Number.isInteger(seconds) && seconds >= REST_SECONDS_MIN && seconds <= REST_SECONDS_MAX ? seconds : null;
}

export function restSecondsError(value) {
  return parseRestSeconds(value) == null
    ? `休憩時間は${REST_SECONDS_MIN}〜${REST_SECONDS_MAX}秒の整数で入力してください。`
    : "";
}

/** 下書き復元時も、時間・残り時間・次セットの形式を一つの定義で確認する。 */
export function isValidRestTimer(timer) {
  return timer != null && parseRestSeconds(timer.durationSeconds) === timer.durationSeconds
    && typeof timer.durationSeconds === "number"
    && Number.isFinite(timer.remainingMs) && timer.remainingMs >= 0
    && timer.remainingMs <= timer.durationSeconds * 1000
    && (timer.deadlineAt === null || validTimestamp(timer.deadlineAt))
    && validNextSet(timer.nextSet);
}

/** 実時間から算出するため、バックグラウンドでtickが遅れても休憩を引き延ばさない。 */
export function remainingRestMilliseconds(timer, now = Date.now()) {
  if (!isValidRestTimer(timer) || !Number.isFinite(now)) return 0;
  const remaining = timer.deadlineAt === null ? timer.remainingMs : timer.deadlineAt - now;
  return Math.max(0, Math.min(timer.durationSeconds * 1000, remaining));
}

export function createRestTimer(input, nextSet, now = Date.now()) {
  const durationSeconds = parseRestSeconds(input);
  if (durationSeconds == null || !validNextSet(nextSet) || !validTimestamp(now)
      || !validTimestamp(now + durationSeconds * 1000)) return null;
  return { durationSeconds, remainingMs: durationSeconds * 1000,
    deadlineAt: now + durationSeconds * 1000, nextSet: { ...nextSet } };
}

export function pauseRestTimer(timer, now = Date.now()) {
  if (!isValidRestTimer(timer) || timer.deadlineAt === null) return timer;
  return { ...timer, remainingMs: remainingRestMilliseconds(timer, now), deadlineAt: null };
}

/** 終了済み0秒は再開しない。再開前の待機時間を休憩へ足さない。 */
export function resumeRestTimer(timer, now = Date.now()) {
  if (!isValidRestTimer(timer) || timer.deadlineAt !== null || timer.remainingMs <= 0 || !validTimestamp(now)) return timer;
  const deadlineAt = now + Math.ceil(timer.remainingMs);
  return validTimestamp(deadlineAt) ? { ...timer, deadlineAt } : timer;
}

/** 置き換えられたタイマーを、古いcallbackが終了させないための識別値。 */
export function restTimerKey(timer) {
  if (!isValidRestTimer(timer)) return null;
  return JSON.stringify([timer.deadlineAt, timer.durationSeconds, timer.nextSet.exerciseId,
    timer.nextSet.setNumber, timer.nextSet.exerciseName]);
}

/** 現行の期限に一致する場合だけ終了する。クリア済み・一時停止中なら変更しない。 */
export function completeRestTimer(current, expected, now = Date.now()) {
  if (!isValidRestTimer(current) || current.deadlineAt === null
      || restTimerKey(current) !== restTimerKey(expected) || remainingRestMilliseconds(current, now) > 0) return current;
  return { ...current, remainingMs: 0, deadlineAt: null };
}

export function formatRestTime(seconds) {
  const whole = Math.max(0, Math.ceil(Number.isFinite(seconds) ? seconds : 0));
  return `${Math.floor(whole / 60)}:${String(whole % 60).padStart(2, "0")}`;
}
