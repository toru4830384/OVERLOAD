package overload_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import overload_api.mapper.MuscleMapper;
import overload_api.model.Muscle;

@Service
public class MuscleService {

    private final MuscleMapper muscleMapper;

    public MuscleService(MuscleMapper muscleMapper) {
        this.muscleMapper = muscleMapper;
    }

    public List<Muscle> findAll() {
        return muscleMapper.findAll();
    }

}