package overload_api.service;

import org.springframework.stereotype.Service;

import overload_api.mapper.UserMapper;
import overload_api.model.User;
import overload_api.exception.ResourceNotFoundException;

/**
 * ユーザー情報に関する業務処理を提供するサービス。
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    /**
     * UserServiceを生成する。
     *
     * @param userMapper ユーザー情報を操作するMapper
     */
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 指定されたIDのユーザー情報を取得する。
     *
     * @param id ユーザーID
     * @return ユーザー情報
     * @throws ResourceNotFoundException ユーザーが存在しない場合
     */
    public User findById(Long id) {
        User user = userMapper.findById(id);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found: " + id);
        }

        return user;
    }

    /**
     * 指定されたIDのユーザー情報を更新する。
     *
     * @param id ユーザーID
     * @param name ユーザー名
     * @param gender 性別
     * @param age 年齢
     * @param bodyWeightKg 体重（kg）
     * @return 更新後のユーザー情報
     * @throws ResourceNotFoundException ユーザーが存在しない場合
     */
    public User update(
            Long id,
            String name,
            String gender,
            Integer age,
            java.math.BigDecimal bodyWeightKg) {

        User user = userMapper.findById(id);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found: " + id);
        }

        user.setName(name);
        user.setGender(gender);
        user.setAge(age);
        user.setBodyWeightKg(bodyWeightKg);

        userMapper.update(user);

        return user;
    }
}