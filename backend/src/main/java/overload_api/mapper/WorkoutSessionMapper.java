package overload_api.mapper;

import java.util.List;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Param;
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
            finished_at AS finishedAt,
            body_weight_kg AS bodyWeightKg, total_calories AS totalCalories,
            client_request_id AS clientRequestId, request_hash AS requestHash
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
            started_at, body_weight_kg, total_calories, client_request_id, request_hash
        )
        VALUES (
            #{userId},
            #{startedAt}, #{bodyWeightKg}, #{totalCalories}, #{clientRequestId}, #{requestHash}
        )
        ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkoutSession workoutSession);

    /**
     * 指定されたワークアウトセッションの終了日時を更新する。
     *
     * @param id ワークアウトセッションID
     * @param finishedAt ワークアウト終了日時
     */
    @Update("""
        UPDATE workout_sessions
        SET finished_at = #{finishedAt}
        WHERE id = #{id}
        """)
    void updateFinishedAt(
            @Param("id") Long id,
            @Param("finishedAt") LocalDateTime finishedAt);

    /** 登録済みの再送識別子を取得する。
     * @param userId ユーザーID
     * @param clientRequestId 再送識別UUID
     * @return セッション。未登録はnull
     */
    @Select("""
        SELECT id, user_id AS userId, request_hash AS requestHash
        FROM workout_sessions WHERE user_id = #{userId} AND client_request_id = #{clientRequestId}
        FOR UPDATE
        """)
    WorkoutSession findByClientRequestId(@Param("userId") Long userId,
            @Param("clientRequestId") String clientRequestId);
}
