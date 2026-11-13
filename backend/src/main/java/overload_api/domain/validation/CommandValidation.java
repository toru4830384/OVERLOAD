package overload_api.domain.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

/** 共通制約をCommand生成時に検証する。検証後のCommandだけをServiceで使用する。 */
public final class CommandValidation {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private CommandValidation() { }

    /** @param command 生成中のCommand。違反はDBへ進む前に拒否する。 */
    public static void validate(Object command) {
        var violations = VALIDATOR.validate(command);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted()
                    .collect(java.util.stream.Collectors.joining("; ")));
        }
    }
}
