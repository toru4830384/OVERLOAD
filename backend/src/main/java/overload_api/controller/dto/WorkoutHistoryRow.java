package overload_api.controller.dto;

import java.math.BigDecimal;

/**
 * ワークアウト履歴の1セット分の情報を保持するDTO。
 */
public class WorkoutHistoryRow {

    /** 
     * ワークアウトセッションID。 
     */
    private Long sessionId;

    /** 
     * ワークアウト開始日時。
     */
    private java.time.LocalDateTime startedAt;

    /** 
     * 種目ID。 
     */
    private Long exerciseId;

    /** 
     * 種目名。 
     */
    private String exerciseName;

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
     * セットに関するメモ。 
     */
    private String note;

    /**
     * ワークアウトセッションIDを取得する。
     *
     * @return ワークアウトセッションID
     */
    public Long getSessionId() {
        return sessionId;
    }

    /**
     * ワークアウト開始日時を取得する。
     *
     * @return ワークアウト開始日時
     */
    public java.time.LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * ワークアウト開始日時を設定する。
     *
     * @param startedAt ワークアウト開始日時
     */
    public void setStartedAt(java.time.LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * ワークアウトセッションIDを設定する。
     *
     * @param sessionId ワークアウトセッションID
     */
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 種目IDを取得する。
     *
     * @return 種目ID
     */
    public Long getExerciseId() {
        return exerciseId;
    }

    /**
     * 種目IDを設定する。
     *
     * @param exerciseId 種目ID
     */
    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    /**
     * 種目名を取得する。
     *
     * @return 種目名
     */
    public String getExerciseName() {
        return exerciseName;
    }

    /**
     * 種目名を設定する。
     *
     * @param exerciseName 種目名
     */
    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
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

    /**
     * セットに関するメモを取得する。
     *
     * @return セットに関するメモ
     */
    public String getNote() {
        return note;
    }

    /**
     * セットに関するメモを設定する。
     *
     * @param note セットに関するメモ
     */
    public void setNote(String note) {
        this.note = note;
    }

}