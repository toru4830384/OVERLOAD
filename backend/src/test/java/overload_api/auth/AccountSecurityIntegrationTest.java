package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import java.util.*;
import java.util.concurrent.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import overload_api.support.MySqlIntegrationTest;
/** 実MySQLで本人再確認、全セッション失効、Google連携とデータ分離を検証する。 */
@AutoConfigureMockMvc
class AccountSecurityIntegrationTest extends MySqlIntegrationTest {
    static final String OLD="current-test-password",NEW="new-test-password-2026";
    static final JsonMapper JSON=JsonMapper.builder().build();
    @Autowired MockMvc mvc;@Autowired JdbcTemplate jdbc;@Autowired AccountService accounts;
    @Autowired AccountSecurityService security;@Autowired PasswordEncoder passwords;
    @MockitoBean GoogleTokenVerifier verifier;
    @BeforeEach void setup() {
        jdbc.update("DELETE FROM google_accounts");
        jdbc.update("DELETE FROM SPRING_SESSION_ATTRIBUTES");jdbc.update("DELETE FROM SPRING_SESSION");
        jdbc.update("DELETE FROM workout_sets");jdbc.update("DELETE FROM workout_sessions");
        jdbc.update("DELETE FROM user_credentials");jdbc.update("DELETE FROM users");
        when(verifier.enabled()).thenReturn(true);when(verifier.clientId()).thenReturn("test.apps.googleusercontent.com");
        when(verifier.verify(anyString(),anyString())).thenAnswer(call -> call.getArgument(0,String.class));
    }
    @AfterEach void clearGoogleRows() {jdbc.update("DELETE FROM google_accounts");}
    UserIdentity create(String id) {return accounts.register(new RegistrationCommand(id,OLD,"本人","male",30,new java.math.BigDecimal("65")));}
    class Browser {
        Map<String,Cookie> cookies=new HashMap<>();String key="";
        MvcResult send(MockHttpServletRequestBuilder request) throws Exception {
            if(!cookies.isEmpty())request.cookie(cookies.values().toArray(Cookie[]::new));
            var result=mvc.perform(request).andReturn();
            for(Cookie c:result.getResponse().getCookies()){if(c.getMaxAge()==0)cookies.remove(c.getName());else cookies.put(c.getName(),c);}
            return result;
        }
        MvcResult write(String path,Object payload) throws Exception {
            var csrf=JSON.readTree(send(get("/api/auth/csrf")).getResponse().getContentAsString());
            return send(post(path).contentType("application/json").content(JSON.writeValueAsString(payload))
                .header(csrf.get("headerName").asText(),csrf.get("token").asText()).header("X-Overload-Account",key));
        }
        MvcResult read(String path) throws Exception {return send(get(path).header("X-Overload-Account",key));}
        void login(String id,String password) throws Exception {
            var r=write("/api/auth/login",Map.of("loginId",id,"password",password));assertEquals(200,r.getResponse().getStatus(),r.getResponse().getContentAsString());
            key=JSON.readTree(r.getResponse().getContentAsString()).get("accountKey").asText();
        }
        void challenge(String purpose) throws Exception {
            String path=purpose.equals("link")?"/api/auth/google/link/challenge":"/api/auth/google/challenge";
            var r=write(path,purpose.equals("link")?Map.of("currentPassword",OLD):Map.of());assertEquals(200,r.getResponse().getStatus(),r.getResponse().getContentAsString());
        }
    }
    @Test void passwordChangeRequiresCurrentPasswordAndInvalidatesAllExistingSessions() throws Exception {
        var user=create("password_user");var a=new Browser();var b=new Browser();a.login(user.loginId(),OLD);b.login(user.loginId(),OLD);
        String originalKey=a.key;
        assertEquals(400,a.write("/api/auth/password",Map.of("currentPassword","wrong-password-123","newPassword",NEW)).getResponse().getStatus());
        assertEquals(200,b.read("/api/me").getResponse().getStatus());
        assertEquals(204,a.write("/api/auth/password",Map.of("currentPassword",OLD,"newPassword",NEW)).getResponse().getStatus());
        assertEquals(401,b.read("/api/me").getResponse().getStatus());assertEquals(401,a.read("/api/auth/me").getResponse().getStatus());
        assertEquals(401,new Browser().write("/api/auth/login",Map.of("loginId",user.loginId(),"password",OLD)).getResponse().getStatus());
        var fresh=new Browser();fresh.login(user.loginId(),NEW);assertEquals(originalKey,fresh.key);
        assertEquals("本人",JSON.readTree(fresh.read("/api/me").getResponse().getContentAsString()).get("name").asText());
        String hash=jdbc.queryForObject("SELECT password_hash FROM user_credentials WHERE user_id=?",String.class,user.userId());
        assertFalse(hash.contains(NEW));assertTrue(passwords.matches(NEW,hash));
    }
    @Test void anonymousCsrfAndWrongAccountCannotChangePasswordOrLinkGoogle() throws Exception {
        var user=create("protected_user");var browser=new Browser();
        assertEquals(401,browser.write("/api/auth/password",Map.of("currentPassword",OLD,"newPassword",NEW)).getResponse().getStatus());
        browser.login(user.loginId(),OLD);
        assertEquals(403,browser.send(post("/api/auth/password").contentType("application/json").content("{}")).getResponse().getStatus());
        browser.key="another:account";
        assertEquals(409,browser.write("/api/auth/password",Map.of("currentPassword",OLD,"newPassword",NEW)).getResponse().getStatus());
        assertEquals(409,browser.write("/api/auth/google/link/challenge",Map.of("currentPassword",OLD)).getResponse().getStatus());
        assertEquals(0,jdbc.queryForObject("SELECT auth_version FROM user_credentials WHERE user_id=?",Long.class,user.userId()));
    }
    @Test void invalidPasswordBoundariesAndReuseLeaveCredentialsUntouched() throws Exception {
        var user=create("boundary_user");var browser=new Browser();browser.login(user.loginId(),OLD);
        for(String value:List.of("x".repeat(11),"x".repeat(129),OLD))
            assertEquals(400,browser.write("/api/auth/password",Map.of("currentPassword",OLD,"newPassword",value)).getResponse().getStatus());
        assertEquals(200,browser.read("/api/me").getResponse().getStatus());
        assertEquals(204,browser.write("/api/auth/password",Map.of("currentPassword",OLD,"newPassword","朝から筋トレを続けるぞ！")).getResponse().getStatus());
        new Browser().login(user.loginId(),"朝から筋トレを続けるぞ！");
    }
    @Test void googleLinkAllowsRecoveryOfSameAccountAndRejectsOtherOwners() throws Exception {
        var user=create("linked_user");var a=new Browser();a.login(user.loginId(),OLD);
        assertEquals(400,a.write("/api/auth/google/link/challenge",Map.of("currentPassword","wrong-password-123")).getResponse().getStatus());
        a.challenge("link");assertEquals(204,a.write("/api/auth/google/link",Map.of("credential","subject_a")).getResponse().getStatus());
        assertEquals(400,a.write("/api/auth/google/link",Map.of("credential","subject_a")).getResponse().getStatus());
        var recovered=new Browser();recovered.challenge("login");
        var result=recovered.write("/api/auth/google/login",Map.of("credential","subject_a"));assertEquals(200,result.getResponse().getStatus());
        var identity=JSON.readTree(result.getResponse().getContentAsString()).get("identity");assertEquals(user.userId().longValue(),identity.get("id").asLong());
        assertEquals(a.key,identity.get("accountKey").asText());
        var second=create("second_user");var b=new Browser();b.login(second.loginId(),OLD);b.challenge("link");
        assertEquals(409,b.write("/api/auth/google/link",Map.of("credential","subject_a")).getResponse().getStatus());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM google_accounts",Integer.class));
    }
    @Test void googleSignupRequiresProofAndDoesNotInventPasswordOrProfile() throws Exception {
        var browser=new Browser();var profile=Map.of("loginId","google_user","name","Google本人","gender","female","age",25,"bodyWeightKg",60);
        assertEquals(400,browser.write("/api/auth/google/register",profile).getResponse().getStatus());
        browser.challenge("login");var proof=browser.write("/api/auth/google/login",Map.of("credential","new_subject"));
        assertEquals("registration_required",JSON.readTree(proof.getResponse().getContentAsString()).get("status").asText());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM users",Integer.class));
        assertEquals(400,browser.write("/api/auth/google/register",Map.of("loginId","google_user","name","","gender","male","age",30)).getResponse().getStatus());
        var result=browser.write("/api/auth/google/register",profile);assertEquals(201,result.getResponse().getStatus(),result.getResponse().getContentAsString());
        browser.key=JSON.readTree(result.getResponse().getContentAsString()).get("accountKey").asText();
        assertEquals(200,browser.read("/api/me").getResponse().getStatus());
        assertNull(jdbc.queryForObject("SELECT password_hash FROM user_credentials WHERE login_id='google_user'",String.class));
        assertEquals(401,new Browser().write("/api/auth/login",Map.of("loginId","google_user","password",OLD)).getResponse().getStatus());
    }
    @Test void duplicateGoogleSignupRollsBackNewProfileAndDoesNotClaimExistingLoginId() throws Exception {
        create("existing_id");var browser=new Browser();browser.challenge("login");
        browser.write("/api/auth/google/login",Map.of("credential","unlinked_subject"));
        assertEquals(409,browser.write("/api/auth/google/register",Map.of("loginId","existing_id","name","Other","gender","male","age",20,"bodyWeightKg",60)).getResponse().getStatus());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM users",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM google_accounts",Integer.class));
    }
    @Test void simultaneousPasswordChangesHaveOneWinner() throws Exception {
        var user=create("parallel_user");var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> action=()->{start.await();try{security.changePassword(user,new PasswordChangeCommand(OLD,NEW));return true;}catch(IllegalArgumentException e){return false;}};
            var a=pool.submit(action);var b=pool.submit(action);start.countDown();assertNotEquals(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS));
            assertEquals(1,jdbc.queryForObject("SELECT auth_version FROM user_credentials WHERE user_id=?",Long.class,user.userId()));
        }finally{pool.shutdownNow();}
    }
}
