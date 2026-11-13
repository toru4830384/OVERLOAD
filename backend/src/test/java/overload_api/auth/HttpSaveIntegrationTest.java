package overload_api.auth;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import overload_api.support.MySqlIntegrationTest;

/** MockMvcを介さず、実際のTomcat・TCP・Cookie・MySQLで初回保存から再取得まで確認する。 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpSaveIntegrationTest extends MySqlIntegrationTest {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    @LocalServerPort int port;

    /** 各要求は5秒で失敗させ、保存の再送は行わない。 */
    private class Browser {
        final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .connectTimeout(Duration.ofSeconds(5)).build();
        String key = "";
        HttpResponse<String> send(String path, String body, boolean chunked) throws Exception {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api" + path))
                .timeout(Duration.ofSeconds(5)).header("X-Overload-Account", key);
            if (body != null) {
                JsonNode csrf = JSON.readTree(send("/auth/csrf", null, false).body());
                request.header(csrf.get("headerName").asText(), csrf.get("token").asText())
                    .header("Content-Type", "application/json");
                request.POST(chunked ? HttpRequest.BodyPublishers.ofInputStream(
                    () -> new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))) : HttpRequest.BodyPublishers.ofString(body));
            }
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
        JsonNode accepted(String path, String body, int status) throws Exception {
            var response = send(path, body, false);
            assertEquals(status, response.statusCode(), response.body());
            return JSON.readTree(response.body());
        }
        void registerAndLogin() throws Exception {
            String id = "tcp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
            String credentials = "\"loginId\":\"" + id + "\",\"password\":\"tcp-test-long-password\"";
            accepted("/auth/register", "{" + credentials + ",\"name\":\"TCP検証\",\"gender\":\"male\",\"age\":30,\"bodyWeightKg\":80}", 201);
            key = accepted("/auth/login", "{" + credentials + "}", 200).get("accountKey").asText();
        }
    }

    @Test void servesPackagedFrontendWithoutOpeningPrivateApisOrSourceFiles() throws Exception {
        // 公開用ビルドでは実際のReact成果物を同梱する。
        org.junit.jupiter.api.Assumptions.assumeTrue(new org.springframework.core.io.ClassPathResource("static/index.html").exists());
        var browser = new Browser();
        // 正規のルートを直接取得し、APIの401をHTMLへ置き換えないことも確認する。
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/"))
            .timeout(Duration.ofSeconds(5)).build();
        var root = browser.client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, root.statusCode()); assertTrue(root.body().contains("<title>OVERLOAD</title>"));
        var matcher = java.util.regex.Pattern.compile("src=\"(/assets/[^\"]+\\.js)\"").matcher(root.body());
        assertTrue(matcher.find());
        var asset = browser.client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + matcher.group(1)))
            .timeout(Duration.ofSeconds(5)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, asset.statusCode()); assertFalse(asset.body().contains("<html"));
        var privacy = browser.client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/privacy.html"))
            .timeout(Duration.ofSeconds(5)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, privacy.statusCode());
        assertTrue(privacy.body().contains("プライバシーについて"));
        assertTrue(privacy.body().contains("mailto:tttt4830@gmail.com"));
        assertEquals(401, browser.send("/me", null, false).statusCode());
        assertEquals(401, browser.client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/.env")).build(), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(404, browser.client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/assets/missing.js")).build(), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(200, browser.send("/health", null, false).statusCode());
    }

    @Test void freshWorkoutsSaveOnFirstPostAndAreRetrievedOverHttp() throws Exception {
        var browser = new Browser(); browser.registerAndLogin();
        var exercises = browser.accepted("/exercises", null, 200);
        assertFalse(exercises.isEmpty());
        long exercise = exercises.get(0).get("id").asLong();
        for (int count = 1; count <= 3; count++) {
            String payload = "{\"clientRequestId\":\"" + UUID.randomUUID() + "\",\"startedAt\":\"2026-01-01T10:00:00+09:00\","
                + "\"finishedAt\":\"2026-01-01T11:00:00+09:00\",\"bodyWeightKg\":80,\"exercises\":[{\"exerciseId\":" + exercise
                + ",\"note\":\"初回保存の検証\",\"sets\":[{\"setNumber\":1,\"weightKg\":50,\"reps\":10}]}]}";
            // UUIDは各記録で新規。1記録につきPOSTは厳密に1回だけ行う。
            var saved = browser.accepted("/workouts", payload, 201);
            var history = browser.accepted("/workouts/history", null, 200);
            assertEquals(count, history.size());
            JsonNode row = null;
            for (var entry : history) if (entry.get("sessionId").asLong() == saved.get(0).get("sessionId").asLong()) row = entry;
            assertNotNull(row); assertEquals(170.1, row.get("totalCalories").asDouble());
            assertEquals("初回保存の検証", row.get("note").asText());
            assertEquals(10, row.get("sets").get(0).get("reps").asInt());
        }
    }

    @Test void chunkedOversizedBodyAndMalformedJsonReturnErrorsWithoutHanging() throws Exception {
        var browser = new Browser(); browser.registerAndLogin();
        assertEquals(400, browser.send("/workouts", "{", false).statusCode());
        assertEquals(413, browser.send("/workouts", "x".repeat(262145), true).statusCode());
        assertEquals(0, browser.accepted("/workouts/history", null, 200).size());
    }
}
