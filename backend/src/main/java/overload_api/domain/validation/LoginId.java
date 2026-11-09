package overload_api.domain.validation;
import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
/** HTTPとServiceで共有する認証入力の制約。 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@NotNull @Pattern(regexp = "[a-z0-9_]{3,32}", message = "ログインIDは小文字英数字と_の3〜32文字です")
public @interface LoginId {
    String message() default "ログインIDは小文字英数字と_の3〜32文字です";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
