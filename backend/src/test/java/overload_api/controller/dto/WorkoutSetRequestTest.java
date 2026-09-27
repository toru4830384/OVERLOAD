package overload_api.controller.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * WorkoutSetRequestのバリデーションを確認するテストクラス。
 */
class WorkoutSetRequestTest {

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
     * 重量がnullの場合に、
     * weightKgに対するNotNullの
     * バリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenWeightKgIsNull() {
        WorkoutSetRequest request =
                createValidRequest();

        request.setWeightKg(null);

        Set<ConstraintViolation<WorkoutSetRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutSetRequest> violation =
                violations.iterator().next();

        assertEquals(
                "weightKg",
                violation.getPropertyPath().toString());

        assertEquals(
                NotNull.class,
                violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType());
    }

    /**
     * 重量が0の場合に、
     * weightKgに対するDecimalMinの
     * バリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenWeightKgIsZero() {
        WorkoutSetRequest request =
                createValidRequest();

        request.setWeightKg(BigDecimal.ZERO);

        Set<ConstraintViolation<WorkoutSetRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutSetRequest> violation =
                violations.iterator().next();

        assertEquals(
                "weightKg",
                violation.getPropertyPath().toString());

        assertEquals(
                DecimalMin.class,
                violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType());

        assertEquals(
                "重量は0より大きい値を入力してください",
                violation.getMessage());
    }

    /**
     * セット番号がnullの場合に、
     * setNumberに対するNotNullの
     * バリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenSetNumberIsNull() {
        WorkoutSetRequest request =
                createValidRequest();

        request.setSetNumber(null);

        Set<ConstraintViolation<WorkoutSetRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutSetRequest> violation =
                violations.iterator().next();

        assertEquals(
                "setNumber",
                violation.getPropertyPath().toString());

        assertEquals(
                NotNull.class,
                violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType());

        assertEquals(
                "セット番号は必須です",
                violation.getMessage());
    }

    /**
     * セット番号が0の場合に、
     * setNumberに対するMinの
     * バリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenSetNumberIsZero() {
        WorkoutSetRequest request =
                createValidRequest();

        request.setSetNumber(0);

        Set<ConstraintViolation<WorkoutSetRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutSetRequest> violation =
                violations.iterator().next();

        assertEquals(
                "setNumber",
                violation.getPropertyPath().toString());

        assertEquals(
                Min.class,
                violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType());

        assertEquals(
                "セット番号は1以上の値を入力してください",
                violation.getMessage());
    }

    /**
     * 回数がnullの場合に、
     * repsに対するNotNullの
     * バリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenRepsIsNull() {
        WorkoutSetRequest request =
                createValidRequest();

        request.setReps(null);

        Set<ConstraintViolation<WorkoutSetRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutSetRequest> violation =
                violations.iterator().next();

        assertEquals(
                "reps",
                violation.getPropertyPath().toString());

        assertEquals(
                NotNull.class,
                violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType());

        assertEquals(
                "回数は必須です",
                violation.getMessage());
    }

    /**
     * 回数が0の場合に、
     * repsに対するMinの
     * バリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenRepsIsZero() {
        WorkoutSetRequest request =
                createValidRequest();

        request.setReps(0);

        Set<ConstraintViolation<WorkoutSetRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutSetRequest> violation =
                violations.iterator().next();

        assertEquals(
                "reps",
                violation.getPropertyPath().toString());

        assertEquals(
                Min.class,
                violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType());

        assertEquals(
                "回数は0より大きい値を入力してください",
                violation.getMessage());
    }

    /**
     * 各項目に最小の許容値を設定した場合に、
     * バリデーションエラーにならないことを確認する。
     */
    @Test
    void validationSucceedsWithMinimumValidValues() {
        WorkoutSetRequest request =
                new WorkoutSetRequest();

        request.setSetNumber(1);
        request.setWeightKg(
                new BigDecimal("0.01"));
        request.setReps(1);

        Set<ConstraintViolation<WorkoutSetRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    /**
     * すべての項目に正常値を設定したリクエストを生成する。
     *
     * @return 正常値が設定されたワークアウトセットリクエスト
     */
    private WorkoutSetRequest createValidRequest() {
        WorkoutSetRequest request =
                new WorkoutSetRequest();

        request.setSetNumber(1);
        request.setWeightKg(
                new BigDecimal("50.00"));
        request.setReps(10);

        return request;
    }
}