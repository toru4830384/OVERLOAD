package overload_api.mapper;
import org.apache.ibatis.annotations.*;
import overload_api.auth.Credential;
/** プロフィールと分離した認証情報の保存と取得を担う。 */
@Mapper
public interface CredentialMapper {
    /** @param loginId 検証済みログインID @return 認証情報。未登録はnull */
    @Select("SELECT user_id, login_id, password_hash, draft_namespace, auth_version FROM user_credentials WHERE login_id = #{loginId}")
    @ConstructorArgs({ @Arg(column="user_id",javaType=Long.class), @Arg(column="login_id",javaType=String.class),
        @Arg(column="password_hash",javaType=String.class), @Arg(column="draft_namespace",javaType=String.class),
        @Arg(column="auth_version",javaType=long.class) })
    Credential findByLoginId(String loginId);
    /** @param userId 本人ID @return 認証情報。変更操作と認証の競合を直列化する */
    @Select("SELECT user_id, login_id, password_hash, draft_namespace, auth_version FROM user_credentials WHERE user_id=#{userId} FOR UPDATE")
    @ConstructorArgs({ @Arg(column="user_id",javaType=Long.class), @Arg(column="login_id",javaType=String.class),
        @Arg(column="password_hash",javaType=String.class), @Arg(column="draft_namespace",javaType=String.class),
        @Arg(column="auth_version",javaType=long.class) })
    Credential lockByUserId(Long userId);
    /** @param userId 本人ID @return 現在の認証世代。削除済みはnull */
    @Select("SELECT auth_version FROM user_credentials WHERE user_id=#{userId}")
    Long currentVersion(Long userId);
    /** @param userId 本人ID @param hash 新しいハッシュ @return 更新件数 */
    @Update("UPDATE user_credentials SET password_hash=#{hash}, auth_version=auth_version+1 WHERE user_id=#{userId}")
    int updatePassword(@Param("userId") Long userId, @Param("hash") String hash);
    /** @param loginId 本人のID。古いセッションの保存競合は認証世代でも拒否する */
    @Delete("DELETE FROM SPRING_SESSION WHERE PRINCIPAL_NAME=#{loginId}")
    void deleteSessions(String loginId);
    /** @param credential 新規ユーザーのハッシュ済み認証情報 */
    @Insert("INSERT INTO user_credentials(user_id, login_id, password_hash, draft_namespace) VALUES(#{userId}, #{loginId}, #{passwordHash}, #{draftNamespace})")
    void insert(Credential credential);
    /** @param userId 本人ID。古いセッションと連携途中の証明を無効にする。 */
    @Update("UPDATE user_credentials SET auth_version=auth_version+1 WHERE user_id=#{userId}")
    int incrementAuthVersion(Long userId);
}
