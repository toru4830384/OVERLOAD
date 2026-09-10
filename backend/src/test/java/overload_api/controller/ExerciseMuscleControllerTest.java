package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.model.ExerciseMuscle;
import overload_api.service.ExerciseMuscleService;

class ExerciseMuscleControllerTest {

    @Test
    void findAll_種目と部位の紐付けが存在する場合_一覧を返す() {
        ExerciseMuscleService exerciseMuscleService =
                mock(ExerciseMuscleService.class);

        ExerciseMuscleController exerciseMuscleController =
                new ExerciseMuscleController(exerciseMuscleService);

        ExerciseMuscle exerciseMuscle1 = new ExerciseMuscle();
        exerciseMuscle1.setExerciseId(1L);
        exerciseMuscle1.setMuscleId(1L);

        ExerciseMuscle exerciseMuscle2 = new ExerciseMuscle();
        exerciseMuscle2.setExerciseId(1L);
        exerciseMuscle2.setMuscleId(2L);

        when(exerciseMuscleService.findAll())
                .thenReturn(List.of(
                        exerciseMuscle1,
                        exerciseMuscle2));

        List<ExerciseMuscle> result =
                exerciseMuscleController.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getExerciseId());
        assertEquals(1L, result.get(0).getMuscleId());
        assertEquals(1L, result.get(1).getExerciseId());
        assertEquals(2L, result.get(1).getMuscleId());
    }
    
    @Test
    void findAll_紐付けが存在しない場合_空のリストを返す() {
        ExerciseMuscleService exerciseMuscleService =
                mock(ExerciseMuscleService.class);

        ExerciseMuscleController exerciseMuscleController =
                new ExerciseMuscleController(exerciseMuscleService);

        when(exerciseMuscleService.findAll())
                .thenReturn(List.of());

        List<ExerciseMuscle> result =
                exerciseMuscleController.findAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}