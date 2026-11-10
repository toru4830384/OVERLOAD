package overload_api.auth;
import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import overload_api.mapper.CredentialMapper;
/** パスワード変更前の認証を、同時ログインや古いセッションの再保存後も失効させる。 */
public class CredentialVersionFilter extends OncePerRequestFilter {
    private final CredentialMapper credentials;
    public CredentialVersionFilter(CredentialMapper credentials) { this.credentials=credentials; }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) { return !ApiPaths.path(request).startsWith("/api/"); }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authentication=SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserIdentity identity) {
            Long version=credentials.currentVersion(identity.userId());
            if (version == null || version.longValue() != identity.authVersion()) {
                SecurityContextHolder.clearContext();
                var session=request.getSession(false); if(session != null) session.invalidate();
                SecurityResponses.problem(response,401,"AUTH_REQUIRED","認証情報が変更されました。再度ログインしてください");
                return;
            }
        }
        chain.doFilter(request,response);
    }
}
