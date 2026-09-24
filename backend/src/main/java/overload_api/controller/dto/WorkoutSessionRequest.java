package overload_api.controller.dto;

import jakarta.validation.constraints.NotNull;

/**
 * ワークアウトセッション登録・更新時のリクエスト情報を保持するDTO。
 */
public class WorkoutSessionRequest {

    /**
     * ワークアウトを実施したユーザーのID。
     *
     * @NotNullにより、ユーザーIDが未入力でないことを検証する。
     */
    @NotNull
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