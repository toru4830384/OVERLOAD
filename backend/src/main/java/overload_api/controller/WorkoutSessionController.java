package overload_api.controller;

import java.util.List;
import overload_api.auth.CurrentUser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.controller.dto.WorkoutSessionResponse;
import overload_api.controller.mapper.WorkoutApiMapper;
import overload_api.service.WorkoutSessionService;

/**
 * ワークアウトセッションに関するAPIを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/workout-sessions")
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    /**
     * WorkoutSessionControllerを生成する。
     *
     * @param workoutSessionService ワークアウトセッション情報を扱うサービス
     */
    public WorkoutSessionController(
            WorkoutSessionService workoutSessionService) {
        this.workoutSessionService = workoutSessionService;
    }

    /**
     * ログイン本人のワークアウトセッションを取得する。
     *
     * @return ワークアウトセッション一覧
     */
    @GetMapping
    public List<WorkoutSessionResponse> findAll() {
        return workoutSessionService.findByUserId(CurrentUser.id())
                .stream()
                .map(WorkoutApiMapper::toSessionResponse)
                .toList();
    }

}
