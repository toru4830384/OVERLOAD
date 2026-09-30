package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import overload_api.model.User;
import overload_api.service.UserService;
import overload_api.exception.GlobalExceptionHandler;
import overload_api.exception.ResourceNotFoundException;

/**
 * UserControllerの単体テストを行うクラス。
 */
class UserControllerTest {

    /**
     * テスト対象のUserService。
     */
    private UserService userService;

    /**
     * HTTPリクエストを模擬するMockMvc。
     */
    private MockMvc mockMvc;

    /**
     * 各テスト実行前に、
     * UserServiceのモックとMockMvcを生成する。
     */
    @BeforeEach
    void setUp() {
        userService =
                mock(UserService.class);

        UserController userController =
                new UserController(userService);

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(userController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    /**
     * ユーザーが存在する場合に、
     * ユーザー情報を返すことを確認する。
     */
    @Test
    void findByIdReturnsUserWhenUserExists() {
        User user = new User();

        user.setId(1L);
        user.setName("田中　透");
        user.setGender("男性");
        user.setAge(31);
        user.setBodyWeightKg(
                new BigDecimal("84.00"));

        when(userService.findById(1L))
                .thenReturn(user);

        UserController userController =
                new UserController(userService);

        User result =
                userController.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("田中　透", result.getName());
        assertEquals("男性", result.getGender());
        assertEquals(31, result.getAge());
        assertEquals(
                new BigDecimal("84.00"),
                result.getBodyWeightKg());

        verify(userService)
                .findById(1L);
    }

    /**
     * 存在しないユーザーの取得で404と共通エラーレスポンスを返す。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void findByIdReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.findById(9999L))
                .thenThrow(new ResourceNotFoundException("User not found: 9999"));

        mockMvc.perform(get("/api/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("User not found: 9999"));

        verify(userService).findById(9999L);
    }

    /**
     * ユーザーが存在する場合に、
     * 更新したユーザー情報を返すことを確認する。
     */
    @Test
    void updateReturnsUpdatedUserWhenUserExists() {
        User user = new User();

        user.setId(1L);
        user.setName("田中　透");
        user.setGender("男性");
        user.setAge(31);
        user.setBodyWeightKg(
                new BigDecimal("84.00"));

        when(userService.update(
                1L,
                "田中　透",
                "男性",
                31,
                new BigDecimal("84.00")))
                .thenReturn(user);

        UserController userController =
                new UserController(userService);

        overload_api.controller.dto.UserUpdateRequest request =
                new overload_api.controller.dto.UserUpdateRequest();

        request.setName("田中　透");
        request.setGender("男性");
        request.setAge(31);
        request.setBodyWeightKg(
                new BigDecimal("84.00"));

        User result =
                userController.update(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("田中　透", result.getName());
        assertEquals("男性", result.getGender());
        assertEquals(31, result.getAge());
        assertEquals(
                new BigDecimal("84.00"),
                result.getBodyWeightKg());

        verify(userService)
                .update(
                        1L,
                        "田中　透",
                        "男性",
                        31,
                        new BigDecimal("84.00"));
    }

    /**
     * 年齢がマイナスの場合に、
     * Bean Validationによって400エラーとなることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void updateReturnsBadRequestWhenAgeIsNegative()
            throws Exception {

        String requestBody = """
                {
                    "name": "田中　透",
                    "gender": "男性",
                    "age": -1,
                    "bodyWeightKg": 84.00
                }
                """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(
                        org.springframework.test.web.servlet.result
                                .MockMvcResultMatchers
                                .status()
                                .isBadRequest());

        verify(userService, never())
                .update(
                        anyLong(),
                        any(),
                        any(),
                        any(),
                        any());
    }

    /**
     * 体重が0の場合に、
     * Bean Validationによって400エラーとなることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void updateReturnsBadRequestWhenBodyWeightIsZero()
            throws Exception {

        String requestBody = """
                {
                    "name": "田中　透",
                    "gender": "男性",
                    "age": 31,
                    "bodyWeightKg": 0
                }
                """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(
                        org.springframework.test.web.servlet.result
                                .MockMvcResultMatchers
                                .status()
                                .isBadRequest());

        verify(userService, never())
                .update(
                        anyLong(),
                        any(),
                        any(),
                        any(),
                        any());
    }

    /**
     * 体重がマイナスの場合に、
     * Bean Validationによって400エラーとなることを確認する。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void updateReturnsBadRequestWhenBodyWeightIsNegative()
            throws Exception {

        String requestBody = """
                {
                    "name": "田中　透",
                    "gender": "男性",
                    "age": 31,
                    "bodyWeightKg": -1.00
                }
                """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders
                        .put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(
                        org.springframework.test.web.servlet.result
                                .MockMvcResultMatchers
                                .status()
                                .isBadRequest());

        verify(userService, never())
                .update(
                        anyLong(),
                        any(),
                        any(),
                        any(),
                        any());
    }

    /**
     * 存在しないユーザーの更新で404と共通エラーレスポンスを返す。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void updateReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.update(
                9999L, "田中　透", "男性", 31, new BigDecimal("84.00")))
                .thenThrow(new ResourceNotFoundException("User not found: 9999"));

        mockMvc.perform(put("/api/users/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequestBody()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("User not found: 9999"));

        verify(userService).update(
                9999L, "田中　透", "男性", 31, new BigDecimal("84.00"));
    }

    /**
     * DB制約違反時に400と共通エラーレスポンスを返し、DB詳細を公開しない。
     *
     * @throws Exception MockMvc実行時の例外
     */
    @Test
    void updateReturnsBadRequestWhenDatabaseConstraintIsViolated() throws Exception {
        when(userService.update(
                1L, "田中　透", "男性", 31, new BigDecimal("84.00")))
                .thenThrow(new DataIntegrityViolationException("internal database constraint detail"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequestBody()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value("入力値が不正です"));

        verify(userService).update(
                1L, "田中　透", "男性", 31, new BigDecimal("84.00"));
    }

    /**
     * 入力検証を通過するユーザー更新リクエストを生成する。
     *
     * @return 正常な更新リクエストのJSON
     */
    private String validUpdateRequestBody() {
        return """
                {
                    "name": "田中　透",
                    "gender": "男性",
                    "age": 31,
                    "bodyWeightKg": 84.00
                }
                """;
    }
}
