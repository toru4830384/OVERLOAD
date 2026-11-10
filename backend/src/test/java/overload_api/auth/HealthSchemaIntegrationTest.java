package overload_api.auth;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import overload_api.controller.HealthController;
import overload_api.support.IsolatedMySqlServer;

/** 移行不足でも正常と返してしまうことを、実際の旧スキーマと隔離DBで防ぐ。 */
class HealthSchemaIntegrationTest {
    @FunctionalInterface private interface Check { void run(DriverManagerDataSource source, String url, IsolatedMySqlServer server) throws Exception; }
    private void withSchema(String version, Check check) throws Exception {
        var server=IsolatedMySqlServer.fromEnvironment();
        String name="overload_migration_"+UUID.randomUUID().toString().replace("-","");
        try(var connection=DriverManager.getConnection(server.url(),server.user(),server.password());var statement=connection.createStatement()) {
            server.verifyConstraints(connection);
            statement.executeUpdate("CREATE DATABASE `"+name+"` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            try {
                String url=server.databaseUrl(name);
                Flyway.configure().dataSource(url,server.user(),server.password()).target(version).cleanDisabled(true).load().migrate();
                check.run(new DriverManagerDataSource(url,server.user(),server.password()),url,server);
            } finally {
                statement.executeUpdate("DROP DATABASE `"+name+"`");
            }
        }
    }
    @Test void versionThreeIsUnavailableAndBecomesHealthyAfterRequiredMigration() throws Exception {
        withSchema("3",(source,url,server)-> {
            var controller=new HealthController(source);
            assertEquals(503,controller.health().getStatusCode().value(),"V4未移行なのに正常として公開してはいけない");
            assertEquals(Map.of("status","down"),controller.health().getBody());
            Flyway.configure().dataSource(url,server.user(),server.password()).cleanDisabled(true).load().migrate();
            assertEquals(200,controller.health().getStatusCode().value());
        });
    }
    @Test void missingGoogleTableIsUnavailableWithoutLeakingDatabaseDetails() throws Exception {
        withSchema("4",(source,url,server)-> {
            var controller=new HealthController(source);assertEquals(200,controller.health().getStatusCode().value());
            new JdbcTemplate(source).execute("DROP TABLE google_accounts");
            assertEquals(503,controller.health().getStatusCode().value());
            assertEquals(Map.of("status","down"),controller.health().getBody());
        });
    }
    @Test void missingAuthenticationVersionColumnIsUnavailable() throws Exception {
        withSchema("4",(source,url,server)-> {
            var controller=new HealthController(source);assertEquals(200,controller.health().getStatusCode().value());
            new JdbcTemplate(source).execute("ALTER TABLE user_credentials DROP COLUMN auth_version");
            assertEquals(503,controller.health().getStatusCode().value());
        });
    }
}
