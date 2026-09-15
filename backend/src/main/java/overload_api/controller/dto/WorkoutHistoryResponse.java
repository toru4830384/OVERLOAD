package overload_api.controller.dto;

import java.util.List;

/**
 * ワークアウト履歴のレスポンス情報を保持するDTO。
 */
public class WorkoutHistoryResponse {

    /**
     * ワークアウトセッションのID。
     */
    private Long sessionId;

    /**
     * ワークアウト開始日時。
     */
    private java.time.LocalDateTime startedAt;

    /**
     * 種目のID。
     */
    private Long exerciseId;

    /**
     * 種目名。
     */
    private String exerciseName;

    /**
     * ワークアウト時のメモ。
     */
    private String note;

    /**
     * セット情報の一覧。
     */
    private List<WorkoutHistorySetResponse> sets;

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
     * ワークアウト時のメモを取得する。
     *
     * @return ワークアウト時のメモ
     */
    public String getNote() {
        return note;
    }

    /**
     * ワークアウト時のメモを設定する。
     *
     * @param note ワークアウト時のメモ
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * セット情報の一覧を取得する。
     *
     * @return セット情報の一覧
     */
    public List<WorkoutHistorySetResponse> getSets() {
        return sets;
    }

    /**
     * セット情報の一覧を設定する。
     *
     * @param sets セット情報の一覧
     */
    public void setSets(List<WorkoutHistorySetResponse> sets) {
        this.sets = sets;
    }

}