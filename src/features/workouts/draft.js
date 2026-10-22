import { workoutPayload } from "./registration.js";
import { profileWeightError } from "../profile/weightValidation.js";
import { workoutRepetitionsError, workoutWeightError } from "./validation.js";

const VERSION = 1;
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const validDate = value => typeof value === "string" && /(Z|[+-]\d{2}:\d{2})$/i.test(value) && Number.isFinite(Date.parse(value));
const clone = value => JSON.parse(JSON.stringify(value));

export const workoutDraftKey = userId => `overload:workout-draft:v${VERSION}:user:${userId}`;

/** 壊れた下書きを部分的に復元・上書きせず、元データを保持して通知する。 */
function validateRecord(record, userId) {
  if (record?.version !== VERSION || record.userId !== userId) throw new Error("下書きの保存形式を確認できません。");
  const { session, pendingPayload } = record;
  if (session == null) {
    if (pendingPayload != null) throw new Error("下書きの保存内容が一致しません。");
    return;
  }
  if (!UUID.test(session.id) || !validDate(session.date) || profileWeightError(session.bodyWeightKg) || !Array.isArray(session.entries)) {
    throw new Error("開始日時・体重などの下書き情報が不正です。");
  }
  for (const entry of session.entries) {
    if (!entry || !Number.isSafeInteger(entry.exerciseId) || entry.exerciseId < 1 || !Array.isArray(entry.sets)
        || (entry.note != null && (typeof entry.note !== "string" || entry.note.length > 100))) {
      throw new Error("種目・メモの下書き情報が不正です。");
    }
    for (const set of entry.sets) {
      if (!set || !((typeof set.id === "string" && set.id.length > 0) || Number.isFinite(set.id)) || typeof set.weight !== "number" || workoutWeightError(set.weight) || typeof set.reps !== "number" || workoutRepetitionsError(set.reps)) throw new Error("セットの下書き情報が不正です。");
    }
  }
  if (pendingPayload != null) {
    if (!validDate(pendingPayload.finishedAt) || Date.parse(pendingPayload.finishedAt) < Date.parse(session.date)) {
      throw new Error("終了日時の下書き情報が不正です。");
    }
    const expected = workoutPayload(session, pendingPayload.finishedAt, userId);
    // JSONのキー順に依存せず、再送する公開項目が実施中の記録と一致するか確認する。
    if (Object.keys(expected).some(key => JSON.stringify(expected[key]) !== JSON.stringify(pendingPayload[key]))) {
      throw new Error("再送する保存内容が下書きと一致しません。");
    }
  }
}

/** 同じ保存キーへの比較・書込・削除を、ブラウザのタブ間で原子的に実行する。 */
function browserExclusiveLock(name, action) {
  if (!globalThis.navigator?.locks?.request) {
    throw new Error("このブラウザでは安全な途中保存を利用できません。対応ブラウザで開いてください。");
  }
  return globalThis.navigator.locks.request(name, { mode: "exclusive" }, action);
}

class DraftConflictError extends Error { }

/**
 * タブ間の排他内で保存前の値を比較し、他タブの新しい下書きを上書きしない。
 * 同一タブの操作は順に処理する。呼出元は完了用payloadと削除だけでなく、
 * 必要な場合は各操作のPromiseを待てる。テストではStorageと排他処理を差し替える。
 */
