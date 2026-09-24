package overload_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import overload_api.mapper.WorkoutSetMapper;
import overload_api.model.WorkoutSet;

/**
 * ワークアウトセットに関する業務処理を提供するサービス。
 */
@Service
public class WorkoutSetService {

    private final WorkoutSetMapper workoutSetMapper;

    /**
     * WorkoutSetServiceを生成する。
     *
     * @param workoutSetMapper ワークアウトセットを操作するMapper
     */
    public WorkoutSetService(WorkoutSetMapper workoutSetMapper) {
        this.workoutSetMapper = workoutSetMapper;
    }

    /**
     * 全てのワークアウトセットを取得する。
     *
     * @return ワークアウトセット一覧
     */
    public List<WorkoutSet> findAll() {
        return workoutSetMapper.findAll();
    }

    /**
     * ワークアウトセットを新規作成する。
     *
     * @param sessionId ワークアウトセッションID
     * @param exerciseId 種目ID
     * @param setNumber セット番号
     * @param weightKg 重量（kg）
     * @param reps 回数
     * @param note メモ
     * @return 作成したワークアウトセット
     */
    public WorkoutSet create(
            Long sessionId,
            Long exerciseId,
            Integer setNumber,
            java.math.BigDecimal weightKg,
            Integer reps,
            String note) {
        WorkoutSet workoutSet = new WorkoutSet();
        workoutSet.setSessionId(sessionId);
        workoutSet.setExerciseId(exerciseId);
        workoutSet.setSetNumber(setNumber);
        workoutSet.setWeightKg(weightKg);
        workoutSet.setReps(reps);
        workoutSet.setNote(note);
        workoutSetMapper.insert(workoutSet);
        return workoutSet;
    }

}