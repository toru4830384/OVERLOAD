package overload_api.controller.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * ワークアウト登録時のセット情報を保持するDTO。
 */
public class WorkoutSetRequest {

    /**
     * セット番号。
     */
    private Integer setNumber;

    /**
     * 重量（kg）。
     *
     * @NotNullにより、重量が未入力でないことを検証する。
     * @DecimalMinにより、重量が0.01kg以上であることを検証する。
     */
    @NotNull
    @DecimalMin(
            value = "0.01",
            message = "重量は0より大きい値を入力してください")
    private BigDecimal weightKg;

    /**
     * 回数。
     */
    @Min(value = 1, message = "回数は0より大きい値を入力してください")
    private Integer reps;

    /**
     * セット番号を取得する。
     *
     * @return セット番号
     */
    public Integer getSetNumber() {
        return setNumber;
    }

    /**
     * セット番号を設定する。
     *
     * @param setNumber セット番号
     */
    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    /**
     * 重量を取得する。
     *
     * @return 重量（kg）
     */
    public BigDecimal getWeightKg() {
        return weightKg;
    }

    /**
     * 重量を設定する。
     *
     * @param weightKg 重量（kg）
     */
    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    /**
     * 回数を取得する。
     *
     * @return 回数
     */
    public Integer getReps() {
        return reps;
    }

    /**
     * 回数を設定する。
     *
     * @param reps 回数
     */
    public void setReps(Integer reps) {
        this.reps = reps;
    }
}