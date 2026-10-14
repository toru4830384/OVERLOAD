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

import overload_api.mapper.ExerciseMapper;
import overload_api.model.Exercise;

/**
 * ExerciseServiceの単体テストを行うクラス。
 */
class ExerciseServiceTest {

    /**
     * 種目が存在する場合に、
     * Mapperから取得した種目一覧を正しく返すことを確認する。
     */
    @Test
    void findAllReturnsExercisesWhenDataExists() {
        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        ExerciseService exerciseService =
                new ExerciseService(exerciseMapper);

        Exercise exercise1 =
                new Exercise();
        exercise1.setId(1L);
        exercise1.setName("ベンチプレス");

        Exercise exercise2 =
                new Exercise();
        exercise2.setId(2L);
        exercise2.setName("スクワット");

        when(exerciseMapper.findAll())
                .thenReturn(List.of(
                        exercise1,
                        exercise2));

        List<Exercise> result =
                exerciseService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                1L,
                result.get(0).getId());
        assertEquals(
                "ベンチプレス",
                result.get(0).getName());

        assertEquals(
                2L,
                result.get(1).getId());
        assertEquals(
                "スクワット",
                result.get(1).getName());

        verify(exerciseMapper, times(1))
                .findAll();
    }

    /**
     * 種目が存在しない場合に、
     * 空のリストを返すことを確認する。
     */
    @Test
    void findAllReturnsEmptyListWhenNoDataExists() {
        ExerciseMapper exerciseMapper =
                mock(ExerciseMapper.class);

        ExerciseService exerciseService =
                new ExerciseService(exerciseMapper);

        when(exerciseMapper.findAll())
                .thenReturn(List.of());

        List<Exercise> result =
                exerciseService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(exerciseMapper, times(1))
                .findAll();
    }
}