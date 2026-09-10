package overload_api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;

import overload_api.model.WorkoutSession;
import overload_api.service.WorkoutSessionService;
import overload_api.controller.dto.WorkoutSessionRequest;

class WorkoutSessionControllerTest {

    @Test
    void findAll_セッションが存在する場合_一覧を返す() {
        WorkoutSessionService workoutSessionService =
                mock(WorkoutSessionService.class);

        WorkoutSession session1 = new WorkoutSession();
        session1.setId(1L);
        session1.setUserId(1L);

        WorkoutSession session2 = new WorkoutSession();
        session2.setId(2L);
        session2.setUserId(1L);

        when(workoutSessionService.findAll())
                .thenReturn(List.of(session1, session2));

        WorkoutSessionController workoutSessionController =
                new WorkoutSessionController(workoutSessionService);

        List<WorkoutSession> result =
                workoutSessionController.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(1L, result.get(1).getUserId());
    }
    
    @Test
    void findAll_セッションが存在しない場合_空のリストを返す() {
        WorkoutSessionService workoutSessionService =
                mock(WorkoutSessionService.class);

        when(workoutSessionService.findAll())
                .thenReturn(List.of());

        WorkoutSessionController workoutSessionController =
                new WorkoutSessionController(workoutSessionService);

        List<WorkoutSession> result =
                workoutSessionController.findAll();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    void create_ユーザーIDを指定した場合_セッションを作成して返す() {
        WorkoutSessionService workoutSessionService =
                mock(WorkoutSessionService.class);

        WorkoutSessionRequest request =
                new WorkoutSessionRequest();
        request.setUserId(1L);

        WorkoutSession session = new WorkoutSession();
        session.setId(1L);
        session.setUserId(1L);

        when(workoutSessionService.create(1L))
                .thenReturn(session);

        WorkoutSessionController workoutSessionController =
                new WorkoutSessionController(workoutSessionService);

        WorkoutSession result =
                workoutSessionController.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        verify(workoutSessionService).create(1L);
    }
}