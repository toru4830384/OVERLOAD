package overload_api.support;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import overload_api.auth.UserIdentity;
/** Controller単体試験の本人情報。フィルターを含む認証試験はSecurityIntegrationTestで別に行う。 */
public abstract class ControllerIdentityTest {
    @BeforeEach protected void authenticateDefaultUser() { asUser(1L); }
    @AfterEach protected void clearIdentity() { SecurityContextHolder.clearContext(); }
    protected static void asUser(long id) {
        var context=SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(new UserIdentity(id,"test_user","test_namespace"),null,List.of()));
        SecurityContextHolder.setContext(context);
    }
}
