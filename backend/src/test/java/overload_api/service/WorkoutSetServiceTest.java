package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.mapper.WorkoutSetMapper;
import overload_api.model.WorkoutSet;

/**
 * WorkoutSetServiceの単体テストを行うクラス。
 */
class WorkoutSetServiceTest {

    /**
     * セットが存在する場合に、
     * Mapperから取得したセット一覧を正しく返すことを確認する。
     */
    @Test
    void findAllReturnsWorkoutSetsWhenDataExists() {
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        WorkoutSetService workoutSetService =
                new WorkoutSetService(workoutSetMapper);

        WorkoutSet set1 =
                new WorkoutSet();

        set1.setId(1L);
        set1.setSessionId(10L);
        set1.setExerciseId(1L);
        set1.setExerciseOrder(1);
        set1.setSetNumber(1);
        set1.setWeightKg(
                new BigDecimal("50.00"));
        set1.setReps(10);
        set1.setNote("1セット目");

        WorkoutSet set2 =
                new WorkoutSet();

        set2.setId(2L);
        set2.setSessionId(10L);
        set2.setExerciseId(1L);
        set2.setExerciseOrder(1);
        set2.setSetNumber(2);
        set2.setWeightKg(
                new BigDecimal("60.00"));
        set2.setReps(8);
        set2.setNote("2セット目");

        when(workoutSetMapper.findAll())
                .thenReturn(List.of(
                        set1,
                        set2));

        List<WorkoutSet> result =
                workoutSetService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                1L,
                result.get(0).getId());

        assertEquals(
                10L,
                result.get(0).getSessionId());

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
                new BigDecimal("50.00"),
                result.get(0).getWeightKg());

        assertEquals(
                10,
                result.get(0).getReps());

        assertEquals(
                "1セット目",
                result.get(0).getNote());

        assertEquals(
                2L,
                result.get(1).getId());

        assertEquals(
                10L,
                result.get(1).getSessionId());

        assertEquals(
                1L,
                result.get(1).getExerciseId());

        assertEquals(
                1,
                result.get(1).getExerciseOrder());

        assertEquals(
                2,
                result.get(1).getSetNumber());

        assertEquals(
                new BigDecimal("60.00"),
                result.get(1).getWeightKg());

        assertEquals(
                8,
                result.get(1).getReps());

        assertEquals(
                "2セット目",
                result.get(1).getNote());

        verify(workoutSetMapper, times(1))
                .findAll();
    }

    /**
     * セットが存在しない場合に、
     * 空のリストを返すことを確認する。
     */
    @Test
    void findAllReturnsEmptyListWhenNoDataExists() {
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        WorkoutSetService workoutSetService =
                new WorkoutSetService(workoutSetMapper);

        when(workoutSetMapper.findAll())
                .thenReturn(List.of());

        List<WorkoutSet> result =
                workoutSetService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(workoutSetMapper, times(1))
                .findAll();
    }
}