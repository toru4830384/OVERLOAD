package overload_api.config;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
/** 本番は同一オリジンを基本とし、別オリジンは明示設定時だけ許可する。 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] origins;
    private final String entranceOrigin;
    /** @param allowedOrigins API全体の許可元。空ならAPI全体のCORSを許可しない
     * @param entranceOrigin 公開ヘルスだけを確認する入口のオリジン。空なら無効 */
    public WebConfig(@Value("${overload.cors.allowed-origins:}") String allowedOrigins,
            @Value("${overload.cors.entrance-origin:}") String entranceOrigin) {
        origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        if (Arrays.stream(origins).anyMatch(s -> s.contains("*"))) throw new IllegalArgumentException("CORSのワイルドカードは使用できません");
        this.entranceOrigin = validateEntranceOrigin(entranceOrigin.trim());
    }
    /** @param value 公開入口の正確なオリジン。空なら入口からの確認を許可しない */
    private static String validateEntranceOrigin(String value) {
        if (value.isEmpty()) return value;
        try {
            var uri = java.net.URI.create(value);
            boolean loopback = "localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost());
            if (uri.getHost() == null || value.contains("*") || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null || !uri.getRawPath().isEmpty()
                    || !("https".equals(uri.getScheme()) || (loopback && "http".equals(uri.getScheme()))))
                throw new IllegalArgumentException();
            return value;
        } catch (RuntimeException invalid) {
            throw new IllegalArgumentException("入口にはパスを含まないHTTPSオリジンを指定してください");
        }
    }
    /** @param registry 必要なオリジン・メソッド・ヘッダーだけを許可する */
    @Override public void addCorsMappings(CorsRegistry registry) {
        // 入口へ公開するのは準備状態だけ。Cookie・認証・記録APIへのアクセスは許可しない。
        if (!entranceOrigin.isEmpty()) registry.addMapping("/api/health").allowedOrigins(entranceOrigin)
            .allowedMethods("GET").allowCredentials(false).maxAge(600);
        if (origins.length > 0) registry.addMapping("/api/**").allowedOrigins(origins)
            .allowedMethods("GET","POST","PUT","OPTIONS")
            .allowedHeaders("Content-Type","X-CSRF-TOKEN","X-Overload-Account").allowCredentials(true);
    }
    /** @param registry 旧所有者指定を個人データAPIで拒否する */
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OwnershipInputInterceptor()).addPathPatterns("/api/me","/api/workouts/**","/api/workout-sessions","/api/workout-sets");
    }
}
