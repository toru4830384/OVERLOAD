package overload_api.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import overload_api.controller.dto.WorkoutHistoryRow;
import overload_api.model.WorkoutSet;

/**
 * ワークアウトセット情報をデータベースから取得・登録するMapper。
 */
@Mapper
public interface WorkoutSetMapper {

    /**
     * 全てのワークアウトセットを取得する。
     *
     * @return ワークアウトセット一覧
     */
    @Select("""
        SELECT
            id,
            session_id AS sessionId,
            exercise_id AS exerciseId,
            set_number AS setNumber,
            weight_kg AS weightKg,
            reps
        FROM workout_sets
        ORDER BY session_id, set_number
        """)
    List<WorkoutSet> findAll();

    /**
     * ワークアウトセットを登録する。
     *
     * @param workoutSet 登録するワークアウトセット情報
     */
    @Insert("""
        INSERT INTO workout_sets (
            session_id,
            exercise_id,
            set_number,
            weight_kg,
            reps,
            note
        )
        VALUES (
            #{sessionId},
            #{exerciseId},
            #{setNumber},
            #{weightKg},
            #{reps},
            #{note}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkoutSet workoutSet);

    /**
     * 指定されたユーザーのワークアウト履歴を取得する。
     *
     * @param userId ユーザーID
     * @return ワークアウト履歴一覧
     */
    @Select("""
        SELECT
            ws.session_id AS sessionId,
            wss.started_at AS startedAt,
            ws.exercise_id AS exerciseId,
            e.name AS exerciseName,
            ws.set_number AS setNumber,
            ws.weight_kg AS weightKg,
            ws.reps,
            ws.note
        FROM workout_sets ws
        INNER JOIN workout_sessions wss
            ON ws.session_id = wss.id
        INNER JOIN exercises e
            ON ws.exercise_id = e.id
        WHERE wss.user_id = #{userId}
        ORDER BY ws.session_id DESC, ws.set_number
        """)
    List<WorkoutHistoryRow> findHistoryByUserId(Long userId);

}