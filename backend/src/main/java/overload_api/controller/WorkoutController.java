package overload_api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.jdbc.UncategorizedSQLException;

import overload_api.controller.dto.WorkoutHistoryResponse;
import overload_api.controller.dto.WorkoutRequest;
import overload_api.model.WorkoutSet;
import overload_api.service.WorkoutService;

/**
 * ワークアウトに関するAPIを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    /**
     * WorkoutControllerを生成する。
     *
     * @param workoutService ワークアウト情報を扱うサービス
     */
    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    /**
     * ワークアウトを登録する。
     *
     * @param request 登録するワークアウト情報
     * @return 登録したワークアウトセット一覧
     * @throws ResponseStatusException 入力値が不正な場合は400を返す
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<WorkoutSet> create(@Valid @RequestBody WorkoutRequest request) {
        try {
            return workoutService.create(request);
        } catch (UncategorizedSQLException e) {
            // データベース登録時の不正な入力をAPIの400エラーとして返す。
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "入力値が不正です",
                    e
            );
        }
    }

    /**
     * 指定されたユーザーのワークアウト履歴を取得する。
     *
     * @param userId ユーザーID
     * @return ワークアウト履歴一覧
     */
    @GetMapping("/history")
    public List<WorkoutHistoryResponse> findHistory(
            @RequestParam("user_id") Long userId) {
        return workoutService.findHistoryByUserId(userId);
    }
}