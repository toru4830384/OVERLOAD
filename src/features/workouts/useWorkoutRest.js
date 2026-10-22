import { useCallback, useEffect, useRef, useState } from "react";
import {
  DEFAULT_REST_SECONDS, completeRestTimer, createRestTimer, pauseRestTimer,
  remainingRestMilliseconds, restSecondsError, restTimerKey, resumeRestTimer,
} from "./restTimer.js";

/** 休憩の設定・期限・一時停止をワークアウト下書きとともに保存する。 */
export function useWorkoutRest({ session, setSession, locked = false, onFinish }) {
  const sessionId = session?.id;
  const timer = session?.restTimer ?? null;
  const input = String(session?.restSeconds ?? DEFAULT_REST_SECONDS);
  const [clock, setClock] = useState(() => Date.now());
  const [finished, setFinished] = useState(null);
  const generation = useRef({ value: 0 });
  const notified = useRef(new Set());

  // セッション終了・保存待ち・アンマウント後は、以前の期限の通知を無効にする。
  useEffect(() => {
    const lifetime = generation.current;
    lifetime.value++;
    return () => { lifetime.value++; };
  }, [sessionId, locked]);

  useEffect(() => {
    if (!sessionId || !timer || timer.deadlineAt === null || locked) return;
    const token = generation.current.value;
    const notificationKey = `${sessionId}:${restTimerKey(timer)}`;
    let disposed = false;
    let timeout;
    function observe() {
      clearTimeout(timeout);
      if (disposed || token !== generation.current.value) return;
      const now = Date.now();
      setClock(now);
      const remaining = remainingRestMilliseconds(timer, now);
      if (remaining > 0) {
        timeout = setTimeout(observe, Math.min(250, remaining));
        return;
      }
      let completed = false;
      Promise.resolve(setSession(previous => {
        if (token !== generation.current.value || previous?.id !== sessionId) return previous;
        const next = completeRestTimer(previous.restTimer, timer, now);
        completed = next !== previous.restTimer;
        return completed ? { ...previous, restTimer: next } : previous;
      })).then(() => {
        // 終了への状態更新でeffectが片付いても、同じ期限の通知は一度だけ行う。
        if (!completed || token !== generation.current.value || notified.current.has(notificationKey)) return;
        notified.current.add(notificationKey);
        setFinished({ sessionId, nextSet: timer.nextSet });
        onFinish?.();
      }).catch(() => { /* 保存失敗の案内は下書きStoreが表示する。 */ });
    }
    // StrictModeの初回effectが片付く前には、終了処理を実行しない。
    timeout = setTimeout(observe, 0);
    globalThis.document?.addEventListener("visibilitychange", observe);
    globalThis.window?.addEventListener("pageshow", observe);
    return () => {
      disposed = true;
      clearTimeout(timeout);
      globalThis.document?.removeEventListener("visibilitychange", observe);
      globalThis.window?.removeEventListener("pageshow", observe);
    };
  }, [sessionId, timer, locked, setSession, onFinish]);

  useEffect(() => {
    if (!finished) return;
    const timeout = setTimeout(() => setFinished(null), 2500);
    return () => clearTimeout(timeout);
  }, [finished]);

  const setInput = useCallback(value => {
    if (locked || !sessionId) return;
    void setSession(previous => previous?.id === sessionId
      ? { ...previous, restSeconds: String(value) } : previous);
  }, [locked, sessionId, setSession]);

  const start = useCallback(nextSet => {
    if (locked || !sessionId) return false;
    const now = Date.now();
    const next = createRestTimer(input, nextSet, now);
    if (!next) return false;
    setFinished(null);
    setClock(now);
    void setSession(previous => {
      if (previous?.id !== sessionId) return previous;
      if (restTimerKey(previous.restTimer) === restTimerKey(next)
          && previous.restTimer?.remainingMs === next.remainingMs
          && String(previous.restSeconds ?? DEFAULT_REST_SECONDS) === input) return previous;
      generation.current.value++;
      return { ...previous, restSeconds: input, restTimer: next };
    });
    return true;
  }, [input, locked, sessionId, setSession]);

  const pause = useCallback(() => {
    if (locked || !sessionId) return;
    const now = Date.now();
    setClock(now);
    void setSession(previous => {
      if (previous?.id !== sessionId || remainingRestMilliseconds(previous.restTimer, now) <= 0) return previous;
      const next = pauseRestTimer(previous.restTimer, now);
      if (next === previous.restTimer) return previous;
      generation.current.value++;
      return { ...previous, restTimer: next };
    });
  }, [locked, sessionId, setSession]);

  const toggle = useCallback(() => {
    if (locked || !sessionId) return;
    const now = Date.now();
    setClock(now);
    void setSession(previous => {
      if (previous?.id !== sessionId || !previous.restTimer
          || remainingRestMilliseconds(previous.restTimer, now) <= 0) return previous;
      const next = previous.restTimer.deadlineAt === null
        ? resumeRestTimer(previous.restTimer, now) : pauseRestTimer(previous.restTimer, now);
      if (next === previous.restTimer) return previous;
      generation.current.value++;
      return { ...previous, restTimer: next };
    });
  }, [locked, sessionId, setSession]);

  // 完了処理からも呼べる。先にcallbackを止め、クリアの永続化をawaitできるようにする。
  const clear = useCallback(() => {
    generation.current.value++;
    setFinished(null);
    if (!sessionId) return Promise.resolve(true);
    return Promise.resolve(setSession(previous => previous?.id === sessionId && previous.restTimer != null
      ? { ...previous, restTimer: null } : previous));
  }, [sessionId, setSession]);

  const remainingMs = remainingRestMilliseconds(timer, clock);
  return { input, setInput, error: restSecondsError(input),
    remaining: timer == null ? null : Math.ceil(remainingMs / 1000),
    running: !locked && timer?.deadlineAt != null && remainingMs > 0,
    durationSeconds: timer?.durationSeconds ?? 0,
    nextSet: timer?.nextSet ?? (finished != null && finished.sessionId === sessionId ? finished.nextSet : null),
    justFinished: finished?.sessionId === sessionId && finished != null,
    start, pause, toggle, clear };
}
