package overload_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.model.ExerciseMuscle;
import overload_api.service.ExerciseMuscleService;

@RestController
@RequestMapping("/api/exercise-muscles")
public class ExerciseMuscleController {

    private final ExerciseMuscleService exerciseMuscleService;

    public ExerciseMuscleController(ExerciseMuscleService exerciseMuscleService) {
        this.exerciseMuscleService = exerciseMuscleService;
    }

    @GetMapping
    public List<ExerciseMuscle> findAll() {
        return exerciseMuscleService.findAll();
    }

}