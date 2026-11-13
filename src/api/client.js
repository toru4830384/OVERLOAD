const API_BASE = import.meta.env?.VITE_API_BASE_URL || "/api";

/** 確定したHTTP拒否と、結果が不明な通信失敗を区別する。 */
export class ApiError extends Error {
  constructor(message, status, code) { super(message); this.name = "ApiError"; this.status = status; this.code = code; }
}

/** タイムアウトはサーバーでの保存結果を断定できない通信エラーとして扱う。 */
export class RequestTimeoutError extends Error {
  constructor(cause) {
    super("通信の応答が20秒以内に届きませんでした。保存結果は未確認です。下書きを保持しています。履歴を確認してください。", { cause });
    this.name = "RequestTimeoutError";
  }
}

/** 応答を解釈し、サーバー内部の例外やHTMLをそのまま表示しない。 */
async function readResponse(response) {
  if (!response.ok) {
    let message = "通信に失敗しました。もう一度お試しください。";
    let code;
    try { const problem = await response.json(); message = problem.detail || message; code = problem.code; }
    catch { /* JSON以外でもHTTPステータスを保持する。 */ }
    throw new ApiError(message, response.status, code);
  }
  if (response.status === 204) return null;
  return response.json();
}

/** トークンは認証の前後・別タブの切替で変わるため、変更操作ごとに取得する。 */
async function request(path, { accountKey, onSessionLost, ...options } = {}) {
  const deadline = AbortSignal.timeout(20_000);
  const signal = options.signal ? AbortSignal.any([options.signal, deadline]) : deadline;
  const headers = { ...options.headers };
  if (accountKey) headers["X-Overload-Account"] = accountKey;
  try {
    if (options.method && !["GET", "HEAD", "OPTIONS"].includes(options.method)) {
      const csrf = await readResponse(await fetch(`${API_BASE}/auth/csrf`, { credentials: "include", cache: "no-store", signal }));
      headers[csrf.headerName] = csrf.token;
    }
    const data = await readResponse(await fetch(`${API_BASE}${path}`, { ...options, signal, credentials: "include", cache: "no-store", headers }));
    return data;
  } catch (error) {
    if (error instanceof ApiError && (error.status === 401 || error.code === "AUTH_SESSION_CHANGED" || error.code === "CSRF_INVALID")) onSessionLost?.();
    if (deadline.aborted && !options.signal?.aborted) throw new RequestTimeoutError(error);
    throw error;
  }
}
const json = body => ({ method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });
export const getAuthenticatedUser = signal => request("/auth/me", { signal });
export const registerAccount = input => request("/auth/register", json(input));
export const loginAccount = input => request("/auth/login", json(input));
export const getGoogleConfiguration = () => request("/auth/google/config");
export const beginGoogleLogin = () => request("/auth/google/challenge", { method: "POST" });
export const googleLogin = credential => request("/auth/google/login", json({ credential }));
export const registerGoogleAccount = input => request("/auth/google/register", json(input));
export const logoutAccount = accountKey => request("/auth/logout", { method: "POST", accountKey });

/** インスタンスに本人キーを固定し、遅い保存キューが次のアカウントへ向かうことを防ぐ。 */
export function createAccountApi(accountKey, onSessionLost) {
  if (typeof accountKey !== "string" || !accountKey) throw new Error("ログイン情報が必要です。");
  const own = (path, options = {}) => request(path, { ...options, accountKey, onSessionLost });
  return Object.freeze({
    getSecurityStatus: () => own("/auth/security"),
    changePassword: input => own("/auth/password", json(input)),
    beginGoogleLink: () => own("/auth/google/link/challenge", { method: "POST" }),
    unlinkGoogle: () => own("/auth/google/unlink", { method: "POST" }),
    linkGoogle: input => own("/auth/google/link", json(input)),
    getExercises: signal => own("/exercises", { signal }),
    getProfile: signal => own("/me", { signal }),
    getWorkoutHistory: signal => own("/workouts/history", { signal }),
    saveProfile: profile => own("/me", { ...json(profile), method: "PUT" }),
    // 下書き形式に旧userIdが残っていても、HTTPへ所有者を送信しない。
    saveWorkout: workout => {
      const { clientRequestId, startedAt, finishedAt, bodyWeightKg, exercises } = workout;
      return own("/workouts", json({ clientRequestId, startedAt, finishedAt, bodyWeightKg, exercises }));
    },
  });
}
