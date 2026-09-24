package overload_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

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
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseStatusException handleResourceNotFound(
            ResourceNotFoundException e) {

        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                e
        );
    }

    /**
     * データベース処理で予期しないSQLエラーが発生した場合の例外を処理する。
     *
     * @param e SQL例外
     * @return 500エラー情報
     */
    @ExceptionHandler(org.springframework.jdbc.UncategorizedSQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseStatusException handleUncategorizedSQLException(
            org.springframework.jdbc.UncategorizedSQLException e) {

        return new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "データベース処理でエラーが発生しました",
                e
        );
    }
}