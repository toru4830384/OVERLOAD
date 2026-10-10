package overload_api.controller.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * UserUpdateRequestのバリデーションを確認するテストクラス。
 */
class UserUpdateRequestTest {

    /**
     * バリデーションを実行するValidator。
     */
    private Validator validator;

    /**
     * 各テスト実行前にValidatorを生成する。
     */
    @BeforeEach
    void setUp() {
        validator =
                Validation.buildDefaultValidatorFactory()
                        .getValidator();
    }

    /**
     * 年齢が0の場合に、
     * バリデーションエラーにならないことを確認する。
     */
    @Test
    void validationSucceedsWhenAgeIsZero() {
        UserUpdateRequest request = createValidRequest();

        request.setAge(0);

        Set<ConstraintViolation<UserUpdateRequest>> violations =
                validator.validate(request);

        assertEquals(0, violations.size());
    }

    /**
     * 年齢がマイナスの場合に、
     * ageフィールドでバリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenAgeIsNegative() {
        UserUpdateRequest request = createValidRequest();

        request.setAge(-1);

        Set<ConstraintViolation<UserUpdateRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "age",
                violations.iterator()
                        .next()
                        .getPropertyPath()
                        .toString());
        assertEquals(
                "年齢は0以上の値を入力してください",
                violations.iterator()
                        .next()
                        .getMessage());
    }

    /**
     * 体重が0の場合に、
     * bodyWeightKgフィールドでバリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenBodyWeightKgIsZero() {
        UserUpdateRequest request = createValidRequest();

        request.setBodyWeightKg(BigDecimal.ZERO);

        Set<ConstraintViolation<UserUpdateRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "bodyWeightKg",
                violations.iterator()
                        .next()
                        .getPropertyPath()
                        .toString());
        assertEquals(
                "体重は0より大きい値を入力してください",
                violations.iterator()
                        .next()
                        .getMessage());
    }

    /**
     * 体重がマイナスの場合に、
     * bodyWeightKgフィールドでバリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenBodyWeightKgIsNegative() {
        UserUpdateRequest request = createValidRequest();

        request.setBodyWeightKg(
                new BigDecimal("-1.00"));

        Set<ConstraintViolation<UserUpdateRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "bodyWeightKg",
                violations.iterator()
                        .next()
                        .getPropertyPath()
                        .toString());
    }

    /**
     * 正常な入力値の場合に、
     * バリデーションエラーにならないことを確認する。
     */
    @Test
    void validationSucceedsWhenRequestIsValid() {
        UserUpdateRequest request = createValidRequest();

        Set<ConstraintViolation<UserUpdateRequest>> violations =
                validator.validate(request);

        assertEquals(0, violations.size());
    }

    /** DBに到達する前に未入力年齢と不正性別を拒否する。 */
    @Test
    void rejectsNullAgeAndInvalidGender() {
        var request = createValidRequest();
        request.setAge(null);
        assertEquals("age", validator.validate(request).iterator().next().getPropertyPath().toString());
        request.setAge(0);
        for (String gender : new String[] {null, "男性", "MALE", "", "other", "none"}) {
            request.setGender(gender);
            var violations = validator.validate(request);
            assertEquals(1, violations.size());
            assertEquals("gender", violations.iterator().next().getPropertyPath().toString());
        }
        for (String gender : new String[] {"male", "female"}) {
            request.setGender(gender);
            assertEquals(0, validator.validate(request).size());
        }
    }

    /**
     * 正常値を設定したユーザー更新リクエストを生成する。
     *
     * @return 正常値を設定したユーザー更新リクエスト
     */
    private UserUpdateRequest createValidRequest() {
        UserUpdateRequest request =
                new UserUpdateRequest();

        request.setName("テストユーザー");
        request.setGender("male");
        request.setAge(31);
        request.setBodyWeightKg(
                new BigDecimal("84.00"));

        return request;
    }
    /** 年齢未指定を拒否し、0歳を受け付ける。 */
    @Test
    void rejectsNullAgeAndAcceptsZero() {
        var request = createValidRequest();
        request.setAge(null);
        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("age", violations.iterator().next().getPropertyPath().toString());
        request.setAge(0);
        assertEquals(0, validator.validate(request).size());
    }
}
