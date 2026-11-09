package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
/** 認証試行は送信元別に制限し、時間経過で再開する。 */
class AuthRateLimitTest {
    @Test void throttlesPerAddressAndDoesNotTrustForwardedFor() throws Exception {
        var filter=new AuthRateLimitFilter(Clock.fixed(Instant.EPOCH,ZoneOffset.UTC),2);
        var accepted=new AtomicInteger();
        for (int i=0;i<3;i++) {
            var request=new MockHttpServletRequest("POST","/api/auth/login");request.setRemoteAddr("192.0.2.1");
            request.addHeader("X-Forwarded-For","192.0.2."+(i+10));
            var response=new MockHttpServletResponse();filter.doFilter(request,response,(q,p)->accepted.incrementAndGet());
            assertEquals(i<2?200:429,response.getStatus());
        }
        var other=new MockHttpServletRequest("POST","/api/auth/register");other.setRemoteAddr("192.0.2.2");
        filter.doFilter(other,new MockHttpServletResponse(),(q,p)->accepted.incrementAndGet());assertEquals(3,accepted.get());
    }
}
