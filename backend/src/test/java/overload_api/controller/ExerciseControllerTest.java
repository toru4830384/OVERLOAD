package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.model.Exercise;
import overload_api.service.ExerciseService;

class ExerciseControllerTest {

    @Test
    void findAll_種目が存在する場合_種目一覧を返す() {
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
        assertEquals("ベンチプレス", result.get(0).getName());
        assertEquals(2L, result.get(1).getId());
        assertEquals("スクワット", result.get(1).getName());
    }
    
    @Test
    void findAll_種目が存在しない場合_空のリストを返す() {
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