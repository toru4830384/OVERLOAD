/** フロントとAPIで必須項目・数値範囲をそろえる。 */
export function profileError(profile) {
  if (!["male", "female"].includes(profile.gender)) return "男性または女性を選択してください。";
  if (profile.age === "" || !Number.isInteger(Number(profile.age)) || Number(profile.age) < 0 || Number(profile.age) > 2147483647) return "年齢は0以上の整数で入力してください。";
  const weight = Number(profile.bodyWeightKg);
  if (profile.bodyWeightKg === "" || !Number.isFinite(weight) || weight <= 0 || weight > 999.99 || !/^\d+(\.\d{1,2})?$/.test(String(profile.bodyWeightKg))) return "体重は0より大きく999.99kg以下、小数第2位まで入力してください。";
  if (typeof profile.name !== "string" || profile.name.length > 50) return "名前は50文字以内で入力してください。";
  return "";
}

export function profilePayload(profile) {
  return { name: profile.name, gender: profile.gender, age: Number(profile.age), bodyWeightKg: Number(profile.bodyWeightKg) };
}
