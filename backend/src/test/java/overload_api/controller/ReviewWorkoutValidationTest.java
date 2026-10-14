package overload_api.controller;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import overload_api.exception.GlobalExceptionHandler;
import overload_api.service.WorkoutService;
/** HTTP入力の重量・メモの保存範囲を検証する。 */
class ReviewWorkoutValidationTest {
    @Test
    void workoutRejectsUnstorableWeightAndLongNoteBeforeCallingService() throws Exception {
        var service = mock(WorkoutService.class);
        when(service.create(any())).thenReturn(List.of());
        var mvc = MockMvcBuilders.standaloneSetup(new WorkoutController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        String format = """
            {"userId":1,"startedAt":"2026-01-01T10:00:00+09:00",
             "exercises":[{"exerciseId":1,"note":"%s","sets":[
             {"setNumber":1,"weightKg":%s,"reps":1}]}]}
            """;
        for (String weight : List.of("10000", "1E4", "50.001", "0.001", "null")) {
            mvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON)
                    .content(format.formatted("", weight))).andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON)
                .content(format.formatted("a".repeat(101), "50"))).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
        for (String weight : List.of("0.01", "9999.99")) {
            mvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON)
                    .content(format.formatted("a".repeat(100), weight))).andExpect(status().isCreated());
        }
        verify(service, times(2)).create(any());
    }
}
