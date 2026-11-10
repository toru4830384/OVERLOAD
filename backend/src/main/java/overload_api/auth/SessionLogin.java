package overload_api.auth;
import java.util.List;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
/** ローカル認証とGoogle認証で、セッションID・CSRF更新と本人情報保存を統一する。 */
@Component
public class SessionLogin {
    private final SecurityContextRepository contexts;
    private final SessionAuthenticationStrategy sessions;
    private final GoogleChallengeStore challenges;
    public SessionLogin(SecurityContextRepository contexts,SessionAuthenticationStrategy sessions,GoogleChallengeStore challenges) {
        this.contexts=contexts;this.sessions=sessions;this.challenges=challenges;
    }
    public AuthResponses.IdentityResponse login(UserIdentity identity,HttpServletRequest request,HttpServletResponse response) {
        var authentication=UsernamePasswordAuthenticationToken.authenticated(identity,null,List.of(new SimpleGrantedAuthority("ROLE_USER")));
        sessions.onAuthentication(authentication,request,response);
        challenges.clear(request.getSession(false));
        var context=SecurityContextHolder.createEmptyContext();context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);contexts.saveContext(context,request,response);
        return AuthApiMapper.toResponse(identity);
    }
}
