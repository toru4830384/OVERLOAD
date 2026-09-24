package overload_api.service.dto;

import java.util.List;

/**
 * ワークアウトで実施する種目のサービス用リクエスト情報を保持するDTO。
 */
public class WorkoutExerciseRequest {

    /**
     * 種目のID。
     */
    private Long exerciseId;

    /**
     * 種目に対して実施したセットの一覧。
     */
    private List<WorkoutSetRequest> sets;

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
     * セット情報の一覧を取得する。
     *
     * @return セット情報の一覧
     */
    public List<WorkoutSetRequest> getSets() {
        return sets;
    }

    /**
     * セット情報の一覧を設定する。
     *
     * @param sets セット情報の一覧
     */
    public void setSets(List<WorkoutSetRequest> sets) {
        this.sets = sets;
    }
}