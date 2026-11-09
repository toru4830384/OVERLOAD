package overload_api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.junit.jupiter.api.Test;

/** MySQL互換のバージョン文字列でも、FlywayがTiDB用のSQLを選ぶことを保証する。 */
class FlywayTiDbSupportTest {
    @Test void choosesTiDbInsteadOfMySqlForTheCloudVersion() throws Exception {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getDatabaseProductName()).thenReturn("MySQL");
        when(metadata.getDatabaseProductVersion()).thenReturn("8.0.11-TiDB-v8.5.3-serverless");
        assertEquals("TiDB", DatabaseTypeRegister.getDatabaseTypeForConnection(connection, Flyway.configure()).getName());
    }
}
