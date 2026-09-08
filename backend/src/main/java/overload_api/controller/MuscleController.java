package overload_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.model.Muscle;
import overload_api.service.MuscleService;

@RestController
@RequestMapping("/api/muscles")
public class MuscleController {

    private final MuscleService muscleService;

    public MuscleController(MuscleService muscleService) {
        this.muscleService = muscleService;
    }

    @GetMapping
    public List<Muscle> findAll() {
        return muscleService.findAll();
    }

}