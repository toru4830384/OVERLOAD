package overload_api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import overload_api.exception.GlobalExceptionHandler;
import overload_api.model.User;
import overload_api.service.UserService;
import overload_api.service.WorkoutService;

/** レビュー4件の境界値をHTTP入力から検証する。 */
class ReviewFourValidationTest {
    @Test
    void profileRejectsMissingWeightAndNameAndAcceptsFiftyCharacters() throws Exception {
        var service = mock(UserService.class);
        when(service.update(anyLong(), anyString(), anyString(), anyInt(), any())).thenReturn(new User());
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        for (String fields : List.of("\"name\":null,\"bodyWeightKg\":70",
                "\"name\":\"" + "a".repeat(51) + "\",\"bodyWeightKg\":70",
                "\"name\":\"Test\",\"bodyWeightKg\":null",
                "\"name\":\"Test\"", "\"bodyWeightKg\":70")) {
            mvc.perform(put("/api/users/1").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"age\":30,\"gender\":\"male\"," + fields + "}"))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
        for (String name : List.of("", "a".repeat(50))) {
            mvc.perform(put("/api/users/1").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"age\":30,\"gender\":\"male\",\"name\":\"" + name + "\",\"bodyWeightKg\":70}"))
                    .andExpect(status().isOk());
        }
        verify(service, times(2)).update(anyLong(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void workoutRejectsUnstorableWeightAndLongNoteBeforeCallingService() throws Exception {
        var service = mock(WorkoutService.class);
        when(service.create(any())).thenReturn(List.of());
        var mvc = MockMvcBuilders.standaloneSetup(new WorkoutController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        String format = """
            {"userId":1,"startedAt":"2026-01-01T10:00:00+09:00",
             "finishedAt":"2026-01-01T11:00:00+09:00","bodyWeightKg":70,
             "clientRequestId":"12345678-1234-1234-1234-123456789abc",
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
