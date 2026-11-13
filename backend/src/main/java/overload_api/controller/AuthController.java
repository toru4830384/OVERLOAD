package overload_api.controller;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import overload_api.auth.*;
import overload_api.auth.AuthResponses.IdentityResponse;
import overload_api.auth.AuthResponses.CsrfResponse;
/** 登録・ログイン・本人確認と、SPAで使うCSRFトークンを提供する。 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountService accounts;
    private final SessionLogin login;
    /** @param accounts 登録認証サービス @param login 共通のセッション認証処理 */
    public AuthController(AccountService accounts, SessionLogin login) { this.accounts=accounts; this.login=login; }
    /** @param token フィルターが生成するトークン @return 画面用トークン */
    @GetMapping("/csrf") public CsrfResponse csrf(CsrfToken token) { return AuthApiMapper.toResponse(token); }
    /** @param request 登録入力 @return 新規登録したID。登録とログインは別の操作。 */
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public IdentityResponse register(@Valid @RequestBody AuthRequests.Register request) {
        UserIdentity identity=accounts.register(AuthApiMapper.toCommand(request));
        return AuthApiMapper.toResponse(identity);
    }
    /** @param input ログイン入力 @param request HTTP要求 @param response HTTP応答 @return 本人情報 */
    @PostMapping("/login")
    public IdentityResponse login(@Valid @RequestBody AuthRequests.Login input, HttpServletRequest request, HttpServletResponse response) {
        UserIdentity identity=accounts.authenticate(input.loginId(),input.password());
        return login.login(identity,request,response);
    }
    /** @return セッションから取得した本人。入力IDは受け取らない。 */
    @GetMapping("/me") public IdentityResponse me() { return AuthApiMapper.toResponse(CurrentUser.identity()); }
    /** @param request HTTP要求 @param response HTTP応答。本人キーの検証後だけセッションを破棄する。 */
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler().logout(
            request, response, SecurityContextHolder.getContext().getAuthentication());
        new org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler("SESSION").logout(request,response,null);
    }
}
