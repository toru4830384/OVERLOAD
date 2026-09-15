package overload_api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import overload_api.model.Exercise;

/**
 * 種目情報をデータベースから取得するMapper。
 */
@Mapper
public interface ExerciseMapper {

    /**
     * 全ての種目情報を取得する。
     *
     * @return 種目情報一覧
     */
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