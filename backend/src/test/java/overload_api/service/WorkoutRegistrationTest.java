package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.exception.WorkoutCaloriesOutOfRangeException;
import overload_api.mapper.ExerciseMapper;
import overload_api.mapper.UserMapper;
import overload_api.mapper.WorkoutSessionMapper;
import overload_api.mapper.WorkoutSetMapper;
import overload_api.service.command.WorkoutCommand;
import overload_api.service.dto.WorkoutExerciseRequest;
import overload_api.service.dto.WorkoutRequest;
import overload_api.service.dto.WorkoutSetRequest;

/** 個別入力だけでなく、計算済みカロリーの保存範囲も確認する。 */
class WorkoutRegistrationTest {

    @Test
    void normalEightyKilogramOneHourWorkoutKeeps170Point10Calories() {
        assertEquals(new BigDecimal("170.10"),
                WorkoutRegistration.calories(WorkoutCommand.from(request("80.00", 10))));
    }

    @Test
    void exactDecimalTwelveTwoMaximumCanBeStored() {
        // 999.90kgと合計2285942880回で、丸め後の計算値が列の上限と一致する。
        var command = WorkoutCommand.from(request("999.90", Integer.MAX_VALUE, 138459233));
        assertEquals(new BigDecimal("9999999999.99"), WorkoutRegistration.calories(command));
    }

    @Test
    void oneAdditionalRepetitionBeyondTheCalorieLimitIsRejected() {
        var command = WorkoutCommand.from(request("999.90", Integer.MAX_VALUE, 138459234));
        assertThrows(WorkoutCaloriesOutOfRangeException.class,
                () -> WorkoutRegistration.calories(command));
    }

    @Test
    void individuallyValidMaximumCountsAreRejectedBeforeAnyDatabaseAccess() {
        var sessionMapper = mock(WorkoutSessionMapper.class);
        var setMapper = mock(WorkoutSetMapper.class);
        var userMapper = mock(UserMapper.class);
        var exerciseMapper = mock(ExerciseMapper.class);
        var service = new WorkoutService(sessionMapper, setMapper, userMapper, exerciseMapper);
        var input = request("999.99", Integer.MAX_VALUE, Integer.MAX_VALUE);

        var failure = assertThrows(WorkoutCaloriesOutOfRangeException.class, () -> service.create(input));

        assertTrue(failure.getMessage().contains("9999999999.99"));
        assertTrue(failure.getMessage().contains("回数・開始日時"));
        verifyNoInteractions(sessionMapper, setMapper, userMapper, exerciseMapper);
    }

    private static WorkoutRequest request(String bodyWeightKg, int... repetitions) {
        List<WorkoutSetRequest> sets = new ArrayList<>();
        for (int index = 0; index < repetitions.length; index++) {
            var set = new WorkoutSetRequest();
            set.setSetNumber(index + 1);
            set.setWeightKg(new BigDecimal("50.00"));
            set.setReps(repetitions[index]);
            sets.add(set);
        }
        var exercise = new WorkoutExerciseRequest();
        exercise.setExerciseId(1L);
        exercise.setNote("");
        exercise.setSets(sets);
        var input = new WorkoutRequest();
        input.setUserId(1L);
        input.setStartedAt(OffsetDateTime.parse("2026-01-01T10:00:00+09:00"));
        input.setFinishedAt(OffsetDateTime.parse("2026-01-01T11:00:00+09:00"));
        input.setBodyWeightKg(new BigDecimal(bodyWeightKg));
        input.setClientRequestId("12345678-1234-1234-1234-123456789abc");
        input.setExercises(List.of(exercise));
        return input;
    }
}
