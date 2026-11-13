package overload_api.support;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/** 遠隔検証でTLSを失ったり、業務DBへ接続したりしないことを確認する。 */
class IsolatedMySqlServerTest {
    private static final String NAME = "overload_review_0123456789abcdef0123456789abcdef";

    @Test void keepsTlsParametersAfterTheGeneratedSchemaName() {
        var server = new IsolatedMySqlServer("jdbc:mysql://db.example:4000/?sslMode=VERIFY_IDENTITY&socketTimeout=15000", "user", "secret");
        assertEquals("jdbc:mysql://db.example:4000/" + NAME + "?sslMode=VERIFY_IDENTITY&socketTimeout=15000", server.databaseUrl(NAME));
        assertFalse(server.toString().contains("secret"));
    }

    @Test void rejectsBusinessSchemasCredentialsAndUnverifiedRemoteConnections() {
        for (String url : new String[] {"jdbc:mysql://db.example:4000/production?sslMode=VERIFY_IDENTITY",
                "jdbc:mysql://db.example:4000/", "jdbc:mysql://db.example:4000/?sslMode=REQUIRED",
                "jdbc:mysql://user:secret@localhost:3306/", "jdbc:mysql://db.example:4000/?sslMode=VERIFY_IDENTITY&password=secret"}) {
            var failure = assertThrows(IllegalArgumentException.class, () -> new IsolatedMySqlServer(url, "user", "secret"));
            assertFalse(failure.getMessage().contains("secret"));
        }
        var local = new IsolatedMySqlServer("jdbc:mysql://127.0.0.1:3306/", "root", "");
        assertEquals("jdbc:mysql://127.0.0.1:3306/" + NAME, local.databaseUrl(NAME));
        assertThrows(IllegalArgumentException.class, () -> local.databaseUrl("overload_db"));
    }
}
