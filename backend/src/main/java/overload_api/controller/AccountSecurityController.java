package overload_api.controller;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import overload_api.auth.*;
/** パスワード変更とGoogle連携。本人IDをリクエストから受け取らない。 */
@RestController
@RequestMapping("/api/auth")
public class AccountSecurityController {
    private final AccountSecurityService security;
    private final GoogleAccountService google;
    private final GoogleTokenVerifier verifier;
    private final GoogleChallengeStore challenges;
    private final SessionLogin login;
    public AccountSecurityController(AccountSecurityService security,GoogleAccountService google,GoogleTokenVerifier verifier,
            GoogleChallengeStore challenges,SessionLogin login) {
        this.security=security;this.google=google;this.verifier=verifier;this.challenges=challenges;this.login=login;
    }
    public record GoogleConfig(boolean enabled,String clientId,boolean registrationPending) {}
    public record ChallengeResponse(String nonce) {}
    public record GoogleResult(String status,AuthResponses.IdentityResponse identity) {}
    @GetMapping("/security") public AccountSecurityResponses.Status status() {return AccountSecurityApiMapper.toResponse(security.status(CurrentUser.identity()));}
    @PostMapping("/password") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody AccountSecurityRequests.PasswordChange body,HttpServletRequest request,HttpServletResponse response) {
        security.changePassword(CurrentUser.identity(),AccountSecurityApiMapper.toCommand(body));
        new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler().logout(request,response,SecurityContextHolder.getContext().getAuthentication());
        new org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler("SESSION").logout(request,response,null);
    }
    @GetMapping("/google/config") public GoogleConfig config(HttpServletRequest request) {
        return new GoogleConfig(verifier.enabled(),verifier.clientId(),challenges.hasPending(request.getSession(false)));
    }
    private void requireEnabled() {if(!verifier.enabled())throw new IllegalArgumentException("Googleログインは準備中です");}
    private void requireAnonymous() {
        var a=SecurityContextHolder.getContext().getAuthentication();
        if(a!=null && a.getPrincipal() instanceof UserIdentity)throw new IllegalArgumentException("既存アカウントの連携はプロフィールから行ってください");
    }
    @PostMapping("/google/challenge") public ChallengeResponse challenge(HttpServletRequest request) {
        requireEnabled();requireAnonymous();return new ChallengeResponse(challenges.issue(request.getSession(),"login",null));
    }
    @PostMapping("/google/login") public GoogleResult googleLogin(@Valid @RequestBody AccountSecurityRequests.GoogleCredential body,HttpServletRequest request,HttpServletResponse response) {
        requireEnabled();requireAnonymous();
        String nonce=challenges.consume(request.getSession(),"login",null);
        String subject=verifier.verify(body.credential(),nonce);
        var identity=google.find(subject);
        if(identity==null){challenges.pending(request.getSession(),subject);return new GoogleResult("registration_required",null);}
        return new GoogleResult("authenticated",login.login(identity,request,response));
    }
    @PostMapping("/google/register") @ResponseStatus(HttpStatus.CREATED)
    public AuthResponses.IdentityResponse googleRegister(@Valid @RequestBody AccountSecurityRequests.GoogleRegistration body,HttpServletRequest request,HttpServletResponse response) {
        requireEnabled();requireAnonymous();
        var identity=google.register(challenges.pendingSubject(request.getSession(false)),AccountSecurityApiMapper.toCommand(body));
        return login.login(identity,request,response);
    }
    @PostMapping("/google/link/challenge")
    public ChallengeResponse linkChallenge(@Valid @RequestBody AccountSecurityRequests.CurrentPassword body,HttpServletRequest request) {
        requireEnabled();var user=CurrentUser.identity();security.confirmPassword(user,AccountSecurityApiMapper.toCommand(body));
        return new ChallengeResponse(challenges.issue(request.getSession(),"link",user));
    }
    @PostMapping("/google/link") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void link(@Valid @RequestBody AccountSecurityRequests.GoogleCredential body,HttpServletRequest request) {
        requireEnabled();var user=CurrentUser.identity();String nonce=challenges.consume(request.getSession(),"link",user);
        google.link(user,verifier.verify(body.credential(),nonce));
    }
}
