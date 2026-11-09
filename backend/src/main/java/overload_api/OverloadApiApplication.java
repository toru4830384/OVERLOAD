package overload_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OVERLOAD APIのSpring Bootアプリケーション。
 */
@SpringBootApplication(exclude = org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration.class)
public class OverloadApiApplication {

    /**
     * アプリケーションを起動する。
     *
     * @param args 起動時のコマンドライン引数
     */
    public static void main(String[] args) {
        if (args.length == 1 && "--migrate-only".equals(args[0])) {
            overload_api.migration.SchemaMigration.migrate(System.getenv());
            return;
        }
        SpringApplication.run(OverloadApiApplication.class, args);
    }

}