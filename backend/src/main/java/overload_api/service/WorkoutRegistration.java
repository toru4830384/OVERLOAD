package overload_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import overload_api.exception.WorkoutCaloriesOutOfRangeException;
import overload_api.service.command.WorkoutCommand;

/** 登録時の計算結果と再送入力の識別を作成する。 */
final class WorkoutRegistration {
    // workout_sessions.total_caloriesのDECIMAL(12,2)に一致させる。
    private static final BigDecimal MAX_TOTAL_CALORIES = new BigDecimal("9999999999.99");

    private WorkoutRegistration() { }

    /** METs概算値を保存精度へ丸め、計算結果もDBの保存範囲内か検証する。 */
    static BigDecimal calories(WorkoutCommand request) {
        double reps = request.exercises().stream().flatMap(e -> e.sets().stream())
                .mapToDouble(set -> set.reps()).sum();
        double active = reps * 3.0 / 60.0;
        double elapsed = Duration.between(request.startedAt().toInstant(),
                request.finishedAt().toInstant()).toMillis() / 60000.0;
        double rest = Math.max(0, elapsed - active);
        BigDecimal calories = BigDecimal.valueOf((5.0 * active + 2.0 * rest)
                * 3.5 * request.bodyWeightKg().doubleValue() / 200.0)
                .setScale(2, RoundingMode.HALF_UP);
        if (calories.compareTo(MAX_TOTAL_CALORIES) > 0) {
            throw new WorkoutCaloriesOutOfRangeException(
                    "計算された消費カロリーが保存可能な上限（" + MAX_TOTAL_CALORIES.toPlainString()
                            + "kcal）を超えています。回数・開始日時を確認してください。");
        }
        return calories;
    }

    /** 長さ付き入力を連結し、同じ識別子の異なる入力を検出する。 */
    static String fingerprint(WorkoutCommand request) {
        StringBuilder input = new StringBuilder();
        append(input, request.userId());
        append(input, request.startedAt().toInstant());
        append(input, request.finishedAt().toInstant());
        append(input, request.bodyWeightKg().stripTrailingZeros().toPlainString());
        append(input, request.exercises().size());
        for (var exercise : request.exercises()) {
            append(input, exercise.exerciseId());
            append(input, exercise.note());
            append(input, exercise.sets().size());
            for (var set : exercise.sets()) {
                append(input, set.setNumber());
                append(input, set.weightKg().stripTrailingZeros().toPlainString());
                append(input, set.reps());
            }
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(input.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void append(StringBuilder result, Object value) {
        if (value == null) { result.append("-1:"); return; }
        String text = value.toString();
        result.append(text.length()).append(':').append(text);
    }
}
