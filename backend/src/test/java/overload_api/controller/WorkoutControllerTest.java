package overload_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import overload_api.service.WorkoutService;

/**
 * WorkoutControllerの単体テストを行うクラス。
 */
@WebMvcTest(WorkoutController.class)
class WorkoutControllerTest {

    /**
     * MockMvcを使用してControllerのHTTPリクエストをテストする。
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * WorkoutServiceをモック化する。
     */
    @MockitoBean
    private WorkoutService workoutService;

    /**
     * ワークアウトを正常に登録し、登録したセット一覧を返すことを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void create_正常系_ワークアウトを登録してセットを返す()
            throws Exception {

        overload_api.model.WorkoutSet workoutSet =
                new overload_api.model.WorkoutSet();

        workoutSet.setId(1L);
        workoutSet.setSessionId(100L);
        workoutSet.setExerciseId(1L);
        workoutSet.setSetNumber(1);
        workoutSet.setWeightKg(new BigDecimal("50.00"));
        workoutSet.setReps(10);
        workoutSet.setNote("単体テスト");

        when(workoutService.create(
                any(overload_api.service.dto.WorkoutRequest.class)))
                .thenReturn(List.of(workoutSet));

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "単体テスト",
                            "sets": [
                                {
                                    "setNumber": 1,
                                    "weightKg": 50.00,
                                    "reps": 10
                                }
                            ]
                        }
                    ]
                }
                """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/workouts")
                        .contentType(
                                org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .status().isCreated())
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].id").value(1))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].sessionId").value(100))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].exerciseId").value(1))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].setNumber").value(1))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].weightKg").value(50.00))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].reps").value(10))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].note").value("単体テスト"));

        verify(workoutService).create(
                any(overload_api.service.dto.WorkoutRequest.class));
    }

    /**
     * ワークアウト登録時に必須項目が不足している場合、
     * バリデーションエラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void create_異常系_userIdがnullの場合_バリデーションエラー()
            throws Exception {

        String requestBody = """
                {
                    "userId": null,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "単体テスト",
                            "sets": [
                                {
                                    "setNumber": 1,
                                    "weightKg": 50.00,
                                    "reps": 10
                                }
                            ]
                        }
                    ]
                }
                """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/workouts")
                        .contentType(
                                org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .status().isBadRequest());
    }

    /**
     * ワークアウト履歴が存在する場合に、
     * 履歴一覧をJSONレスポンスとして返すことを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void findHistory_履歴が存在する場合_履歴一覧を返す()
            throws Exception {

        overload_api.service.dto.WorkoutHistorySetResponse set =
                new overload_api.service.dto.WorkoutHistorySetResponse();

        set.setSetNumber(1);
        set.setWeightKg(new BigDecimal("50.00"));
        set.setReps(10);

        overload_api.service.dto.WorkoutHistoryResponse history =
                new overload_api.service.dto.WorkoutHistoryResponse();

        history.setSessionId(100L);
        history.setStartedAt(
                LocalDateTime.of(2026, 9, 9, 10, 0));
        history.setExerciseId(1L);
        history.setExerciseName("ベンチプレス");
        history.setNote("単体テスト");
        history.setSets(List.of(set));

        when(workoutService.findHistoryByUserId(1L))
                .thenReturn(List.of(history));

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/workouts/history")
                        .param("user_id", "1"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .status().isOk())
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].sessionId").value(100))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].exerciseId").value(1))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].exerciseName")
                                .value("ベンチプレス"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].note")
                                .value("単体テスト"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].sets[0].setNumber")
                                .value(1))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].sets[0].weightKg")
                                .value(50.00))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$[0].sets[0].reps")
                                .value(10));
    }

    /**
     * ワークアウト履歴が存在しない場合に、
     * 空のJSON配列を返すことを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void findHistory_履歴が存在しない場合_空のリストを返す()
            throws Exception {

        when(workoutService.findHistoryByUserId(9999L))
                .thenReturn(List.of());

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/workouts/history")
                        .param("user_id", "9999"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .status().isOk())
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.length()").value(0));
    }
}