package overload_api.controller.mapper;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import overload_api.controller.dto.*;
import overload_api.model.*;

/** API公開項目のコピーと日時の復元を確認する。 */
class WorkoutApiMapperTest {
    @Test
    void historyRestoresInstantAndHandlesNullableNoteAndSets() {
        var start = OffsetDateTime.parse("2026-01-01T10:00:00+09:00");
        var history = new overload_api.service.dto.WorkoutHistoryResponse();
        history.setStartedAt(start.atZoneSameInstant(ZoneId.of("Asia/Tokyo")).toLocalDateTime());
        var response = WorkoutApiMapper.toHistoryResponses(List.of(history)).get(0);
        assertEquals(start.toInstant(), response.getStartedAt().toInstant());
        assertNull(response.getNote());
        assertTrue(response.getSets().isEmpty());
    }
}
