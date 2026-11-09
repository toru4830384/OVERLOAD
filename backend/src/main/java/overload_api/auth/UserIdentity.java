package overload_api.auth;
import java.io.Serializable;
/** セッションへ保存する本人情報。パスワードやハッシュは含めない。 */
public record UserIdentity(Long userId, String loginId, String draftNamespace) implements Serializable, java.security.Principal {
    /** @return セッション索引用の短いログインID */
    @Override public String getName() { return loginId; }
    /** @return 旧匿名下書きと混同しないアカウント境界キー */
    public String accountKey() { return userId + ":" + draftNamespace; }
}
