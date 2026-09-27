package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import overload_api.model.User;
import overload_api.service.UserService;

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
     * ユーザーが存在しない場合に、
     * 404エラーとなることを確認する。
     */
    @Test
    void findByIdThrowsNotFoundWhenUserDoesNotExist() {
        when(userService.findById(9999L))
                .thenReturn(null);

        UserController userController =
                new UserController(userService);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userController.findById(9999L));

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode());

        verify(userService)
                .findById(9999L);
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
     * ユーザーが存在しない場合に、
     * 更新時に404エラーとなることを確認する。
     */
    @Test
    void updateThrowsNotFoundWhenUserDoesNotExist() {
        when(userService.update(
                9999L,
                "田中　透",
                "男性",
                31,
                new BigDecimal("84.00")))
                .thenReturn(null);

        UserController userController =
                new UserController(userService);

        overload_api.controller.dto.UserUpdateRequest request =
                new overload_api.controller.dto.UserUpdateRequest();

        request.setName("田中　透");
        request.setGender("男性");
        request.setAge(31);
        request.setBodyWeightKg(
                new BigDecimal("84.00"));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userController.update(
                                9999L,
                                request));

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode());

        verify(userService)
                .update(
                        9999L,
                        "田中　透",
                        "男性",
                        31,
                        new BigDecimal("84.00"));
    }
}