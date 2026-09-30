package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import overload_api.mapper.UserMapper;
import overload_api.model.User;
import overload_api.exception.ResourceNotFoundException;

/**
 * UserServiceの単体テストを行うクラス。
 */
class UserServiceTest {

    /**
     * ユーザーが存在する場合に、
     * 指定されたIDのユーザー情報を返すことを確認する。
     */
    @Test
    void findByIdReturnsUserWhenUserExists() {
        UserMapper userMapper =
                mock(UserMapper.class);

        UserService userService =
                new UserService(userMapper);

        User user =
                new User();

        user.setId(1L);
        user.setName("田中　透");
        user.setGender("男性");
        user.setAge(31);
        user.setBodyWeightKg(
                new BigDecimal("84.00"));

        when(userMapper.findById(1L))
                .thenReturn(user);

        User result =
                userService.findById(1L);

        assertNotNull(result);

        assertEquals(
                1L,
                result.getId());

        assertEquals(
                "田中　透",
                result.getName());

        assertEquals(
                "男性",
                result.getGender());

        assertEquals(
                31,
                result.getAge());

        assertEquals(
                new BigDecimal("84.00"),
                result.getBodyWeightKg());

        verify(userMapper, times(1))
                .findById(1L);
    }

    /**
     * ユーザーが存在しない場合に、
     * ResourceNotFoundExceptionが発生することを確認する。
     */
    @Test
    void findByIdThrowsResourceNotFoundExceptionWhenUserDoesNotExist() {
        UserMapper userMapper =
                mock(UserMapper.class);

        UserService userService =
                new UserService(userMapper);

        when(userMapper.findById(9999L))
                .thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findById(9999L));

        verify(userMapper, times(1))
                .findById(9999L);
    }

    /**
     * ユーザーが存在する場合に、
     * ユーザー情報を更新して返すことを確認する。
     * あわせて、更新後のユーザー情報が
     * Mapperに渡されることを確認する。
     */
    @Test
    void updateUpdatesAndReturnsUserWhenUserExists() {
        UserMapper userMapper =
                mock(UserMapper.class);

        UserService userService =
                new UserService(userMapper);

        User user =
                new User();

        user.setId(1L);
        user.setName("変更前");
        user.setGender("男性");
        user.setAge(30);
        user.setBodyWeightKg(
                new BigDecimal("80.00"));

        when(userMapper.findById(1L))
                .thenReturn(user);

        User result =
                userService.update(
                        1L,
                        "田中　透",
                        "男性",
                        31,
                        new BigDecimal("84.00"));

        assertNotNull(result);

        assertEquals(
                1L,
                result.getId());

        assertEquals(
                "田中　透",
                result.getName());

        assertEquals(
                "男性",
                result.getGender());

        assertEquals(
                31,
                result.getAge());

        assertEquals(
                new BigDecimal("84.00"),
                result.getBodyWeightKg());

        verify(userMapper, times(1))
                .findById(1L);

        /*
         * 更新対象のIDだけでなく、
         * Serviceで設定した各項目がMapperへ
         * 正しく渡されることを確認する。
         */
        verify(userMapper, times(1))
                .update(argThat(updatedUser ->
                        Long.valueOf(1L).equals(updatedUser.getId())
                                && "田中　透".equals(updatedUser.getName())
                                && "男性".equals(updatedUser.getGender())
                                && Integer.valueOf(31)
                                        .equals(updatedUser.getAge())
                                && new BigDecimal("84.00")
                                        .compareTo(
                                                updatedUser.getBodyWeightKg())
                                        == 0));
    }

    /**
     * ユーザーが存在しない場合に、
     * 更新を行わずResourceNotFoundExceptionが発生することを確認する。
     */
    @Test
    void updateThrowsResourceNotFoundExceptionWhenUserDoesNotExist() {
        UserMapper userMapper =
                mock(UserMapper.class);

        UserService userService =
                new UserService(userMapper);

        when(userMapper.findById(9999L))
                .thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.update(
                        9999L,
                        "存在しないユーザー",
                        "男性",
                        30,
                        new BigDecimal("70.00")));

        verify(userMapper, times(1))
                .findById(9999L);

        verify(userMapper, never())
                .update(org.mockito.ArgumentMatchers.any(User.class));
    }
}