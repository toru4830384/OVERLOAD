package overload_api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import overload_api.service.WorkoutService;
import overload_api.exception.GlobalExceptionHandler;

/** API境界で不正入力を拒否しServiceを呼ばないことを確認する。 */
class ReviewValidationTest {
    @Test
    void missingMalformedAndOffsetlessStartTimeAreRejected() throws Exception {
        var service = mock(WorkoutService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new WorkoutController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        for (String field : new String[] {"", "\"startedAt\":null,", "\"startedAt\":\"invalid\",", "\"startedAt\":\"2026-01-01T10:00:00\","}) {
            mvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON)
                    .content("{" + field + "\"userId\":1,\"exercises\":[{\"exerciseId\":1,\"sets\":[{\"setNumber\":1,\"weightKg\":50,\"reps\":10}]}]}"))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

}
