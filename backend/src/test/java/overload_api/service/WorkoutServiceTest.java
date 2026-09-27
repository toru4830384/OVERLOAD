package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import overload_api.exception.ResourceNotFoundException;
import overload_api.mapper.ExerciseMapper;
import overload_api.mapper.UserMapper;
import overload_api.mapper.WorkoutSessionMapper;
import overload_api.mapper.WorkoutSetMapper;
import overload_api.mapper.dto.WorkoutHistoryRow;
import overload_api.model.User;
import overload_api.model.WorkoutSession;
import overload_api.model.WorkoutSet;
import overload_api.service.dto.WorkoutExerciseRequest;
import overload_api.service.dto.WorkoutHistoryResponse;
import overload_api.service.dto.WorkoutRequest;
import overload_api.service.dto.WorkoutSetRequest;

/**
 * WorkoutServiceのテストクラス。
 */
class WorkoutServiceTest {

    /**
     * ワークアウトを正常に登録し、
     * 登録したセット一覧を返すことを確認する。
     */
    @Test
    void createReturnsRegisteredSetsForValidRequest() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        User user = new User();
        user.setId(1L);

        when(userMapper.findById(1L))
                .thenReturn(user);

        when(exerciseMapper.findById(1L))
                .thenReturn(mock(overload_api.model.Exercise.class));

        org.mockito.Mockito.doAnswer(invocation -> {
            WorkoutSession session =
                    invocation.getArgument(0);
            session.setId(100L);
            return null;
        }).when(workoutSessionMapper).insert(
                any(WorkoutSession.class));

        org.mockito.Mockito.doAnswer(invocation -> {
            WorkoutSet set =
                    invocation.getArgument(0);
            set.setId(1L);
            return null;
        }).when(workoutSetMapper).insert(
                any(WorkoutSet.class));

        WorkoutSetRequest setRequest =
                new WorkoutSetRequest();

        setRequest.setSetNumber(1);
        setRequest.setWeightKg(
                new BigDecimal("60.00"));
        setRequest.setReps(10);

        WorkoutExerciseRequest exerciseRequest =
                new WorkoutExerciseRequest();

        exerciseRequest.setExerciseId(1L);
        exerciseRequest.setNote("単体テスト");
        exerciseRequest.setSets(
                List.of(setRequest));

        WorkoutRequest request =
                new WorkoutRequest();

        request.setUserId(1L);
        request.setExercises(
                List.of(exerciseRequest));

        List<WorkoutSet> result =
                workoutService.create(request);

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(
                100L,
                result.get(0).getSessionId());

        assertEquals(
                1L,
                result.get(0).getExerciseId());

        assertEquals(
                1,
                result.get(0).getSetNumber());

        assertEquals(
                new BigDecimal("60.00"),
                result.get(0).getWeightKg());

        assertEquals(
                10,
                result.get(0).getReps());

        assertEquals(
                "単体テスト",
                result.get(0).getNote());

        ArgumentCaptor<WorkoutSession> sessionCaptor =
                ArgumentCaptor.forClass(
                        WorkoutSession.class);

        verify(workoutSessionMapper, times(1))
                .insert(sessionCaptor.capture());

        WorkoutSession insertedSession =
                sessionCaptor.getValue();

        assertEquals(
                1L,
                insertedSession.getUserId());

        assertNotNull(
                insertedSession.getStartedAt());

        verify(exerciseMapper, times(1))
                .findById(1L);

        verify(workoutSetMapper, times(1))
                .insert(any(WorkoutSet.class));

