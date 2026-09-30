package overload_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import overload_api.controller.dto.UserUpdateRequest;
import overload_api.model.User;
import overload_api.service.UserService;

/**
 * ユーザー情報に関するAPIを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/users")
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
     * 指定されたIDのユーザー情報を取得する。
     *
     * @param id ユーザーID
     * @return ユーザー情報
     */
    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    /**
     * 指定されたIDのユーザー情報を更新する。
     *
     * @param id ユーザーID
     * @param request 更新するユーザー情報
     * @return 更新後のユーザー情報
     */
    @PutMapping("/{id}")
    public User update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        return userService.update(
                id,
                request.getName(),
                request.getGender(),
                request.getAge(),
                request.getBodyWeightKg());
    }
}