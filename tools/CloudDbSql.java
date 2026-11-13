import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;

/** 公開DB準備用。アプリと同じJDBCドライバーでSQLを実行し、秘密をログへ出さない。 */
public final class CloudDbSql {
    private CloudDbSql() {}

    /**
     * SQLは長さ付きの標準入力、接続情報は子プロセスの環境変数だけから受け取る。
     * 複数SQLも同じ接続で実行し、パスワード内のセミコロンを分割しない。
     * @param args 使用しない。資格情報をプロセス引数へ含めない。
     */
    public static void main(String[] args) {
        try (var input = new DataInputStream(System.in)) {
            int count = input.readInt();
            if (count < 1 || count > 32) throw new IllegalArgumentException();
            var statements = new String[count];
            for (int i = 0; i < count; i++) {
                int length = input.readInt();
                if (length < 1 || length > 1_000_000) throw new IllegalArgumentException();
                byte[] bytes = input.readNBytes(length);
                if (bytes.length != length) throw new IllegalArgumentException();
                statements[i] = new String(bytes, StandardCharsets.UTF_8);
            }
            try (var connection = DriverManager.getConnection(System.getenv("OVERLOAD_SQL_URL"),
                    System.getenv("OVERLOAD_SQL_USER"), System.getenv("OVERLOAD_SQL_PASSWORD"))) {
                var output = new StringBuilder();
                for (String sql : statements) {
                    try (var statement = connection.createStatement()) {
                        statement.setQueryTimeout(45);
                        if (statement.execute(sql)) {
                            try (var rows = statement.getResultSet()) {
                                int columns = rows.getMetaData().getColumnCount();
                                while (rows.next()) {
                                    for (int column = 1; column <= columns; column++) {
                                        if (column > 1) output.append('\t');
                                        String value = rows.getString(column);
                                        output.append(value == null ? "NULL" : value);
                                    }
                                    output.append('\n');
                                }
                            }
                        }
                    }
                }
                System.out.print(output);
            }
        } catch (SQLException error) {
            // JDBCの例外本文にはSQLや認証情報が含まれ得るため、番号だけを返す。
            System.err.println("DB_ERROR:" + error.getErrorCode());
            System.exit(1);
        } catch (Exception error) {
            System.err.println("CLIENT_ERROR");
            System.exit(1);
        }
    }
}
