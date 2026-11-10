package overload_api.auth;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import overload_api.mapper.CredentialMapper;
import overload_api.mapper.UserMapper;
import overload_api.model.User;
import overload_api.exception.RequestConflictException;
/** 新規登録と認証を提供する。認証失敗からIDの登録有無を推測させない。 */
@Service
public class AccountService {
    private final CredentialMapper credentials;
    private final UserMapper users;
    private final PasswordEncoder passwords;
    private final String dummyHash;
    /** @param credentials 認証情報Mapper @param users ユーザーMapper @param passwords 適応型ハッシュ */
    public AccountService(CredentialMapper credentials, UserMapper users, PasswordEncoder passwords) {
        this.credentials = credentials; this.users = users; this.passwords = passwords;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }
    /** @param command 共通制約を満たす登録入力 @return 新規ユーザーの識別情報 */
    @Transactional
    public UserIdentity register(RegistrationCommand command) {
        var user = new User();
        user.setName(command.name()); user.setGender(command.gender());
        user.setAge(command.age()); user.setBodyWeightKg(command.bodyWeightKg());
        String hash = passwords.encode(command.password());
        users.insert(user);
        String namespace = UUID.randomUUID().toString();
        try { credentials.insert(new Credential(user.getId(), command.loginId(), hash, namespace)); }
        catch (DuplicateKeyException e) { throw new RequestConflictException("このログインIDは利用できません"); }
        // 既存ユーザーIDを入力として受け取らず、登録と認証情報を必ず同一トランザクションで作る。
        return new UserIdentity(user.getId(), command.loginId(), namespace);
    }
    /** @param loginId ログインID @param password 入力パスワード @return 認証成功した本人 */
    public UserIdentity authenticate(String loginId, String password) {
        Credential credential = credentials.findByLoginId(loginId);
        boolean valid = passwords.matches(password, credential == null || credential.passwordHash() == null ? dummyHash : credential.passwordHash());
        if (credential == null || credential.passwordHash() == null || !valid) throw new BadCredentialsException("ログインIDまたはパスワードが違います");
        return new UserIdentity(credential.userId(), credential.loginId(), credential.draftNamespace(), credential.authVersion());
    }
}
