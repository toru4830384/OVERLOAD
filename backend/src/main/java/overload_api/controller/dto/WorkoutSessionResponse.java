package overload_api.controller.dto;

import java.time.LocalDateTime;

/**
 * ワークアウトセッションAPIのレスポンス情報を保持するDTO。
 */
public class WorkoutSessionResponse {

    /**
     * ワークアウトセッションID。
     */
    private Long id;

    /**
     * ユーザーID。
     */
    private Long userId;

    /**
     * ワークアウト開始日時。
     */
    private LocalDateTime startedAt;

    /**
     * ワークアウト終了日時。
     */
    private LocalDateTime finishedAt;

    /**
     * ワークアウトセッションIDを取得する。
     *
     * @return ワークアウトセッションID
     */
    public Long getId() {
        return id;
    }

    /**
     * ワークアウトセッションIDを設定する。
     *
     * @param id ワークアウトセッションID
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * ワークアウト終了日時を取得する。
     *
     * @return ワークアウト終了日時
     */
    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    /**
     * ワークアウト終了日時を設定する。
     *
     * @param finishedAt ワークアウト終了日時
     */
    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}