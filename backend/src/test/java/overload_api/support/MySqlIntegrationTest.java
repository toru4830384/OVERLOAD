package overload_api.support;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;
import org.flywaydb.core.Flyway;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 実行専用のMySQLスキーマを共有する結合テストの基底クラス。
 * 既存の開発用・テスト用スキーマには接続しない。
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class MySqlIntegrationTest {

    /**
     * 接続先を実行ごとの隔離スキーマへ固定する。
     *
     * @param registry テスト用設定
     */
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> Database.URL);
        registry.add("spring.datasource.username", () -> Database.USER);
        registry.add("spring.datasource.password", () -> Database.PASSWORD);
        registry.add("spring.flyway.user", () -> Database.USER);
        registry.add("spring.flyway.password", () -> Database.PASSWORD);
    }

    /** テストJVMの間だけ存在するスキーマ。 */
    private static final class Database {
        private static final IsolatedMySqlServer SERVER = IsolatedMySqlServer.fromEnvironment();
        private static final String USER = SERVER.user();
        private static final String PASSWORD = SERVER.password();
        private static final String NAME = "overload_review_"
                + UUID.randomUUID().toString().replace("-", "");
        private static final String URL = initialize();

        private static String initialize() {
            try (var connection = DriverManager.getConnection(SERVER.url(), USER, PASSWORD);
                    var statement = connection.createStatement()) {
                SERVER.verifyConstraints(connection);
                statement.executeUpdate("CREATE DATABASE `" + NAME
                        + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                Runtime.getRuntime().addShutdownHook(new Thread(Database::drop));
                Flyway.configure().dataSource(SERVER.databaseUrl(NAME), USER, PASSWORD).load().migrate();
                return SERVER.databaseUrl(NAME);
            } catch (SQLException e) {
                throw new IllegalStateException("Cannot initialize isolated MySQL test schema", e);
            }
        }

        private static void drop() {
            try (var connection = DriverManager.getConnection(SERVER.url(), USER, PASSWORD);
                    var statement = connection.createStatement()) {
                statement.executeUpdate("DROP DATABASE IF EXISTS `" + NAME + "`");
            } catch (SQLException e) {
                System.err.println("Could not remove test schema " + NAME + ": " + e.getMessage());
            }
        }
    }
}
