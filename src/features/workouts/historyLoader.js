import { restoreHistory } from "./history.js";

/** 後から開始した履歴取得を優先し、古い応答で保存後の一覧を戻さない。 */
export function createHistoryLoader(load, apply) {
  let latestRequest = 0;
  return async signal => {
    const requestId = ++latestRequest;
    const history = restoreHistory(await load(signal));
    if (!signal?.aborted && requestId === latestRequest) apply(history);
    return history;
  };
}
