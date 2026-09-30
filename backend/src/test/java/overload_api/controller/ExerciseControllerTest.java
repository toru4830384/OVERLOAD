package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.model.Exercise;
import overload_api.service.ExerciseService;

/**
 * ExerciseControllerの単体テストを行うクラス。
 */
class ExerciseControllerTest {

    /**
     * 種目が存在する場合に、
     * 種目一覧を返すことを確認する。
     */
    @Test
    void findAllReturnsExercisesWhenExercisesExist() {
        ExerciseService exerciseService =
                mock(ExerciseService.class);

        ExerciseController exerciseController =
                new ExerciseController(exerciseService);

        Exercise exercise1 = new Exercise();
        exercise1.setId(1L);
        exercise1.setName("ベンチプレス");

        Exercise exercise2 = new Exercise();
        exercise2.setId(2L);
        exercise2.setName("スクワット");

        when(exerciseService.findAll())
                .thenReturn(List.of(exercise1, exercise2));

        List<Exercise> result =
                exerciseController.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(
                "ベンチプレス",
                result.get(0).getName());
        assertEquals(2L, result.get(1).getId());
        assertEquals(
                "スクワット",
                result.get(1).getName());
    }

    /**
     * 種目が存在しない場合に、
     * 空のリストを返すことを確認する。
     */
    @Test
    void findAllReturnsEmptyListWhenExercisesDoNotExist() {
        ExerciseService exerciseService =
                mock(ExerciseService.class);

        ExerciseController exerciseController =
                new ExerciseController(exerciseService);

        when(exerciseService.findAll())
                .thenReturn(List.of());

        List<Exercise> result =
                exerciseController.findAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}