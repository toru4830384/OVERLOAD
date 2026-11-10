package overload_api.controller;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.*;

/** DBと必要な表へ接続できることを確認し、内部情報を公開しない準備完了チェック。 */
@RestController
public class HealthController {
    private final JdbcTemplate jdbc;
    /** @param dataSource 業務用の制限されたDB接続 */
    public HealthController(DataSource dataSource) {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.setQueryTimeout(3);
    }
    /** @return DBの接続・スキーマを確認できたときだけ200、それ以外は詳細を隠して503 */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String,String>> health() {
        try {
            // 行を取得せず、認証世代列とGoogle連携表を含めて移行不足を検出する。
            jdbc.queryForObject("SELECT (SELECT COUNT(auth_version) FROM user_credentials WHERE 1=0)"
                + " + (SELECT COUNT(google_subject) FROM google_accounts WHERE 1=0)"
                + " + (SELECT COUNT(*) FROM workout_sessions WHERE 1=0)"
                + " + (SELECT COUNT(*) FROM SPRING_SESSION WHERE 1=0)", Integer.class);
            return ResponseEntity.ok(Map.of("status", "up"));
        } catch (DataAccessException unavailable) {
            return ResponseEntity.status(503).body(Map.of("status", "down"));
        }
    }
}
