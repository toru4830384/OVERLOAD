package overload_api.auth;

/** 認証APIが明示的に公開する応答情報。永続化モデルを含めない。 */
public final class AuthResponses {
    private AuthResponses() {}
    /** 本人の識別情報。アカウントキーだけでは認証は成立しない。 */
    public record IdentityResponse(Long id, String loginId, String accountKey) {}
    /** SPAが変更要求へ付けるCSRFトークンとヘッダー名。 */
    public record CsrfResponse(String token, String headerName) {}
}
