package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import overload_api.service.WorkoutService;
import overload_api.service.dto.WorkoutRequest;

/** Springの実際のJSON設定で、整数入力が暗黙に切り捨てられないことを確認する。 */
@WebMvcTest(WorkoutController.class)
class WorkoutIntegerInputContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkoutService workoutService;

    @Test
    void fractionalRepetitionsAndIntegerOverflowAreRejectedBeforeCallingService() throws Exception {
        for (String reps : List.of("10.9", "10.0", "0", "-1", "2147483648")) {
            mockMvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON)
                    .content(request("1", "1", "1", reps)))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(workoutService);
    }

    @Test
    void fractionalIdentifiersAndSetNumbersAreAlsoRejectedBeforeCallingService() throws Exception {
        for (String body : List.of(
                request("1.9", "1", "1", "10"),
                request("1", "1.9", "1", "10"),
                request("1", "1", "1.9", "10"))) {
            mockMvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON)
                    .content(body)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(workoutService);
    }

    @Test
    void validIntegerRepetitionsReachServiceUnchangedIncludingConvertedScientificNotation() throws Exception {
        when(workoutService.create(any())).thenReturn(List.of());
        // フロントで1e2をNumber変換したJSONは100になる。
        List<Integer> values = List.of(1, 100, Integer.MAX_VALUE);
        for (int reps : values) {
            mockMvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON)
                    .content(request("1", "1", "1", Integer.toString(reps))))
                    .andExpect(status().isCreated());
        }
        var captor = ArgumentCaptor.forClass(WorkoutRequest.class);
        verify(workoutService, times(values.size())).create(captor.capture());
        assertEquals(values, captor.getAllValues().stream()
                .map(value -> value.getExercises().get(0).getSets().get(0).getReps())
                .toList());
    }

    private static String request(String userId, String exerciseId, String setNumber, String reps) {
        return """
                {"userId":%s,"startedAt":"2026-01-01T10:00:00+09:00",
                 "finishedAt":"2026-01-01T11:00:00+09:00","bodyWeightKg":70,
                 "clientRequestId":"12345678-1234-1234-1234-123456789abc",
                 "exercises":[{"exerciseId":%s,"note":"test","sets":[
                 {"setNumber":%s,"weightKg":50,"reps":%s}]}]}
                """.formatted(userId, exerciseId, setNumber, reps);
    }
}
