package overload_api.auth;

import org.springframework.security.web.csrf.CsrfToken;

/** HTTPの登録入力と応答を、認証サービスの情報へ明示的に変換する。 */
public final class AuthApiMapper {
    private AuthApiMapper() {}
    /** @param request 検証済み登録要求 @return 変更不能な登録コマンド */
    public static RegistrationCommand toCommand(AuthRequests.Register request) {
        return new RegistrationCommand(request.loginId(), request.password(), request.name(),
                request.gender(), request.age(), request.bodyWeightKg());
    }
    /** @param identity 認証された本人 @return 公開する本人情報だけを複写したDTO */
    public static AuthResponses.IdentityResponse toResponse(UserIdentity identity) {
        return new AuthResponses.IdentityResponse(identity.userId(), identity.loginId(), identity.accountKey());
    }
    /** @param token サーバーが発行したトークン @return 変更要求に使うトークン情報 */
    public static AuthResponses.CsrfResponse toResponse(CsrfToken token) {
        return new AuthResponses.CsrfResponse(token.getToken(), token.getHeaderName());
    }
}
