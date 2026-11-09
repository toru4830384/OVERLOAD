package overload_api.auth;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import overload_api.support.IsolatedMySqlServer;
/** 本番と同じ移行ファイルで、既存データを保持し、自動baselineしないことを確認する。 */
class MigrationIntegrationTest {
    @Test void populatedLegacySchemaRequiresExplicitBaselineAndPreservesExistingRecords() throws Exception {
        var server=IsolatedMySqlServer.fromEnvironment();
        String user=server.user();
        String password=server.password();
        String name="overload_migration_"+UUID.randomUUID().toString().replace("-","");
        try(var c=DriverManager.getConnection(server.url(),user,password);var statement=c.createStatement()) {
            server.verifyConstraints(c);
            statement.executeUpdate("CREATE DATABASE `"+name+"` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            try {
                c.setCatalog(name);
                var data=c.createStatement();
                ScriptUtils.executeSqlScript(c,new ClassPathResource("db/migration/V1__initial_schema.sql"));
                data.executeUpdate("INSERT INTO users(id,name,gender,age,body_weight_kg) VALUES(4830,'既存ユーザー','male',30,80)");
                data.executeUpdate("INSERT INTO workout_sessions(id,user_id,started_at) VALUES(7000,4830,'2026-01-01 10:00:00')");
                Flyway flyway=Flyway.configure().dataSource(server.databaseUrl(name),user,password).baselineVersion("1").cleanDisabled(true).load();
                assertThrows(FlywayException.class,flyway::migrate);
                flyway.baseline();flyway.migrate();flyway.migrate();
                try(var rs=data.executeQuery("SELECT user_id,body_weight_kg,total_calories FROM workout_sessions WHERE id=7000")) {
                    assertTrue(rs.next());assertEquals(4830,rs.getLong(1));assertNull(rs.getObject(2));assertNull(rs.getObject(3));
                }
                try(var rs=data.executeQuery("SELECT COUNT(*) FROM user_credentials")) {rs.next();assertEquals(0,rs.getInt(1));}
                assertThrows(FlywayException.class,flyway::clean);
            } finally {
                try(var cleanup=DriverManager.getConnection(server.url(),user,password);var drop=cleanup.createStatement()) {
                    drop.executeUpdate("DROP DATABASE `"+name+"`");
                }
            }
        }
    }
}
