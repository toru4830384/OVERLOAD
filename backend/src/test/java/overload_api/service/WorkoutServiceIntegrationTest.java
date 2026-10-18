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
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class WorkoutServiceIntegrationTest extends MySqlIntegrationTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

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
        var request = validRequest();
        request.setStartedAt(java.time.OffsetDateTime.now().minusHours(1).withNano(0));
        request.setFinishedAt(java.time.OffsetDateTime.now().withNano(0));
        var result = workoutService.create(request);
        var expectedStart = request.getStartedAt().atZoneSameInstant(java.time.ZoneId.of("Asia/Tokyo")).toLocalDateTime();
        assertEquals(expectedStart, jdbcTemplate.queryForObject(
                "SELECT started_at FROM workout_sessions", java.sql.Timestamp.class).toLocalDateTime());
        var finish = jdbcTemplate.queryForObject(
                "SELECT finished_at FROM workout_sessions", java.sql.Timestamp.class).toLocalDateTime();
        assertTrue(java.time.Duration.between(expectedStart, finish).toMinutes() >= 59);
        assertEquals(expectedStart, workoutService.findHistoryByUserId(request.getUserId()).get(0).getStartedAt());
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
        workoutService.create(request);
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions", Integer.class));
        assertEquals(2, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sets", Integer.class));
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

    /** 再読込・プロフィール変更後も保存済みの概算値を維持する。 */
    @Test
    void preservesCaloriesAndWeightSnapshotAcrossProfileChanges() {
        var request = validRequest();
        var result = workoutService.create(request);
        var history = workoutService.findHistoryByUserId(request.getUserId());
        assertEquals(new BigDecimal("150.31"), history.get(0).getTotalCalories());
        assertEquals(new BigDecimal("70.00"), history.get(0).getBodyWeightKg());
        assertEquals(request.getFinishedAt().atZoneSameInstant(java.time.ZoneId.of("Asia/Tokyo")).toLocalDateTime(),
                history.get(0).getFinishedAt());
        jdbcTemplate.update("UPDATE users SET body_weight_kg = 90 WHERE id = ?", request.getUserId());
        assertEquals(new BigDecimal("150.31"), workoutService.findHistoryByUserId(request.getUserId()).get(0).getTotalCalories());
        assertEquals(result.get(0).getId(), workoutService.create(request).get(0).getId());
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions", Integer.class));
        assertEquals(2, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sets", Integer.class));
        request.setBodyWeightKg(new BigDecimal("75.00"));
        assertThrows(overload_api.exception.RequestConflictException.class, () -> workoutService.create(request));
        assertEquals(2, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sets", Integer.class));
    }

    /** 同時再送でもDB上は1セッションだけ登録する。 */
    @Test
    void concurrentRetriesOnlyInsertOneSession() throws Exception {
        var request = validRequest();
        var ready = new java.util.concurrent.CountDownLatch(2);
        var start = new java.util.concurrent.CountDownLatch(1);
        var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<List<overload_api.model.WorkoutSet>> task = () -> {
                ready.countDown();
                start.await();
                return workoutService.create(request);
            };
            var first = pool.submit(task);
            var second = pool.submit(task);
            assertTrue(ready.await(5, java.util.concurrent.TimeUnit.SECONDS));
            start.countDown();
            assertEquals(first.get(10, java.util.concurrent.TimeUnit.SECONDS).get(0).getId(),
                    second.get(10, java.util.concurrent.TimeUnit.SECONDS).get(0).getId());
            assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions", Integer.class));
            assertEquals(2, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sets", Integer.class));
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
    }

    /** HTTP登録・再送・履歴取得を通して計算値とセッション数を確認する。 */
    @Test
    void httpRegistrationAndHistoryPreserveCanonicalCalories() throws Exception {
        var request = validRequest();
        String body = """
                {"userId":%d,"startedAt":"2026-01-01T10:00:00+09:00",
                 "finishedAt":"2026-01-01T11:00:00+09:00","bodyWeightKg":70,
                 "clientRequestId":"%s","exercises":[{"exerciseId":%d,"note":"Integration Test",
                  "sets":[{"setNumber":1,"weightKg":50,"reps":10},{"setNumber":2,"weightKg":55,"reps":8}]}]}
                """.formatted(request.getUserId(), request.getClientRequestId(), request.getExercises().get(0).getExerciseId());
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/workouts")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(body))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
        }
        jdbcTemplate.update("UPDATE users SET body_weight_kg = 90 WHERE id = ?", request.getUserId());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/workouts/history")
                .param("user_id", request.getUserId().toString()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].totalCalories").value(150.31))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].bodyWeightKg").value(70.0))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].finishedAt").value("2026-01-01T11:00:00+09:00"));
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workout_sessions", Integer.class));
    }

    /** レビューの80kg・60分・10回の記録を、プロフィール変更後も170.10kcalで復元する。 */
    @Test
    void httpHistoryKeepsRestCaloriesAndStartWeightAfterReload() throws Exception {
        var request = validRequest();
        String body = """
                {"userId":%d,"startedAt":"2026-01-01T10:00:00+09:00",
                 "finishedAt":"2026-01-01T11:00:00+09:00","bodyWeightKg":80,
                 "clientRequestId":"%s","exercises":[{"exerciseId":%d,"note":"休憩を含む60分",
                  "sets":[{"setNumber":1,"weightKg":50,"reps":10}]}]}
                """.formatted(request.getUserId(), request.getClientRequestId(), request.getExercises().get(0).getExerciseId());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/workouts")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(body))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated());
        assertEquals(new BigDecimal("170.10"), jdbcTemplate.queryForObject(
                "SELECT total_calories FROM workout_sessions", BigDecimal.class));
        assertEquals(new BigDecimal("80.00"), jdbcTemplate.queryForObject(
                "SELECT body_weight_kg FROM workout_sessions", BigDecimal.class));
        // 初回取得と、プロフィールを変えた後の再取得で同じ確定値を返す。
        for (int read = 0; read < 2; read++) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/workouts/history")
                    .param("user_id", request.getUserId().toString()))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].totalCalories").value(170.1))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].bodyWeightKg").value(80.0))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].startedAt").value("2026-01-01T10:00:00+09:00"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].finishedAt").value("2026-01-01T11:00:00+09:00"));
            jdbcTemplate.update("UPDATE users SET body_weight_kg = 90 WHERE id = ?", request.getUserId());
        }
    }

    /** 登録済みのユーザー・種目を使った正常な入力を生成する。 */
    private WorkoutRequest validRequest() {
        Long userId = insert(jdbcTemplate,
                "INSERT INTO users (name, gender, age, body_weight_kg) VALUES (?, ?, ?, ?)",
                "Integration Test User", "male", 27, new BigDecimal("70.00"));
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
        request.setStartedAt(java.time.OffsetDateTime.parse("2026-01-01T10:00:00+09:00"));
        request.setFinishedAt(java.time.OffsetDateTime.parse("2026-01-01T11:00:00+09:00"));
        request.setBodyWeightKg(new BigDecimal("70.00"));
        request.setClientRequestId(java.util.UUID.randomUUID().toString());
        request.setUserId(userId);
        request.setExercises(List.of(exercise));
        return request;
    }
}
