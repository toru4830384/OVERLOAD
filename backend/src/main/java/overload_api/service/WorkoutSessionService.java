package overload_api.service;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import overload_api.mapper.WorkoutSessionMapper;
import overload_api.model.WorkoutSession;

@Service
public class WorkoutSessionService {

    private final WorkoutSessionMapper workoutSessionMapper;

    public WorkoutSessionService(WorkoutSessionMapper workoutSessionMapper) {
        this.workoutSessionMapper = workoutSessionMapper;
    }

    public List<WorkoutSession> findAll() {
        return workoutSessionMapper.findAll();
    }

    public WorkoutSession create(Long userId) {
        WorkoutSession workoutSession = new WorkoutSession();

        workoutSession.setUserId(userId);
        workoutSession.setStartedAt(LocalDateTime.now());

        workoutSessionMapper.insert(workoutSession);

        return workoutSession;
    }
}