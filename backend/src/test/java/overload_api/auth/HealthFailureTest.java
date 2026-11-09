package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.Map;
import org.junit.jupiter.api.Test;
import overload_api.controller.HealthController;
/** 接続失敗時も秘密や内部例外をHTTP応答へ出さない。 */
class HealthFailureTest {
    @Test void databaseFailureReturnsOnlyGenericUnavailableStatus() throws Exception {
        var source = mock(DataSource.class);
        when(source.getConnection()).thenThrow(new SQLException("internal host and secret must not appear"));
        var result = new HealthController(source).health();
        assertEquals(503, result.getStatusCode().value());
        assertEquals(Map.of("status", "down"), result.getBody());
    }
}
