package overload_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORSの設定を行う設定クラス。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * CORSの許可設定を行う。
	 *
	 * @param registry CORS設定用のレジストリ
	 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}