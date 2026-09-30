package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.model.WorkoutSession;
import overload_api.service.WorkoutSessionService;
import overload_api.controller.dto.WorkoutSessionResponse;

/**
 * WorkoutSessionControllerの単体テストを行うクラス。
 */
class WorkoutSessionControllerTest {

    /**
     * セッションが存在する場合に、
     * セッション一覧を正しく返すことを確認する。
     */
    @Test
    void findAllReturnsWorkoutSessionsWhenDataExists() {
        WorkoutSessionService workoutSessionService =
                mock(WorkoutSessionService.class);

        WorkoutSession session1 =
                new WorkoutSession();
        session1.setId(1L);
        session1.setUserId(1L);
        session1.setStartedAt(
                LocalDateTime.of(2026, 9, 29, 10, 0));
        session1.setFinishedAt(
                LocalDateTime.of(2026, 9, 29, 11, 0));

        WorkoutSession session2 =
                new WorkoutSession();
        session2.setId(2L);
        session2.setUserId(1L);
        session2.setStartedAt(
                LocalDateTime.of(2026, 9, 29, 12, 0));
        session2.setFinishedAt(
                LocalDateTime.of(2026, 9, 29, 13, 0));

        when(workoutSessionService.findAll())
                .thenReturn(List.of(
                        session1,
                        session2));

        WorkoutSessionController workoutSessionController =
                new WorkoutSessionController(workoutSessionService);

        List<WorkoutSessionResponse> result =
                workoutSessionController.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                1L,
                result.get(0).getId());
        assertEquals(
                1L,
                result.get(0).getUserId());
        assertEquals(
                LocalDateTime.of(2026, 9, 29, 10, 0),
                result.get(0).getStartedAt());
        assertEquals(
                LocalDateTime.of(2026, 9, 29, 11, 0),
                result.get(0).getFinishedAt());

        assertEquals(
                2L,
                result.get(1).getId());
        assertEquals(
                1L,
                result.get(1).getUserId());
        assertEquals(
                LocalDateTime.of(2026, 9, 29, 12, 0),
                result.get(1).getStartedAt());
        assertEquals(
                LocalDateTime.of(2026, 9, 29, 13, 0),
                result.get(1).getFinishedAt());

        verify(workoutSessionService, times(1))
                .findAll();
    }

    /**
     * セッションが存在しない場合に、
     * 空のリストを返すことを確認する。
     */
    @Test
    void findAllReturnsEmptyListWhenNoDataExists() {
        WorkoutSessionService workoutSessionService =
                mock(WorkoutSessionService.class);

        when(workoutSessionService.findAll())
                .thenReturn(List.of());

        WorkoutSessionController workoutSessionController =
                new WorkoutSessionController(workoutSessionService);

        List<WorkoutSessionResponse> result =
                workoutSessionController.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(workoutSessionService, times(1))
                .findAll();
    }
}