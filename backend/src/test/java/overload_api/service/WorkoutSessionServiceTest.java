package overload_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.mapper.WorkoutSessionMapper;
import overload_api.model.WorkoutSession;

/**
 * WorkoutSessionServiceの単体テスト。
 */
class WorkoutSessionServiceTest {

    /**
     * セッションが存在する場合、一覧を返すことを確認する。
     */
    @Test
    void findAll_セッションが存在する場合_一覧を返す() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSessionService workoutSessionService =
                new WorkoutSessionService(workoutSessionMapper);

        WorkoutSession session1 = new WorkoutSession();
        session1.setId(1L);
        session1.setUserId(1L);

        WorkoutSession session2 = new WorkoutSession();
        session2.setId(2L);
        session2.setUserId(1L);

        when(workoutSessionMapper.findAll())
                .thenReturn(List.of(session1, session2));

        List<WorkoutSession> result =
                workoutSessionService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(1L, result.get(1).getUserId());
    }

    /**
     * セッションが存在しない場合、空のリストを返すことを確認する。
     */
    @Test
    void findAll_セッションが存在しない場合_空のリストを返す() {
        WorkoutSessionMapper workoutSessionMapper =
                mock(WorkoutSessionMapper.class);

        WorkoutSessionService workoutSessionService =
                new WorkoutSessionService(workoutSessionMapper);

        when(workoutSessionMapper.findAll())
                .thenReturn(List.of());

        List<WorkoutSession> result =
                workoutSessionService.findAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}