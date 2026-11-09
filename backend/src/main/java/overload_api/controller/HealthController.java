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
            // 個人の記録を取得せず、移行不足・DB停止・権限不足も検出する。
            jdbc.queryForObject("SELECT (SELECT COUNT(*) FROM user_credentials WHERE 1=0)"
                + " + (SELECT COUNT(*) FROM workout_sessions WHERE 1=0)"
                + " + (SELECT COUNT(*) FROM SPRING_SESSION WHERE 1=0)", Integer.class);
            return ResponseEntity.ok(Map.of("status", "up"));
        } catch (DataAccessException unavailable) {
            return ResponseEntity.status(503).body(Map.of("status", "down"));
        }
    }
}
