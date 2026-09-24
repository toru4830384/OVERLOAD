package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.model.WorkoutSet;
import overload_api.service.WorkoutSetService;
import overload_api.controller.dto.WorkoutSetRequest;

class WorkoutSetControllerTest {

    @Test
    void findAll_セットが存在する場合_一覧を返す() {
        WorkoutSetService workoutSetService =
                mock(WorkoutSetService.class);

        WorkoutSet set1 = new WorkoutSet();
        set1.setId(1L);
        set1.setSessionId(1L);
        set1.setExerciseId(1L);
        set1.setSetNumber(1);
        set1.setWeightKg(new BigDecimal("50.00"));
        set1.setReps(10);

        WorkoutSet set2 = new WorkoutSet();
        set2.setId(2L);
        set2.setSessionId(1L);
        set2.setExerciseId(1L);
        set2.setSetNumber(2);
        set2.setWeightKg(new BigDecimal("60.00"));
        set2.setReps(8);

        when(workoutSetService.findAll())
                .thenReturn(List.of(set1, set2));

        WorkoutSetController workoutSetController =
                new WorkoutSetController(workoutSetService);

        List<WorkoutSet> result =
                workoutSetController.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(50, result.get(0).getWeightKg().intValue());
        assertEquals(10, result.get(0).getReps());
        assertEquals(2L, result.get(1).getId());
        assertEquals(60, result.get(1).getWeightKg().intValue());
        assertEquals(8, result.get(1).getReps());
    }

    @Test
    void findAll_セットが存在しない場合_空のリストを返す() {
        WorkoutSetService workoutSetService =
                mock(WorkoutSetService.class);

        when(workoutSetService.findAll())
                .thenReturn(List.of());

        WorkoutSetController workoutSetController =
                new WorkoutSetController(workoutSetService);

        List<WorkoutSet> result =
                workoutSetController.findAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void create_セット情報を指定した場合_セットを作成して返す() {
        WorkoutSetService workoutSetService =
                mock(WorkoutSetService.class);

        WorkoutSetRequest request =
                new WorkoutSetRequest();
        request.setSessionId(1L);
        request.setExerciseId(1L);
        request.setSetNumber(1);
        request.setWeightKg(new BigDecimal("50.00"));
        request.setReps(10);
        request.setNote("テスト");

        WorkoutSet workoutSet = new WorkoutSet();
        workoutSet.setId(1L);
        workoutSet.setSessionId(1L);
        workoutSet.setExerciseId(1L);
        workoutSet.setSetNumber(1);
        workoutSet.setWeightKg(new BigDecimal("50.00"));
        workoutSet.setReps(10);

        when(workoutSetService.create(
                1L,
                1L,
                1,
                new BigDecimal("50.00"),
                10,
                "テスト"
        )).thenReturn(workoutSet);

        WorkoutSetController workoutSetController =
                new WorkoutSetController(workoutSetService);

        WorkoutSet result =
                workoutSetController.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getSessionId());
        assertEquals(1L, result.getExerciseId());
        assertEquals(1, result.getSetNumber());
        assertEquals(
                new BigDecimal("50.00"),
                result.getWeightKg()
        );
        assertEquals(10, result.getReps());

        verify(workoutSetService).create(
                1L,
                1L,
                1,
                new BigDecimal("50.00"),
                10,
                "テスト"
        );
    }
}