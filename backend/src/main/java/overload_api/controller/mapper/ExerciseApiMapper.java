package overload_api.controller.mapper;

import java.util.Objects;

import overload_api.controller.dto.ExerciseResponse;
import overload_api.model.Exercise;

/**
 * Exerciseの永続化モデルをAPIレスポンスDTOへ変換する。
 */
public final class ExerciseApiMapper {

    private ExerciseApiMapper() {
    }

    /**
     * 永続化モデルからAPI公開項目だけをコピーする。
     *
     * @param model サービスが返した永続化モデル
     * @return APIレスポンスDTO
     */
    public static ExerciseResponse toResponse(Exercise model) {
        Objects.requireNonNull(model, "Response model must not be null");
        ExerciseResponse response = new ExerciseResponse();
        response.setId(model.getId());
        response.setName(model.getName());
        response.setCategory(model.getCategory());
        response.setBarbell(model.getBarbell());
        response.setEquipment(model.getEquipment());
        response.setPattern(model.getPattern());
        return response;
    }
}
