package overload_api.migration;

import java.util.Map;
import org.flywaydb.core.Flyway;
import overload_api.config.HostedDeploymentConfig;

/** Webサーバーを開かず、専用資格情報で公開先のスキーマを移行する。 */
public final class SchemaMigration {
    private SchemaMigration() {}
    /** @param environment 移行用環境変数。パスワードを引数やログへ含めない。 */
    public static void migrate(Map<String,String> environment) {
        String url = environment.get("OVERLOAD_DB_URL");
        HostedDeploymentConfig.validateDatabaseUrl(url);
        String user = environment.get("OVERLOAD_MIGRATION_USER");
        String password = environment.get("OVERLOAD_MIGRATION_PASSWORD");
        if (user == null || user.isBlank() || password == null || password.isBlank())
            throw new IllegalArgumentException("移行専用のユーザーとパスワードを環境変数で指定してください。");
        Flyway.configure().dataSource(url, user, password)
            .baselineOnMigrate(false).cleanDisabled(true).load().migrate();
    }
}
