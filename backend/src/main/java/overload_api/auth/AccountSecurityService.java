package overload_api.auth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import overload_api.mapper.CredentialMapper;
import overload_api.mapper.GoogleAccountMapper;
/** 本人の再確認とパスワード変更を、認証情報の行ロック内で行う。 */
@Service
public class AccountSecurityService {
    private final CredentialMapper credentials;
    private final GoogleAccountMapper google;
    private final PasswordEncoder passwords;
    public AccountSecurityService(CredentialMapper credentials,GoogleAccountMapper google,PasswordEncoder passwords) {
        this.credentials=credentials;this.google=google;this.passwords=passwords;
    }
    public record Status(boolean hasPassword,boolean googleLinked) {}
    @Transactional(readOnly=true)
    public Status status(UserIdentity user) {
        var credential=credentials.findByLoginId(user.loginId());
        return new Status(credential!=null && credential.passwordHash()!=null,google.subjectForUser(user.userId())!=null);
    }
    /** @param user セッションの本人 @param command 検証済みの現在のパスワード */
    @Transactional
    public void confirmPassword(UserIdentity user,PasswordConfirmationCommand command) { check(credentials.lockByUserId(user.userId()),user,command.currentPassword()); }
    private void check(Credential credential,UserIdentity user,String password) {
        if(credential==null || credential.authVersion()!=user.authVersion())
            throw new IllegalArgumentException("認証情報が変更されています。再度ログインしてください");
        if(credential.passwordHash()==null) throw new IllegalArgumentException("このアカウントはGoogleでログインしてください");
        if(password==null || !passwords.matches(password,credential.passwordHash()))
            throw new IllegalArgumentException("現在のパスワードが違います");
    }
    /** 変更と全セッションの失効を同一トランザクションで行い、下書きの境界キーは保持する。 */
    @Transactional
    public void changePassword(UserIdentity user,PasswordChangeCommand command) {
        var credential=credentials.lockByUserId(user.userId());check(credential,user,command.currentPassword());
        if(passwords.matches(command.newPassword(),credential.passwordHash()))
            throw new IllegalArgumentException("現在とは異なる新しいパスワードを入力してください");
        credentials.updatePassword(user.userId(),passwords.encode(command.newPassword()));
        credentials.deleteSessions(user.loginId());
    }
}
