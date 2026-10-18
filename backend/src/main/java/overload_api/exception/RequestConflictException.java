package overload_api.exception;

/** 同じ保存識別子が異なる入力に再利用された場合の例外。 */
public class RequestConflictException extends RuntimeException {
    /** @param message 競合の説明 */
    public RequestConflictException(String message) { super(message); }
}
