/** パスワードは自動保存せず、明示的な送信時だけ検証する。 */
export function passwordChangeError({ currentPassword, newPassword, confirmation }) {
  if (typeof currentPassword !== "string" || currentPassword.length < 12 || currentPassword.length > 128) return "現在のパスワードを12〜128文字で入力してください。";
  if (typeof newPassword !== "string" || newPassword.length < 12 || newPassword.length > 128) return "新しいパスワードは12〜128文字で入力してください。";
  if (newPassword !== confirmation) return "新しいパスワードと確認用の入力が一致しません。";
  if (currentPassword === newPassword) return "現在とは異なる新しいパスワードを入力してください。";
  return "";
}
