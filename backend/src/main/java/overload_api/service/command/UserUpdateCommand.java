package overload_api.service.command;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import overload_api.domain.validation.BodyWeight;
import overload_api.domain.validation.CommandValidation;
import overload_api.domain.validation.ProfileAge;
import overload_api.domain.validation.ProfileGender;
import overload_api.domain.validation.ProfileName;

/** 検証済み・変更不能なプロフィール更新Command。HTTP以外の呼び出しにも保存条件を適用する。 */
public record UserUpdateCommand(@NotNull Long userId, @ProfileName String name,
        @ProfileGender String gender, @ProfileAge Integer age, @BodyWeight BigDecimal bodyWeightKg) {

    /** プロフィールの共通保存条件を満たすCommandだけを生成する。 */
    public UserUpdateCommand(Long userId, String name, String gender, Integer age, BigDecimal bodyWeightKg) {
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.bodyWeightKg = bodyWeightKg;
        CommandValidation.validate(this);
    }
}
