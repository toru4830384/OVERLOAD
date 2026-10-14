package overload_api.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 回数の不変条件を定義する。 HTTP DTOと登録Commandが共有する。 */
@Documented
@Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@NotNull(message = "回数は必須です")
@Min(value = 1, message = "回数は0より大きい値を入力してください")
public @interface WorkoutRepetitions {
    String message() default "入力値が不正です";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
