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
    @Test void versionFourPreservesExistingPasswordAndDraftNamespace() throws Exception {
        var server=IsolatedMySqlServer.fromEnvironment();
        String name="overload_migration_"+UUID.randomUUID().toString().replace("-","");
        try(var c=DriverManager.getConnection(server.url(),server.user(),server.password());var admin=c.createStatement()) {
            server.verifyConstraints(c);
            admin.executeUpdate("CREATE DATABASE `"+name+"` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            try {
                Flyway.configure().dataSource(server.databaseUrl(name),server.user(),server.password())
                    .target("3").cleanDisabled(true).load().migrate();
                c.setCatalog(name);
                var data=c.createStatement();
                data.executeUpdate("INSERT INTO users(id,name,gender,age,body_weight_kg) VALUES(4830,'既存','male',30,80)");
                data.executeUpdate("INSERT INTO user_credentials(user_id,login_id,password_hash,draft_namespace) "
                    +"VALUES(4830,'migration_user','unchanged-password-hash','unchanged-draft-key')");
                Flyway.configure().dataSource(server.databaseUrl(name),server.user(),server.password()).cleanDisabled(true).load().migrate();
                try(var rs=data.executeQuery("SELECT password_hash,draft_namespace,auth_version FROM user_credentials WHERE user_id=4830")) {
                    assertTrue(rs.next());assertEquals("unchanged-password-hash",rs.getString(1));
                    assertEquals("unchanged-draft-key",rs.getString(2));assertEquals(0,rs.getLong(3));
                }
                data.executeUpdate("UPDATE user_credentials SET password_hash=NULL WHERE user_id=4830");
                data.executeUpdate("INSERT INTO google_accounts(user_id,google_subject) VALUES(4830,'verified-subject')");
            } finally {
                try(var cleanup=DriverManager.getConnection(server.url(),server.user(),server.password());var drop=cleanup.createStatement()) {
                    drop.executeUpdate("DROP DATABASE `"+name+"`");
                }
            }
        }
    }
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
