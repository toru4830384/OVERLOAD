package overload_api.mapper;
import org.apache.ibatis.annotations.*;
import overload_api.auth.Credential;
/** メールアドレスではなくGoogleが発行する変更不能なsubで本人を識別する。 */
@Mapper
public interface GoogleAccountMapper {
    @Select("SELECT c.user_id,c.login_id,c.password_hash,c.draft_namespace,c.auth_version FROM user_credentials c JOIN google_accounts g ON g.user_id=c.user_id WHERE g.google_subject=#{subject}")
    @ConstructorArgs({@Arg(column="user_id",javaType=Long.class),@Arg(column="login_id",javaType=String.class),
        @Arg(column="password_hash",javaType=String.class),@Arg(column="draft_namespace",javaType=String.class),
        @Arg(column="auth_version",javaType=long.class)})
    Credential findBySubject(String subject);
    @Select("SELECT google_subject FROM google_accounts WHERE user_id=#{userId}")
    String subjectForUser(Long userId);
    @Insert("INSERT INTO google_accounts(user_id,google_subject) VALUES(#{userId},#{subject})")
    void insert(@Param("userId") Long userId,@Param("subject") String subject);
    /** @param userId セッションで確定した本人。記録やプロフィールは削除しない。 */
    @Delete("DELETE FROM google_accounts WHERE user_id=#{userId}")
    int deleteForUser(Long userId);
}
