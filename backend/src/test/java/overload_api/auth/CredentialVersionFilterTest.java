package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import overload_api.mapper.CredentialMapper;
/** セッション削除後に古い認証が再保存されても、資格情報の版で拒否する。 */
class CredentialVersionFilterTest {
    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }
    @Test void lateSavedSessionIsRejectedWhenPasswordVersionChanged() throws Exception { checkRejected(1L); }
    @Test void missingCredentialIsRejected() throws Exception { checkRejected(null); }
    private void checkRejected(Long version) throws Exception {
        var mapper=mock(CredentialMapper.class);when(mapper.currentVersion(1L)).thenReturn(version);
        authenticate();var request=new MockHttpServletRequest("GET","/api/users/me");
        var session=request.getSession();var response=new MockHttpServletResponse();var chain=new MockFilterChain();
        new CredentialVersionFilter(mapper).doFilter(request,response,chain);
        assertEquals(401,response.getStatus());assertNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(((MockHttpSession)session).isInvalid());
    }
    @Test void matchingVersionCanContinue() throws Exception {
        var mapper=mock(CredentialMapper.class);when(mapper.currentVersion(1L)).thenReturn(0L);
        authenticate();var request=new MockHttpServletRequest("GET","/api/users/me");var chain=new MockFilterChain();
        new CredentialVersionFilter(mapper).doFilter(request,new MockHttpServletResponse(),chain);
        assertSame(request,chain.getRequest());
    }
    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
            new UserIdentity(1L,"version_test","draft_namespace",0),null,java.util.List.of()));
    }
}
