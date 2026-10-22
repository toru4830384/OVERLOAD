const API_BASE = import.meta.env?.VITE_API_BASE_URL || "http://localhost:8080/api";

/** APIが応答した失敗と、保存結果が不明な通信失敗を区別する。 */
export class ApiError extends Error {
  constructor(message, status, code) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
  }
}

/** HTTP失敗を画面へ渡す。成功レスポンスのJSONもここで処理する。 */
async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, options);
  if (!response.ok) {
    let message = "通信に失敗しました。もう一度お試しください。";
    let code;
    try {
      const problem = await response.json();
      if (problem.detail) message = problem.detail;
      code = problem.code;
    } catch { /* JSONでないエラーでも共通メッセージを表示する。 */ }
    throw new ApiError(message, response.status, code);
  }
  return response.json();
}

export const getExercises = (signal) => request("/exercises", { signal });
export const getProfile = (userId, signal) => request(`/users/${userId}`, { signal });
export const getWorkoutHistory = (userId, signal) => request(`/workouts/history?user_id=${userId}`, { signal });
export const saveProfile = (userId, profile) => request(`/users/${userId}`, {
  method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(profile),
});
export const saveWorkout = (workout) => request("/workouts", {
  method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(workout),
});
