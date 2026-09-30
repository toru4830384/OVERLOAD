package overload_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import overload_api.mapper.MuscleMapper;
import overload_api.model.Muscle;

/**
 * 筋肉情報に関する業務処理を提供するサービス。
 */
@Service
public class MuscleService {

    private final MuscleMapper muscleMapper;

    /**
     * MuscleServiceを生成する。
     *
     * @param muscleMapper 筋肉情報を操作するMapper
     */
    public MuscleService(MuscleMapper muscleMapper) {
        this.muscleMapper = muscleMapper;
    }

    /**
     * 全ての筋肉情報を取得する。
     *
     * @return 筋肉情報一覧
     */
    public List<Muscle> findAll() {
        return muscleMapper.findAll();
    }

}