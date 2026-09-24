package overload_api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.controller.dto.WorkoutSessionRequest;
import overload_api.model.WorkoutSession;
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
    public WorkoutSessionController(WorkoutSessionService workoutSessionService) {
        this.workoutSessionService = workoutSessionService;
    }

    /**
     * 全てのワークアウトセッションを取得する。
     *
     * @return ワークアウトセッション一覧
     */
    @GetMapping
    public List<WorkoutSession> findAll() {
        return workoutSessionService.findAll();
    }

    /**
     * ワークアウトセッションを登録する。
     *
     * @param request 登録するワークアウトセッション情報
     * @return 登録したワークアウトセッション
     */
    @PostMapping
    public WorkoutSession create(
            @Valid @RequestBody WorkoutSessionRequest request) {
        return workoutSessionService.create(request.getUserId());
    }

}