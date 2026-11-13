package overload_api.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/** 公開環境では、DBのTLS検証とSecure Cookieを起動前に必須確認する。 */
@Configuration
@Profile("render")
public class HostedDeploymentConfig {
    /** @param environment 秘密情報を含む設定。設定値を例外メッセージに含めない。 */
    public HostedDeploymentConfig(Environment environment) {
        validateDatabaseUrl(environment.getProperty("spring.datasource.url"));
        if (environment.getProperty("server.servlet.session.cookie.secure", Boolean.class, true) != Boolean.TRUE
                || environment.acceptsProfiles(org.springframework.core.env.Profiles.of("local"))) {
            throw new IllegalArgumentException("公開環境ではSecure Cookieを有効にし、localプロファイルを外してください。");
        }
    }

    /** @param url 資格情報を別設定へ分離したJDBC URL。ホスト名を含めTLSで検証する。 */
    public static void validateDatabaseUrl(String url) {
        final String message = "公開DBはMySQL形式のURLとsslMode=VERIFY_IDENTITYを指定し、資格情報は別の秘密設定へ入力してください。";
        try {
            if (url == null || !url.startsWith("jdbc:mysql://")) throw new IllegalArgumentException(message);
            URI uri = URI.create(url.substring(5));
            if (uri.getHost() == null || uri.getRawUserInfo() != null || uri.getFragment() != null
                    || uri.getPath() == null || !uri.getPath().matches("/[A-Za-z][A-Za-z0-9_]{0,63}"))
                throw new IllegalArgumentException(message);
            Map<String, String> parameters = new HashMap<>();
            for (String pair : (uri.getRawQuery() == null ? "" : uri.getRawQuery()).split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length != 2) throw new IllegalArgumentException(message);
                String originalKey = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String key = originalKey.toLowerCase(java.util.Locale.ROOT);
                if (key.equals("sslmode") && !originalKey.equals("sslMode"))
                    throw new IllegalArgumentException(message);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                if (parameters.putIfAbsent(key, value) != null) throw new IllegalArgumentException(message);
            }
            if (!"VERIFY_IDENTITY".equals(parameters.get("sslmode"))
                    || parameters.containsKey("user") || parameters.containsKey("password")
                    || "false".equalsIgnoreCase(parameters.get("usessl"))
                    || "false".equalsIgnoreCase(parameters.get("verifyservercertificate")))
                throw new IllegalArgumentException(message);
        } catch (RuntimeException invalid) {
            // URL自体や原因例外をログへ漏らさず、設定の修正方法だけを通知する。
            throw new IllegalArgumentException(message);
        }
    }
}
