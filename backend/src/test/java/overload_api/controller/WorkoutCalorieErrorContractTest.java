package overload_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import overload_api.exception.WorkoutCaloriesOutOfRangeException;
import overload_api.service.WorkoutService;

/** 計算上限超過はDB障害ではなく、編集して再送できる入力拒否として返す。 */
@WebMvcTest(WorkoutController.class)
class WorkoutCalorieErrorContractTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private WorkoutService service;

    @Test
    void excessiveCalculatedCaloriesReturn422AndStableErrorCode() throws Exception {
        when(service.create(any())).thenThrow(new WorkoutCaloriesOutOfRangeException("カロリーが上限を超えています"));
        mockMvc.perform(post("/api/workouts").contentType(MediaType.APPLICATION_JSON).content("""
                {"userId":1,"startedAt":"2026-01-01T10:00:00+09:00",
                 "finishedAt":"2026-01-01T11:00:00+09:00","bodyWeightKg":80,
                 "clientRequestId":"12345678-1234-1234-1234-123456789abc",
                 "exercises":[{"exerciseId":1,"sets":[{"setNumber":1,"weightKg":50,"reps":10}]}]}
                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("WORKOUT_CALORIES_OUT_OF_RANGE"))
                .andExpect(jsonPath("$.detail").value("カロリーが上限を超えています"));
    }
}
