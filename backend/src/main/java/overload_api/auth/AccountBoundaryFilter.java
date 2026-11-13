package overload_api.auth;
import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
/** 別タブでログイン先が変わっても、古い画面の書込や読込を別人へ向けない。 */
public class AccountBoundaryFilter extends OncePerRequestFilter {
    /** @return 個人データAPIだけにアカウント境界を要求する */
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = ApiPaths.path(request);
        return !(p.equals("/api/auth/logout") || p.equals("/api/auth/password") || p.equals("/api/auth/security") || p.equals("/api/auth/google/unlink") || p.startsWith("/api/auth/google/link") || p.equals("/api/me") || p.startsWith("/api/workouts")
                || p.equals("/api/workout-sessions") || p.equals("/api/workout-sets"));
    }
    /** 認証情報と画面の所有者キーを照合し、不一致ならServiceへ進めない。 */
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserIdentity identity
                && !identity.accountKey().equals(request.getHeader("X-Overload-Account"))) {
            SecurityResponses.problem(response, 409, "AUTH_SESSION_CHANGED", "ログイン状態が変わりました。同じアカウントでログインし直してください");
            return;
        }
        chain.doFilter(request, response);
    }
}
