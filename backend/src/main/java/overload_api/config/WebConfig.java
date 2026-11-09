package overload_api.config;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
/** 本番は同一オリジンを基本とし、別オリジンは明示設定時だけ許可する。 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] origins;
    /** @param allowedOrigins カンマ区切りの許可元。空ならCORSを許可しない */
    public WebConfig(@Value("${overload.cors.allowed-origins:}") String allowedOrigins) {
        origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        if (Arrays.stream(origins).anyMatch(s -> s.contains("*"))) throw new IllegalArgumentException("CORSのワイルドカードは使用できません");
    }
    /** @param registry 必要なオリジン・メソッド・ヘッダーだけを許可する */
    @Override public void addCorsMappings(CorsRegistry registry) {
        if (origins.length > 0) registry.addMapping("/api/**").allowedOrigins(origins)
            .allowedMethods("GET","POST","PUT","OPTIONS")
            .allowedHeaders("Content-Type","X-CSRF-TOKEN","X-Overload-Account").allowCredentials(true);
    }
    /** @param registry 旧所有者指定を個人データAPIで拒否する */
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OwnershipInputInterceptor()).addPathPatterns("/api/me","/api/workouts/**","/api/workout-sessions","/api/workout-sets");
    }
}
