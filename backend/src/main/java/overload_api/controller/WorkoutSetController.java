package overload_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.controller.dto.WorkoutSetRequest;
import overload_api.model.WorkoutSet;
import overload_api.service.WorkoutSetService;

@RestController
@RequestMapping("/api/workout-sets")
public class WorkoutSetController {

    private final WorkoutSetService workoutSetService;

    public WorkoutSetController(WorkoutSetService workoutSetService) {
        this.workoutSetService = workoutSetService;
    }

    @GetMapping
    public List<WorkoutSet> findAll() {
        return workoutSetService.findAll();
    }

    @PostMapping
    public WorkoutSet create(@RequestBody WorkoutSetRequest request) {
        return workoutSetService.create(
                request.getSessionId(),
                request.getExerciseId(),
                request.getSetNumber(),
                request.getWeightKg(),
                request.getReps()
        );
    }

}