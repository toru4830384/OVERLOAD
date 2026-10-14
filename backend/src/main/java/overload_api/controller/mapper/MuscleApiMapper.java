package overload_api.controller.mapper;

import java.util.Objects;

import overload_api.controller.dto.MuscleResponse;
import overload_api.model.Muscle;

/**
 * Muscleの永続化モデルをAPIレスポンスDTOへ変換する。
 */
public final class MuscleApiMapper {

    private MuscleApiMapper() {
    }

    /**
     * 永続化モデルからAPI公開項目だけをコピーする。
     *
     * @param model サービスが返した永続化モデル
     * @return APIレスポンスDTO
     */
    public static MuscleResponse toResponse(Muscle model) {
        Objects.requireNonNull(model, "Response model must not be null");
        MuscleResponse response = new MuscleResponse();
        response.setId(model.getId());
        response.setName(model.getName());
        return response;
    }
}
