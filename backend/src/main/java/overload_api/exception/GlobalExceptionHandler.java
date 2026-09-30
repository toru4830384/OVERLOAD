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
}