export function createWorkoutDraftStore(userId, getStorage = () => globalThis.localStorage,
  withLock = browserExclusiveLock) {
  const key = workoutDraftKey(userId);
  const listeners = new Set();
  let state;
  let observedRaw;
  let tail = Promise.resolve();
  let pendingWrites = 0;
  const emit = next => {
    state = { ...next, isPersisting: pendingWrites > 0 };
    listeners.forEach(listener => listener());
  };
  const storage = () => {
    const value = getStorage();
    if (!value) throw new Error("このブラウザでは途中保存を利用できません。");
    return value;
  };
  function read() {
    try {
      const raw = storage().getItem(key);
      observedRaw = raw;
      const record = raw == null ? { version: VERSION, userId, session: null, pendingPayload: null } : JSON.parse(raw);
      validateRecord(record, userId);
      return { session: record.session, pendingPayload: record.pendingPayload, error: "", errorKind: "" };
    } catch {
      return { session: null, pendingPayload: null, error: "途中保存した記録を読み込めませんでした。元の下書きは保持しています。", errorKind: "load" };
    }
  }

  /** 比較と更新は必ずwithLockの内側で行う。エラー時も入力はメモリへ残す。 */
  function save(session, pendingPayload, rethrow = false) {
    try {
      const target = storage();
      if (target.getItem(key) !== observedRaw) throw new DraftConflictError();
      const record = clone({ version: VERSION, userId, session, pendingPayload });
      validateRecord(record, userId);
      const raw = session == null ? null : JSON.stringify(record);
      if (raw == null) target.removeItem(key);
      else target.setItem(key, raw);
      observedRaw = raw;
      emit({ session: record.session, pendingPayload: record.pendingPayload, error: "", errorKind: "" });
      return true;
    } catch (cause) {
      if (cause instanceof DraftConflictError) {
        emit({ ...(session == null ? state : { session, pendingPayload }), error: "別のタブで記録が更新されています。この画面の入力は保持しています。他のタブを確認してから保存済みの記録を読み込んでください。", errorKind: "conflict" });
      } else if (session == null) {
        emit({ session: null, pendingPayload: null, error: "下書きを削除できませんでした。再読み込みで再表示される可能性があるため、削除を再試行してください。", errorKind: "clear" });
      } else {
        emit({ session, pendingPayload, error: "途中保存に失敗しました。入力はこの画面に保持しています。画面を閉じる前に途中保存を再試行してください。", errorKind: "save" });
      }
      if (rethrow) throw new Error(state.error, { cause });
      return false;
    }
  }

  /** 同じタブの入力順と、タブ間の排他の両方を維持する。 */
  function enqueue(action, rethrow = false) {
    pendingWrites++;
    emit(state);
    const task = tail.then(() => withLock(key, action)).catch(cause => {
      if (!state.error) emit({ ...state, error: cause.message || "途中保存に失敗しました。", errorKind: "save" });
      if (rethrow) throw cause;
      return false;
    }).finally(() => {
      pendingWrites--;
      emit(state);
    });
    tail = task.catch(() => {});
    return task;
  }
  const blocked = () => ["load", "clear", "conflict"].includes(state.errorKind);
  state = { ...read(), isPersisting: false };
  return {
    subscribe(listener) { listeners.add(listener); return () => listeners.delete(listener); },
    getSnapshot: () => state,
    setSession(update) {
      return enqueue(() => {
        if (blocked() || state.pendingPayload) return false;
        return save(typeof update === "function" ? update(state.session) : update, null);
      });
    },
    freezeForSave(finishedAt = new Date().toISOString()) {
      return enqueue(() => {
        if (!state.session || blocked()) throw new Error(state.error || "実施中の記録を確認してください。");
        const payload = state.pendingPayload || workoutPayload(state.session, finishedAt, userId);
        // 再送情報の永続化に失敗した場合はPOSTを開始させない。
        save(state.session, payload, true);
        return clone(payload);
      }, true);
    },
    complete() { return enqueue(() => save(null, null)); },
    retry() {
      return enqueue(() => {
        if (state.errorKind === "conflict") return false;
        if (state.errorKind === "load") { emit(read()); return !state.error; }
        return save(state.session, state.pendingPayload);
      });
    },
    /** 呼出元が未保存の入力を破棄してよいと確認した場合だけ使用する。 */
    reload() { return enqueue(() => { emit(read()); return !state.error; }); },
    discard() { return enqueue(() => save(null, null)); },
    /** 書込前に確定拒否された場合のみ、元の記録を維持したまま編集を再開する。 */
    resumeAfterRejection(status) {
      return enqueue(() => {
        if (![400, 404, 422].includes(status) || blocked() || !state.pendingPayload) return false;
        return save(state.session, null);
      });
    },
  };
}
