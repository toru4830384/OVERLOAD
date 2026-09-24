package overload_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import overload_api.mapper.ExerciseMapper;
import overload_api.model.Exercise;

/**
 * 種目情報に関する業務処理を提供するサービス。
 */
@Service
public class ExerciseService {

    private final ExerciseMapper exerciseMapper;

    /**
     * ExerciseServiceを生成する。
     *
     * @param exerciseMapper 種目情報を操作するMapper
     */
    public ExerciseService(ExerciseMapper exerciseMapper) {
        this.exerciseMapper = exerciseMapper;
    }

    /**
     * 全ての種目情報を取得する。
     *
     * @return 種目情報一覧
     */
    public List<Exercise> findAll() {
        return exerciseMapper.findAll();
    }

}