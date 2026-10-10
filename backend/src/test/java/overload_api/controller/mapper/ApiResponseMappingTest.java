package overload_api.controller.mapper;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import overload_api.controller.dto.*;
import overload_api.model.*;

/** API公開項目のコピーと日時の復元を確認する。 */
class ApiResponseMappingTest {
    @Test
    void userResponseCopiesFieldsWithoutSharingPersistenceModel() {
        var user = new User();
        user.setId(1L);
        user.setName("Test");
        user.setGender("female");
        user.setAge(0);
        user.setBodyWeightKg(new BigDecimal("65.25"));
        var response = UserApiMapper.toResponse(user);
        user.setName("Changed");
        assertEquals(1L, response.getId());
        assertEquals("Test", response.getName());
        assertEquals("female", response.getGender());
        assertEquals(0, response.getAge());
        assertEquals(new BigDecimal("65.25"), response.getBodyWeightKg());
    }

    @Test
    void exerciseMuscleAndRelationshipKeepAllPublicFields() {
        var exercise = new Exercise();
        exercise.setId(7L);
        exercise.setName("Bench");
        exercise.setCategory("Chest");
        exercise.setBarbell(false);
        exercise.setEquipment("Dumbbell");
        exercise.setPattern("Push");
        var response = ExerciseApiMapper.toResponse(exercise);
        assertEquals(7L, response.getId());
        assertEquals("Bench", response.getName());
        assertEquals("Chest", response.getCategory());
        assertEquals(false, response.getBarbell());
        assertEquals("Dumbbell", response.getEquipment());
        assertEquals("Push", response.getPattern());
        var muscle = new Muscle();
        muscle.setId(3L);
        muscle.setName("Chest");
        assertEquals(3L, MuscleApiMapper.toResponse(muscle).getId());
        assertEquals("Chest", MuscleApiMapper.toResponse(muscle).getName());
        var relationship = new ExerciseMuscle();
        relationship.setExerciseId(7L);
        relationship.setMuscleId(3L);
        assertEquals(7L, ExerciseMuscleApiMapper.toResponse(relationship).getExerciseId());
        assertEquals(3L, ExerciseMuscleApiMapper.toResponse(relationship).getMuscleId());
    }

    @Test
    void historyRestoresInstantAndHandlesNullableNoteAndSets() {
        var start = OffsetDateTime.parse("2026-01-01T10:00:00+09:00");
        var history = new overload_api.service.dto.WorkoutHistoryResponse();
        history.setStartedAt(start.atZoneSameInstant(ZoneId.of("Asia/Tokyo")).toLocalDateTime());
        var response = WorkoutApiMapper.toHistoryResponses(List.of(history)).get(0);
        assertEquals(start.toInstant(), response.getStartedAt().toInstant());
        assertNull(response.getNote());
        assertTrue(response.getSets().isEmpty());
    }
}
