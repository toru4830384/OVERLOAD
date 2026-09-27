package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.mapper.WorkoutSessionMapper;
import overload_api.model.WorkoutSession;

/**
 * WorkoutSessionServiceの単体テストを行うクラス。
 */
class WorkoutSessionServiceTest {

    /**
     * セッションが存在する場合に、
     * Mapperから取得したセッション一覧を正しく返すことを確認する。
     */
    @Test
    void findAllReturnsWorkoutSessionsWhenDataExists() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSessionService workoutSessionService =
                new WorkoutSessionService(workoutSessionMapper);

        WorkoutSession session1 =
                new WorkoutSession();
        session1.setId(1L);
        session1.setUserId(1L);

        WorkoutSession session2 =
                new WorkoutSession();
        session2.setId(2L);
        session2.setUserId(1L);

        when(workoutSessionMapper.findAll())
                .thenReturn(List.of(
                        session1,
                        session2));

        List<WorkoutSession> result =
                workoutSessionService.findAll();

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

        verify(workoutSessionMapper, times(1))
                .findAll();
    }

    /**
     * セッションが存在しない場合に、
     * 空のリストを返すことを確認する。
     */
    @Test
    void findAllReturnsEmptyListWhenNoDataExists() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSessionService workoutSessionService =
                new WorkoutSessionService(workoutSessionMapper);

        when(workoutSessionMapper.findAll())
                .thenReturn(List.of());

        List<WorkoutSession> result =
                workoutSessionService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(workoutSessionMapper, times(1))
                .findAll();
    }
}