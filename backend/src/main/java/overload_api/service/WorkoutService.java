package overload_api.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import overload_api.controller.dto.WorkoutHistoryResponse;
import overload_api.controller.dto.WorkoutHistoryRow;
import overload_api.controller.dto.WorkoutHistorySetResponse;
import overload_api.controller.dto.WorkoutExerciseRequest;
import overload_api.controller.dto.WorkoutRequest;
import overload_api.controller.dto.WorkoutSetRequest;
import overload_api.mapper.WorkoutSessionMapper;
import overload_api.mapper.WorkoutSetMapper;
import overload_api.mapper.UserMapper;
import overload_api.model.WorkoutSession;
import overload_api.model.WorkoutSet;

@Service
public class WorkoutService {

    private final WorkoutSessionMapper workoutSessionMapper;
    private final WorkoutSetMapper workoutSetMapper;
    private final UserMapper userMapper;

    public WorkoutService(
            WorkoutSessionMapper workoutSessionMapper,
            WorkoutSetMapper workoutSetMapper,
            UserMapper userMapper) {
        this.workoutSessionMapper = workoutSessionMapper;
        this.workoutSetMapper = workoutSetMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public List<WorkoutSet> create(WorkoutRequest request) {
    	
    	if (userMapper.findById(request.getUserId()) == null) {
    	    throw new ResponseStatusException(
    	            HttpStatus.NOT_FOUND,
    	            "User not found");
    	}

        WorkoutSession workoutSession = new WorkoutSession();
        workoutSession.setUserId(request.getUserId());

        workoutSessionMapper.insert(workoutSession);

        Long sessionId = workoutSession.getId();

        List<WorkoutSet> workoutSets = new ArrayList<>();

        for (WorkoutExerciseRequest exerciseRequest : request.getExercises()) {

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

    public List<WorkoutHistoryResponse> findHistoryByUserId(Long userId) {

        List<WorkoutHistoryRow> rows =
                workoutSetMapper.findHistoryByUserId(userId);

        Map<String, WorkoutHistoryResponse> historyMap =
                new LinkedHashMap<>();

        for (WorkoutHistoryRow row : rows) {

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
