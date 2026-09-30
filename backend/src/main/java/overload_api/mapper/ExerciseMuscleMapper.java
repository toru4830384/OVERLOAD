package overload_api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import overload_api.model.ExerciseMuscle;

/**
 * 種目と筋肉の関連情報をデータベースから取得するMapper。
 */
@Mapper
public interface ExerciseMuscleMapper {

    /**
     * 全ての種目と筋肉の関連情報を取得する。
     *
     * @return 種目と筋肉の関連情報一覧
     */
    @Select("""
        SELECT
            exercise_id AS exerciseId,
            muscle_id AS muscleId
        FROM exercise_muscles
        ORDER BY exercise_id, muscle_id
        """)
    List<ExerciseMuscle> findAll();

}