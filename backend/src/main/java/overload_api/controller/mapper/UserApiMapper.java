package overload_api.controller.mapper;

import java.util.Objects;

import overload_api.controller.dto.UserResponse;
import overload_api.model.User;

/**
 * Userの永続化モデルをAPIレスポンスDTOへ変換する。
 */
public final class UserApiMapper {

    private UserApiMapper() {
    }

    /**
     * 永続化モデルからAPI公開項目だけをコピーする。
     *
     * @param model サービスが返した永続化モデル
     * @return APIレスポンスDTO
     */
    public static UserResponse toResponse(User model) {
        Objects.requireNonNull(model, "Response model must not be null");
        UserResponse response = new UserResponse();
        response.setId(model.getId());
        response.setName(model.getName());
        response.setGender(model.getGender());
        response.setAge(model.getAge());
        response.setBodyWeightKg(model.getBodyWeightKg());
        return response;
    }
}
