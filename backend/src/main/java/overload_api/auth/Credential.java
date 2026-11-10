package overload_api.auth;
/** 認証専用のDBモデル。APIのレスポンスへ直接返さない。 */
public record Credential(Long userId, String loginId, String passwordHash, String draftNamespace, long authVersion) {
    /** 既存の登録は認証世代0から開始する。 */
    public Credential(Long userId, String loginId, String passwordHash, String draftNamespace) {
        this(userId, loginId, passwordHash, draftNamespace, 0);
    }
    /** 調査ログなどへ認証情報が漏れないよう、ハッシュも出力しない。 */
    @Override public String toString() { return "Credential[redacted]"; }
}
