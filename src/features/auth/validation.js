import { profileError, profilePayload } from "../profile/validation.js";
/** ログインIDとパスワードの形式をAPIの共通制約へそろえる。 */
export function credentialsError(input) {
  if (!/^[a-z0-9_]{3,32}$/.test(input.loginId)) return "ログインIDは小文字英数字と_の3〜32文字です。";
  if (typeof input.password !== "string" || input.password.length < 12 || input.password.length > 128) return "パスワードは12〜128文字で入力してください。";
  return "";
}
export function registrationError(input) { return credentialsError(input) || profileError(input); }
export function registrationPayload(input) { return { loginId: input.loginId, password: input.password, ...profilePayload(input) }; }
