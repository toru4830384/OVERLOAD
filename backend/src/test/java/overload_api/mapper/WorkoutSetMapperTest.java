package overload_api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import overload_api.mapper.dto.WorkoutHistoryRow;
import overload_api.model.WorkoutSet;

/**
 * WorkoutSetMapperのデータベースアクセスを確認するテストクラス。
 */
@SpringBootTest
@ActiveProfiles("test")
class WorkoutSetMapperTest {

    /**
     * テスト対象のWorkoutSetMapper。
     */
    @Autowired
    private WorkoutSetMapper workoutSetMapper;

    /**
     * テストデータの登録および初期化に使用する。
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 各テスト実行前にテスト用データベースを初期化する。
     */
    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM workout_sets");
        jdbcTemplate.update("DELETE FROM workout_sessions");
        jdbcTemplate.update("DELETE FROM exercise_muscles");
        jdbcTemplate.update("DELETE FROM exercises");
        jdbcTemplate.update("DELETE FROM muscle");
        jdbcTemplate.update("DELETE FROM users");
    }

    /**
     * ワークアウトセットを取得した場合、
     * 種目順とメモを含む全ての項目が正しく取得されることを確認する。
     */
    @Test
    void findAllReturnsExerciseOrderAndNote() {
        Long userId = insertUser();

        Long exerciseId = insertExercise(
                "ベンチプレス",
                "胸");

        Long sessionId = insertWorkoutSession(
                userId,
                LocalDateTime.of(2026, 9, 27, 10, 0));

        jdbcTemplate.update("""
                INSERT INTO workout_sets (
                    session_id,
                    exercise_id,
                    exercise_order,
                    set_number,
                    weight_kg,
                    reps,
                    note
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                sessionId,
                exerciseId,
                2,
                1,
                new BigDecimal("50.00"),
                10,
                "Mapperテスト");

        List<WorkoutSet> result = workoutSetMapper.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        WorkoutSet workoutSet = result.get(0);

        assertNotNull(workoutSet.getId());
        assertEquals(sessionId, workoutSet.getSessionId());
        assertEquals(exerciseId, workoutSet.getExerciseId());
        assertEquals(2, workoutSet.getExerciseOrder());
        assertEquals(1, workoutSet.getSetNumber());
        assertEquals(
                0,
                new BigDecimal("50.00").compareTo(
                        workoutSet.getWeightKg()));
        assertEquals(10, workoutSet.getReps());
        assertEquals("Mapperテスト", workoutSet.getNote());
    }

    /**
     * ワークアウトセットを登録した場合、
     * 種目順とメモを含む全ての項目がデータベースへ保存されることを確認する。
     */
    @Test
    void insertSavesExerciseOrderAndNote() {
        Long userId = insertUser();

        Long exerciseId = insertExercise(
                "スクワット",
                "脚");

        Long sessionId = insertWorkoutSession(
                userId,
                LocalDateTime.of(2026, 9, 27, 11, 0));

        WorkoutSet workoutSet = new WorkoutSet();
        workoutSet.setSessionId(sessionId);
        workoutSet.setExerciseId(exerciseId);
        workoutSet.setExerciseOrder(3);
        workoutSet.setSetNumber(1);
        workoutSet.setWeightKg(new BigDecimal("80.00"));
        workoutSet.setReps(8);
        workoutSet.setNote("登録テスト");

        workoutSetMapper.insert(workoutSet);

        assertNotNull(workoutSet.getId());

        Integer exerciseOrder = jdbcTemplate.queryForObject(
                """
                SELECT exercise_order
                FROM workout_sets
                WHERE id = ?
                """,
                Integer.class,
                workoutSet.getId());

        String note = jdbcTemplate.queryForObject(
                """
                SELECT note
                FROM workout_sets
                WHERE id = ?
                """,
                String.class,
                workoutSet.getId());

        assertEquals(3, exerciseOrder);
        assertEquals("登録テスト", note);
    }

    /**
     * 同一セッションに複数種目が存在する場合、
     * 種目の登録順、セット番号の順で履歴が取得されることを確認する。
     */
    @Test
    void findHistoryByUserIdReturnsRowsInExerciseAndSetOrder() {
        Long userId = insertUser();

        Long firstExerciseId = insertExercise(
                "ベンチプレス",
                "胸");

        Long secondExerciseId = insertExercise(
                "スクワット",
                "脚");

        Long sessionId = insertWorkoutSession(
                userId,
                LocalDateTime.of(2026, 9, 27, 12, 0));

        /*
         * INSERT順やセット番号に依存せず、
         * exercise_order、set_numberの順で取得されることを確認するため、
         * 意図的に順番を入れ替えて登録する。
         */
        insertWorkoutSet(
                sessionId,
                firstExerciseId,
                2,
                1,
                new BigDecimal("80.00"),
                8,
                "2番目の種目");

        insertWorkoutSet(
                sessionId,
                secondExerciseId,
                1,
                2,
                new BigDecimal("60.00"),
                8,
                "1番目の種目");

        insertWorkoutSet(
                sessionId,
                secondExerciseId,
                1,
                1,
                new BigDecimal("50.00"),
                10,
                "1番目の種目");

        List<WorkoutHistoryRow> result =
                workoutSetMapper.findHistoryByUserId(userId);

        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals(
                secondExerciseId,
                result.get(0).getExerciseId());
        assertEquals(
                "スクワット",
                result.get(0).getExerciseName());
        assertEquals(
                1,
                result.get(0).getExerciseOrder());
        assertEquals(
                1,
                result.get(0).getSetNumber());
        assertEquals(
                "1番目の種目",
                result.get(0).getNote());

        assertEquals(
                secondExerciseId,
                result.get(1).getExerciseId());
        assertEquals(
                "スクワット",
                result.get(1).getExerciseName());
        assertEquals(
                1,
                result.get(1).getExerciseOrder());
        assertEquals(
                2,
                result.get(1).getSetNumber());
        assertEquals(
                "1番目の種目",
                result.get(1).getNote());

        assertEquals(
                firstExerciseId,
                result.get(2).getExerciseId());
        assertEquals(
                "ベンチプレス",
                result.get(2).getExerciseName());
        assertEquals(
                2,
                result.get(2).getExerciseOrder());
        assertEquals(
                1,
                result.get(2).getSetNumber());
        assertEquals(
                "2番目の種目",
                result.get(2).getNote());
    }

    /**
     * テスト用ユーザーを登録する。
     *
     * @return 登録したユーザーID
     */
    private Long insertUser() {
        jdbcTemplate.update("""
                INSERT INTO users (
                    name,
                    gender,
                    age,
                    body_weight_kg
                )
                VALUES (?, ?, ?, ?)
                """,
                "Mapper Test User",
                "男性",
                27,
                new BigDecimal("70.00"));

        return jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Long.class);
    }

    /**
     * テスト用種目を登録する。
     *
     * @param name 種目名
     * @param category カテゴリ
     * @return 登録した種目ID
     */
    private Long insertExercise(
            String name,
            String category) {

        jdbcTemplate.update("""
                INSERT INTO exercises (
                    name,
                    category,
                    barbell,
                    equipment,
                    pattern
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                name,
                category,
                false,
                "テスト器具",
                "テスト動作");

        return jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Long.class);
    }

    /**
     * テスト用ワークアウトセッションを登録する。
     *
     * @param userId ユーザーID
     * @param startedAt ワークアウト開始日時
     * @return 登録したワークアウトセッションID
     */
    private Long insertWorkoutSession(
            Long userId,
            LocalDateTime startedAt) {

        jdbcTemplate.update("""
                INSERT INTO workout_sessions (
                    user_id,
                    started_at
                )
                VALUES (?, ?)
                """,
                userId,
                startedAt);

        return jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Long.class);
    }

    /**
     * テスト用ワークアウトセットを登録する。
     *
     * @param sessionId ワークアウトセッションID
     * @param exerciseId 種目ID
     * @param exerciseOrder 種目の登録順
     * @param setNumber セット番号
     * @param weightKg 重量（kg）
     * @param reps 回数
     * @param note メモ
     */
    private void insertWorkoutSet(
            Long sessionId,
            Long exerciseId,
            Integer exerciseOrder,
            Integer setNumber,
            BigDecimal weightKg,
            Integer reps,
            String note) {

        jdbcTemplate.update("""
                INSERT INTO workout_sets (
                    session_id,
                    exercise_id,
                    exercise_order,
                    set_number,
                    weight_kg,
                    reps,
                    note
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                sessionId,
                exerciseId,
                exerciseOrder,
                setNumber,
                weightKg,
                reps,
                note);
    }
}