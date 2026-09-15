package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.controller.dto.WorkoutExerciseRequest;
import overload_api.controller.dto.WorkoutRequest;
import overload_api.controller.dto.WorkoutSetRequest;
import overload_api.controller.dto.WorkoutHistoryResponse;
import overload_api.controller.dto.WorkoutHistoryRow;
import overload_api.mapper.UserMapper;
import overload_api.mapper.WorkoutSessionMapper;
import overload_api.mapper.WorkoutSetMapper;
import overload_api.model.User;
import overload_api.model.WorkoutSet;

class WorkoutServiceTest {

    @Test
    void create_正常系_ワークアウトを登録してセットを返す() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);
        UserMapper userMapper =
                mock(UserMapper.class);
        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper);

        User user = new User();
        user.setId(1L);
        when(userMapper.findById(1L)).thenReturn(user);

        org.mockito.Mockito.doAnswer(invocation -> {
            overload_api.model.WorkoutSession session =
                    invocation.getArgument(0);
            session.setId(100L);
            return null;
        }).when(workoutSessionMapper).insert(
                org.mockito.ArgumentMatchers.any());

        org.mockito.Mockito.doAnswer(invocation -> {
            WorkoutSet set = invocation.getArgument(0);
            set.setId(1L);
            return null;
        }).when(workoutSetMapper).insert(
                org.mockito.ArgumentMatchers.any());

        WorkoutSetRequest setRequest = new WorkoutSetRequest();
        setRequest.setSetNumber(1);
        setRequest.setWeightKg(new BigDecimal("60.00"));
        setRequest.setReps(10);
        setRequest.setNote("単体テスト");

        WorkoutExerciseRequest exerciseRequest =
                new WorkoutExerciseRequest();
        exerciseRequest.setExerciseId(1L);
        exerciseRequest.setSets(List.of(setRequest));

        WorkoutRequest request = new WorkoutRequest();
        request.setUserId(1L);
        request.setExercises(List.of(exerciseRequest));

        List<WorkoutSet> result =
                workoutService.create(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getSessionId());
        assertEquals(1L, result.get(0).getExerciseId());
        assertEquals(1, result.get(0).getSetNumber());
        assertEquals(
                new BigDecimal("60.00"),
                result.get(0).getWeightKg());
        assertEquals(10, result.get(0).getReps());
        assertEquals("単体テスト", result.get(0).getNote());
    }

    @Test
    void create_存在しないユーザーの場合_404例外を返す() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);
        UserMapper userMapper =
                mock(UserMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper);

        WorkoutRequest request = new WorkoutRequest();
        request.setUserId(9999L);

        when(userMapper.findById(9999L)).thenReturn(null);

        org.springframework.web.server.ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> workoutService.create(request));

        assertEquals(
                org.springframework.http.HttpStatus.NOT_FOUND,
                exception.getStatusCode());
    }

    @Test
    void create_複数セットの場合_全セットを登録して返す() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);
        UserMapper userMapper =
                mock(UserMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper);

        User user = new User();
        user.setId(1L);
        when(userMapper.findById(1L)).thenReturn(user);

        org.mockito.Mockito.doAnswer(invocation -> {
            overload_api.model.WorkoutSession session =
                    invocation.getArgument(0);
            session.setId(101L);
            return null;
        }).when(workoutSessionMapper).insert(
                org.mockito.ArgumentMatchers.any());

        org.mockito.Mockito.doAnswer(invocation -> {
            WorkoutSet set = invocation.getArgument(0);
            return null;
        }).when(workoutSetMapper).insert(
                org.mockito.ArgumentMatchers.any());

        WorkoutSetRequest set1 = new WorkoutSetRequest();
        set1.setSetNumber(1);
        set1.setWeightKg(new BigDecimal("60.00"));
        set1.setReps(10);
        set1.setNote("1セット目");

        WorkoutSetRequest set2 = new WorkoutSetRequest();
        set2.setSetNumber(2);
        set2.setWeightKg(new BigDecimal("65.00"));
        set2.setReps(8);
        set2.setNote("2セット目");

        WorkoutExerciseRequest exerciseRequest =
                new WorkoutExerciseRequest();
        exerciseRequest.setExerciseId(1L);
        exerciseRequest.setSets(List.of(set1, set2));

        WorkoutRequest request = new WorkoutRequest();
        request.setUserId(1L);
        request.setExercises(List.of(exerciseRequest));

        List<WorkoutSet> result =
                workoutService.create(request);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(101L, result.get(0).getSessionId());
        assertEquals(1L, result.get(0).getExerciseId());
        assertEquals(1, result.get(0).getSetNumber());
        assertEquals(
                new BigDecimal("60.00"),
                result.get(0).getWeightKg());
        assertEquals(10, result.get(0).getReps());

        assertEquals(101L, result.get(1).getSessionId());
        assertEquals(1L, result.get(1).getExerciseId());
        assertEquals(2, result.get(1).getSetNumber());
        assertEquals(
                new BigDecimal("65.00"),
                result.get(1).getWeightKg());
        assertEquals(8, result.get(1).getReps());
    }

    @Test
    void findHistoryByUserId_履歴がある場合_正しく履歴を返す() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);
        UserMapper userMapper =
                mock(UserMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper);

        WorkoutHistoryRow row = new WorkoutHistoryRow();
        row.setSessionId(100L);
        row.setStartedAt(
                java.time.LocalDateTime.of(2026, 9, 8, 10, 0));
        row.setExerciseId(1L);
        row.setExerciseName("ベンチプレス");
        row.setSetNumber(1);
        row.setWeightKg(new BigDecimal("60.00"));
        row.setReps(10);
        row.setNote("単体テスト");

        when(workoutSetMapper.findHistoryByUserId(1L))
                .thenReturn(List.of(row));

        List<WorkoutHistoryResponse> result =
                workoutService.findHistoryByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        WorkoutHistoryResponse history = result.get(0);

        assertEquals(100L, history.getSessionId());
        assertEquals(1L, history.getExerciseId());
        assertEquals("ベンチプレス", history.getExerciseName());
        assertEquals("単体テスト", history.getNote());

        assertEquals(1, history.getSets().size());
        assertEquals(1, history.getSets().get(0).getSetNumber());
        assertEquals(
                new BigDecimal("60.00"),
                history.getSets().get(0).getWeightKg());
        assertEquals(10, history.getSets().get(0).getReps());
    }

    @Test
    void findHistoryByUserId_複数セットの場合_同じ種目にまとめて返す() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);
        UserMapper userMapper =
                mock(UserMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper);

        WorkoutHistoryRow row1 = new WorkoutHistoryRow();
        row1.setSessionId(100L);
        row1.setStartedAt(
                java.time.LocalDateTime.of(2026, 9, 8, 10, 0));
        row1.setExerciseId(1L);
        row1.setExerciseName("ベンチプレス");
        row1.setSetNumber(1);
        row1.setWeightKg(new BigDecimal("60.00"));
        row1.setReps(10);
        row1.setNote("複数セットテスト");

        WorkoutHistoryRow row2 = new WorkoutHistoryRow();
        row2.setSessionId(100L);
        row2.setStartedAt(
                java.time.LocalDateTime.of(2026, 9, 8, 10, 0));
        row2.setExerciseId(1L);
        row2.setExerciseName("ベンチプレス");
        row2.setSetNumber(2);
        row2.setWeightKg(new BigDecimal("65.00"));
        row2.setReps(8);
        row2.setNote("複数セットテスト");

        when(workoutSetMapper.findHistoryByUserId(1L))
                .thenReturn(List.of(row1, row2));

        List<WorkoutHistoryResponse> result =
                workoutService.findHistoryByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        WorkoutHistoryResponse history = result.get(0);

        assertEquals(100L, history.getSessionId());
        assertEquals(1L, history.getExerciseId());
        assertEquals("ベンチプレス", history.getExerciseName());

        assertEquals(2, history.getSets().size());

        assertEquals(1, history.getSets().get(0).getSetNumber());
        assertEquals(
                new BigDecimal("60.00"),
                history.getSets().get(0).getWeightKg());
        assertEquals(10, history.getSets().get(0).getReps());

        assertEquals(2, history.getSets().get(1).getSetNumber());
        assertEquals(
                new BigDecimal("65.00"),
                history.getSets().get(1).getWeightKg());
        assertEquals(8, history.getSets().get(1).getReps());
    }

    @Test
    void findHistoryByUserId_複数種目の場合_種目ごとに分けて返す() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);
        UserMapper userMapper =
                mock(UserMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper);

        WorkoutHistoryRow benchPress = new WorkoutHistoryRow();
        benchPress.setSessionId(100L);
        benchPress.setStartedAt(
                java.time.LocalDateTime.of(2026, 9, 8, 10, 0));
        benchPress.setExerciseId(1L);
        benchPress.setExerciseName("ベンチプレス");
        benchPress.setSetNumber(1);
        benchPress.setWeightKg(new BigDecimal("60.00"));
        benchPress.setReps(10);
        benchPress.setNote("複数種目テスト");

        WorkoutHistoryRow squat = new WorkoutHistoryRow();
        squat.setSessionId(100L);
        squat.setStartedAt(
                java.time.LocalDateTime.of(2026, 9, 8, 10, 0));
        squat.setExerciseId(2L);
        squat.setExerciseName("スクワット");
        squat.setSetNumber(1);
        squat.setWeightKg(new BigDecimal("80.00"));
        squat.setReps(8);
        squat.setNote("複数種目テスト");

        when(workoutSetMapper.findHistoryByUserId(1L))
                .thenReturn(List.of(benchPress, squat));

        List<WorkoutHistoryResponse> result =
                workoutService.findHistoryByUserId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        WorkoutHistoryResponse benchPressHistory = result.get(0);
        assertEquals(100L, benchPressHistory.getSessionId());
        assertEquals(1L, benchPressHistory.getExerciseId());
        assertEquals("ベンチプレス",
                benchPressHistory.getExerciseName());
        assertEquals(1, benchPressHistory.getSets().size());
        assertEquals(new BigDecimal("60.00"),
                benchPressHistory.getSets().get(0).getWeightKg());
        assertEquals(10,
                benchPressHistory.getSets().get(0).getReps());

        WorkoutHistoryResponse squatHistory = result.get(1);
        assertEquals(100L, squatHistory.getSessionId());
        assertEquals(2L, squatHistory.getExerciseId());
        assertEquals("スクワット",
                squatHistory.getExerciseName());
        assertEquals(1, squatHistory.getSets().size());
        assertEquals(new BigDecimal("80.00"),
                squatHistory.getSets().get(0).getWeightKg());
        assertEquals(8,
                squatHistory.getSets().get(0).getReps());
    }

    @Test
    void findHistoryByUserId_履歴がない場合_空のリストを返す() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);
        UserMapper userMapper =
                mock(UserMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper);

        when(workoutSetMapper.findHistoryByUserId(1L))
                .thenReturn(List.of());

        List<WorkoutHistoryResponse> result =
                workoutService.findHistoryByUserId(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}