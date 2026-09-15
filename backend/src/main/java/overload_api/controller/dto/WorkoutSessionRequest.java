package overload_api.controller.dto;

/**
 * ワークアウトセッション登録・更新時のリクエスト情報を保持するDTO。
 */
public class WorkoutSessionRequest {

    /**
     * ワークアウトを実施したユーザーのID。
     */
    private Long userId;

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

}