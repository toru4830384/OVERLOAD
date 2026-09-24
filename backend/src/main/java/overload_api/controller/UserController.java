package overload_api.controller;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
     * @throws ResponseStatusException ユーザーが存在しない場合
     */
    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {

        User user = userService.findById(id);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        return user;
    }

    /**
     * 指定されたIDのユーザー情報を更新する。
     *
     * @param id ユーザーID
     * @param request 更新するユーザー情報
     * @return 更新後のユーザー情報
     * @throws ResponseStatusException 入力値が不正な場合、またはユーザーが存在しない場合
     */
    @PutMapping("/{id}")
    public User update(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {

        // 年齢と体重の入力値を事前にチェックする。
        if (request.getAge() != null && request.getAge() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "年齢は0以上の値を入力してください"
            );
        }

        if (request.getBodyWeightKg() != null
                && request.getBodyWeightKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "体重は0より大きい値を入力してください"
            );
        }

        User user;

        try {
            user = userService.update(
                    id,
                    request.getName(),
                    request.getGender(),
                    request.getAge(),
                    request.getBodyWeightKg()
            );
        } catch (DataIntegrityViolationException e) {
            // DBの制約違反をAPIの400エラーとして返す。
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "入力値が不正です",
                    e
            );
        }

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        return user;
    }

}