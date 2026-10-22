package overload_api.model;

import java.math.BigDecimal;

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


    /** ワークアウト時の体重。過去データはnull。 */
    private BigDecimal bodyWeightKg;

    /** @return ワークアウト時の体重。過去データはnull。 */
    public BigDecimal getBodyWeightKg() {
        return bodyWeightKg;
    }

    /** @param bodyWeightKg ワークアウト時の体重。過去データはnull。 */
    public void setBodyWeightKg(BigDecimal bodyWeightKg) {
        this.bodyWeightKg = bodyWeightKg;
    }

    /** 登録時に計算したkcal。過去データはnull。 */
    private BigDecimal totalCalories;

    /** @return 登録時に計算したkcal。過去データはnull。 */
    public BigDecimal getTotalCalories() {
        return totalCalories;
    }

    /** @param totalCalories 登録時に計算したkcal。過去データはnull。 */
    public void setTotalCalories(BigDecimal totalCalories) {
        this.totalCalories = totalCalories;
    }

    /** 再送識別UUID。 */
    private String clientRequestId;

    /** @return 再送識別UUID。 */
    public String getClientRequestId() {
        return clientRequestId;
    }

    /** @param clientRequestId 再送識別UUID。 */
    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    /** 再送の入力一致を確認するSHA-256。 */
    private String requestHash;

    /** @return 再送の入力一致を確認するSHA-256。 */
    public String getRequestHash() {
        return requestHash;
    }

    /** @param requestHash 再送の入力一致を確認するSHA-256。 */
    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }
}
