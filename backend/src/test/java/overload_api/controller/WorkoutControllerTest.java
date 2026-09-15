package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import overload_api.controller.dto.WorkoutExerciseRequest;
import overload_api.controller.dto.WorkoutRequest;
import overload_api.controller.dto.WorkoutSetRequest;
import overload_api.controller.dto.WorkoutHistoryResponse;
import overload_api.model.WorkoutSet;
import overload_api.service.WorkoutService;

class WorkoutControllerTest {

    @Test
    void create_正常系_ワークアウトを登録してセットを返す() {
        WorkoutService workoutService =
                mock(WorkoutService.class);

        WorkoutController workoutController =
                new WorkoutController(workoutService);

        WorkoutSet workoutSet = new WorkoutSet();
        workoutSet.setId(1L);
        workoutSet.setSessionId(100L);
        workoutSet.setExerciseId(1L);
        workoutSet.setSetNumber(1);
        workoutSet.setWeightKg(new BigDecimal("50.00"));
        workoutSet.setReps(10);

        WorkoutSetRequest setRequest =
                new WorkoutSetRequest();
        setRequest.setSessionId(100L);
        setRequest.setExerciseId(1L);
        setRequest.setSetNumber(1);
        setRequest.setWeightKg(new BigDecimal("50.00"));
        setRequest.setReps(10);

        WorkoutExerciseRequest exerciseRequest =
                new WorkoutExerciseRequest();
        exerciseRequest.setExerciseId(1L);
        exerciseRequest.setSets(List.of(setRequest));

        WorkoutRequest request =
                new WorkoutRequest();
        request.setUserId(1L);
        request.setExercises(List.of(exerciseRequest));

        when(workoutService.create(request))
                .thenReturn(List.of(workoutSet));

        List<WorkoutSet> result =
                workoutController.create(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(100L, result.get(0).getSessionId());
        assertEquals(1L, result.get(0).getExerciseId());
        assertEquals(1, result.get(0).getSetNumber());
        assertEquals(
                new BigDecimal("50.00"),
                result.get(0).getWeightKg());
        assertEquals(10, result.get(0).getReps());
    }
    
    @Test
    void create_SQLエラーが発生した場合_400例外を返す() {
        WorkoutService workoutService =
                mock(WorkoutService.class);

        WorkoutController workoutController =
                new WorkoutController(workoutService);

        WorkoutRequest request =
                new WorkoutRequest();
        request.setUserId(1L);
        request.setExercises(List.of());

        org.mockito.Mockito.when(workoutService.create(request))
                .thenThrow(
                        new org.springframework.jdbc.UncategorizedSQLException(
                                "test",
                                "test",
                                new SQLException()));

        org.springframework.web.server.ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> workoutController.create(request));

        assertEquals(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                exception.getStatusCode());
    }
    
    @Test
    void findHistory_履歴が存在する場合_履歴一覧を返す() {
        WorkoutService workoutService =
                mock(WorkoutService.class);

        WorkoutController workoutController =
                new WorkoutController(workoutService);

        overload_api.controller.dto.WorkoutHistorySetResponse set =
                new overload_api.controller.dto.WorkoutHistorySetResponse();
        set.setSetNumber(1);
        set.setWeightKg(new BigDecimal("50.00"));
        set.setReps(10);

        overload_api.controller.dto.WorkoutHistoryResponse history =
                new overload_api.controller.dto.WorkoutHistoryResponse();
        history.setSessionId(100L);
        history.setStartedAt(
                java.time.LocalDateTime.of(2026, 9, 9, 10, 0));
        history.setExerciseId(1L);
        history.setExerciseName("ベンチプレス");
        history.setNote("単体テスト");
        history.setSets(List.of(set));

        when(workoutService.findHistoryByUserId(1L))
                .thenReturn(List.of(history));

        List<WorkoutHistoryResponse> result =
                workoutController.findHistory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(100L, result.get(0).getSessionId());
        assertEquals(
                java.time.LocalDateTime.of(2026, 9, 9, 10, 0),
                result.get(0).getStartedAt());
        assertEquals(1L, result.get(0).getExerciseId());
        assertEquals(
                "ベンチプレス",
                result.get(0).getExerciseName());
        assertEquals(
                "単体テスト",
                result.get(0).getNote());

        assertNotNull(result.get(0).getSets());
        assertEquals(1, result.get(0).getSets().size());
        assertEquals(
                1,
                result.get(0).getSets().get(0).getSetNumber());
        assertEquals(
                new BigDecimal("50.00"),
                result.get(0).getSets().get(0).getWeightKg());
        assertEquals(
                10,
                result.get(0).getSets().get(0).getReps());
    }
    
    @Test
    void findHistory_履歴が存在しない場合_空のリストを返す() {
        WorkoutService workoutService =
                mock(WorkoutService.class);

        WorkoutController workoutController =
                new WorkoutController(workoutService);

        when(workoutService.findHistoryByUserId(9999L))
                .thenReturn(List.of());

        List<WorkoutHistoryResponse> result =
                workoutController.findHistory(9999L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}