/** 必須・50文字以内。NOT NULL仕様では空文字は許可される。 */
export function profileNameError(name) {
  return typeof name !== "string" || name.length > 50 ? "名前は50文字以内で入力してください。" : "";
}
