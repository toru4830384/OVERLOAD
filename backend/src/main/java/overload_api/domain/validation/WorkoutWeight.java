package overload_api.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/** 重量のDB保存条件（DECIMAL(6,2)）を一箇所で定義する。 HTTP DTOと登録Commandが共有する。 */
@Documented
@Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@NotNull
@DecimalMin(value = "0.01", message = "重量は0.01kg以上の値を入力してください")
@Digits(integer = 4, fraction = 2, message = "重量は9999.99kg以下、小数第2位まで入力してください")
public @interface WorkoutWeight {
    String message() default "入力値が不正です";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
