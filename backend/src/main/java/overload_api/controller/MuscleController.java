package overload_api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import overload_api.model.Muscle;
import overload_api.service.MuscleService;

/**
 * 筋肉情報に関するAPIを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/muscles")
public class MuscleController {

    private final MuscleService muscleService;

    /**
     * MuscleControllerを生成する。
     *
     * @param muscleService 筋肉情報を扱うサービス
     */
    public MuscleController(MuscleService muscleService) {
        this.muscleService = muscleService;
    }

    /**
     * 全ての筋肉情報を取得する。
     *
     * @return 筋肉情報一覧
     */
    @GetMapping
    public List<Muscle> findAll() {
        return muscleService.findAll();
    }

}