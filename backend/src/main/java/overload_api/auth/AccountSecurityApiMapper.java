package overload_api.auth;
/** HTTPの入力と変更不能なセキュリティコマンドを変換する。 */
public final class AccountSecurityApiMapper {
    private AccountSecurityApiMapper() {}
    public static AccountSecurityResponses.Status toResponse(AccountSecurityService.Status status) {
        return new AccountSecurityResponses.Status(status.hasPassword(), status.googleLinked());
    }
    public static PasswordConfirmationCommand toCommand(AccountSecurityRequests.CurrentPassword r) {
        return new PasswordConfirmationCommand(r.currentPassword());
    }
    public static PasswordChangeCommand toCommand(AccountSecurityRequests.PasswordChange r) {
        return new PasswordChangeCommand(r.currentPassword(),r.newPassword());
    }
    public static GoogleRegistrationCommand toCommand(AccountSecurityRequests.GoogleRegistration r) {
        return new GoogleRegistrationCommand(r.loginId(),r.name(),r.gender(),r.age(),r.bodyWeightKg());
    }
}
