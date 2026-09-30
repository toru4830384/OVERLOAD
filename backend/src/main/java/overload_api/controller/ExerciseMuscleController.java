package overload_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.model.ExerciseMuscle;
import overload_api.service.ExerciseMuscleService;

/**
 * 種目と筋肉の関連情報に関するAPIを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/exercise-muscles")
public class ExerciseMuscleController {

    private final ExerciseMuscleService exerciseMuscleService;

    /**
     * ExerciseMuscleControllerを生成する。
     *
     * @param exerciseMuscleService 種目と筋肉の関連情報を扱うサービス
     */
    public ExerciseMuscleController(ExerciseMuscleService exerciseMuscleService) {
        this.exerciseMuscleService = exerciseMuscleService;
    }

    /**
     * 全ての種目と筋肉の関連情報を取得する。
     *
     * @return 種目と筋肉の関連情報一覧
     */
    @GetMapping
    public List<ExerciseMuscle> findAll() {
        return exerciseMuscleService.findAll();
    }

}