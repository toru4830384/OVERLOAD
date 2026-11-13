import { parseWorkoutRepetitions, workoutRepetitionsError, workoutWeightError } from "./validation.js";

const DRAFT_FIELDS = new Set(["draftWeight", "draftReps", "note"]);

/**
 * 配列位置ではなく種目IDを使い、最新セッションの対象だけを更新する。
 * 保存待ち中に対象が削除された場合は、元のセッションをそのまま返す。
 */
function changeEntry(session, exerciseId, change) {
  if (!Array.isArray(session?.entries)) return session;
  const index = session.entries.findIndex(entry => entry?.exerciseId === exerciseId);
  if (index < 0) return session;
  const entry = session.entries[index];
  const next = change(entry);
  if (next === entry) return session;
  return { ...session, entries: session.entries.map((value, position) => position === index ? next : value) };
}

/** 入力途中の文字列を保持する。入力項目以外のプロパティは書き換えない。 */
export function updateWorkoutDraft(session, exerciseId, field, value) {
  if (!DRAFT_FIELDS.has(field) || typeof value !== "string") return session;
  return changeEntry(session, exerciseId, entry => entry[field] === value ? entry : { ...entry, [field]: value });
}

/** 対象が既に削除されていても、別の種目を削除しない。 */
export function removeWorkoutEntry(session, exerciseId) {
  if (!Array.isArray(session?.entries) || !session.entries.some(entry => entry?.exerciseId === exerciseId)) return session;
  return { ...session, entries: session.entries.filter(entry => entry?.exerciseId !== exerciseId) };
}

/** セットも安定したIDで特定し、存在しないセットへの操作は無視する。 */
export function removeWorkoutSet(session, exerciseId, setId) {
  return changeEntry(session, exerciseId, entry => {
    if (!Array.isArray(entry.sets) || !entry.sets.some(set => set?.id === setId)) return entry;
    return { ...entry, sets: entry.sets.filter(set => set?.id !== setId) };
  });
}

/**
 * 最新の入力を共通ルールで検証して追加し、種目ごとのエラーと次セット案内を返す。
 * 数値への変換は検証後に行い、削除済みの対象へセットを生成しない。
 */
export function addWorkoutSet(session, exerciseId, { setId } = {}) {
  const result = { session, added: false, errors: { weight: "", reps: "" }, nextSet: null };
  result.session = changeEntry(session, exerciseId, entry => {
    if (!Array.isArray(entry.sets)) return entry;
    result.errors = { weight: workoutWeightError(entry.draftWeight), reps: workoutRepetitionsError(entry.draftReps) };
    if (result.errors.weight || result.errors.reps) return entry;
    const id = setId ?? globalThis.crypto.randomUUID();
    if (entry.sets.some(set => set?.id === id)) return entry;
    const sets = [...entry.sets, { id, weight: Number(entry.draftWeight), reps: parseWorkoutRepetitions(entry.draftReps) }];
    result.added = true;
    result.nextSet = { exerciseId, exerciseName: entry.exerciseName || "", setNumber: sets.length + 1 };
    return { ...entry, sets };
  });
  return result;
}
