package overload_api.controller.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * WorkoutSetRequestのバリデーションをテストするクラス。
 */
class WorkoutSetRequestTest {

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
     * weightKgがnullの場合にバリデーションエラーになることを確認する。
     */
    @Test
    void weightKgがnullの場合_バリデーションエラーになる() {
        WorkoutSetRequest request =
                new WorkoutSetRequest();

        request.setWeightKg(null);

        Set<ConstraintViolation<WorkoutSetRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
    }
}