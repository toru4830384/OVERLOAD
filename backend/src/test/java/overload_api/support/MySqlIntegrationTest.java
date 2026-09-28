package overload_api.support;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
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
    }

    /** テストJVMの間だけ存在するスキーマ。 */
    private static final class Database {
        private static final String SERVER = System.getenv().getOrDefault(
                "OVERLOAD_TEST_MYSQL_SERVER", "jdbc:mysql://127.0.0.1:3306/");
        private static final String USER = System.getenv().getOrDefault(
                "OVERLOAD_TEST_MYSQL_USER", "root");
        private static final String PASSWORD = System.getenv().getOrDefault(
                "OVERLOAD_TEST_MYSQL_PASSWORD", "");
        private static final String NAME = "overload_review_"
                + UUID.randomUUID().toString().replace("-", "");
        private static final String URL = initialize();

        private static String initialize() {
            if (!SERVER.matches("jdbc:mysql://[^/?]+/")) {
                throw new IllegalArgumentException(
                        "OVERLOAD_TEST_MYSQL_SERVER must be jdbc:mysql://host:port/");
            }
            try (var connection = DriverManager.getConnection(SERVER, USER, PASSWORD);
                    var statement = connection.createStatement()) {
                statement.executeUpdate("CREATE DATABASE `" + NAME
                        + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                Runtime.getRuntime().addShutdownHook(new Thread(Database::drop));
                connection.setCatalog(NAME);
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema-test.sql"));
                return SERVER + NAME;
            } catch (SQLException e) {
                throw new IllegalStateException("Cannot initialize isolated MySQL test schema", e);
            }
        }

        private static void drop() {
            try (var connection = DriverManager.getConnection(SERVER, USER, PASSWORD);
                    var statement = connection.createStatement()) {
                statement.executeUpdate("DROP DATABASE IF EXISTS `" + NAME + "`");
            } catch (SQLException e) {
                System.err.println("Could not remove test schema " + NAME + ": " + e.getMessage());
            }
        }
    }
}
