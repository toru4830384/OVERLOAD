package overload_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import overload_api.mapper.WorkoutSessionMapper;
import overload_api.model.WorkoutSession;

/**
 * ワークアウトセッションに関する業務処理を提供するサービス。
 */
@Service
public class WorkoutSessionService {

    private final WorkoutSessionMapper workoutSessionMapper;

    /**
     * WorkoutSessionServiceを生成する。
     *
     * @param workoutSessionMapper ワークアウトセッションを操作するMapper
     */
    public WorkoutSessionService(WorkoutSessionMapper workoutSessionMapper) {
        this.workoutSessionMapper = workoutSessionMapper;
    }

    /**
     * 指定ユーザーのワークアウトセッションを取得する。
     *
     * @param userId 認証済み所有者ID
     * @return ワークアウトセッション一覧
     */
    public List<WorkoutSession> findByUserId(Long userId) {
        return workoutSessionMapper.findByUserId(userId);
    }
}