/** 準備確認は公開GETだけを対象とし、期限を過ぎたら停止する。 */
export async function waitForReadiness({ url, signal, fetchImpl = fetch,
  now = () => performance.now(), pause = abortableDelay, maxWaitMs = 120_000,
  requestTimeoutMs = 15_000, intervalMs = 5_000 }) {
  const deadline = now() + maxWaitMs;
  let lastResult = "未接続";
  while (now() < deadline) {
    signal.throwIfAborted();
    const attempt = new AbortController();
    const abort = () => attempt.abort(signal.reason);
    signal.addEventListener("abort", abort, { once: true });
    const timeout = setTimeout(() => attempt.abort(), Math.min(requestTimeoutMs, deadline - now()));
    try {
      const response = await fetchImpl(url, { method: "GET", credentials: "omit",
        cache: "no-store", redirect: "error", signal: attempt.signal });
      if (response.status === 200 && response.headers.get("content-type")?.includes("application/json")) {
        const body = await response.json();
        if (body.status === "up" && now() < deadline && !signal.aborted) return;
        lastResult = "サーバーの準備未完了";
      } else if ([200, 502, 503, 504].includes(response.status)) {
        // 起動中のHTMLや中継エラーを、準備完了のJSONとして扱わない。
        lastResult = `準備完了の応答なし（HTTP ${response.status}）`;
      } else {
        throw new ReadinessRejectedError(`接続設定を確認できませんでした（HTTP ${response.status}）。`);
      }
    } catch (error) {
      if (signal.aborted) throw signal.reason;
      if (error instanceof ReadinessRejectedError) throw error;
      lastResult = "通信・接続先の状態を確認できません";
    } finally {
      clearTimeout(timeout);
      signal.removeEventListener("abort", abort);
    }
    const remaining = deadline - now();
    if (remaining > 0) await pause(Math.min(intervalMs, remaining), signal);
  }
  throw new Error(`接続確認を終了しました。${lastResult}。時間を置いて再度お試しください。`);
}

export class ReadinessRejectedError extends Error {}

/** 中断時は待機も解除し、バックグラウンドで接続確認を継続しない。 */
export function abortableDelay(ms, signal) {
  return new Promise((resolve, reject) => {
    signal.throwIfAborted();
    const abort = () => { clearTimeout(timer); reject(signal.reason); };
    const timer = setTimeout(() => { signal.removeEventListener("abort", abort); resolve(); }, ms);
    signal.addEventListener("abort", abort, { once: true });
  });
}
