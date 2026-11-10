package overload_api.auth;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Base64;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
/** nonceと登録待ちの本人証明だけをセッションに短時間保持し、IDトークンは保存しない。 */
@Component
public class GoogleChallengeStore {
    private static final String CHALLENGE="OVERLOAD_GOOGLE_CHALLENGE",PENDING="OVERLOAD_GOOGLE_PENDING";
    private static final long TTL=300_000;
    private final java.time.Clock clock;
    public GoogleChallengeStore() { this(java.time.Clock.systemUTC()); }
    GoogleChallengeStore(java.time.Clock clock) { this.clock=clock; }
    private record Challenge(String nonce,String purpose,UserIdentity user,long expires) implements Serializable {}
    private record Pending(String subject,long expires) implements Serializable {}
    public String issue(HttpSession session,String purpose,UserIdentity user) {
        byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);
        String nonce=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(CHALLENGE,new Challenge(nonce,purpose,user,clock.millis()+TTL));
        session.removeAttribute(PENDING);return nonce;
    }
    public String consume(HttpSession session,String purpose,UserIdentity user) {
        Object value=session.getAttribute(CHALLENGE);session.removeAttribute(CHALLENGE);
        if(!(value instanceof Challenge c) || c.expires()<clock.millis() || !c.purpose().equals(purpose)
            || !java.util.Objects.equals(c.user(),user))
            throw new IllegalArgumentException("Googleの確認期限が切れたか、ログイン状態が変わりました。連携をやり直してください");
        return c.nonce();
    }
    public void pending(HttpSession session,String subject) {session.setAttribute(PENDING,new Pending(subject,clock.millis()+TTL));}
    public String pendingSubject(HttpSession session) {
        if(session==null || !(session.getAttribute(PENDING) instanceof Pending p) || p.expires()<clock.millis())
            throw new IllegalArgumentException("Googleの確認期限が切れました。Googleログインをやり直してください");
        return p.subject();
    }
    public boolean hasPending(HttpSession session) {try{pendingSubject(session);return true;}catch(IllegalArgumentException e){return false;}}
    public void clear(HttpSession session) {if(session!=null){session.removeAttribute(CHALLENGE);session.removeAttribute(PENDING);}}
}
