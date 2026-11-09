package overload_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import overload_api.auth.CurrentUser;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import overload_api.controller.dto.UserUpdateRequest;
import overload_api.controller.dto.UserResponse;
import overload_api.controller.mapper.UserApiMapper;
import overload_api.service.UserService;

/**
 * ユーザー情報に関するAPIを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/me")
public class UserController {

    private final UserService userService;

    /**
     * UserControllerを生成する。
     *
     * @param userService ユーザー情報を操作するサービス
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ログイン本人のユーザー情報を取得する。
     *
     * @return ユーザー情報
     */
    @GetMapping
    public UserResponse findById() {
        return UserApiMapper.toResponse(userService.findById(CurrentUser.id()));
    }

    /**
     * ログイン本人のユーザー情報を更新する。
     *
     * @param request 更新するユーザー情報
     * @return 更新後のユーザー情報
     */
    @PutMapping
    public UserResponse update(
            @Valid @RequestBody UserUpdateRequest request) {

        return UserApiMapper.toResponse(userService.update(
                CurrentUser.id(),
                request.getName(),
                request.getGender(),
                request.getAge(),
                request.getBodyWeightKg()));
    }
}