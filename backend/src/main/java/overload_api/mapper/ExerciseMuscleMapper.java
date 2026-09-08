package overload_api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import overload_api.model.ExerciseMuscle;

@Mapper
public interface ExerciseMuscleMapper {

    @Select("""
    SELECT
        exercise_id AS exerciseId,
        muscle_id AS muscleId
    FROM exercise_muscles
    ORDER BY exercise_id, muscle_id
    """)
    List<ExerciseMuscle> findAll();

}