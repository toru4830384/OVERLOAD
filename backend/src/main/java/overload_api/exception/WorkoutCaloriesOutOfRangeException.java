package overload_api.exception;

/** 計算済みカロリーを保存できず、DB書き込み前に登録を拒否したことを表す。 */
public class WorkoutCaloriesOutOfRangeException extends IllegalArgumentException {
    /** @param message 入力修正が必要な理由 */
    public WorkoutCaloriesOutOfRangeException(String message) {
        super(message);
    }
}
