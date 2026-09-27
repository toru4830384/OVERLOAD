package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import overload_api.service.dto.WorkoutExerciseRequest;
import overload_api.service.dto.WorkoutRequest;
import overload_api.service.dto.WorkoutSetRequest;

/**
 * WorkoutServiceのトランザクション処理を確認する結合テストクラス。
 */
@SpringBootTest
@ActiveProfiles("test")
class WorkoutServiceIntegrationTest {

    /**
     * テスト対象のWorkoutService。
     */
    @Autowired
    private WorkoutService workoutService;

    /**
     * テストデータの登録およびDB状態の確認に使用する。
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
     * 複数セット登録中に2セット目のINSERTが失敗した場合、
     * セッションと先に登録されたセットがともにロールバックされることを確認する。
     */
    @Test
    void createRollsBackSessionAndSetsWhenSecondSetInsertFails() {
        jdbcTemplate.update("""
                INSERT INTO users (
                    name,
                    gender,
                    age,
                    body_weight_kg
                )
                VALUES (?, ?, ?, ?)
                """,
                "Integration Test User",
                "男性",
                27,
                new BigDecimal("70.00"));

        Long userId = jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Long.class);

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
                "Integration Test Exercise",
                "胸",
                false,
                "バーベル",
                "プレス");

        Long exerciseId = jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()",
                Long.class);

        WorkoutSetRequest firstSet = new WorkoutSetRequest();
        firstSet.setSetNumber(1);
        firstSet.setWeightKg(new BigDecimal("50.00"));
        firstSet.setReps(10);

        WorkoutSetRequest secondSet = new WorkoutSetRequest();
        secondSet.setSetNumber(2);
        secondSet.setWeightKg(new BigDecimal("50.00"));
        secondSet.setReps(0);

        WorkoutExerciseRequest exerciseRequest =
                new WorkoutExerciseRequest();
        exerciseRequest.setExerciseId(exerciseId);
        exerciseRequest.setNote("Integration Test");
        exerciseRequest.setSets(
                List.of(firstSet, secondSet));

        WorkoutRequest request = new WorkoutRequest();
        request.setUserId(userId);
        request.setExercises(
                List.of(exerciseRequest));

        assertThrows(
                UncategorizedSQLException.class,
                () -> workoutService.create(request));

        Integer sessionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workout_sessions",
                Integer.class);

        Integer setCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workout_sets",
                Integer.class);

        assertEquals(0, sessionCount);
        assertEquals(0, setCount);
    }
}