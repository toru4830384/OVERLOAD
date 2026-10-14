/** 重量をDBのDECIMAL(6,2)で保存できる値に限定する。 */
export function workoutWeightError(value) {
  const weight = Number(value);
  if (!/^\d+(\.\d{1,2})?$/.test(String(value)) || !Number.isFinite(weight) || weight < 0.01 || weight > 9999.99) {
    return "重量は0.01kg以上9999.99kg以下、小数第2位まで入力してください";
  }
  return "";
}
