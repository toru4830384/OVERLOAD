package overload_api.auth;
import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
/** 認証・登録の過剰な試行を送信元ごとに制限する。未信頼の転送ヘッダーは使わない。 */
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private record Window(long start, int count) {}
    private final Map<String, Window> windows = new HashMap<>();
    private final Clock clock;
    private final int limit;
    /** @param clock 現在時刻 @param limit 1分間の上限。正数のみ。 */
    public AuthRateLimitFilter(Clock clock, int limit) {
        if (limit < 1) throw new IllegalArgumentException("認証試行上限は正数です");
        this.clock = clock; this.limit = limit;
    }
    /** @return パスワード照合と登録にのみ試行制限を適用する */
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getMethod().equals("POST") || !(ApiPaths.path(request).equals("/api/auth/login")
            || ApiPaths.path(request).equals("/api/auth/register") || ApiPaths.path(request).equals("/api/auth/password")
            || ApiPaths.path(request).startsWith("/api/auth/google/"));
    }
    private synchronized boolean accept(String key) {
        long now = clock.millis();
        windows.entrySet().removeIf(e -> now - e.getValue().start() >= 60_000);
        Window previous = windows.get(key);
        // 未知の送信元で無制限にメモリを消費しない。既存キーは通常どおり判定する。
        if (previous == null && windows.size() >= 10_000) return false;
        if (previous != null && previous.count() >= limit) return false;
        windows.put(key, new Window(previous == null ? now : previous.start(), previous == null ? 1 : previous.count() + 1));
        return true;
    }
    /** 過剰な試行はハッシュ処理を行う前に429で拒否する。 */
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (!accept(request.getRemoteAddr())) {
            response.setHeader("Retry-After", "60");
            SecurityResponses.problem(response, 429, "AUTH_RATE_LIMITED", "試行回数が多いため、1分ほど待ってから再試行してください");
            return;
        }
        chain.doFilter(request, response);
    }
}
