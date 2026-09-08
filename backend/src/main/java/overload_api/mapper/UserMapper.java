package overload_api.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import overload_api.model.User;

@Mapper
public interface UserMapper {

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
