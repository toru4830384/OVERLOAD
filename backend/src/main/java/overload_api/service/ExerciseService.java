package overload_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import overload_api.mapper.ExerciseMapper;
import overload_api.model.Exercise;

@Service
public class ExerciseService {

    private final ExerciseMapper exerciseMapper;

    public ExerciseService(ExerciseMapper exerciseMapper) {
        this.exerciseMapper = exerciseMapper;
    }

    public List<Exercise> findAll() {
        return exerciseMapper.findAll();
    }
}