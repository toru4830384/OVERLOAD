package overload_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import overload_api.exception.ResourceNotFoundException;
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
     * ワークアウトを正常に登録し、
     * 登録したセット一覧を返すことを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsRegisteredSetsForValidRequest()
            throws Exception {

        overload_api.model.WorkoutSet workoutSet =
                new overload_api.model.WorkoutSet();

        workoutSet.setId(1L);
        workoutSet.setSessionId(100L);
        workoutSet.setExerciseId(1L);
        workoutSet.setExerciseOrder(1);
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
                                .jsonPath("$[0].exerciseOrder").value(1))
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
     * 存在しないリソースが指定された場合に、
     * 404エラーと共通エラーレスポンスが返されることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsNotFoundWhenResourceDoesNotExist()
            throws Exception {

        when(workoutService.create(
                any(overload_api.service.dto.WorkoutRequest.class)))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Exercise not found"));

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 9999,
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
                                .status().isNotFound())
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.status").value(404))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.title")
                                .value("Resource not found"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.detail")
                                .value("Exercise not found"));

        verify(workoutService).create(
                any(overload_api.service.dto.WorkoutRequest.class));
    }

    /**
     * userIdがnullの場合に、
     * バリデーションエラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenUserIdIsNull()
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

        verify(workoutService, never()).create(any());
    }

    /**
     * セット番号がnullの場合に、
     * バリデーションエラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenSetNumberIsNull()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "test",
                            "sets": [
                                {
                                    "setNumber": null,
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

        verify(workoutService, never()).create(any());
    }

    /**
     * セット番号が0の場合に、
     * バリデーションエラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenSetNumberIsZero()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "test",
                            "sets": [
                                {
                                    "setNumber": 0,
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

        verify(workoutService, never()).create(any());
    }

    /**
     * 回数がnullの場合に、
     * バリデーションエラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenRepsIsNull()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "test",
                            "sets": [
                                {
                                    "setNumber": 1,
                                    "weightKg": 50.00,
                                    "reps": null
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

        verify(workoutService, never()).create(any());
    }

    /**
     * 回数が0の場合に、
     * バリデーションエラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenRepsIsZero()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "test",
                            "sets": [
                                {
                                    "setNumber": 1,
                                    "weightKg": 50.00,
                                    "reps": 0
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

        verify(workoutService, never()).create(any());
    }

    /**
     * exercisesがnullの場合に、
     * バリデーションエラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenExercisesIsNull()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": null
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

        verify(workoutService, never()).create(any());
    }

    /**
     * exercisesが空の場合に、
     * バリデーションエラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenExercisesIsEmpty()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": []
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

        verify(workoutService, never()).create(any());
    }

    /**
     * exercisesにnull要素が含まれる場合に、
     * 400エラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenExercisesContainsNull()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        null
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

        verifyNoInteractions(workoutService);
    }

    /**
     * exerciseIdがnullの場合に、
     * ネストしたバリデーションが実行されて
     * 400エラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenExerciseIdIsNull()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": null,
                            "note": "test",
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

        verify(workoutService, never()).create(any());
    }

    /**
     * setsがnullの場合に、
     * ネストしたバリデーションが実行されて
     * 400エラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenSetsIsNull()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "test",
                            "sets": null
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

        verify(workoutService, never()).create(any());
    }

    /**
     * setsが空の場合に、
     * ネストしたバリデーションが実行されて
     * 400エラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenSetsIsEmpty()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "test",
                            "sets": []
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

        verify(workoutService, never()).create(any());
    }

    /**
     * setsにnull要素が含まれる場合に、
     * 400エラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenSetsContainsNull()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "test",
                            "sets": [
                                null
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

        verifyNoInteractions(workoutService);
    }

    /**
     * weightKgがnullの場合に、
     * ネストしたバリデーションが実行されて
     * 400エラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenWeightKgIsNull()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "test",
                            "sets": [
                                {
                                    "setNumber": 1,
                                    "weightKg": null,
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

        verify(workoutService, never()).create(any());
    }

    /**
     * weightKgが0の場合に、
     * DecimalMinのバリデーションによって
     * 400エラーになることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void createReturnsBadRequestWhenWeightKgIsZero()
            throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "exercises": [
                        {
                            "exerciseId": 1,
                            "note": "test",
                            "sets": [
                                {
                                    "setNumber": 1,
                                    "weightKg": 0,
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

        verify(workoutService, never()).create(any());
    }

    /**
     * ワークアウト履歴が存在する場合に、
     * 履歴一覧をJSONレスポンスとして返すことを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void findHistoryReturnsHistoryWhenHistoryExists()
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

        verify(workoutService)
                .findHistoryByUserId(1L);
    }

    /**
     * ワークアウト履歴が存在しない場合に、
     * 空のJSON配列を返すことを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void findHistoryReturnsEmptyListWhenHistoryDoesNotExist()
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

        verify(workoutService)
                .findHistoryByUserId(9999L);
    }
}