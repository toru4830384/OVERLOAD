package overload_api.exception;

/**
 * 指定されたリソースが存在しない場合に発生する例外。
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * ResourceNotFoundExceptionを生成する。
     *
     * @param message 例外メッセージ
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}