        verify(workoutSessionMapper, times(1))
                .updateFinishedAt(100L);
    }

    /**
     * セット情報がnullの場合に例外が発生し、
     * 登録処理が実行されないことを確認する。
     */
    @Test
    void createThrowsExceptionWhenSetsAreNull() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        User user = new User();
        user.setId(1L);

        when(userMapper.findById(1L))
                .thenReturn(user);

        when(exerciseMapper.findById(1L))
                .thenReturn(mock(overload_api.model.Exercise.class));

        WorkoutExerciseRequest exerciseRequest =
                new WorkoutExerciseRequest();

        exerciseRequest.setExerciseId(1L);
        exerciseRequest.setSets(null);

        WorkoutRequest request =
                new WorkoutRequest();

        request.setUserId(1L);
        request.setExercises(
                List.of(exerciseRequest));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> workoutService.create(request));

        assertEquals(
                "Workout sets must not be null or empty",
                exception.getMessage());

        verify(exerciseMapper, times(1))
                .findById(1L);

        verify(workoutSessionMapper, never())
                .insert(any(WorkoutSession.class));

        verify(workoutSetMapper, never())
                .insert(any(WorkoutSet.class));

        verify(workoutSessionMapper, never())
                .updateFinishedAt(anyLong());
    }

    /**
     * セット情報が空の場合に例外が発生し、
     * 登録処理が実行されないことを確認する。
     */
    @Test
    void createThrowsExceptionWhenSetsAreEmpty() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        User user = new User();
        user.setId(1L);

        when(userMapper.findById(1L))
                .thenReturn(user);

        when(exerciseMapper.findById(1L))
                .thenReturn(mock(overload_api.model.Exercise.class));

        WorkoutExerciseRequest exerciseRequest =
                new WorkoutExerciseRequest();

        exerciseRequest.setExerciseId(1L);
        exerciseRequest.setSets(List.of());

        WorkoutRequest request =
                new WorkoutRequest();

        request.setUserId(1L);
        request.setExercises(
                List.of(exerciseRequest));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> workoutService.create(request));

        assertEquals(
                "Workout sets must not be null or empty",
                exception.getMessage());

        verify(exerciseMapper, times(1))
                .findById(1L);

        verify(workoutSessionMapper, never())
                .insert(any(WorkoutSession.class));

        verify(workoutSetMapper, never())
                .insert(any(WorkoutSet.class));

        verify(workoutSessionMapper, never())
                .updateFinishedAt(anyLong());
    }

    /**
     * 存在しないユーザーの場合に、
     * リソース未存在例外が発生することを確認する。
     */
    @Test
    void createThrowsResourceNotFoundExceptionWhenUserDoesNotExist() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        WorkoutRequest request =
                new WorkoutRequest();

        request.setUserId(9999L);

        when(userMapper.findById(9999L))
                .thenReturn(null);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workoutService.create(request));

        assertEquals(
                "User not found",
                exception.getMessage());

        verify(exerciseMapper, never())
                .findById(anyLong());

        verify(workoutSessionMapper, never())
                .insert(any(WorkoutSession.class));

        verify(workoutSetMapper, never())
                .insert(any(WorkoutSet.class));

        verify(workoutSessionMapper, never())
                .updateFinishedAt(anyLong());
    }

    /**
     * 存在しない種目IDが指定された場合に、
     * リソース未存在例外が発生し、
     * 登録処理が実行されないことを確認する。
     */
    @Test
    void createThrowsResourceNotFoundExceptionWhenExerciseDoesNotExist() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        User user = new User();
        user.setId(1L);

        when(userMapper.findById(1L))
                .thenReturn(user);

        when(exerciseMapper.findById(9999L))
                .thenReturn(null);

        WorkoutSetRequest setRequest =
                new WorkoutSetRequest();

        setRequest.setSetNumber(1);
        setRequest.setWeightKg(
                new BigDecimal("60.00"));
        setRequest.setReps(10);

        WorkoutExerciseRequest exerciseRequest =
                new WorkoutExerciseRequest();

        exerciseRequest.setExerciseId(9999L);
        exerciseRequest.setSets(
                List.of(setRequest));

        WorkoutRequest request =
                new WorkoutRequest();

        request.setUserId(1L);
        request.setExercises(
                List.of(exerciseRequest));

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workoutService.create(request));

        assertEquals(
                "Exercise not found",
                exception.getMessage());

        verify(userMapper, times(1))
                .findById(1L);

        verify(exerciseMapper, times(1))
                .findById(9999L);

        verify(workoutSessionMapper, never())
                .insert(any(WorkoutSession.class));

        verify(workoutSetMapper, never())
                .insert(any(WorkoutSet.class));

        verify(workoutSessionMapper, never())
                .updateFinishedAt(anyLong());
    }

    /**
     * 複数セットの場合に、
     * すべてのセットを登録して返すことを確認する。
     * あわせて、種目単位のメモが各セットに
     * 設定されることを確認する。
     */
    @Test
    void createRegistersAllSetsWithExerciseNote() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        User user = new User();
        user.setId(1L);

        when(userMapper.findById(1L))
                .thenReturn(user);

        when(exerciseMapper.findById(1L))
                .thenReturn(mock(overload_api.model.Exercise.class));

        org.mockito.Mockito.doAnswer(invocation -> {
            WorkoutSession session =
                    invocation.getArgument(0);
            session.setId(101L);
            return null;
        }).when(workoutSessionMapper).insert(
                any(WorkoutSession.class));

        org.mockito.Mockito.doAnswer(invocation -> {
            WorkoutSet set =
                    invocation.getArgument(0);
            set.setId(
                    set.getSetNumber().longValue());
            return null;
        }).when(workoutSetMapper).insert(
                any(WorkoutSet.class));

        WorkoutSetRequest set1 =
                new WorkoutSetRequest();

        set1.setSetNumber(1);
        set1.setWeightKg(
                new BigDecimal("60.00"));
        set1.setReps(10);

        WorkoutSetRequest set2 =
                new WorkoutSetRequest();

        set2.setSetNumber(2);
        set2.setWeightKg(
                new BigDecimal("65.00"));
        set2.setReps(8);

        WorkoutExerciseRequest exerciseRequest =
                new WorkoutExerciseRequest();

        exerciseRequest.setExerciseId(1L);
        exerciseRequest.setNote("複数セットテスト");
        exerciseRequest.setSets(
                List.of(set1, set2));

        WorkoutRequest request =
                new WorkoutRequest();

        request.setUserId(1L);
        request.setExercises(
                List.of(exerciseRequest));

        List<WorkoutSet> result =
                workoutService.create(request);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                101L,
                result.get(0).getSessionId());

        assertEquals(
                1L,
                result.get(0).getExerciseId());

        assertEquals(
                1,
                result.get(0).getSetNumber());

        assertEquals(
                new BigDecimal("60.00"),
                result.get(0).getWeightKg());

        assertEquals(
                10,
                result.get(0).getReps());

        assertEquals(
                "複数セットテスト",
                result.get(0).getNote());

        assertEquals(
                101L,
                result.get(1).getSessionId());

        assertEquals(
                1L,
                result.get(1).getExerciseId());

        assertEquals(
                2,
                result.get(1).getSetNumber());

        assertEquals(
                new BigDecimal("65.00"),
                result.get(1).getWeightKg());

        assertEquals(
                8,
                result.get(1).getReps());

        assertEquals(
                "複数セットテスト",
                result.get(1).getNote());

        verify(exerciseMapper, times(1))
                .findById(1L);

        verify(workoutSessionMapper, times(1))
                .insert(any(WorkoutSession.class));

        verify(workoutSetMapper, times(2))
                .insert(any(WorkoutSet.class));

        verify(workoutSessionMapper, times(1))
                .updateFinishedAt(101L);
    }

    /**
     * 複数種目を登録した場合に、
     * リクエスト内の種目順に応じて種目順が設定されることを確認する。
     * 同一種目の複数セットには同じ種目順が設定されることを確認する。
     */
    @Test
    void createSetsExerciseOrderBasedOnRequestOrder() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        User user = new User();
        user.setId(1L);

        when(userMapper.findById(1L))
                .thenReturn(user);

        when(exerciseMapper.findById(1L))
                .thenReturn(mock(overload_api.model.Exercise.class));

        when(exerciseMapper.findById(2L))
                .thenReturn(mock(overload_api.model.Exercise.class));

        org.mockito.Mockito.doAnswer(invocation -> {
            WorkoutSession session =
                    invocation.getArgument(0);
            session.setId(101L);
            return null;
        }).when(workoutSessionMapper).insert(
                any(WorkoutSession.class));

        WorkoutSetRequest firstSet =
                new WorkoutSetRequest();

        firstSet.setSetNumber(1);
        firstSet.setWeightKg(
                new BigDecimal("60.00"));
        firstSet.setReps(10);

        WorkoutSetRequest secondSet =
                new WorkoutSetRequest();

        secondSet.setSetNumber(2);
        secondSet.setWeightKg(
                new BigDecimal("65.00"));
        secondSet.setReps(8);

        WorkoutExerciseRequest firstExercise =
                new WorkoutExerciseRequest();

        firstExercise.setExerciseId(1L);
        firstExercise.setNote("1番目の種目");
        firstExercise.setSets(
                List.of(firstSet, secondSet));

        WorkoutSetRequest thirdSet =
                new WorkoutSetRequest();

        thirdSet.setSetNumber(1);
        thirdSet.setWeightKg(
                new BigDecimal("80.00"));
        thirdSet.setReps(5);

        WorkoutExerciseRequest secondExercise =
                new WorkoutExerciseRequest();

        secondExercise.setExerciseId(2L);
        secondExercise.setNote("2番目の種目");
        secondExercise.setSets(
                List.of(thirdSet));

        WorkoutRequest request =
                new WorkoutRequest();

        request.setUserId(1L);
        request.setExercises(
                List.of(
                        firstExercise,
                        secondExercise));

        List<WorkoutSet> result =
                workoutService.create(request);

        assertNotNull(result);
        assertEquals(3, result.size());

        /*
         * 1番目の種目に含まれる2セットには、
         * 同じ種目順1が設定されることを確認する。
         */
        assertEquals(
                1L,
                result.get(0).getExerciseId());

        assertEquals(
                1,
                result.get(0).getExerciseOrder());

        assertEquals(
                1,
                result.get(0).getSetNumber());

        assertEquals(
                1L,
                result.get(1).getExerciseId());

        assertEquals(
                1,
                result.get(1).getExerciseOrder());

        assertEquals(
                2,
                result.get(1).getSetNumber());

        /*
         * 2番目の種目には、
         * 種目順2が設定されることを確認する。
         */
        assertEquals(
                2L,
                result.get(2).getExerciseId());

        assertEquals(
                2,
                result.get(2).getExerciseOrder());

        assertEquals(
                1,
                result.get(2).getSetNumber());

        verify(userMapper, times(1))
                .findById(1L);

        verify(exerciseMapper, times(1))
                .findById(1L);

        verify(exerciseMapper, times(1))
                .findById(2L);

        verify(workoutSessionMapper, times(1))
                .insert(any(WorkoutSession.class));

        verify(workoutSetMapper, times(3))
                .insert(any(WorkoutSet.class));

        verify(workoutSessionMapper, times(1))
                .updateFinishedAt(101L);
    }

    /**
     * 履歴が存在する場合に、
     * 正しく履歴を返すことを確認する。
     */
    @Test
    void findHistoryByUserIdReturnsHistoryWhenHistoryExists() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        WorkoutHistoryRow row =
                new WorkoutHistoryRow();

        row.setSessionId(100L);
        row.setStartedAt(
                LocalDateTime.of(
                        2026, 9, 8, 10, 0));
        row.setExerciseId(1L);
        row.setExerciseName("ベンチプレス");
        row.setSetNumber(1);
        row.setWeightKg(
                new BigDecimal("60.00"));
        row.setReps(10);
        row.setNote("単体テスト");

        when(workoutSetMapper.findHistoryByUserId(1L))
                .thenReturn(List.of(row));

        List<WorkoutHistoryResponse> result =
                workoutService.findHistoryByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        WorkoutHistoryResponse history =
                result.get(0);

        assertEquals(
                100L,
                history.getSessionId());

        assertEquals(
                LocalDateTime.of(
                        2026, 9, 8, 10, 0),
                history.getStartedAt());

        assertEquals(
                1L,
                history.getExerciseId());

        assertEquals(
                "ベンチプレス",
                history.getExerciseName());

        assertEquals(
                "単体テスト",
                history.getNote());

        assertEquals(
                1,
                history.getSets().size());

        assertEquals(
                1,
                history.getSets()
                        .get(0)
                        .getSetNumber());

        assertEquals(
                new BigDecimal("60.00"),
                history.getSets()
                        .get(0)
                        .getWeightKg());

        assertEquals(
                10,
                history.getSets()
                        .get(0)
                        .getReps());
    }

    /**
     * 複数セットの場合に、
     * 同じ種目のセットをまとめて返すことを確認する。
     * あわせて、種目単位のメモが履歴に
     * 正しく保持されることを確認する。
     */
    @Test
    void findHistoryByUserIdGroupsMultipleSetsForSameExercise() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        WorkoutHistoryRow row1 =
                new WorkoutHistoryRow();

        row1.setSessionId(100L);
        row1.setStartedAt(
                LocalDateTime.of(
                        2026, 9, 8, 10, 0));
        row1.setExerciseId(1L);
        row1.setExerciseName("ベンチプレス");
        row1.setSetNumber(1);
        row1.setWeightKg(
                new BigDecimal("60.00"));
        row1.setReps(10);
        row1.setNote("複数セットテスト");

        WorkoutHistoryRow row2 =
                new WorkoutHistoryRow();

        row2.setSessionId(100L);
        row2.setStartedAt(
                LocalDateTime.of(
                        2026, 9, 8, 10, 0));
        row2.setExerciseId(1L);
        row2.setExerciseName("ベンチプレス");
        row2.setSetNumber(2);
        row2.setWeightKg(
                new BigDecimal("65.00"));
        row2.setReps(8);
        row2.setNote("複数セットテスト");

        when(workoutSetMapper.findHistoryByUserId(1L))
                .thenReturn(
                        List.of(row1, row2));

        List<WorkoutHistoryResponse> result =
                workoutService.findHistoryByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        WorkoutHistoryResponse history =
                result.get(0);

        assertEquals(
                100L,
                history.getSessionId());

        assertEquals(
                1L,
                history.getExerciseId());

        assertEquals(
                "ベンチプレス",
                history.getExerciseName());

        assertEquals(
                "複数セットテスト",
                history.getNote());

        assertEquals(
                2,
                history.getSets().size());

        assertEquals(
                1,
                history.getSets()
                        .get(0)
                        .getSetNumber());

        assertEquals(
                new BigDecimal("60.00"),
                history.getSets()
                        .get(0)
                        .getWeightKg());

        assertEquals(
                10,
                history.getSets()
                        .get(0)
                        .getReps());

        assertEquals(
                2,
                history.getSets()
                        .get(1)
                        .getSetNumber());

        assertEquals(
                new BigDecimal("65.00"),
                history.getSets()
                        .get(1)
                        .getWeightKg());

        assertEquals(
                8,
                history.getSets()
                        .get(1)
                        .getReps());
    }

    /**
     * 複数種目の場合に、
     * 種目ごとに履歴を分けて返すことを確認する。
     */
    @Test
    void findHistoryByUserIdSeparatesHistoryByExercise() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        WorkoutHistoryRow benchPress =
                new WorkoutHistoryRow();

        benchPress.setSessionId(100L);
        benchPress.setStartedAt(
                LocalDateTime.of(
                        2026, 9, 8, 10, 0));
        benchPress.setExerciseId(1L);
        benchPress.setExerciseName("ベンチプレス");
        benchPress.setSetNumber(1);
        benchPress.setWeightKg(
                new BigDecimal("60.00"));
        benchPress.setReps(10);
        benchPress.setNote("複数種目テスト");

        WorkoutHistoryRow squat =
                new WorkoutHistoryRow();

        squat.setSessionId(100L);
        squat.setStartedAt(
                LocalDateTime.of(
                        2026, 9, 8, 10, 0));
        squat.setExerciseId(2L);
        squat.setExerciseName("スクワット");
        squat.setSetNumber(1);
        squat.setWeightKg(
                new BigDecimal("80.00"));
        squat.setReps(8);
        squat.setNote("複数種目テスト");

        when(workoutSetMapper.findHistoryByUserId(1L))
                .thenReturn(
                        List.of(benchPress, squat));

        List<WorkoutHistoryResponse> result =
                workoutService.findHistoryByUserId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        WorkoutHistoryResponse benchPressHistory =
                result.get(0);

        assertEquals(
                100L,
                benchPressHistory.getSessionId());

        assertEquals(
                1L,
                benchPressHistory.getExerciseId());

        assertEquals(
                "ベンチプレス",
                benchPressHistory.getExerciseName());

        assertEquals(
                "複数種目テスト",
                benchPressHistory.getNote());

        assertEquals(
                1,
                benchPressHistory.getSets().size());

        assertEquals(
                new BigDecimal("60.00"),
                benchPressHistory.getSets()
                        .get(0)
                        .getWeightKg());

        assertEquals(
                10,
                benchPressHistory.getSets()
                        .get(0)
                        .getReps());

        WorkoutHistoryResponse squatHistory =
                result.get(1);

        assertEquals(
                100L,
                squatHistory.getSessionId());

        assertEquals(
                2L,
                squatHistory.getExerciseId());

        assertEquals(
                "スクワット",
                squatHistory.getExerciseName());

        assertEquals(
                "複数種目テスト",
                squatHistory.getNote());

        assertEquals(
                1,
                squatHistory.getSets().size());

        assertEquals(
                new BigDecimal("80.00"),
                squatHistory.getSets()
                        .get(0)
                        .getWeightKg());

        assertEquals(
                8,
                squatHistory.getSets()
                        .get(0)
                        .getReps());
    }

    /**
     * 同一セッション内で同じ種目が複数回実施された場合に、
     * 種目の実施順ごとに履歴を分けて返すことを確認する。
     */
    @Test
    void findHistoryByUserIdSeparatesSameExerciseByExerciseOrder() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        WorkoutHistoryRow firstBenchPress =
                new WorkoutHistoryRow();

        firstBenchPress.setSessionId(100L);
        firstBenchPress.setStartedAt(
                LocalDateTime.of(
                        2026, 9, 8, 10, 0));
        firstBenchPress.setExerciseId(1L);
        firstBenchPress.setExerciseName("ベンチプレス");
        firstBenchPress.setExerciseOrder(1);
        firstBenchPress.setSetNumber(1);
        firstBenchPress.setWeightKg(
                new BigDecimal("60.00"));
        firstBenchPress.setReps(10);
        firstBenchPress.setNote("最初のベンチプレス");

        WorkoutHistoryRow squat =
                new WorkoutHistoryRow();

        squat.setSessionId(100L);
        squat.setStartedAt(
                LocalDateTime.of(
                        2026, 9, 8, 10, 0));
        squat.setExerciseId(2L);
        squat.setExerciseName("スクワット");
        squat.setExerciseOrder(2);
        squat.setSetNumber(1);
        squat.setWeightKg(
                new BigDecimal("80.00"));
        squat.setReps(8);
        squat.setNote("スクワット");

        WorkoutHistoryRow secondBenchPress =
                new WorkoutHistoryRow();

        secondBenchPress.setSessionId(100L);
        secondBenchPress.setStartedAt(
                LocalDateTime.of(
                        2026, 9, 8, 10, 0));
        secondBenchPress.setExerciseId(1L);
        secondBenchPress.setExerciseName("ベンチプレス");
        secondBenchPress.setExerciseOrder(3);
        secondBenchPress.setSetNumber(1);
        secondBenchPress.setWeightKg(
                new BigDecimal("50.00"));
        secondBenchPress.setReps(12);
        secondBenchPress.setNote("2回目のベンチプレス");

        when(workoutSetMapper.findHistoryByUserId(1L))
                .thenReturn(
                        List.of(
                                firstBenchPress,
                                squat,
                                secondBenchPress));

        List<WorkoutHistoryResponse> result =
                workoutService.findHistoryByUserId(1L);

        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals(
                1L,
                result.get(0).getExerciseId());
        assertEquals(
                "最初のベンチプレス",
                result.get(0).getNote());
        assertEquals(
                new BigDecimal("60.00"),
                result.get(0).getSets()
                        .get(0)
                        .getWeightKg());

        assertEquals(
                2L,
                result.get(1).getExerciseId());
        assertEquals(
                "スクワット",
                result.get(1).getNote());

        assertEquals(
                1L,
                result.get(2).getExerciseId());
        assertEquals(
                "2回目のベンチプレス",
                result.get(2).getNote());
        assertEquals(
                new BigDecimal("50.00"),
                result.get(2).getSets()
                        .get(0)
                        .getWeightKg());
    }

    /**
     * 履歴が存在しない場合に、
     * 空のリストを返すことを確認する。
     */
    @Test
    void findHistoryByUserIdReturnsEmptyListWhenHistoryDoesNotExist() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        UserMapper userMapper =
                mock(UserMapper.class);

        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        WorkoutService workoutService =
                new WorkoutService(
                        workoutSessionMapper,
                        workoutSetMapper,
                        userMapper,
                        exerciseMapper);

        when(workoutSetMapper.findHistoryByUserId(1L))
                .thenReturn(List.of());

        List<WorkoutHistoryResponse> result =
                workoutService.findHistoryByUserId(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}