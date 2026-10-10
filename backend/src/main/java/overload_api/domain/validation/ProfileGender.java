package overload_api.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** 性別の保存値。male / femaleの選択を必須にする。 API DTOとプロフィールCommandが共有する。 */
@Documented
@Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@NotNull(message = "性別は必須です")
@Pattern(regexp = "male|female", message = "性別は男性または女性を選択してください")
public @interface ProfileGender {
    String message() default "入力値が不正です";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
