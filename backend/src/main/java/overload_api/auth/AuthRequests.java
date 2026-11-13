package overload_api.auth;
import java.math.BigDecimal;
import overload_api.domain.validation.*;
/** 認証APIの入力。認証情報をtoStringへ展開しない。 */
public final class AuthRequests {
    private AuthRequests() {}
    /** 登録時点で必須プロフィールも受け取り、推測した体重等を保存しない。 */
    public record Register(@LoginId String loginId, @AccountPassword String password,
            @ProfileName String name, @ProfileGender String gender, @ProfileAge Integer age,
            @BodyWeight BigDecimal bodyWeightKg) {
        /** @return 機密値を除いた文字列表現 */
        @Override public String toString() { return "Register[redacted]"; }
    }
    /** 入力サイズを制限し、存在しないIDでも同じ認証失敗形式にする。 */
    public record Login(@LoginId String loginId, @AccountPassword String password) {
        /** @return 機密値を除いた文字列表現 */
        @Override public String toString() { return "Login[redacted]"; }
    }
}
