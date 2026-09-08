package overload_api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import overload_api.model.Exercise;

@Mapper
public interface ExerciseMapper {

    @Select("""
        SELECT
            id,
            name,
            category,
            barbell,
            equipment,
            pattern
        FROM exercises
        ORDER BY id
        """)
    List<Exercise> findAll();
}