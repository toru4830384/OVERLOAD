package overload_api.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

/** 種目メモの保存条件を定義する。nullは許可する。 HTTP DTOと登録Commandが共有する。 */
@Documented
@Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Size(max = 100, message = "メモは100文字以内で入力してください")
public @interface WorkoutNote {
    String message() default "入力値が不正です";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
