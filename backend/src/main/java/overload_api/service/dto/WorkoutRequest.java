package overload_api.service.dto;

import java.util.List;
import java.time.OffsetDateTime;

/**
 * ワークアウト登録時のサービス用リクエスト情報を保持するDTO。
 */
public class WorkoutRequest {

    /**
     * 実際に開始操作を行った日時（UTCオフセット付き）。
     */
    private OffsetDateTime startedAt;

    /**
     * @return 実開始日時
     */
    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * @param startedAt 実開始日時
     */
    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * ワークアウトを実施したユーザーのID。
     */
    private Long userId;

    /**
     * ワークアウトで実施した種目の一覧。
     */
    private List<WorkoutExerciseRequest> exercises;

    /**
     * ユーザーIDを取得する。
     *
     * @return ユーザーID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * ユーザーIDを設定する。
     *
     * @param userId ユーザーID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * ワークアウトで実施した種目の一覧を取得する。
     *
     * @return 種目一覧
     */
    public List<WorkoutExerciseRequest> getExercises() {
        return exercises;
    }

    /**
     * ワークアウトで実施した種目の一覧を設定する。
     *
     * @param exercises 種目一覧
     */
    public void setExercises(List<WorkoutExerciseRequest> exercises) {
        this.exercises = exercises;
    }
}