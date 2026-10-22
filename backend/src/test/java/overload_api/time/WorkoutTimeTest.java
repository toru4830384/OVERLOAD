package overload_api.time;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

/** 実行環境のタイムゾーンに依存せず、同じ瞬間を保存・復元できることを確認する。 */
class WorkoutTimeTest {

    @Test
    void preservesInstantUnderDifferentServerTimeZones() {
        TimeZone original = TimeZone.getDefault();
        try {
            for (String zone : new String[] {"UTC", "America/Los_Angeles", "Asia/Tokyo"}) {
                TimeZone.setDefault(TimeZone.getTimeZone(zone));
                OffsetDateTime input = OffsetDateTime.parse("2026-01-01T01:00:00Z");
                LocalDateTime stored = WorkoutTime.toDatabaseTime(input);
                assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), stored);
                assertEquals(input.toInstant(), WorkoutTime.toApiTime(stored).toInstant());
                assertEquals(9 * 3600, WorkoutTime.toApiTime(stored).getOffset().getTotalSeconds());
                assertEquals(9 * 3600, WorkoutTime.now().getOffset().getTotalSeconds());
            }
        } finally {
            TimeZone.setDefault(original);
        }
    }

    @Test
    void preservesNullableFinishAndRejectsMissingRequiredTime() {
        assertNull(WorkoutTime.toApiTime(null));
        assertThrows(NullPointerException.class, () -> WorkoutTime.toDatabaseTime(null));
    }
}
