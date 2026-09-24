package overload_api.controller.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

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
     * setsがnullの場合にバリデーションエラーになることを確認する。
     */
    @Test
    void setsがnullの場合_バリデーションエラーになる() {
        WorkoutExerciseRequest request =
                new WorkoutExerciseRequest();

        request.setExerciseId(1L);
        request.setSets(null);

        Set<ConstraintViolation<WorkoutExerciseRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
    }

    /**
     * setsが空の場合にバリデーションエラーになることを確認する。
     */
    @Test
    void setsが空の場合_バリデーションエラーになる() {
        WorkoutExerciseRequest request =
                new WorkoutExerciseRequest();

        request.setExerciseId(1L);
        request.setSets(java.util.List.of());

        Set<ConstraintViolation<WorkoutExerciseRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
    }
}