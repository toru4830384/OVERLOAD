package overload_api.domain.validation;
import java.lang.annotation.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
/** 必須かつ保存可能な件数に制限するワークアウト一覧の共通制約。 */
@Target({ElementType.FIELD,ElementType.PARAMETER,ElementType.RECORD_COMPONENT,ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy={})
@NotEmpty @Size(max=1000) @ReportAsSingleViolation
public @interface WorkoutSets {
    String message() default "一覧は1〜1000件で指定してください";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
