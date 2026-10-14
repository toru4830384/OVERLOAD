package overload_api.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import overload_api.model.User;

/**
 * ユーザー情報をデータベースから取得・更新するMapper。
 */
@Mapper
public interface UserMapper {

    /**
     * 指定されたIDのユーザー情報を取得する。
     *
     * @param id ユーザーID
     * @return ユーザー情報
     */
    @Select("""
        SELECT
            id,
            name,
            gender,
            age,
            body_weight_kg AS bodyWeightKg
        FROM users
        WHERE id = #{id}
        """)
    User findById(Long id);

    /**
     * ユーザー情報を更新する。
     *
     * @param user 更新するユーザー情報
     */
    @Update("""
        UPDATE users
        SET
            name = #{name},
            gender = #{gender},
            age = #{age},
            body_weight_kg = #{bodyWeightKg}
        WHERE id = #{id}
        """)
    void update(User user);

}