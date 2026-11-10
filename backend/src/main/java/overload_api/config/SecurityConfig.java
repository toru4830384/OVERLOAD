package overload_api.config;
import java.time.Clock;
import jakarta.servlet.DispatcherType;
import java.util.Map;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.*;
import org.springframework.security.web.csrf.*;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.*;
import overload_api.auth.*;
/** 本人データAPIをセッション認証で保護し、CSRF・Cookie・JSONエラーを統一する。 */
@Configuration
public class SecurityConfig {
    /** @return アルゴリズム識別子付きの適応型ハッシュ */
    @Bean PasswordEncoder passwordEncoder() {
        return new DelegatingPasswordEncoder("argon2", Map.of("argon2", new Argon2PasswordEncoder(16, 32, 1, 19456, 2)));
    }
    /** @return HTTPセッションを使う認証情報の保存先 */
    @Bean SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }
    /** @return ログイン前後でセッションIDとCSRFトークンを更新する標準戦略 */
    @Bean SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new CompositeSessionAuthenticationStrategy(java.util.List.of(
            new ChangeSessionIdAuthenticationStrategy(), new CsrfAuthenticationStrategy(new HttpSessionCsrfTokenRepository())));
    }
    /** @param http セキュリティ構築設定 @param limit 認証試行上限 @return フィルター構成 */
    @Bean SecurityFilterChain apiSecurity(HttpSecurity http, @Value("${overload.auth.attempts-per-minute:20}") int limit, overload_api.mapper.CredentialMapper credentials) throws Exception {
        return http.cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(org.springframework.security.config.Customizer.withDefaults())
            .securityContext(c -> c.securityContextRepository(securityContextRepository()))
            .requestCache(c -> c.disable())
            .authorizeHttpRequests(a -> a
                // エラーの応答処理と、公開する画面ファイルだけを認証前に配信する。
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/favicon.svg", "/icons.svg", "/privacy.html", "/assets/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf", "/api/auth/google/config", "/api/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/google/challenge", "/api/auth/google/login", "/api/auth/google/register").permitAll()
                .requestMatchers("/api/users/**").denyAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((q, p, ex) -> SecurityResponses.problem(p,401,"AUTH_REQUIRED","ログインしてください"))
                .accessDeniedHandler((q, p, ex) -> SecurityResponses.problem(p,403,
                    ex instanceof CsrfException ? "CSRF_INVALID" : "ACCESS_DENIED", "操作を許可できません。ログイン状態を確認してください")))
            .logout(l -> l.disable())
            .addFilterAfter(new CredentialVersionFilter(credentials), AnonymousAuthenticationFilter.class)
            .addFilterAfter(new AccountBoundaryFilter(), CredentialVersionFilter.class)
            .addFilterBefore(new ApiRequestLimitFilter(), CsrfFilter.class)
            .addFilterBefore(new AuthRateLimitFilter(Clock.systemUTC(),limit), CsrfFilter.class)
            .build();
    }
}
