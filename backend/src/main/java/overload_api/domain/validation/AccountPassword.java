package overload_api.domain.validation;
import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
/** HTTPとServiceで共有する認証入力の制約。 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@NotNull @Size(min = 12, max = 128, message = "パスワードは12〜128文字で入力してください")
public @interface AccountPassword {
    String message() default "パスワードは12〜128文字で入力してください";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
