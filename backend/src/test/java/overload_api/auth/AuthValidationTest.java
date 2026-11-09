package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
/** HTTPを経由しない登録も同じ境界で拒否し、認証情報を文字列化しない。 */
class AuthValidationTest {
    @Test void serviceCommandRejectsInvalidFieldsBeforePersistence() {
        for (String id:java.util.List.of("AAa","aa","has space","x".repeat(33)))
            assertThrows(IllegalArgumentException.class,()->new RegistrationCommand(id,"long-password-12345","","male",0,new BigDecimal("0.01")));
        for (String password:java.util.List.of("x".repeat(14),"x".repeat(129)))
            assertThrows(IllegalArgumentException.class,()->new RegistrationCommand("valid_id",password,"","male",0,new BigDecimal("0.01")));
        assertThrows(IllegalArgumentException.class,()->new RegistrationCommand("valid_id","long-password-12345","","male",null,new BigDecimal("0.01")));
        assertThrows(IllegalArgumentException.class,()->new RegistrationCommand("valid_id","long-password-12345","","male",0,null));
        assertDoesNotThrow(()->new RegistrationCommand("valid_id","x".repeat(128),"","female",0,new BigDecimal("999.99")));
    }
    @Test void credentialsNeverAppearInRequestAndCommandToString() {
        String password="never-log-this-password";
        assertFalse(new AuthRequests.Login("valid_id",password).toString().contains(password));
        assertFalse(new RegistrationCommand("valid_id",password,"","male",0,new BigDecimal("80")).toString().contains(password));
    }
}
