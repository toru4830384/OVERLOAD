import { workoutPayload } from "./registration.js";
import { profileWeightError } from "../profile/weightValidation.js";
import { workoutRepetitionsError, workoutWeightError } from "./validation.js";
import { isValidRestTimer } from "./restTimer.js";

const VERSION = 1;
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const validDate = value => typeof value === "string" && /(Z|[+-]\d{2}:\d{2})$/i.test(value) && Number.isFinite(Date.parse(value));
const clone = value => JSON.parse(JSON.stringify(value));
const rawInput = value => typeof value === "string" || (typeof value === "number" && Number.isFinite(value));

export const workoutDraftKey = userId => `overload:workout-draft:v${VERSION}:user:${userId}`;

/** 壊れた下書きを部分的に復元・上書きせず、元データを保持して通知する。 */
function validateRecord(record, userId) {
  if (record?.version !== VERSION || record.userId !== userId || !("session" in record) || !("pendingPayload" in record)) throw new Error("下書きの保存形式を確認できません。");
  const { session, pendingPayload } = record;
  if (session == null) {
    if (pendingPayload != null) throw new Error("下書きの保存内容が一致しません。");
    return;
  }
  if (!UUID.test(session.id) || !validDate(session.date) || profileWeightError(session.bodyWeightKg) || !Array.isArray(session.entries)) {
    throw new Error("開始日時・体重などの下書き情報が不正です。");
  }
  if ((session.restSeconds !== undefined && !rawInput(session.restSeconds))
      || (session.restTimer != null && !isValidRestTimer(session.restTimer))) {
    throw new Error("休憩タイマーの下書き情報が不正です。");
  }
  const exerciseIds = new Set();
  for (const entry of session.entries) {
    if (!entry || !Number.isSafeInteger(entry.exerciseId) || entry.exerciseId < 1 || !Array.isArray(entry.sets)
        || (entry.note != null && (typeof entry.note !== "string" || entry.note.length > 100))
        || (entry.exerciseName !== undefined && typeof entry.exerciseName !== "string")
        || (entry.draftWeight !== undefined && !rawInput(entry.draftWeight))
        || (entry.draftReps !== undefined && !rawInput(entry.draftReps))) {
      throw new Error("種目・メモの下書き情報が不正です。");
    }
    if (exerciseIds.has(entry.exerciseId)) throw new Error("同じ種目が下書き内で重複しています。");
    exerciseIds.add(entry.exerciseId);
    const setIds = new Set();
    for (const set of entry.sets) {
      if (!set || !((typeof set.id === "string" && set.id.length > 0) || Number.isFinite(set.id)) || typeof set.weight !== "number" || workoutWeightError(set.weight) || typeof set.reps !== "number" || workoutRepetitionsError(set.reps)) throw new Error("セットの下書き情報が不正です。");
      const setKey = String(set.id);
      if (setIds.has(setKey)) throw new Error("セットの識別子が下書き内で重複しています。");
      setIds.add(setKey);
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
 * 入力は最新メモリへ同期反映し、永続化だけをタブ間排他内で順に処理する。
 * 各書込は独立した検証済みsnapshotを使い、遅い保存結果で最新入力を戻さない。
 * 完了用payloadは永続化の成功後だけ返す。テストではStorageと排他を差し替える。
 */
export function createWorkoutDraftStore(userId, getStorage = () => globalThis.localStorage,
  withLock = browserExclusiveLock) {
  const key = workoutDraftKey(userId);
  const listeners = new Set();
  let state;
  let observedRaw;
  let tail = Promise.resolve();
  let pendingWrites = 0;
  let editingBarriers = 0;
  let memoryRevision = 0;
  let persistedRevision = 0;
  const emit = next => {
    state = { ...next, isPersisting: pendingWrites > 0, hasUnsavedChanges: memoryRevision !== persistedRevision };
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
  function prepare(session, pendingPayload) {
    if (session === undefined) throw new Error("下書きの更新結果がありません。");
    const record = { version: VERSION, userId, session, pendingPayload };
    validateRecord(record, userId);
    const snapshot = clone(record);
    validateRecord(snapshot, userId);
    return snapshot;
  }
  function publish(record) {
    memoryRevision++;
    emit({ session: record.session, pendingPayload: record.pendingPayload, error: "", errorKind: "" });
    return memoryRevision;
  }
  function validationFailure(cause) {
    emit({ ...state, error: `下書きの入力を更新できませんでした。${cause?.message || "入力内容を確認してください。"}`, errorKind: "validation" });
  }

  /** 必ずwithLock内で実行する。UIは世代の一致する結果だけで保存済みにする。 */
  function persist(record, revision, rethrow = false) {
    try {
      if (state.errorKind === "conflict") throw new DraftConflictError();
      const target = storage();
      if (target.getItem(key) !== observedRaw) throw new DraftConflictError();
      const raw = record.session == null ? null : JSON.stringify(record);
      if (raw == null) target.removeItem(key);
      else target.setItem(key, raw);
      observedRaw = raw;
      persistedRevision = revision;
      if (revision === memoryRevision && state.errorKind !== "validation") emit({ ...state, error: "", errorKind: "" });
      else emit(state);
      return true;
    } catch (cause) {
      if (cause instanceof DraftConflictError) {
        emit({ ...state, error: "別のタブで記録が更新されています。この画面の入力は保持しています。他のタブを確認してから保存済みの記録を読み込んでください。", errorKind: "conflict" });
      } else if (record.session == null && revision === memoryRevision) {
        emit({ ...state, error: "下書きを削除できませんでした。再読み込みで再表示される可能性があるため、削除を再試行してください。", errorKind: "clear" });
      } else if (!["conflict", "validation"].includes(state.errorKind)) {
        emit({ ...state, error: "途中保存に失敗しました。入力はこの画面に保持しています。画面を閉じる前に途中保存を再試行してください。", errorKind: "save" });
      }
      if (rethrow) throw new Error(state.error || "途中保存に失敗しました。", { cause });
      return false;
    }
  }

  /** 書込の順序とタブ間の比較・更新を原子的に維持する。 */
  function enqueue(action, rethrow = false) {
    pendingWrites++;
    emit(state);
    const task = tail.then(() => withLock(key, action)).catch(cause => {
      if (!["conflict", "validation"].includes(state.errorKind)) {
        emit({ ...state, error: cause?.message || "途中保存に失敗しました。", errorKind: "save" });
      }
      if (rethrow) throw cause instanceof Error ? cause : new Error(state.error || "途中保存に失敗しました。");
      return false;
    }).finally(() => {
      pendingWrites--;
      emit(state);
    });
    tail = task.catch(() => {});
    return task;
  }
  function control(action) {
    editingBarriers++;
    return enqueue(action).finally(() => { editingBarriers--; });
  }
  const blocked = () => ["load", "clear", "conflict"].includes(state.errorKind);
  function replaceFromStorage() {
    const loaded = read();
    if (loaded.error) { emit({ ...state, error: loaded.error, errorKind: loaded.errorKind }); return false; }
    memoryRevision++;
    persistedRevision = memoryRevision;
    emit(loaded);
    return true;
  }
  function remove(explicitDiscard) {
    if (editingBarriers || state.errorKind === "conflict" || (!explicitDiscard && state.errorKind === "load")) return Promise.resolve(false);
    const record = prepare(null, null);
    const revision = publish(record);
    return control(() => persist(record, revision));
  }
  state = { ...read(), isPersisting: false, hasUnsavedChanges: false };
  return {
    subscribe(listener) { listeners.add(listener); return () => listeners.delete(listener); },
    getSnapshot: () => state,
    /** updaterは同期的に最新stateへ適用する。blocked時は呼ばず、無効な値は公開しない。 */
    setSession(update) {
      if (blocked() || editingBarriers || state.pendingPayload) return Promise.resolve(false);
      let record;
      try {
        const next = typeof update === "function" ? update(clone(state.session)) : update;
        record = prepare(next, null);
      } catch (cause) {
        validationFailure(cause);
        return Promise.resolve(false);
      }
      // 同値の操作は保存済みsnapshotや既存キューをそのまま使う。
      if (JSON.stringify(record.session) === JSON.stringify(state.session) && !state.hasUnsavedChanges) {
        if (state.errorKind === "validation") emit({ ...state, error: "", errorKind: "" });
        return Promise.resolve(true);
      }
      const revision = publish(record);
      return enqueue(() => persist(record, revision));
    },
    freezeForSave(finishedAt = new Date().toISOString()) {
      if (!state.session || blocked() || editingBarriers || state.errorKind === "validation") {
        return Promise.reject(new Error(state.error || "実施中の記録を確認してください。"));
      }
      let record;
      let revision = memoryRevision;
      try {
        record = prepare(state.session, state.pendingPayload || workoutPayload(state.session, finishedAt, userId));
        if (!state.pendingPayload) revision = publish(record); // キュー待ち中も追加編集を受け付けない。
      } catch (cause) {
        validationFailure(cause);
        return Promise.reject(cause instanceof Error ? cause : new Error(state.error));
      }
      return enqueue(() => {
        if (blocked()) throw new Error(state.error || "実施中の記録を確認してください。");
        // 直前の入力書込を待ち、再送情報の永続化成功後だけPOSTを許可する。
        persist(record, revision, true);
        return clone(record.pendingPayload);
      }, true);
    },
    complete() { return remove(false); },
    retry() {
      if (state.errorKind === "conflict" || editingBarriers) return Promise.resolve(false);
      if (state.errorKind === "load") return control(replaceFromStorage);
      const record = prepare(state.session, state.pendingPayload);
      // 明示的な再試行は、拒否した更新ではなく保持中の正常値を再検証して保存する。
      // 新たな世代にすることで、先行する古い保存結果ではこの操作を完了扱いにしない。
      const revision = state.errorKind === "validation" ? publish(record) : memoryRevision;
      return enqueue(() => persist(record, revision));
    },
    /** 呼出元が未保存入力を破棄してよいと確認した場合だけ使用する。 */
    reload() { return editingBarriers ? Promise.resolve(false) : control(replaceFromStorage); },
    discard() { return remove(true); },
    /** 確定拒否の場合のみ、元の記録を維持して編集を再開する。 */
    resumeAfterRejection(status) {
      if (![400, 404, 422].includes(status) || blocked() || editingBarriers || !state.pendingPayload) return Promise.resolve(false);
      const record = prepare(state.session, null);
      const revision = publish(record);
      return control(() => persist(record, revision));
    },
  };
}
