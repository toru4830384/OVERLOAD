package overload_api.auth;
import overload_api.domain.validation.*;
/** Google連携の本人再確認にも、HTTPとService共通のパスワード制約を適用する。 */
public record PasswordConfirmationCommand(@AccountPassword String currentPassword) {
    public PasswordConfirmationCommand { CommandValidation.validate(new Input(currentPassword)); }
    private record Input(@AccountPassword String currentPassword) {}
    @Override public String toString() { return "PasswordConfirmationCommand[redacted]"; }
}
