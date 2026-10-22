import { useCallback, useEffect, useMemo, useState } from "react";
import { getExercises, getWorkoutHistory } from "../../api/client.js";
import { DEFAULT_EXERCISES } from "../../data/exercises.js";
import { createHistoryLoader } from "./historyLoader.js";

/** 種目と履歴の取得を分離し、一方の障害で他方の成功を捨てない。 */
export function useWorkoutData(userId) {
  const [exercises, setExercises] = useState(DEFAULT_EXERCISES);
  const [sessions, setSessions] = useState([]);
  const [catalogStatus, setCatalogStatus] = useState({ state: "loading", error: "" });
  const [historyStatus, setHistoryStatus] = useState({ state: "loading", error: "" });
  const [revision, setRevision] = useState(0);
  const loadHistory = useMemo(() => createHistoryLoader(
    signal => getWorkoutHistory(userId, signal), setSessions, setHistoryStatus,
  ), [userId]);

  useEffect(() => {
    const controller = new AbortController();
    getExercises(controller.signal).then(catalog => {
      if (controller.signal.aborted) return;
      setExercises(catalog);
      setCatalogStatus({ state: "ready", error: "" });
    }).catch(error => {
      if (!controller.signal.aborted) setCatalogStatus({ state: "error", error: error.message });
    });
    // 履歴のエラー表示はloaderが要求順を確認して更新する。
    loadHistory(controller.signal).catch(() => {});
    return () => controller.abort();
  }, [loadHistory, revision]);

  const retry = useCallback(() => setRevision(value => value + 1), []);
  return {
    exercises, sessions, loadHistory, retry,
    catalogReady: catalogStatus.state === "ready",
    ready: catalogStatus.state === "ready" && historyStatus.state === "ready",
    error: [catalogStatus.error, historyStatus.error].filter(Boolean).join(" / "),
  };
}
