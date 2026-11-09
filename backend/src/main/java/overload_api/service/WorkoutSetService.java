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
     * 指定ユーザーのワークアウトセットを取得する。
     *
     * @param userId 認証済み所有者ID
     * @return ワークアウトセット一覧
     */
    public List<WorkoutSet> findByUserId(Long userId) {
        return workoutSetMapper.findByUserId(userId);
    }
}