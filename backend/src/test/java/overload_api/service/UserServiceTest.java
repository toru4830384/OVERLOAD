package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import overload_api.mapper.UserMapper;
import overload_api.model.User;

class UserServiceTest {

    @Test
    void findById_ユーザーが存在する場合_ユーザーを返す() {

        UserMapper userMapper = mock(UserMapper.class);
        UserService userService = new UserService(userMapper);

        User user = new User();
        user.setId(1L);
        user.setName("田中　透");
        user.setGender("男性");
        user.setAge(31);
        user.setBodyWeightKg(new BigDecimal("84.00"));

        when(userMapper.findById(1L)).thenReturn(user);

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("田中　透", result.getName());
        assertEquals("男性", result.getGender());
        assertEquals(31, result.getAge());
        assertEquals(new BigDecimal("84.00"), result.getBodyWeightKg());
    }

    @Test
    void findById_ユーザーが存在しない場合_nullを返す() {

        UserMapper userMapper = mock(UserMapper.class);
        UserService userService = new UserService(userMapper);

        when(userMapper.findById(9999L)).thenReturn(null);

        User result = userService.findById(9999L);

        assertEquals(null, result);
     }

    @Test
    void update_ユーザーが存在する場合_ユーザー情報を更新して返す() {

        UserMapper userMapper = mock(UserMapper.class);
        UserService userService = new UserService(userMapper);

        User user = new User();
        user.setId(1L);
        user.setName("変更前");
        user.setGender("男性");
        user.setAge(30);
        user.setBodyWeightKg(new BigDecimal("80.00"));

        when(userMapper.findById(1L)).thenReturn(user);

        User result = userService.update(
                1L,
                "田中　透",
                "男性",
                31,
                new BigDecimal("84.00")
        );

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("田中　透", result.getName());
        assertEquals("男性", result.getGender());
        assertEquals(31, result.getAge());
        assertEquals(new BigDecimal("84.00"), result.getBodyWeightKg());
    }

    @Test
    void update_ユーザーが存在しない場合_nullを返す() {

        UserMapper userMapper = mock(UserMapper.class);
        UserService userService = new UserService(userMapper);

        when(userMapper.findById(9999L)).thenReturn(null);

        User result = userService.update(
                9999L,
                "存在しないユーザー",
                "男性",
                30,
                new BigDecimal("70.00")
        );

        assertEquals(null, result);
    }
}