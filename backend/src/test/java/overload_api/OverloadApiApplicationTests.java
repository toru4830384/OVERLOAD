package overload_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * アプリケーション全体のSpringコンテキスト起動を確認するテストクラス。
 */
@SpringBootTest
class OverloadApiApplicationTests {

    /**
     * Springのアプリケーションコンテキストが
     * 正常に読み込まれることを確認する。
     */
    @Test
    void contextLoads() {
        // コンテキストの起動に失敗した場合はテスト自体が失敗する。
    }
}