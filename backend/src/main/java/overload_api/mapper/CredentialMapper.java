package overload_api.mapper;
import org.apache.ibatis.annotations.*;
import overload_api.auth.Credential;
/** プロフィールと分離した認証情報の保存と取得を担う。 */
@Mapper
public interface CredentialMapper {
    /** @param loginId 検証済みログインID @return 認証情報。未登録はnull */
    @Select("SELECT user_id, login_id, password_hash, draft_namespace FROM user_credentials WHERE login_id = #{loginId}")
    @ConstructorArgs({ @Arg(column="user_id",javaType=Long.class), @Arg(column="login_id",javaType=String.class),
        @Arg(column="password_hash",javaType=String.class), @Arg(column="draft_namespace",javaType=String.class) })
    Credential findByLoginId(String loginId);
    /** @param credential 新規ユーザーのハッシュ済み認証情報 */
    @Insert("INSERT INTO user_credentials(user_id, login_id, password_hash, draft_namespace) VALUES(#{userId}, #{loginId}, #{passwordHash}, #{draftNamespace})")
    void insert(Credential credential);
}
