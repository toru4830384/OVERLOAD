package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import overload_api.config.HostedDeploymentConfig;
/** 公開時だけの設定誤りを検出し、DB資格情報をログへ漏らさない。 */
class HostedDeploymentConfigTest {
    private static final String URL = "jdbc:mysql://example.tidbcloud.com:4000/overload?sslMode=VERIFY_IDENTITY&connectTimeout=10000&socketTimeout=15000";
    @Test void requiresHostnameVerificationAndSeparateCredentials() {
        assertDoesNotThrow(() -> HostedDeploymentConfig.validateDatabaseUrl(URL));
        for (String input : List.of(URL.replace("VERIFY_IDENTITY", "REQUIRED"), URL + "&password=private-value",
                URL + "&user=private-user", URL + "&sslMode=DISABLED", URL + "&useSSL=false", URL.replace("sslMode", "SSLMODE"),
                URL.replace("example.tidbcloud.com", "name:secret@example.tidbcloud.com"),
                "jdbc:mysql://localhost/overload", "jdbc:mysql://example/overload?sslMode=VERIFY_IDENTITY&sslmode=DISABLED")) {
            var failure = assertThrows(IllegalArgumentException.class, () -> HostedDeploymentConfig.validateDatabaseUrl(input));
            assertFalse(failure.getMessage().contains("private-value"));
            assertFalse(failure.getMessage().contains("name:secret"));
            assertNull(failure.getCause());
        }
    }
    @Test void rejectsLocalProfileAndInsecureCookiesInHostedDeployment() {
        var env = new MockEnvironment().withProperty("spring.datasource.url", URL);
        assertDoesNotThrow(() -> new HostedDeploymentConfig(env));
        env.setProperty("server.servlet.session.cookie.secure", "false");
        assertThrows(IllegalArgumentException.class, () -> new HostedDeploymentConfig(env));
        env.setProperty("server.servlet.session.cookie.secure", "true"); env.setActiveProfiles("render", "local");
        assertThrows(IllegalArgumentException.class, () -> new HostedDeploymentConfig(env));
    }
}
