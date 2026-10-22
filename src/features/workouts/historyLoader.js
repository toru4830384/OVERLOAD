import { restoreHistory } from "./history.js";

/** 履歴と取得状態を最新の要求だけから更新する。古い失敗でも表示を戻さない。 */
export function createHistoryLoader(load, apply, onStatus = () => {}) {
  let latestRequest = 0;
  return async signal => {
    const requestId = ++latestRequest;
    onStatus({ state: "loading", error: "" });
    try {
      const history = restoreHistory(await load(signal));
      if (!signal?.aborted && requestId === latestRequest) {
        apply(history);
        onStatus({ state: "ready", error: "" });
      }
      return history;
    } catch (error) {
      if (!signal?.aborted && requestId === latestRequest) {
        onStatus({ state: "error", error: error.message });
      }
      throw error;
    }
  };
}
