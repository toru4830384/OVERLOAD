package overload_api.service;

import org.springframework.stereotype.Service;

import overload_api.mapper.UserMapper;
import overload_api.model.User;
import overload_api.exception.ResourceNotFoundException;
import overload_api.service.command.UserUpdateCommand;

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

        normalizeGender(user);
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
     * @throws IllegalArgumentException 名前・性別・年齢・体重が共通の保存条件に反する場合
     */
    public User update(
            Long id,
            String name,
            String gender,
            Integer age,
            java.math.BigDecimal bodyWeightKg) {

        UserUpdateCommand command = new UserUpdateCommand(id, name, gender, age, bodyWeightKg);
        User user = userMapper.findById(command.userId());

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found: " + id);
        }

        user.setName(command.name());
        user.setGender(command.gender());
        user.setAge(command.age());
        user.setBodyWeightKg(command.bodyWeightKg());

        userMapper.update(user);

        return user;
    }
    /**
     * 既存データの表示名をAPI値へ変換し、プロフィールの選択状態を復元する。
     * 未選択・未知値はnullとして返し、プロフィール更新時に選択を必須にする。
     *
     * @param user 取得したユーザー情報
     */
    private void normalizeGender(User user) {
        String gender = user.getGender();
        user.setGender(gender == null ? null : switch (gender) {
            case "男性", "MALE", "male" -> "male";
            case "女性", "FEMALE", "female" -> "female";
            // 旧データの未選択・未知値は、性別を推測せず再選択を求める。
            default -> null;
        });
    }
}
