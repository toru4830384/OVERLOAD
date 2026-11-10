package overload_api.auth;
import overload_api.domain.validation.*;
/** 本人確認と新パスワードにHTTP・Service共通の制約を適用する。 */
public record PasswordChangeCommand(@AccountPassword String currentPassword, @AccountPassword String newPassword) {
    public PasswordChangeCommand { CommandValidation.validate(new Input(currentPassword,newPassword)); }
    private record Input(@AccountPassword String currentPassword,@AccountPassword String newPassword) {}
    @Override public String toString() { return "PasswordChangeCommand[redacted]"; }
}
