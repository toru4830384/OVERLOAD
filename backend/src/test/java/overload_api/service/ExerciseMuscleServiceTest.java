package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.mapper.ExerciseMuscleMapper;
import overload_api.model.ExerciseMuscle;

/**
 * ExerciseMuscleServiceの単体テストを行うクラス。
 */
class ExerciseMuscleServiceTest {

    /**
     * 種目と部位の紐付けが存在する場合に、
     * Mapperから取得した一覧を正しく返すことを確認する。
     */
    @Test
    void findAllReturnsExerciseMusclesWhenDataExists() {
        ExerciseMuscleMapper exerciseMuscleMapper =
                mock(ExerciseMuscleMapper.class);

        ExerciseMuscleService exerciseMuscleService =
                new ExerciseMuscleService(exerciseMuscleMapper);

        ExerciseMuscle exerciseMuscle1 =
                new ExerciseMuscle();
        exerciseMuscle1.setExerciseId(1L);
        exerciseMuscle1.setMuscleId(1L);

        ExerciseMuscle exerciseMuscle2 =
                new ExerciseMuscle();
        exerciseMuscle2.setExerciseId(1L);
        exerciseMuscle2.setMuscleId(2L);

        when(exerciseMuscleMapper.findAll())
                .thenReturn(List.of(
                        exerciseMuscle1,
                        exerciseMuscle2));

        List<ExerciseMuscle> result =
                exerciseMuscleService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                1L,
                result.get(0).getExerciseId());
        assertEquals(
                1L,
                result.get(0).getMuscleId());

        assertEquals(
                1L,
                result.get(1).getExerciseId());
        assertEquals(
                2L,
                result.get(1).getMuscleId());

        verify(exerciseMuscleMapper, times(1))
                .findAll();
    }

    /**
     * 種目と部位の紐付けが存在しない場合に、
     * 空のリストを返すことを確認する。
     */
    @Test
    void findAllReturnsEmptyListWhenNoDataExists() {
        ExerciseMuscleMapper exerciseMuscleMapper =
                mock(ExerciseMuscleMapper.class);

        ExerciseMuscleService exerciseMuscleService =
                new ExerciseMuscleService(exerciseMuscleMapper);

        when(exerciseMuscleMapper.findAll())
                .thenReturn(List.of());

        List<ExerciseMuscle> result =
                exerciseMuscleService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(exerciseMuscleMapper, times(1))
                .findAll();
    }
}