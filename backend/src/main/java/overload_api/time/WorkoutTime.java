package overload_api.time;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * ワークアウト日時の保存・公開形式を統一する。
 * 既存DBのDATETIMEは日本時間として扱い、APIではUTCオフセットを明示する。
 * 実行環境のデフォルトタイムゾーンには依存しない。
 */
public final class WorkoutTime {

    private static final ZoneId DATABASE_ZONE = ZoneId.of("Asia/Tokyo");

    private WorkoutTime() {
    }

    /**
     * 現在日時をオフセット付きで取得する。
     *
     * @return 現在日時
     */
    public static OffsetDateTime now() {
        return OffsetDateTime.now(DATABASE_ZONE);
    }

    /**
     * 同じ瞬間をDBの日本時間へ変換する。
     *
     * @param time オフセット付き日時（必須）
     * @return DB保存用日時
     */
    public static LocalDateTime toDatabaseTime(OffsetDateTime time) {
        Objects.requireNonNull(time, "Workout time is required");
        return time.atZoneSameInstant(DATABASE_ZONE).toLocalDateTime();
    }

    /**
     * DBの日本時間へUTCオフセットを付けてAPIの日時へ変換する。
     *
     * @param time DB日時（未終了などのnullも許可）
     * @return オフセット付き日時。入力がnullの場合はnull
     */
    public static OffsetDateTime toApiTime(LocalDateTime time) {
        return time == null ? null : time.atZone(DATABASE_ZONE).toOffsetDateTime();
    }
}
