package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.Date;
import java.security.*;
import java.security.interfaces.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.*;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
/** 外部Googleに依存せず、実際のRSA署名検証とトークンの各制約を試験する。 */
class GoogleTokenVerifierTest {
    static KeyPair keys;
    static final String CLIENT="test-client.apps.googleusercontent.com",NONCE="server-generated-nonce";
    GoogleTokenVerifier verifier;
    @BeforeAll static void keys() throws Exception {var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);keys=generator.generateKeyPair();}
    @BeforeEach void setup() {verifier=new GoogleTokenVerifier(CLIENT,NimbusJwtDecoder.withPublicKey((RSAPublicKey)keys.getPublic()).build());}
    String token(String issuer,String audience,String nonce,Instant expiry,PrivateKey key) throws Exception {
        var claims=new JWTClaimsSet.Builder().issuer(issuer).subject("google_user_123").audience(audience)
            .issueTime(Date.from(Instant.now().minusSeconds(5))).expirationTime(Date.from(expiry)).claim("nonce",nonce).build();
        var jwt=new SignedJWT(new JWSHeader(JWSAlgorithm.RS256),claims);jwt.sign(new RSASSASigner(key));return jwt.serialize();
    }
    @Test void validSignatureIssuerAudienceExpiryAndNonceAreRequired() throws Exception {
        assertTrue(verifier.enabled());
        assertEquals("google_user_123",verifier.verify(token("https://accounts.google.com",CLIENT,NONCE,Instant.now().plusSeconds(600),keys.getPrivate()),NONCE));
    }
    @Test void incorrectIssuerAudienceNonceAndExpiredProofAreRejected() throws Exception {
        for(String[] values:java.util.List.of(new String[]{"https://attacker.example",CLIENT,NONCE},
                new String[]{"https://accounts.google.com","wrong-client",NONCE},
                new String[]{"https://accounts.google.com",CLIENT,"wrong-nonce"})) {
            String raw=token(values[0],values[1],values[2],Instant.now().plusSeconds(600),keys.getPrivate());
            assertThrows(IllegalArgumentException.class,()->verifier.verify(raw,NONCE));
        }
        String expired=token("https://accounts.google.com",CLIENT,NONCE,Instant.now().minusSeconds(120),keys.getPrivate());
        assertThrows(IllegalArgumentException.class,()->verifier.verify(expired,NONCE));
    }
    @Test void wrongSigningKeyAndUnsignedTokensAreRejectedWithoutEchoingToken() throws Exception {
        var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);
        String raw=token("https://accounts.google.com",CLIENT,NONCE,Instant.now().plusSeconds(600),generator.generateKeyPair().getPrivate());
        var error=assertThrows(IllegalArgumentException.class,()->verifier.verify(raw,NONCE));assertFalse(error.getMessage().contains(raw));
        assertThrows(IllegalArgumentException.class,()->verifier.verify("not-a-signed-token",NONCE));
    }
    @Test void unconfiguredFeatureStaysDisabled() {assertFalse(new GoogleTokenVerifier("",token -> null).enabled());}
}
