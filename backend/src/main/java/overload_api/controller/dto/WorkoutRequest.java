package overload_api.controller.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

/**
 * ワークアウト登録時のリクエスト情報を保持するDTO。
 */
public class WorkoutRequest {

    /**
     * ワークアウトを実施したユーザーのID。
     */
    @NotNull
    private Long userId;

    /**
     * ワークアウトで実施した種目の一覧。
     */
    @NotEmpty(message = "種目を1つ以上指定してください")
    @Valid
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