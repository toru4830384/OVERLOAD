/** 体重が空欄・範囲外なら保存しない。DECIMAL(5,2)の仕様と合わせる。 */
export function profileWeightError(value) {
  const weight = Number(value);
  return !/^\d+(\.\d{1,2})?$/.test(String(value)) || !Number.isFinite(weight) || weight <= 0 || weight > 999.99
    ? "体重は0より大きく999.99kg以下、小数第2位まで入力してください。" : "";
}
