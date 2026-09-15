package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.mapper.WorkoutSetMapper;
import overload_api.model.WorkoutSet;

class WorkoutSetServiceTest {

    @Test
    void findAll_セットが存在する場合_一覧を返す() {
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        WorkoutSetService workoutSetService =
                new WorkoutSetService(workoutSetMapper);

        WorkoutSet set1 = new WorkoutSet();
        set1.setId(1L);
        set1.setSessionId(10L);
        set1.setExerciseId(1L);
        set1.setSetNumber(1);
        set1.setWeightKg(new BigDecimal("50.00"));
        set1.setReps(10);

        WorkoutSet set2 = new WorkoutSet();
        set2.setId(2L);
        set2.setSessionId(10L);
        set2.setExerciseId(1L);
        set2.setSetNumber(2);
        set2.setWeightKg(new BigDecimal("60.00"));
        set2.setReps(8);

        when(workoutSetMapper.findAll())
                .thenReturn(List.of(set1, set2));

        List<WorkoutSet> result =
                workoutSetService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(10L, result.get(0).getSessionId());
        assertEquals(1L, result.get(0).getExerciseId());
        assertEquals(1, result.get(0).getSetNumber());
        assertEquals(new BigDecimal("50.00"), result.get(0).getWeightKg());
        assertEquals(10, result.get(0).getReps());

        assertEquals(2L, result.get(1).getId());
        assertEquals(10L, result.get(1).getSessionId());
        assertEquals(1L, result.get(1).getExerciseId());
        assertEquals(2, result.get(1).getSetNumber());
        assertEquals(new BigDecimal("60.00"), result.get(1).getWeightKg());
        assertEquals(8, result.get(1).getReps());
    }
    
    @Test
    void findAll_セットが存在しない場合_空のリストを返す() {
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        WorkoutSetService workoutSetService =
                new WorkoutSetService(workoutSetMapper);

        when(workoutSetMapper.findAll())
                .thenReturn(List.of());

        List<WorkoutSet> result =
                workoutSetService.findAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    void create_セット情報を指定した場合_セットを作成して返す() {
        WorkoutSetMapper workoutSetMapper =
                mock(WorkoutSetMapper.class);

        WorkoutSetService workoutSetService =
                new WorkoutSetService(workoutSetMapper);

        BigDecimal weightKg = new BigDecimal("50.00");

        WorkoutSet result =
                workoutSetService.create(
                        10L,
                        1L,
                        1,
                        weightKg,
                        10);

        assertNotNull(result);
        assertEquals(10L, result.getSessionId());
        assertEquals(1L, result.getExerciseId());
        assertEquals(1, result.getSetNumber());
        assertEquals(weightKg, result.getWeightKg());
        assertEquals(10, result.getReps());
        verify(workoutSetMapper).insert(result);
    }
}