package overload_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OVERLOAD APIのSpring Bootアプリケーション。
 */
@SpringBootApplication
public class OverloadApiApplication {

    /**
     * アプリケーションを起動する。
     *
     * @param args 起動時のコマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(OverloadApiApplication.class, args);
    }

}