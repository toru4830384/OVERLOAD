package overload_api.auth;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
/** HTTPの入力値ではなく、認証済みセッションから所有者を取得する。 */
public final class CurrentUser {
    private CurrentUser() {}
    /** @return 認証済み本人情報。未認証なら処理を開始しない */
    public static UserIdentity identity() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserIdentity identity)) {
            throw new AuthenticationCredentialsNotFoundException("ログインしてください");
        }
        return identity;
    }
    /** @return 認証済みユーザーID */
    public static Long id() { return identity().userId(); }
}
