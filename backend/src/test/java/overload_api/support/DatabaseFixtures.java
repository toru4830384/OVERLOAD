package overload_api.support;

import java.sql.Statement;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

/** テストデータ登録時に、同じINSERTから生成キーを取得する。 */
public final class DatabaseFixtures {
    private DatabaseFixtures() {
    }

    /**
     * パラメータを設定してINSERTを実行し、生成されたIDを返す。
     *
     * @param jdbcTemplate DB操作オブジェクト
     * @param sql INSERT文
     * @param values パラメータ
     * @return 生成されたID
     */
    public static Long insert(JdbcTemplate jdbcTemplate, String sql, Object... values) {
        var keys = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            return statement;
        }, keys);
        return Objects.requireNonNull(keys.getKey(), "INSERT did not return a generated key")
                .longValue();
    }
}
