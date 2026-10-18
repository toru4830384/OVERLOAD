package overload_api.service.command;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import overload_api.domain.validation.BodyWeight;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import overload_api.domain.validation.WorkoutNote;
import overload_api.domain.validation.WorkoutRepetitions;
import overload_api.domain.validation.WorkoutRequestIdentifier;
import overload_api.domain.validation.WorkoutSetNumber;
import overload_api.domain.validation.WorkoutWeight;
import overload_api.service.dto.WorkoutRequest;
import overload_api.time.WorkoutTime;

/**
 * 検証済みのワークアウト登録Command。入力DTOから独立した変更不能なスナップショット。
 * HTTPの形式・パースはControllerが扱い、保存条件は共通制約に集約する。
 */
public record WorkoutCommand(
        @NotNull Long userId,
        @NotNull OffsetDateTime startedAt,
        @NotNull OffsetDateTime finishedAt,
        @BodyWeight BigDecimal bodyWeightKg,
        @WorkoutRequestIdentifier String clientRequestId,
        @NotEmpty List<@NotNull @Valid Exercise> exercises) {

    /** 保存条件と日時の順序を検証する。 */
    public WorkoutCommand(Long userId, OffsetDateTime startedAt, OffsetDateTime finishedAt,
            BigDecimal bodyWeightKg, String clientRequestId, List<Exercise> exercises) {
        this.userId = userId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.bodyWeightKg = bodyWeightKg;
        this.clientRequestId = clientRequestId;
        this.exercises = immutable(exercises);
        validate(this);
        if (finishedAt.toInstant().isBefore(startedAt.toInstant())
                || finishedAt.toInstant().isAfter(WorkoutTime.now().toInstant())) {
            throw new IllegalArgumentException("Workout timestamps must be ordered and not in the future");
        }
    }

    /** 入力DTOを検証済みCommandへ変換する。null入力もDBアクセス前に拒否する。 */
    public static WorkoutCommand from(WorkoutRequest input) {
        require(input, "Workout request is required");
        require(input.getExercises(), "Workout exercises are required");
        var exercises = input.getExercises().stream().map(exercise -> {
            require(exercise, "Workout exercise is required");
            require(exercise.getSets(), "Workout sets are required");
            var sets = exercise.getSets().stream().map(set -> {
                require(set, "Workout set is required");
                return new Set(set.getSetNumber(), set.getWeightKg(), set.getReps());
            }).toList();
            return new Exercise(exercise.getExerciseId(), exercise.getNote(), sets);
        }).toList();
        return new WorkoutCommand(input.getUserId(), input.getStartedAt(), input.getFinishedAt(),
                input.getBodyWeightKg(), input.getClientRequestId(), exercises);
    }

    /** 種目・メモ・セットの変更不能な値。 */
    public record Exercise(@NotNull Long exerciseId, @WorkoutNote String note,
            @NotEmpty List<@NotNull @Valid Set> sets) {
        /** 種目情報をコピーし、メモ長・セット一覧を検証する。 */
        public Exercise(Long exerciseId, String note, List<Set> sets) {
            this.exerciseId = exerciseId;
            this.note = note;
            this.sets = immutable(sets);
            validate(this);
        }
    }

    /** セット番号・重量・回数の変更不能な値。 */
    public record Set(@WorkoutSetNumber Integer setNumber, @WorkoutWeight BigDecimal weightKg,
            @WorkoutRepetitions Integer reps) {
        /** 共通の保存条件を満たすセットだけを生成する。 */
        public Set(Integer setNumber, BigDecimal weightKg, Integer reps) {
            this.setNumber = setNumber;
            this.weightKg = weightKg;
            this.reps = reps;
            validate(this);
        }
    }

    /** null要素もBean Validationで拒否できるよう、検証前のコピーでは例外を出さない。 */
    private static <T> List<T> immutable(List<T> input) {
        return input == null ? null : java.util.Collections.unmodifiableList(new java.util.ArrayList<>(input));
    }

    private static void validate(Object value) {
        var violations = ValidationHolder.VALIDATOR.validate(value);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted()
                    .collect(java.util.stream.Collectors.joining("; ")));
        }
    }

    private static void require(Object value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
    }
    private static class ValidationHolder {
        // 登録のたびにFactoryを生成せず、Command間でValidatorを共有する。
        private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
