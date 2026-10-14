package overload_api.controller.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * WorkoutExerciseRequestのバリデーションをテストするクラス。
 */
class WorkoutExerciseRequestTest {

    /**
     * Bean Validationを実行するためのValidator。
     */
    private Validator validator;

    /**
     * テスト実行前にValidatorを生成する。
     */
    @BeforeEach
    void setUp() {
        validator =
                Validation.buildDefaultValidatorFactory()
                        .getValidator();
    }

    /**
     * setsがnullの場合に、
     * setsに対するNotEmptyのバリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenSetsIsNull() {
        WorkoutExerciseRequest request =
                new WorkoutExerciseRequest();

        request.setExerciseId(1L);
        request.setSets(null);

        Set<ConstraintViolation<WorkoutExerciseRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutExerciseRequest> violation =
                violations.iterator().next();

        assertEquals(
                "sets",
                violation.getPropertyPath().toString());

        assertEquals(
                NotEmpty.class,
                violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType());
    }

    /**
     * setsが空の場合に、
     * setsに対するNotEmptyのバリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenSetsIsEmpty() {
        WorkoutExerciseRequest request =
                new WorkoutExerciseRequest();

        request.setExerciseId(1L);
        request.setSets(List.of());

        Set<ConstraintViolation<WorkoutExerciseRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutExerciseRequest> violation =
                violations.iterator().next();

        assertEquals(
                "sets",
                violation.getPropertyPath().toString());

        assertEquals(
                NotEmpty.class,
                violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType());
    }

    /**
     * setsにnull要素が含まれる場合に、
     * バリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenSetsContainsNullElement() {
        WorkoutExerciseRequest request =
                new WorkoutExerciseRequest();

        request.setExerciseId(1L);
        request.setSets(
                java.util.Arrays.asList(
                        (WorkoutSetRequest) null));

        Set<ConstraintViolation<WorkoutExerciseRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
    }

    /**
     * exerciseIdがnullの場合に、
     * exerciseIdに対するNotNullの
     * バリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenExerciseIdIsNull() {
        WorkoutSetRequest setRequest =
                new WorkoutSetRequest();

        setRequest.setSetNumber(1);
        setRequest.setWeightKg(
                new BigDecimal("60.00"));
        setRequest.setReps(10);

        WorkoutExerciseRequest request =
                new WorkoutExerciseRequest();

        request.setExerciseId(null);
        request.setSets(List.of(setRequest));

        Set<ConstraintViolation<WorkoutExerciseRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutExerciseRequest> violation =
                violations.iterator().next();

        assertEquals(
                "exerciseId",
                violation.getPropertyPath().toString());

        assertEquals(
                NotNull.class,
                violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType());
    }

    /**
     * sets内のセット番号が0の場合に、
     * Validによって子DTOのMinの
     * バリデーションエラーが検出されることを確認する。
     */
    @Test
    void validationFailsWhenNestedSetNumberIsZero() {
        WorkoutSetRequest setRequest =
                new WorkoutSetRequest();

        setRequest.setSetNumber(0);
        setRequest.setWeightKg(
                new BigDecimal("60.00"));
        setRequest.setReps(10);

        WorkoutExerciseRequest request =
                new WorkoutExerciseRequest();

        request.setExerciseId(1L);
        request.setSets(List.of(setRequest));

        Set<ConstraintViolation<WorkoutExerciseRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutExerciseRequest> violation =
                violations.iterator().next();

        assertEquals(
                "sets[0].setNumber",
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
     * noteが100文字の場合に、
     * バリデーションエラーにならないことを確認する。
     */
    @Test
    void validationSucceedsWhenNoteHas100Characters() {
        WorkoutSetRequest setRequest =
                new WorkoutSetRequest();

        setRequest.setSetNumber(1);
        setRequest.setWeightKg(
                new BigDecimal("50.00"));
        setRequest.setReps(10);

        WorkoutExerciseRequest request =
                new WorkoutExerciseRequest();

        request.setExerciseId(1L);
        request.setNote("a".repeat(100));
        request.setSets(List.of(setRequest));

        Set<ConstraintViolation<WorkoutExerciseRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    /**
     * noteが101文字の場合に、
     * Sizeのバリデーションエラーになることを確認する。
     */
    @Test
    void validationFailsWhenNoteHas101Characters() {
        WorkoutSetRequest setRequest =
                new WorkoutSetRequest();

        setRequest.setSetNumber(1);
        setRequest.setWeightKg(
                new BigDecimal("50.00"));
        setRequest.setReps(10);

        WorkoutExerciseRequest request =
                new WorkoutExerciseRequest();

        request.setExerciseId(1L);
        request.setNote("a".repeat(101));
        request.setSets(List.of(setRequest));

        Set<ConstraintViolation<WorkoutExerciseRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<WorkoutExerciseRequest> violation =
                violations.iterator().next();

        assertEquals(
                "note",
                violation.getPropertyPath().toString());

        assertEquals(
                "メモは100文字以内で入力してください",
                violation.getMessage());
    }
}