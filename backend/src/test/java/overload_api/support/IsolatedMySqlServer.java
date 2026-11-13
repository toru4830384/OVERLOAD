package overload_api.support;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import overload_api.config.HostedDeploymentConfig;

/** TLS設定を維持し、実行ごとに生成した使い捨てスキーマだけへ接続する。 */
public record IsolatedMySqlServer(String url, String user, String password) {
    /** @param url DB名を含まないサーバーURL。遠隔接続ではTLSのホスト名検証を必須とする。 */
    public IsolatedMySqlServer {
        try {
            if (url == null || !url.startsWith("jdbc:mysql://")) throw new IllegalArgumentException();
            URI uri = URI.create(url.substring(5));
            if (uri.getHost() == null || !"/".equals(uri.getPath())
                    || uri.getRawUserInfo() != null || uri.getFragment() != null)
                throw new IllegalArgumentException();
            boolean loopback = java.util.Set.of("localhost", "127.0.0.1", "[::1]").contains(uri.getHost());
            if (!loopback || uri.getRawQuery() != null)
                HostedDeploymentConfig.validateDatabaseUrl(schemaUrl(url, "overload_preflight"));
        } catch (RuntimeException invalid) {
            // 資格情報を誤ってURLへ入力した場合も、そのURLを表示しない。
            throw new IllegalArgumentException("隔離DB用URLはjdbc:mysql://host:port/形式とし、遠隔接続ではsslMode=VERIFY_IDENTITYを指定してください。");
        }
    }

    /** @return 所有者が設定した接続先。既存の業務DB名は受け付けない。 */
    public static IsolatedMySqlServer fromEnvironment() {
        Map<String, String> env = System.getenv();
        return new IsolatedMySqlServer(env.getOrDefault("OVERLOAD_TEST_MYSQL_SERVER", "jdbc:mysql://127.0.0.1:3306/"),
            env.getOrDefault("OVERLOAD_TEST_MYSQL_USER", "root"), env.getOrDefault("OVERLOAD_TEST_MYSQL_PASSWORD", ""));
    }

    /** @param name テスト自身が生成したUUID付きスキーマ名 @return TLS設定を引き継いだ接続先 */
    public String databaseUrl(String name) {
        if (name == null || !name.matches("overload_(review|migration)_[0-9a-f]{32}"))
            throw new IllegalArgumentException("この実行で生成した隔離スキーマ名だけを指定してください。");
        return schemaUrl(url, name);
    }

    private static String schemaUrl(String server, String name) {
        int query = server.indexOf('?');
        return query < 0 ? server + name : server.substring(0, query) + name + server.substring(query);
    }

    /** @param connection スキーマ作成前の接続。制約を無効にしたTiDBでは検証を開始しない。 */
    public void verifyConstraints(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.setQueryTimeout(10);
            try (var version = statement.executeQuery("SELECT VERSION()")) {
                version.next();
                if (!version.getString(1).toLowerCase(java.util.Locale.ROOT).contains("tidb")) return;
            }
            try (var flags = statement.executeQuery("SELECT @@GLOBAL.tidb_enable_check_constraint,"
                    + " @@GLOBAL.tidb_enable_foreign_key, @@SESSION.foreign_key_checks")) {
                flags.next();
                if (flags.getInt(1) != 1 || flags.getInt(2) != 1 || flags.getInt(3) != 1)
                    throw new IllegalStateException("TiDBのCHECK制約と外部キーを有効にしてから検証してください。");
            }
        }
    }

    /** @return パスワードを含まない説明。record標準の文字列表現で秘密を漏らさない。 */
    @Override public String toString() { return "IsolatedMySqlServer[接続情報は非表示]"; }
}
