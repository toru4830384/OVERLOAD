package overload_api.controller.dto;

import java.math.BigDecimal;

import overload_api.domain.validation.WorkoutRepetitions;
import overload_api.domain.validation.WorkoutSetNumber;
import overload_api.domain.validation.WorkoutWeight;

/**
 * ワークアウト登録時のセット情報を保持するDTO。
 */
public class WorkoutSetRequest {

	/**
	 * セット番号。
	 *
	 * 共通の{@code @WorkoutSetNumber}で必須・1以上を検証する。
	 */
    @WorkoutSetNumber
    private Integer setNumber;

    /**
     * 重量（kg）。
     *
     * 共通の{@code @WorkoutWeight}で0.01kg以上・整数4桁小数2桁を検証する。
     */
    @WorkoutWeight
    private BigDecimal weightKg;

    /**
     * 回数。
     *
     * 共通の{@code @WorkoutRepetitions}で必須・1以上を検証する。
     */
    @WorkoutRepetitions
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