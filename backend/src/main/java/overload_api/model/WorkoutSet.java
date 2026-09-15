package overload_api.model;

import java.math.BigDecimal;

/**
 * ワークアウトのセット情報を保持するモデル。
 */
public class WorkoutSet {

    /**
     * ワークアウトセットのID。
     */
    private Long id;

    /**
     * ワークアウトセッションのID。
     */
    private Long sessionId;

    /**
     * 種目のID。
     */
    private Long exerciseId;

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
     * メモ。
     */
    private String note;

    /**
     * ワークアウトセットのIDを取得する。
     *
     * @return ワークアウトセットのID
     */
    public Long getId() {
        return id;
    }

    /**
     * ワークアウトセットのIDを設定する。
     *
     * @param id ワークアウトセットのID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * ワークアウトセッションのIDを取得する。
     *
     * @return ワークアウトセッションのID
     */
    public Long getSessionId() {
        return sessionId;
    }

    /**
     * ワークアウトセッションのIDを設定する。
     *
     * @param sessionId ワークアウトセッションのID
     */
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 種目のIDを取得する。
     *
     * @return 種目のID
     */
    public Long getExerciseId() {
        return exerciseId;
    }

    /**
     * 種目のIDを設定する。
     *
     * @param exerciseId 種目のID
     */
    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

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

    /**
     * メモを取得する。
     *
     * @return メモ
     */
    public String getNote() {
        return note;
    }

    /**
     * メモを設定する。
     *
     * @param note メモ
     */
    public void setNote(String note) {
        this.note = note;
    }
    
}