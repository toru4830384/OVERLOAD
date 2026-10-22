package overload_api.controller.mapper;

import java.util.Objects;

import overload_api.controller.dto.ExerciseMuscleResponse;
import overload_api.model.ExerciseMuscle;

/**
 * ExerciseMuscleの永続化モデルをAPIレスポンスDTOへ変換する。
 */
public final class ExerciseMuscleApiMapper {

    private ExerciseMuscleApiMapper() {
    }

    /**
     * 永続化モデルからAPI公開項目だけをコピーする。
     *
     * @param model サービスが返した永続化モデル
     * @return APIレスポンスDTO
     */
    public static ExerciseMuscleResponse toResponse(ExerciseMuscle model) {
        Objects.requireNonNull(model, "Response model must not be null");
        ExerciseMuscleResponse response = new ExerciseMuscleResponse();
        response.setExerciseId(model.getExerciseId());
        response.setMuscleId(model.getMuscleId());
        return response;
    }
}
