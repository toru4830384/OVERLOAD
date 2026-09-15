package overload_api.model;

/**
 * 種目と筋肉の関連情報を保持するモデル。
 */
public class ExerciseMuscle {

    /**
     * 種目のID。
     */
    private Long exerciseId;

    /**
     * 筋肉のID。
     */
    private Long muscleId;

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
     * 筋肉のIDを取得する。
     *
     * @return 筋肉のID
     */
    public Long getMuscleId() {
        return muscleId;
    }

    /**
     * 筋肉のIDを設定する。
     *
     * @param muscleId 筋肉のID
     */
    public void setMuscleId(Long muscleId) {
        this.muscleId = muscleId;
    }
    
}