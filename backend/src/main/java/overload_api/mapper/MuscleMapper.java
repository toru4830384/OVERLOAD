package overload_api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import overload_api.model.Muscle;

/**
 * 筋肉情報をデータベースから取得するMapper。
 */
@Mapper
public interface MuscleMapper {

    /**
     * 全ての筋肉情報を取得する。
     *
     * @return 筋肉情報一覧
     */
    @Select("""
        SELECT
            id,
            name
        FROM muscle
        ORDER BY id
        """)
    List<Muscle> findAll();

}