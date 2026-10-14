package overload_api.service.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import overload_api.mapper.*;
import overload_api.service.WorkoutService;
import overload_api.service.dto.*;

/** HTTP経由以外の入力も同じ保存条件を守り、登録Commandの内容は変更できない。 */
class WorkoutCommandTest {
    private WorkoutRequest input() {
        var set = new WorkoutSetRequest();
        set.setSetNumber(1); set.setWeightKg(new BigDecimal("50.00")); set.setReps(1);
        var exercise = new WorkoutExerciseRequest();
        exercise.setExerciseId(1L); exercise.setSets(new ArrayList<>(List.of(set)));
        var input = new WorkoutRequest();
        input.setUserId(1L); input.setStartedAt(OffsetDateTime.now().minusHours(1));
        input.setExercises(new ArrayList<>(List.of(exercise)));
        return input;
    }
    @Test
    void rejectsLongNoteAndInvalidWeightsBeforeAnyDatabaseAccess() {
        var users = mock(UserMapper.class); var exercises = mock(ExerciseMapper.class);
        var sessions = mock(WorkoutSessionMapper.class); var sets = mock(WorkoutSetMapper.class);
        var service = new WorkoutService(sessions, sets, users, exercises);
        var input = input(); input.getExercises().get(0).setNote("a".repeat(101));
        assertThrows(IllegalArgumentException.class, () -> service.create(input));
        input.getExercises().get(0).setNote(null);
        for (String weight : List.of("10000", "1E4", "50.001", "0.001")) {
            input.getExercises().get(0).getSets().get(0).setWeightKg(new BigDecimal(weight));
            assertThrows(IllegalArgumentException.class, () -> service.create(input));
        }
        verifyNoInteractions(users, exercises, sessions, sets);
    }
    @Test
    void acceptsBoundariesAndFreezesNestedDtoContents() {
        for (String weight : List.of("0.01", "9999.99")) {
            var input = input(); var exercise = input.getExercises().get(0);
            exercise.setNote("a".repeat(100)); exercise.getSets().get(0).setWeightKg(new BigDecimal(weight));
            var command = WorkoutCommand.from(input);
            exercise.setNote("changed"); exercise.getSets().get(0).setReps(99);
            input.getExercises().clear();
            assertEquals("a".repeat(100), command.exercises().get(0).note());
            assertEquals(1, command.exercises().get(0).sets().get(0).reps());
            assertThrows(UnsupportedOperationException.class, () -> command.exercises().clear());
            assertThrows(UnsupportedOperationException.class, () -> command.exercises().get(0).sets().clear());
        }
    }
}
