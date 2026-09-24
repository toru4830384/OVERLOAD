package overload_api.service.dto;

import java.math.BigDecimal;

/**
 * ワークアウト履歴のセット情報を保持するサービス用DTO。
 */
public class WorkoutHistorySetResponse {

    /**
     * セット番号。
     */
    private Integer setNumber;

    /**
     * 重量（kg）。
     */
    private BigDecimal weightKg;

    /**
     * 回数。
     */
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
     * 重量（kg）を取得する。
     *
     * @return 重量（kg）
     */
    public BigDecimal getWeightKg() {
        return weightKg;
    }

    /**
     * 重量（kg）を設定する。
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