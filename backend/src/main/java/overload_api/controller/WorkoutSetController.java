package overload_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.controller.dto.WorkoutSetRequest;
import overload_api.model.WorkoutSet;
import overload_api.service.WorkoutSetService;

/**
 * ワークアウトセットに関するAPIを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/workout-sets")
public class WorkoutSetController {

    private final WorkoutSetService workoutSetService;

    /**
     * WorkoutSetControllerを生成する。
     *
     * @param workoutSetService ワークアウトセット情報を扱うサービス
     */
    public WorkoutSetController(WorkoutSetService workoutSetService) {
        this.workoutSetService = workoutSetService;
    }

    /**
     * 全てのワークアウトセットを取得する。
     *
     * @return ワークアウトセット一覧
     */
    @GetMapping
    public List<WorkoutSet> findAll() {
        return workoutSetService.findAll();
    }

    /**
     * ワークアウトセットを登録する。
     *
     * @param request 登録するワークアウトセット情報
     * @return 登録したワークアウトセット
     */
    @PostMapping
    public WorkoutSet create(@RequestBody WorkoutSetRequest request) {
        return workoutSetService.create(
                request.getSessionId(),
                request.getExerciseId(),
                request.getSetNumber(),
                request.getWeightKg(),
                request.getReps(),
                request.getNote()
        );
    }
}