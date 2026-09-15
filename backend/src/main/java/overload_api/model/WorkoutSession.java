package overload_api.model;

import java.time.LocalDateTime;

/**
 * ワークアウトセッションの情報を保持するモデル。
 */
public class WorkoutSession {

    /**
     * ワークアウトセッションのID。
     */
    private Long id;

    /**
     * ユーザーのID。
     */
    private Long userId;

    /**
     * ワークアウトの開始日時。
     */
    private LocalDateTime startedAt;

    /**
     * ワークアウトの終了日時。
     */
    private LocalDateTime finishedAt;

    /**
     * ワークアウトセッションのIDを取得する。
     *
     * @return ワークアウトセッションのID
     */
    public Long getId() {
        return id;
    }

    /**
     * ワークアウトセッションのIDを設定する。
     *
     * @param id ワークアウトセッションのID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * ユーザーのIDを取得する。
     *
     * @return ユーザーのID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * ユーザーのIDを設定する。
     *
     * @param userId ユーザーのID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * ワークアウトの開始日時を取得する。
     *
     * @return ワークアウトの開始日時
     */
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /**
     * ワークアウトの開始日時を設定する。
     *
     * @param startedAt ワークアウトの開始日時
     */
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    /**
     * ワークアウトの終了日時を取得する。
     *
     * @return ワークアウトの終了日時
     */
    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    /**
     * ワークアウトの終了日時を設定する。
     *
     * @param finishedAt ワークアウトの終了日時
     */
    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
    
}