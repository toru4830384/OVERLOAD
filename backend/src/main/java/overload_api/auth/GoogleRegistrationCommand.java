package overload_api.auth;
import java.math.BigDecimal;
import overload_api.domain.validation.*;
/** Google本人確認の後でも、必須プロフィールは利用者が入力する。 */
public record GoogleRegistrationCommand(@LoginId String loginId, @ProfileName String name,
        @ProfileGender String gender, @ProfileAge Integer age, @BodyWeight BigDecimal bodyWeightKg) {
    public GoogleRegistrationCommand { CommandValidation.validate(new Input(loginId,name,gender,age,bodyWeightKg)); }
    private record Input(@LoginId String loginId,@ProfileName String name,@ProfileGender String gender,
        @ProfileAge Integer age,@BodyWeight BigDecimal bodyWeightKg) {}
}
