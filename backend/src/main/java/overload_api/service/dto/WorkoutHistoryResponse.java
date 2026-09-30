package overload_api.service.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ワークアウト履歴のサービス用レスポンス情報を保持するDTO。
 */
public class WorkoutHistoryResponse {

    /**
     * ワークアウトセッションID。
     */
    private Long sessionId;

    /**
     * ワークアウト開始日時。
     */
    private LocalDateTime startedAt;

    /**
     * 種目ID。
     */
    private Long exerciseId;

    /**
     * 種目名。
     */
    private String exerciseName;

    /**
     * 種目に関するメモ。
     */
    private String note;

    /**
     * セット情報の一覧。
     */
    private List<WorkoutHistorySetResponse> sets;

    /**
     * ワークアウトセッションIDを取得する。
     *
     * @return ワークアウトセッションID
     */
    public Long getSessionId() {
        return sessionId;
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
     * ワークアウト開始日時を取得する。
     *
     * @return ワークアウト開始日時
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * ワークアウト開始日時を設定する。
     *
     * @param startedAt ワークアウト開始日時
     */
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
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
     * 種目に関するメモを取得する。
     *
     * @return 種目に関するメモ
     */
    public String getNote() {
        return note;
    }

    /**
     * 種目に関するメモを設定する。
     *
     * @param note 種目に関するメモ
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