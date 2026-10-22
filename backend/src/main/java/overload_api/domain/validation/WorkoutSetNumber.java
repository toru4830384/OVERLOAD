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

/** セット番号の不変条件を定義する。 HTTP DTOと登録Commandが共有する。 */
@Documented
@Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@NotNull(message = "セット番号は必須です")
@Min(value = 1, message = "セット番号は1以上の値を入力してください")
public @interface WorkoutSetNumber {
    String message() default "入力値が不正です";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
