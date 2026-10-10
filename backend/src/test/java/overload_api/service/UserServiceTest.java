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
        user.setGender("male");
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
                "male",
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
        user.setGender("male");
        user.setAge(30);
        user.setBodyWeightKg(
                new BigDecimal("80.00"));

        when(userMapper.findById(1L))
                .thenReturn(user);

        User result =
                userService.update(
                        1L,
                        "田中　透",
                        "male",
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
                "male",
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
                                && "male".equals(updatedUser.getGender())
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
                        "male",
                        30,
                        new BigDecimal("70.00")));

        verify(userMapper, times(1))
                .findById(9999L);

        verify(userMapper, never())
                .update(org.mockito.ArgumentMatchers.any(User.class));
    }
    /** 既存の表示名データもAPI許可値で返す。 */
    @Test
    void restoresLegacyGenderAsApiValue() {
        var mapper = mock(overload_api.mapper.UserMapper.class);
        var user = new overload_api.model.User();
        user.setGender("男性");
        when(mapper.findById(1L)).thenReturn(user);
        assertEquals("male", new UserService(mapper).findById(1L).getGender());
    }
    /** 未選択・未知の既存値を男性や女性に推測変換しない。 */
    @Test
    void legacyUnsetAndUnknownGenderRequireSelection() {
        var mapper = mock(UserMapper.class);
        var service = new UserService(mapper);
        for (String gender : new String[] {null, "none", "未設定", "other"}) {
            var user = new User();
            user.setGender(gender);
            when(mapper.findById(1L)).thenReturn(user);
            org.junit.jupiter.api.Assertions.assertNull(service.findById(1L).getGender());
        }
    }

    /** Serviceの直接呼び出しでも未選択を保存しない。 */
    @Test
    void updateRejectsUnsetGenderBeforeWriting() {
        var mapper = mock(UserMapper.class);
        when(mapper.findById(1L)).thenReturn(new User());
        var service = new UserService(mapper);
        for (String gender : new String[] {null, "none", "other"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.update(1L, "Test", gender, 30, new BigDecimal("70")));
        }
        verify(mapper, never()).update(org.mockito.ArgumentMatchers.any(User.class));
    }
    /** 年齢未指定・負数をDB書き込み前に拒否する。 */
    @Test
    void updateRejectsMissingAndNegativeAgeBeforeWriting() {
        var mapper = mock(UserMapper.class);
        when(mapper.findById(1L)).thenReturn(new User());
        var service = new UserService(mapper);
        for (Integer age : new Integer[] {null, -1}) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.update(1L, "Test", "male", age, new BigDecimal("70")));
        }
        verify(mapper, never()).update(org.mockito.ArgumentMatchers.any(User.class));
    }
}
