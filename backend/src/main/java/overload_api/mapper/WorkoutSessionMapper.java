package overload_api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import overload_api.model.WorkoutSession;

@Mapper
public interface WorkoutSessionMapper {

    @Select("""
        SELECT
            id,
            user_id AS userId,
            started_at AS startedAt,
            finished_at AS finishedAt
        FROM workout_sessions
        ORDER BY id
        """)
    List<WorkoutSession> findAll();

    @Insert("""
        INSERT INTO workout_sessions (
            user_id,
            started_at
        )
        VALUES (
            #{userId},
            NOW()
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkoutSession workoutSession);

    @org.apache.ibatis.annotations.Update("""
    	    UPDATE workout_sessions
    	    SET finished_at = NOW()
    	    WHERE id = #{id}
    	    """)
    	void updateFinishedAt(Long id);
}