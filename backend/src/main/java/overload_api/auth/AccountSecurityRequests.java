package overload_api.auth;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import overload_api.domain.validation.*;
/** セキュリティ設定のHTTP入力。秘密を文字列表現へ展開しない。 */
public final class AccountSecurityRequests {
    private AccountSecurityRequests() {}
    public record PasswordChange(@AccountPassword String currentPassword,@AccountPassword String newPassword) {
        @Override public String toString() { return "PasswordChange[redacted]"; }
    }
    public record CurrentPassword(@AccountPassword String currentPassword) {
        @Override public String toString() { return "CurrentPassword[redacted]"; }
    }
    public record GoogleCredential(@NotBlank @Size(max=16384) String credential) {
        @Override public String toString() { return "GoogleCredential[redacted]"; }
    }
    public record GoogleRegistration(@LoginId String loginId,@ProfileName String name,@ProfileGender String gender,
            @ProfileAge Integer age,@BodyWeight BigDecimal bodyWeightKg) {}
}
