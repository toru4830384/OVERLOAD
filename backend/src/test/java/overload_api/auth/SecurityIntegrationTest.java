package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import overload_api.support.MySqlIntegrationTest;

/** 実MySQL・Flyway・Securityフィルター・Cookieを通す認証とユーザー分離の結合試験。 */
@AutoConfigureMockMvc
class SecurityIntegrationTest extends MySqlIntegrationTest {
    private static final AtomicInteger sequence=new AtomicInteger();
    private static final JsonMapper JSON=JsonMapper.builder().build();
    private static final String PASSWORD="testing-only-long-password-4830";
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccountService accounts;
    private long exercise;
    @BeforeEach void fixtures() {
        jdbc.update("DELETE FROM SPRING_SESSION_ATTRIBUTES"); jdbc.update("DELETE FROM SPRING_SESSION");
        jdbc.update("DELETE FROM user_credentials"); jdbc.update("DELETE FROM workout_sets");
        jdbc.update("DELETE FROM workout_sessions"); jdbc.update("DELETE FROM exercise_muscles");
        jdbc.update("DELETE FROM exercises"); jdbc.update("DELETE FROM muscle"); jdbc.update("DELETE FROM users");
        exercise=overload_api.support.DatabaseFixtures.insert(jdbc,
            "INSERT INTO exercises(name,category,barbell,equipment,pattern) VALUES('テスト種目','胸',false,'barbell','push_horizontal')");
    }
    private class Browser {
        final Map<String,Cookie> cookies=new HashMap<>();
        String key;
        MvcResult send(MockHttpServletRequestBuilder request) throws Exception {
            if (!cookies.isEmpty()) request.cookie(cookies.values().toArray(Cookie[]::new));
            var result=mvc.perform(request).andReturn();
            for (Cookie cookie: result.getResponse().getCookies()) {
                if (cookie.getMaxAge()==0) cookies.remove(cookie.getName()); else cookies.put(cookie.getName(),cookie);
            }
            return result;
        }
        MvcResult write(String path, String body) throws Exception {
            var csrf=JSON.readTree(send(get("/api/auth/csrf")).getResponse().getContentAsString());
            return send(post(path).contentType("application/json").content(body)
                .header(csrf.get("headerName").asText(),csrf.get("token").asText())
                .header("X-Overload-Account",key==null ? "" : key));
        }
        MvcResult read(String path) throws Exception { return send(get(path).header("X-Overload-Account",key==null ? "" : key)); }
        JsonNode login(String id) throws Exception {
            var result=write("/api/auth/login", "{\"loginId\":\""+id+"\",\"password\":\""+PASSWORD+"\"}");
            assertEquals(200,result.getResponse().getStatus(),result.getResponse().getContentAsString());
            var identity=JSON.readTree(result.getResponse().getContentAsString()); key=identity.get("accountKey").asText(); return identity;
        }
        String register() throws Exception {
            String id="user_"+sequence.incrementAndGet();
            var result=write("/api/auth/register",registration(id));
            assertEquals(201,result.getResponse().getStatus(),result.getResponse().getContentAsString());return id;
        }
    }
    private static String registration(String id) {
        return "{\"loginId\":\""+id+"\",\"password\":\""+PASSWORD+"\",\"name\":\"本人\",\"gender\":\"male\",\"age\":30,\"bodyWeightKg\":80}";
    }
    private String workout(String uuid) {
        return "{\"startedAt\":\"2026-01-01T10:00:00+09:00\",\"finishedAt\":\"2026-01-01T11:00:00+09:00\",\"bodyWeightKg\":80,\"clientRequestId\":\""+uuid+"\",\"exercises\":[{\"exerciseId\":"+exercise+",\"sets\":[{\"setNumber\":1,\"weightKg\":50,\"reps\":10}]}]}";
    }
    @Test void anonymousRequestsCannotReadOrWritePersonalData() throws Exception {
        var browser=new Browser();
        for(String path:List.of("/api/auth/me","/api/me","/api/workouts/history","/api/workout-sessions","/api/workout-sets"))
            assertEquals(401,browser.read(path).getResponse().getStatus(),path);
        assertEquals(401,browser.write("/api/workouts",workout(UUID.randomUUID().toString())).getResponse().getStatus());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM workout_sessions",Integer.class));
    }
    @Test void registrationPersistsOnlyHashAndNeverClaimsLegacyProfile() throws Exception {
        long old=overload_api.support.DatabaseFixtures.insert(jdbc,"INSERT INTO users(name,gender,age,body_weight_kg) VALUES('旧記録','male',30,70)");
        var browser=new Browser();String id=browser.register();var identity=browser.login(id);
        assertNotEquals(old,identity.get("id").asLong());
        assertFalse(identity.has("password"));assertFalse(identity.has("passwordHash"));
        String stored=jdbc.queryForObject("SELECT password_hash FROM user_credentials WHERE login_id=?",String.class,id);
        assertTrue(stored.startsWith("{argon2}"));assertFalse(stored.contains(PASSWORD));
        assertEquals("旧記録",jdbc.queryForObject("SELECT name FROM users WHERE id=?",String.class,old));
        assertEquals(200,browser.read("/api/me").getResponse().getStatus());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM user_credentials WHERE user_id=?",Integer.class,old));
    }
    @Test void sessionIdRotatesAndLogoutInvalidatesCopiedCookie() throws Exception {
        var browser=new Browser();String id=browser.register();String before=browser.cookies.get("SESSION").getValue();
        browser.login(id);String after=browser.cookies.get("SESSION").getValue();assertNotEquals(before,after);
        var copied=new Browser(); copied.cookies.putAll(browser.cookies); copied.key=browser.key;
        assertEquals(204,browser.write("/api/auth/logout", "").getResponse().getStatus());
        assertEquals(401,copied.read("/api/auth/me").getResponse().getStatus());
        assertEquals(401,browser.read("/api/me").getResponse().getStatus());
    }
    @Test void csrfIsRequiredForRegisterLoginLogoutAndWorkout() throws Exception {
        var browser=new Browser();String id=browser.register();browser.login(id);
        for (String path:List.of("/api/auth/register","/api/auth/login","/api/auth/logout","/api/workouts")) {
            var result=browser.send(post(path).contentType("application/json").content("{}"));
            assertEquals(403,result.getResponse().getStatus(),path);
            assertEquals("CSRF_INVALID",JSON.readTree(result.getResponse().getContentAsString()).get("code").asText());
        }
        assertEquals(200,browser.read("/api/auth/me").getResponse().getStatus());
    }
    @Test void oldCsrfTokenIsRejectedAfterLogin() throws Exception {
        var browser=new Browser();String id=browser.register();
        var old=JSON.readTree(browser.send(get("/api/auth/csrf")).getResponse().getContentAsString());
        browser.login(id);
        assertEquals(403,browser.send(post("/api/workouts").header(old.get("headerName").asText(),old.get("token").asText())
            .header("X-Overload-Account",browser.key).contentType("application/json").content(workout(UUID.randomUUID().toString()))).getResponse().getStatus());
        assertEquals(201,browser.write("/api/workouts",workout(UUID.randomUUID().toString())).getResponse().getStatus());
    }
    @Test void twoUsersWithSameRequestUuidRemainSeparatedAcrossAllDataApis() throws Exception {
        var a=new Browser();var b=new Browser();String aid=a.register(),bid=b.register();
        long aId=a.login(aid).get("id").asLong(),bId=b.login(bid).get("id").asLong();
        String uuid=UUID.randomUUID().toString();
        assertEquals(201,a.write("/api/workouts",workout(uuid)).getResponse().getStatus());
        assertEquals(201,a.write("/api/workouts",workout(uuid)).getResponse().getStatus());
        for(String path:List.of("/api/workouts/history","/api/workout-sessions","/api/workout-sets"))
            assertEquals(0,JSON.readTree(b.read(path).getResponse().getContentAsString()).size(),path);
        assertEquals(201,b.write("/api/workouts",workout(uuid)).getResponse().getStatus());
        for(var browser:List.of(a,b)) {
            var history=JSON.readTree(browser.read("/api/workouts/history").getResponse().getContentAsString());
            assertEquals(1,history.size());assertEquals(170.1,history.get(0).get("totalCalories").asDouble());
            var sessions=JSON.readTree(browser.read("/api/workout-sessions").getResponse().getContentAsString());
            assertEquals(1,sessions.size());assertEquals(browser==a?aId:bId,sessions.get(0).get("userId").asLong());
        }
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM workout_sessions",Integer.class));
    }
    @Test void clientCannotChooseAnotherOwnerInBodyPathOrQuery() throws Exception {
        var browser=new Browser();String id=browser.register();browser.login(id);
        assertEquals(400,browser.write("/api/workouts",workout(UUID.randomUUID().toString()).replaceFirst("\\{","{\"userId\":999,")).getResponse().getStatus());
        assertEquals(400,browser.read("/api/workouts/history?user_id=999").getResponse().getStatus());
        assertEquals(403,browser.read("/api/users/999").getResponse().getStatus());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM workout_sessions",Integer.class));
    }
    @Test void staleTabCannotReadWriteOrLogoutAnotherAccount() throws Exception {
        var browser=new Browser();String a=browser.register(),b=browser.register();browser.login(a);
        String old=browser.key;browser.login(b);browser.key=old;
        for(String path:List.of("/api/me","/api/workouts/history","/api/workout-sessions","/api/workout-sets"))
            assertEquals(409,browser.read(path).getResponse().getStatus(),path);
        assertEquals(409,browser.write("/api/workouts",workout(UUID.randomUUID().toString())).getResponse().getStatus());
        assertEquals(409,browser.write("/api/auth/logout", "").getResponse().getStatus());
        assertEquals(b,JSON.readTree(browser.read("/api/auth/me").getResponse().getContentAsString()).get("loginId").asText());
    }
    @Test void failedLoginDoesNotDiscloseWhetherLoginIdExists() throws Exception {
        var browser=new Browser();String id=browser.register();
        var one=browser.write("/api/auth/login","{\"loginId\":\""+id+"\",\"password\":\"invalid-but-long-password\"}");
        var two=browser.write("/api/auth/login","{\"loginId\":\"missing_user\",\"password\":\"invalid-but-long-password\"}");
        assertEquals(401,one.getResponse().getStatus());assertEquals(401,two.getResponse().getStatus());
        assertEquals(JSON.readTree(one.getResponse().getContentAsString()).get("detail"),JSON.readTree(two.getResponse().getContentAsString()).get("detail"));
    }
    @Test void duplicateRegistrationRollsBackProfileAndConcurrentRegistrationHasSingleWinner() throws Exception {
        var browser=new Browser();String id=browser.register();
        assertEquals(409,browser.write("/api/auth/register",registration(id)).getResponse().getStatus());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM users",Integer.class));
        String parallel="parallel_"+sequence.incrementAndGet();var start=new CountDownLatch(1);
        var pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> action=() -> { start.await();try { accounts.register(new RegistrationCommand(parallel,PASSWORD,"同時登録","male",30,new java.math.BigDecimal("80"))); return true; }
                catch(overload_api.exception.RequestConflictException ex) {return false;} };
            var first=pool.submit(action);var second=pool.submit(action);start.countDown();
            assertNotEquals(first.get(20,TimeUnit.SECONDS),second.get(20,TimeUnit.SECONDS));
            assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM users",Integer.class));
            assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM user_credentials",Integer.class));
        } finally {pool.shutdownNow();}
    }
    @Test void maliciousOrInvalidRegistrationNeverPersistsProfile() throws Exception {
        var browser=new Browser();
        for(String body:List.of(registration("BAD"),registration("valid_user").replace(PASSWORD,"short"),
            registration("valid_user").replace("\"age\":30","\"age\":null"),
            registration("valid_user").replace("\"bodyWeightKg\":80","\"bodyWeightKg\":1000")))
            assertEquals(400,browser.write("/api/auth/register",body).getResponse().getStatus());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM users",Integer.class));
    }
    @Test void expiredSessionCannotAccessPersonalData() throws Exception {
        var browser=new Browser();String id=browser.register();browser.login(id);
        jdbc.update("UPDATE SPRING_SESSION SET EXPIRY_TIME=0, LAST_ACCESS_TIME=0");
        assertEquals(401,browser.read("/api/me").getResponse().getStatus());
    }
    @Test void oversizedJsonIsRejectedBeforeCreatingAnything() throws Exception {
        var browser=new Browser();
        assertEquals(413,browser.write("/api/auth/register","{\"padding\":\""+"x".repeat(262144)+"\"}").getResponse().getStatus());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM users",Integer.class));
    }
    @Test void productionCorsDoesNotGrantArbitraryOrigins() throws Exception {
        var result=mvc.perform(options("/api/me").header("Origin","https://attacker.example").header("Access-Control-Request-Method","PUT")).andReturn();
        assertNotEquals("https://attacker.example",result.getResponse().getHeader("Access-Control-Allow-Origin"));
    }
}
