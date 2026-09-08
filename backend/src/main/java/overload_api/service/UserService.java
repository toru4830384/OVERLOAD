package overload_api.service;

import org.springframework.stereotype.Service;

import overload_api.mapper.UserMapper;
import overload_api.model.User;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findById(Long id) {
        return userMapper.findById(id);
    }

    public User update(
            Long id,
            String name,
            String gender,
            Integer age,
            java.math.BigDecimal bodyWeightKg) {

        User user = userMapper.findById(id);

        if (user == null) {
            return null;
        }

        user.setName(name);
        user.setGender(gender);
        user.setAge(age);
        user.setBodyWeightKg(bodyWeightKg);

        userMapper.update(user);

        return user;
    }

}
