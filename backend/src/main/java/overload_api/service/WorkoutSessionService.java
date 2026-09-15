package overload_api.service;

import java.time.LocalDateTime;
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
     * 全てのワークアウトセッションを取得する。
     *
     * @return ワークアウトセッション一覧
     */
    public List<WorkoutSession> findAll() {
        return workoutSessionMapper.findAll();
    }

    /**
     * ワークアウトセッションを新規作成する。
     *
     * @param userId ユーザーID
     * @return 作成したワークアウトセッション
     */
    public WorkoutSession create(Long userId) {
        WorkoutSession workoutSession = new WorkoutSession();
        workoutSession.setUserId(userId);
        workoutSession.setStartedAt(LocalDateTime.now());
        workoutSessionMapper.insert(workoutSession);
        return workoutSession;
    }

}