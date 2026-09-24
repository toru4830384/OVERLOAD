package overload_api.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import overload_api.exception.ResourceNotFoundException;
import overload_api.mapper.UserMapper;
import overload_api.mapper.WorkoutSessionMapper;
import overload_api.mapper.WorkoutSetMapper;
import overload_api.mapper.dto.WorkoutHistoryRow;
import overload_api.model.WorkoutSession;
import overload_api.model.WorkoutSet;
import overload_api.service.dto.WorkoutExerciseRequest;
import overload_api.service.dto.WorkoutHistoryResponse;
import overload_api.service.dto.WorkoutHistorySetResponse;
import overload_api.service.dto.WorkoutRequest;
import overload_api.service.dto.WorkoutSetRequest;

/**
 * ワークアウトに関する業務処理を提供するサービス。
 */
@Service
public class WorkoutService {

    private final WorkoutSessionMapper workoutSessionMapper;
    private final WorkoutSetMapper workoutSetMapper;
    private final UserMapper userMapper;

    /**
     * WorkoutServiceを生成する。
     *
     * @param workoutSessionMapper ワークアウトセッションを操作するMapper
     * @param workoutSetMapper ワークアウトセットを操作するMapper
     * @param userMapper ユーザー情報を操作するMapper
     */
    public WorkoutService(
            WorkoutSessionMapper workoutSessionMapper,
            WorkoutSetMapper workoutSetMapper,
            UserMapper userMapper) {
        this.workoutSessionMapper = workoutSessionMapper;
        this.workoutSetMapper = workoutSetMapper;
        this.userMapper = userMapper;
    }

    /**
     * ワークアウトを登録する。
     *
     * @param request ワークアウト登録リクエスト
     * @return 登録したワークアウトセット一覧
     * @throws ResourceNotFoundException ユーザーが存在しない場合
     */
    @Transactional
    public List<WorkoutSet> create(WorkoutRequest request) {
        if (userMapper.findById(request.getUserId()) == null) {
            throw new ResourceNotFoundException("User not found");
        }

        WorkoutSession workoutSession = new WorkoutSession();
        workoutSession.setUserId(request.getUserId());
        workoutSessionMapper.insert(workoutSession);

        Long sessionId = workoutSession.getId();
        List<WorkoutSet> workoutSets = new ArrayList<>();

        // 種目ごとのセット情報をワークアウトセットとして登録する。
        for (WorkoutExerciseRequest exerciseRequest : request.getExercises()) {
            if (exerciseRequest.getSets() == null) {
                continue;
            }

            for (WorkoutSetRequest setRequest : exerciseRequest.getSets()) {
                WorkoutSet workoutSet = new WorkoutSet();
                workoutSet.setSessionId(sessionId);
                workoutSet.setExerciseId(exerciseRequest.getExerciseId());
                workoutSet.setSetNumber(setRequest.getSetNumber());
                workoutSet.setWeightKg(setRequest.getWeightKg());
                workoutSet.setReps(setRequest.getReps());
                workoutSet.setNote(setRequest.getNote());
                workoutSetMapper.insert(workoutSet);
                workoutSets.add(workoutSet);
            }
        }

        workoutSessionMapper.updateFinishedAt(sessionId);
        return workoutSets;
    }

    /**
     * 指定されたユーザーのワークアウト履歴を取得する。
     *
     * @param userId ユーザーID
     * @return ワークアウト履歴一覧
     */
    public List<WorkoutHistoryResponse> findHistoryByUserId(Long userId) {
        List<WorkoutHistoryRow> rows =
                workoutSetMapper.findHistoryByUserId(userId);

        Map<String, WorkoutHistoryResponse> historyMap =
                new LinkedHashMap<>();

        for (WorkoutHistoryRow row : rows) {
            // 同一セッション内の同一種目ごとにセットをまとめるためのキーを作成する。
            String key = row.getSessionId() + "-" + row.getExerciseId();
            WorkoutHistoryResponse response = historyMap.get(key);

            if (response == null) {
                response = new WorkoutHistoryResponse();
                response.setSessionId(row.getSessionId());
                response.setStartedAt(row.getStartedAt());
                response.setExerciseId(row.getExerciseId());
                response.setExerciseName(row.getExerciseName());
                response.setNote(row.getNote());
                response.setSets(new ArrayList<>());
                historyMap.put(key, response);
            }

            WorkoutHistorySetResponse set =
                    new WorkoutHistorySetResponse();
            set.setSetNumber(row.getSetNumber());
            set.setWeightKg(row.getWeightKg());
            set.setReps(row.getReps());
            response.getSets().add(set);
        }

        return new ArrayList<>(historyMap.values());
    }
}