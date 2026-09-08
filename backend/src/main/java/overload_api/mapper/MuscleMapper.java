package overload_api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import overload_api.model.Muscle;

@Mapper
public interface MuscleMapper {

    @Select("""
        SELECT
            id,
            name
        FROM muscle
        ORDER BY id
        """)
    List<Muscle> findAll();

}