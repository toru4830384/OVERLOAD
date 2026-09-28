package overload_api.service;

import static overload_api.support.DatabaseFixtures.insert;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import overload_api.service.dto.WorkoutExerciseRequest;
import overload_api.service.dto.WorkoutRequest;
import overload_api.service.dto.WorkoutSetRequest;
import overload_api.support.MySqlIntegrationTest;

/**
 * WorkoutServiceのトランザクション処理を確認する結合テストクラス。
 */
class WorkoutServiceIntegrationTest extends MySqlIntegrationTest {

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

    /** 正常な2セットがコミットされることを確認する。 */
    @Test
    void createCommitsSessionAndBothSets() {
        var result = workoutService.create(validRequest());
        assertEquals(2, result.size());
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workout_sessions WHERE finished_at IS NOT NULL", Integer.class));
        assertEquals(2, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM workout_sets", Integer.class));
        assertTrue(result.stream().allMatch(set -> set.getId() != null));
    }

    /** 1セット目の実INSERTを確認したDBトリガーで2セット目を失敗させる。 */
    @Test
    void createRollsBackSessionAndSetsWhenSecondSetInsertFails() {
        var request = validRequest();
        jdbcTemplate.execute("""
                CREATE TRIGGER fail_second_workout_set BEFORE INSERT ON workout_sets
                FOR EACH ROW
                BEGIN
                    IF NEW.set_number = 2 THEN
                        IF EXISTS (SELECT 1 FROM workout_sets
                                   WHERE session_id = NEW.session_id AND set_number = 1) THEN
                            SIGNAL SQLSTATE '45000'
                                SET MESSAGE_TEXT = 'review_second_insert_after_first_success';
                        ELSE
                            SIGNAL SQLSTATE '45000'
                                SET MESSAGE_TEXT = 'review_first_insert_missing';
                        END IF;
                    END IF;
                END
                """);
        try {
            DataAccessException exception = assertThrows(
                    DataAccessException.class, () -> workoutService.create(request));
            assertTrue(exception.getMostSpecificCause().getMessage()
                    .contains("review_second_insert_after_first_success"));
            assertEquals(0, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM workout_sessions", Integer.class));
            assertEquals(0, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM workout_sets", Integer.class));
        } finally {
            jdbcTemplate.execute("DROP TRIGGER IF EXISTS fail_second_workout_set");
        }
    }

    /** 同じ種目を再実施した場合もDBへの保存と履歴への復元ができる。 */
    @Test
    void createAndReadHistoryPreservesRepeatedExerciseOrder() {
        var request = validRequest();
        var first = request.getExercises().get(0);
        Long otherId = insert(jdbcTemplate,
                "INSERT INTO exercises (name, category, barbell, equipment, pattern) VALUES (?, ?, ?, ?, ?)",
                "Second exercise", "test", false, "test", "test");
        var second = new WorkoutExerciseRequest();
        second.setExerciseId(otherId);
        second.setNote("second");
        second.setSets(first.getSets());
        var third = new WorkoutExerciseRequest();
        third.setExerciseId(first.getExerciseId());
        third.setNote("third");
        third.setSets(first.getSets());
        request.setExercises(List.of(first, second, third));
        var result = workoutService.create(request);
        assertEquals(List.of(1, 1, 2, 2, 3, 3),
                result.stream().map(overload_api.model.WorkoutSet::getExerciseOrder).toList());
        var history = workoutService.findHistoryByUserId(request.getUserId());
        assertEquals(3, history.size());
        assertEquals(List.of(first.getExerciseId(), otherId, first.getExerciseId()),
                history.stream().map(overload_api.service.dto.WorkoutHistoryResponse::getExerciseId).toList());
        assertEquals(List.of("Integration Test", "second", "third"),
                history.stream().map(overload_api.service.dto.WorkoutHistoryResponse::getNote).toList());
        assertTrue(history.stream().allMatch(item -> item.getSets().size() == 2));
    }

    /** 登録済みのユーザー・種目を使った正常な入力を生成する。 */
    private WorkoutRequest validRequest() {
        Long userId = insert(jdbcTemplate,
                "INSERT INTO users (name, gender, age, body_weight_kg) VALUES (?, ?, ?, ?)",
                "Integration Test User", "男性", 27, new BigDecimal("70.00"));
        Long exerciseId = insert(jdbcTemplate,
                "INSERT INTO exercises (name, category, barbell, equipment, pattern) VALUES (?, ?, ?, ?, ?)",
                "Integration Test Exercise", "胸", false, "バーベル", "プレス");
        WorkoutSetRequest firstSet = new WorkoutSetRequest();
        firstSet.setSetNumber(1);
        firstSet.setWeightKg(new BigDecimal("50.00"));
        firstSet.setReps(10);
        WorkoutSetRequest secondSet = new WorkoutSetRequest();
        secondSet.setSetNumber(2);
        secondSet.setWeightKg(new BigDecimal("55.00"));
        secondSet.setReps(8);
        WorkoutExerciseRequest exercise = new WorkoutExerciseRequest();
        exercise.setExerciseId(exerciseId);
        exercise.setNote("Integration Test");
        exercise.setSets(List.of(firstSet, secondSet));
        WorkoutRequest request = new WorkoutRequest();
        request.setUserId(userId);
        request.setExercises(List.of(exercise));
        return request;
    }
}
