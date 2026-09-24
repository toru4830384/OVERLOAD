package overload_api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import overload_api.model.WorkoutSession;

/**
 * ワークアウトセッション情報をデータベースから取得・登録・更新するMapper。
 */
@Mapper
public interface WorkoutSessionMapper {

    /**
     * 全てのワークアウトセッションを取得する。
     *
     * @return ワークアウトセッション一覧
     */
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

    /**
     * ワークアウトセッションを登録する。
     *
     * @param workoutSession 登録するワークアウトセッション情報
     */
    @Insert("""
        INSERT INTO workout_sessions (
            user_id,
            started_at
        )
        VALUES (
            #{userId},
            #{startedAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkoutSession workoutSession);

    /**
     * 指定されたワークアウトセッションの終了日時を更新する。
     *
     * @param id ワークアウトセッションID
     */
    @Update("""
        UPDATE workout_sessions
        SET finished_at = NOW()
        WHERE id = #{id}
        """)
    void updateFinishedAt(Long id);

}