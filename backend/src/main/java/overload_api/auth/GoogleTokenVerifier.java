package overload_api.auth;
import java.time.Instant;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
/** Googleの署名鍵でIDトークンを検証し、発行元・宛先・時刻・nonceを照合する。 */
@Component
public class GoogleTokenVerifier {
    private final String clientId;
    private final JwtDecoder decoder;
    @org.springframework.beans.factory.annotation.Autowired
    public GoogleTokenVerifier(@Value("${overload.google.client-id:}") String clientId) {
        this(clientId,defaultDecoder());
    }
    GoogleTokenVerifier(String clientId,JwtDecoder decoder) {this.clientId=clientId;this.decoder=decoder;}
    private static JwtDecoder defaultDecoder() {
        var factory=new SimpleClientHttpRequestFactory();factory.setConnectTimeout(5000);factory.setReadTimeout(5000);
        return NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .restOperations(new RestTemplate(factory)).build();
    }
    public boolean enabled() {return clientId.matches("[A-Za-z0-9-]+\\.apps\\.googleusercontent\\.com");}
    public String clientId() {return enabled()?clientId:"";}
    public String verify(String token,String nonce) {
        if(!enabled()) throw new IllegalArgumentException("Googleログインは準備中です");
        try {
            Jwt jwt=decoder.decode(token);Instant now=Instant.now();
            String issuer=jwt.getClaimAsString("iss"),subject=jwt.getSubject();
            String received=jwt.getClaimAsString("nonce"),azp=jwt.getClaimAsString("azp");
            if(!"https://accounts.google.com".equals(issuer) || jwt.getAudience().size()!=1
                || !jwt.getAudience().contains(clientId) || (azp!=null && !clientId.equals(azp))
                || subject==null || !subject.matches("[A-Za-z0-9_-]{1,255}")
                || jwt.getExpiresAt()==null || !jwt.getExpiresAt().isAfter(now)
                || jwt.getIssuedAt()==null || jwt.getIssuedAt().isAfter(now.plusSeconds(60))
                || received==null || !MessageDigest.isEqual(nonce.getBytes(StandardCharsets.UTF_8),received.getBytes(StandardCharsets.UTF_8)))
                throw new IllegalArgumentException();
            return subject;
        } catch(RuntimeException invalid) {throw new IllegalArgumentException("Googleの本人確認に失敗しました。もう一度Googleログインを開始してください");}
    }
}
