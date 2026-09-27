package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.model.WorkoutSession;
import overload_api.service.WorkoutSessionService;

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

        WorkoutSession session2 =
                new WorkoutSession();
        session2.setId(2L);
        session2.setUserId(1L);

        when(workoutSessionService.findAll())
                .thenReturn(List.of(
                        session1,
                        session2));

        WorkoutSessionController workoutSessionController =
                new WorkoutSessionController(workoutSessionService);

        List<WorkoutSession> result =
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
                2L,
                result.get(1).getId());
        assertEquals(
                1L,
                result.get(1).getUserId());

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

        List<WorkoutSession> result =
                workoutSessionController.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(workoutSessionService, times(1))
                .findAll();
    }
}