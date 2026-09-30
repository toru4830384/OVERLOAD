package overload_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import overload_api.mapper.ExerciseMuscleMapper;
import overload_api.model.ExerciseMuscle;

/**
 * 種目と筋肉の関連情報に関する業務処理を提供するサービス。
 */
@Service
public class ExerciseMuscleService {

    private final ExerciseMuscleMapper exerciseMuscleMapper;

    /**
     * ExerciseMuscleServiceを生成する。
     *
     * @param exerciseMuscleMapper 種目と筋肉の関連情報を操作するMapper
     */
    public ExerciseMuscleService(ExerciseMuscleMapper exerciseMuscleMapper) {
        this.exerciseMuscleMapper = exerciseMuscleMapper;
    }

    /**
     * 全ての種目と筋肉の関連情報を取得する。
     *
     * @return 種目と筋肉の関連情報一覧
     */
    public List<ExerciseMuscle> findAll() {
        return exerciseMuscleMapper.findAll();
    }

}