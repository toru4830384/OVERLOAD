package overload_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import overload_api.mapper.WorkoutSetMapper;
import overload_api.model.WorkoutSet;

@Service
public class WorkoutSetService {

    private final WorkoutSetMapper workoutSetMapper;

    public WorkoutSetService(WorkoutSetMapper workoutSetMapper) {
        this.workoutSetMapper = workoutSetMapper;
    }

    public List<WorkoutSet> findAll() {
        return workoutSetMapper.findAll();
    }

    public WorkoutSet create(
            Long sessionId,
            Long exerciseId,
            Integer setNumber,
            java.math.BigDecimal weightKg,
            Integer reps) {

        WorkoutSet workoutSet = new WorkoutSet();

        workoutSet.setSessionId(sessionId);
        workoutSet.setExerciseId(exerciseId);
        workoutSet.setSetNumber(setNumber);
        workoutSet.setWeightKg(weightKg);
        workoutSet.setReps(reps);

        workoutSetMapper.insert(workoutSet);

        return workoutSet;
    }
}