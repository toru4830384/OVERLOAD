package overload_api.auth;
/** セキュリティ設定で公開する項目だけを保持する応答DTO。 */
public final class AccountSecurityResponses {
    private AccountSecurityResponses() {}
    public record Status(boolean hasPassword, boolean googleLinked) {}
}
