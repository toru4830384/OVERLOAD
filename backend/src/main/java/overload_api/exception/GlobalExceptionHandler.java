package overload_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * APIで発生した例外を共通して処理するハンドラー。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * リソースが存在しない場合の例外を処理する。
     *
     * @param e リソース未存在例外
     * @return 404エラー情報
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(
            ResourceNotFoundException e) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        e.getMessage());

        problemDetail.setTitle("Resource not found");

        return problemDetail;
    }

    /**
     * データベース制約違反が発生した場合の例外を処理する。
     *
     * @param e データベース制約違反例外
     * @return 400エラー情報
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException e) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "入力値が不正です");

        problemDetail.setTitle("Invalid request");

        return problemDetail;
    }

    /**
     * データベース処理で予期しないSQLエラーが発生した場合の例外を処理する。
     *
     * @param e SQL例外
     * @return 500エラー情報
     */
    @ExceptionHandler(
            org.springframework.jdbc.UncategorizedSQLException.class)
    public ProblemDetail handleUncategorizedSQLException(
            org.springframework.jdbc.UncategorizedSQLException e) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "データベース処理でエラーが発生しました");

        problemDetail.setTitle("Database error");

        return problemDetail;
    }
    /** Serviceの入力検証エラーをHTTP 400として返す。
     * @param e 入力検証エラー
     * @return 400エラー情報
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        detail.setTitle("Invalid request");
        return detail;
    }
    /** @param e 保存識別子の競合
     * @return 409エラー情報
     */
    @ExceptionHandler(RequestConflictException.class)
    public ProblemDetail handleRequestConflict(RequestConflictException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
    /**
     * 計算結果がDBの保存可能な桁数を超える場合、入力の修正を要求する。
     * @param e カロリー上限超過
     * @return 422エラー情報
     */
    @ExceptionHandler(WorkoutCaloriesOutOfRangeException.class)
    public ProblemDetail handleCaloriesOutOfRange(WorkoutCaloriesOutOfRangeException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        detail.setProperty("code", "WORKOUT_CALORIES_OUT_OF_RANGE");
        return detail;
    }

}
