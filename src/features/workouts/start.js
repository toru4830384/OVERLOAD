import { profileError } from "../profile/validation.js";

/** 記録の読込とプロフィール保存が完了した場合だけ開始を許可する。 */
export function workoutStartError(profileState, { dataReady, dataError }) {
  if (!dataReady || dataError) return "記録の読み込みを確認してください。";
  if (profileState.loadState !== "ready") return "プロフィールを読み込んでください。";
  const inputError = profileError(profileState.profile);
  if (inputError) return inputError;
  if (profileState.saveState !== "saved") return "プロフィールの保存が完了してから開始してください。";
  return "";
}
