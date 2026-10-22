import { useEffect, useMemo, useSyncExternalStore } from "react";
import { createWorkoutDraftStore } from "./draft.js";

const EMPTY = { session: null, pendingPayload: null, error: "", errorKind: "", isPersisting: false, hasUnsavedChanges: false };

/** 実施中の記録と再送情報を保持し、書込前のページ終了では警告する。 */
export function useWorkoutDraft(userId) {
  const store = useMemo(() => createWorkoutDraftStore(userId), [userId]);
  const state = useSyncExternalStore(store.subscribe, store.getSnapshot, () => EMPTY);
  useEffect(() => {
    const beforeUnload = event => {
      const current = store.getSnapshot();
      if (current.isPersisting || current.hasUnsavedChanges || ["save", "conflict"].includes(current.errorKind)) {
        event.preventDefault();
        event.returnValue = "";
      }
    };
    window.addEventListener("beforeunload", beforeUnload);
    return () => window.removeEventListener("beforeunload", beforeUnload);
  }, [store]);
  return { ...state, getSnapshot: store.getSnapshot, setSession: store.setSession, freezeForSave: store.freezeForSave,
    complete: store.complete, retry: store.retry, discard: store.discard, reload: store.reload,
    resumeAfterRejection: store.resumeAfterRejection };
}
