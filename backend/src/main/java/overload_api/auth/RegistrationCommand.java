package overload_api.auth;
import java.math.BigDecimal;
import overload_api.domain.validation.*;
/** 新規登録の不変入力。表示名・性別・年齢・体重は既存の保存制約を共有する。 */
public record RegistrationCommand(@LoginId String loginId, @AccountPassword String password,
        @ProfileName String name, @ProfileGender String gender, @ProfileAge Integer age,
        @BodyWeight BigDecimal bodyWeightKg) {
    /** HTTP以外からも同じ条件で検証する。 */
    public RegistrationCommand {
        CommandValidation.validate(new RegistrationInput(loginId, password, name, gender, age, bodyWeightKg));
    }
    /** 検証前のrecord本体を参照せず、構築中の引数を検証する。 */
    private record RegistrationInput(@LoginId String loginId, @AccountPassword String password,
            @ProfileName String name, @ProfileGender String gender, @ProfileAge Integer age,
            @BodyWeight BigDecimal bodyWeightKg) {}
    /** @return ログへ認証情報を出さない文字列表現 */
    @Override public String toString() { return "RegistrationCommand[redacted]"; }
}
