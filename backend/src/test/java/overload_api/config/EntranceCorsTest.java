package overload_api.config;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import static org.junit.jupiter.api.Assertions.*;

/** 入口からは公開ヘルスの読み取りだけを許可し、資格情報の境界を保つ。 */
class EntranceCorsTest {
    private static final String ORIGIN = "https://overload-entry.example";
    private static class Registry extends CorsRegistry {
        Map<String, CorsConfiguration> mappings() { return getCorsConfigurations(); }
    }
    private Map<String, CorsConfiguration> mappings(String origin) {
        var registry = new Registry();
        new WebConfig("", origin).addCorsMappings(registry);
        return registry.mappings();
    }
    @Test void defaultsGrantNoNewOrigin() { assertTrue(mappings("").isEmpty()); }
    @Test void onlyHealthGetIsAllowedWithoutCredentials() throws Exception {
        var configs = mappings(ORIGIN);
        assertEquals(java.util.Set.of("/api/health"), configs.keySet());
        var request = new MockHttpServletRequest("GET", "/api/health");
        request.addHeader("Origin", ORIGIN);
        var response = new MockHttpServletResponse();
        assertTrue(new DefaultCorsProcessor().processRequest(configs.get("/api/health"), request, response));
        assertEquals(ORIGIN, response.getHeader("Access-Control-Allow-Origin"));
        assertNull(response.getHeader("Access-Control-Allow-Credentials"));
        assertNull(configs.get("/api/me"));
    }
    @Test void otherOriginsAndWritesAreRejected() throws Exception {
        var config = mappings(ORIGIN).get("/api/health");
        for (String origin : new String[] { ORIGIN, "https://other.example" }) {
            var request = new MockHttpServletRequest("OPTIONS", "/api/health");
            request.addHeader("Origin", origin);
            request.addHeader("Access-Control-Request-Method", origin.equals(ORIGIN) ? "POST" : "GET");
            var response = new MockHttpServletResponse();
            assertFalse(new DefaultCorsProcessor().processRequest(config, request, response));
            assertNull(response.getHeader("Access-Control-Allow-Origin"));
        }
    }
    @Test void wildcardCredentialsPathsAndInsecurePublicOriginsFailClosed() {
        for (String origin : new String[] { "*", "https://*.example", "https://user:pass@example.com",
                "https://example.com/path", "http://example.com", "https://example.com?q=1", "null" }) {
            assertThrows(IllegalArgumentException.class, () -> mappings(origin));
        }
        assertDoesNotThrow(() -> mappings("http://127.0.0.1:5196"));
    }
}
