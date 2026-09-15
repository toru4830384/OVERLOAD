package overload_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.model.Exercise;
import overload_api.service.ExerciseService;

/**
 * 種目に関するAPIを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    /**
     * ExerciseControllerを生成する。
     *
     * @param exerciseService 種目情報を扱うサービス
     */
    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    /**
     * 全ての種目を取得する。
     *
     * @return 種目一覧
     */
    @GetMapping
    public List<Exercise> findAll() {
        return exerciseService.findAll();
    }

}