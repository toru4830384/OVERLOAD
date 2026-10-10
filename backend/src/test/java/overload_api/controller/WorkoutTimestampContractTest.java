package overload_api.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import overload_api.model.WorkoutSession;
import overload_api.service.WorkoutSessionService;
import overload_api.service.WorkoutService;
import overload_api.service.dto.WorkoutHistoryResponse;

/** 履歴とセッション一覧の日時表現が同じであることをHTTP境界で確認する。 */
class WorkoutTimestampContractTest {

    @Test
    void historyAndSessionsExposeTheSameOffsetAndNullableFinish() throws Exception {
        var sessions = mock(WorkoutSessionService.class);
        var workouts = mock(WorkoutService.class);
        var start = LocalDateTime.of(2026, 1, 1, 10, 0);
        var session = new WorkoutSession();
        session.setId(1L);
        session.setStartedAt(start);
        var history = new WorkoutHistoryResponse();
        history.setSessionId(1L);
        history.setStartedAt(start);
        when(sessions.findAll()).thenReturn(List.of(session));
        when(workouts.findHistoryByUserId(1L)).thenReturn(List.of(history));
        var mvc = MockMvcBuilders.standaloneSetup(
                new WorkoutSessionController(sessions), new WorkoutController(workouts)).build();
        mvc.perform(get("/api/workout-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startedAt").value("2026-01-01T10:00:00+09:00"))
                .andExpect(jsonPath("$[0].finishedAt").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(get("/api/workouts/history").param("user_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startedAt").value("2026-01-01T10:00:00+09:00"));
        session.setFinishedAt(start.plusHours(1));
        mvc.perform(get("/api/workout-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].finishedAt").value("2026-01-01T11:00:00+09:00"));
    }
}
