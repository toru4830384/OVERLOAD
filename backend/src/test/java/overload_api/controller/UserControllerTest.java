package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import overload_api.model.User;
import overload_api.service.UserService;
import overload_api.controller.dto.UserUpdateRequest;

class UserControllerTest {

    @Test
    void findById_ユーザーが存在する場合_ユーザーを返す() {
        UserService userService = mock(UserService.class);
        UserController userController =
                new UserController(userService);

        User user = new User();
        user.setId(1L);
        user.setName("田中　透");
        user.setGender("男性");
        user.setAge(31);
        user.setBodyWeightKg(new BigDecimal("84.00"));

        when(userService.findById(1L)).thenReturn(user);

        User result = userController.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("田中　透", result.getName());
        assertEquals("男性", result.getGender());
        assertEquals(31, result.getAge());
        assertEquals(
                new BigDecimal("84.00"),
                result.getBodyWeightKg());
    }
    
    @Test
    void findById_ユーザーが存在しない場合_404例外を返す() {
        UserService userService =
                mock(UserService.class);
        UserController userController =
                new UserController(userService);

        when(userService.findById(9999L))
                .thenReturn(null);

        ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> userController.findById(9999L));

        assertEquals(
                org.springframework.http.HttpStatus.NOT_FOUND,
                exception.getStatusCode());
    }
    
    @Test
    void update_ユーザーが存在する場合_更新したユーザーを返す() {
        UserService userService =
                mock(UserService.class);
        UserController userController =
                new UserController(userService);

        User user = new User();
        user.setId(1L);
        user.setName("田中　透");
        user.setGender("男性");
        user.setAge(31);
        user.setBodyWeightKg(new BigDecimal("84.00"));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("田中　透");
        request.setGender("男性");
        request.setAge(31);
        request.setBodyWeightKg(new BigDecimal("84.00"));

        when(userService.update(
                1L,
                "田中　透",
                "男性",
                31,
                new BigDecimal("84.00")))
                .thenReturn(user);

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
    }
    
    @Test
    void update_年齢がマイナスの場合_400例外を返す() {
        UserService userService =
                mock(UserService.class);
        UserController userController =
                new UserController(userService);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("田中　透");
        request.setGender("男性");
        request.setAge(-1);
        request.setBodyWeightKg(new BigDecimal("84.00"));

        ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> userController.update(1L, request));

        assertEquals(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                exception.getStatusCode());
    }
    
    @Test
    void update_体重が0の場合_400例外を返す() {
        UserService userService =
                mock(UserService.class);
        UserController userController =
                new UserController(userService);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("田中　透");
        request.setGender("男性");
        request.setAge(31);
        request.setBodyWeightKg(BigDecimal.ZERO);

        ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> userController.update(1L, request));

        assertEquals(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                exception.getStatusCode());
    }
    
    @Test
    void update_体重がマイナスの場合_400例外を返す() {
        UserService userService =
                mock(UserService.class);
        UserController userController =
                new UserController(userService);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("田中　透");
        request.setGender("男性");
        request.setAge(31);
        request.setBodyWeightKg(new BigDecimal("-1.00"));

        ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> userController.update(1L, request));

        assertEquals(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                exception.getStatusCode());
    }
    
    @Test
    void update_ユーザーが存在しない場合_404例外を返す() {
        UserService userService =
                mock(UserService.class);
        UserController userController =
                new UserController(userService);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("田中　透");
        request.setGender("男性");
        request.setAge(31);
        request.setBodyWeightKg(new BigDecimal("84.00"));

        when(userService.update(
                9999L,
                "田中　透",
                "男性",
                31,
                new BigDecimal("84.00")))
                .thenReturn(null);

        ResponseStatusException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> userController.update(9999L, request));

        assertEquals(
                org.springframework.http.HttpStatus.NOT_FOUND,
                exception.getStatusCode());
    }
}