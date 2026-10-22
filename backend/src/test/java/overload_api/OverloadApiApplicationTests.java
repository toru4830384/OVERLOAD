package overload_api;

import org.junit.jupiter.api.Test;
import overload_api.support.MySqlIntegrationTest;

/**
 * アプリケーション全体のSpringコンテキスト起動を確認するテストクラス。
 */
class OverloadApiApplicationTests extends MySqlIntegrationTest {

    /**
     * Springのアプリケーションコンテキストが
     * 正常に読み込まれることを確認する。
     */
    @Test
    void contextLoads() {
        // コンテキストの起動に失敗した場合はテスト自体が失敗する。
    }
}