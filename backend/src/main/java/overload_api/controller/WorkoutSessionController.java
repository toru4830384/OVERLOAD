package overload_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.controller.dto.WorkoutSessionRequest;
import overload_api.model.WorkoutSession;
import overload_api.service.WorkoutSessionService;

@RestController
@RequestMapping("/api/workout-sessions")
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    public WorkoutSessionController(WorkoutSessionService workoutSessionService) {
        this.workoutSessionService = workoutSessionService;
    }

    @GetMapping
    public List<WorkoutSession> findAll() {
        return workoutSessionService.findAll();
    }

    @PostMapping
    public WorkoutSession create(@RequestBody WorkoutSessionRequest request) {
        return workoutSessionService.create(request.getUserId());
    }
}