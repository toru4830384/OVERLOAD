package overload_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import overload_api.mapper.ExerciseMuscleMapper;
import overload_api.model.ExerciseMuscle;

@Service
public class ExerciseMuscleService {

    private final ExerciseMuscleMapper exerciseMuscleMapper;

    public ExerciseMuscleService(ExerciseMuscleMapper exerciseMuscleMapper) {
        this.exerciseMuscleMapper = exerciseMuscleMapper;
    }

    public List<ExerciseMuscle> findAll() {
        return exerciseMuscleMapper.findAll();
    }

}