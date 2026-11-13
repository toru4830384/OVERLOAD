package overload_api.auth;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import overload_api.mapper.*;
import overload_api.model.User;
import overload_api.exception.RequestConflictException;
/** 検証済みGoogleの識別子を本人へ関連付け、既存の記録を別ユーザーへ移さない。 */
@Service
public class GoogleAccountService {
    private final GoogleAccountMapper google;
    private final CredentialMapper credentials;
    private final UserMapper users;
    public GoogleAccountService(GoogleAccountMapper google,CredentialMapper credentials,UserMapper users) {
        this.google=google;this.credentials=credentials;this.users=users;
    }
    public UserIdentity find(String subject) {
        var c=google.findBySubject(subject);return c==null?null:identity(c);
    }
    private UserIdentity identity(Credential c) {return new UserIdentity(c.userId(),c.loginId(),c.draftNamespace(),c.authVersion());}
    @Transactional
    public UserIdentity register(String subject,GoogleRegistrationCommand command) {
        if(find(subject)!=null) throw new RequestConflictException("Google連携は登録済みです。Googleでログインし直してください");
        var user=new User();user.setName(command.name());user.setGender(command.gender());user.setAge(command.age());user.setBodyWeightKg(command.bodyWeightKg());
        users.insert(user);
        var c=new Credential(user.getId(),command.loginId(),null,UUID.randomUUID().toString());
        try {credentials.insert(c);google.insert(user.getId(),subject);}
        catch(DuplicateKeyException e) {throw new RequestConflictException("このログインIDまたはGoogleアカウントは利用できません");}
        return identity(c);
    }
    /** ログイン中の同じ本人だけに連携する。別アカウントの連携や置換は拒否する。 */
    @Transactional
    public void link(UserIdentity user,String subject) {
        var c=credentials.lockByUserId(user.userId());
        if(c==null || c.authVersion()!=user.authVersion()) throw new IllegalArgumentException("再度ログインして連携してください");
        String old=google.subjectForUser(user.userId());
        if(subject.equals(old)) return;
        if(old!=null) throw new RequestConflictException("すでに別のGoogleアカウントと連携しています");
        try {google.insert(user.userId(),subject);}
        catch(DuplicateKeyException e) {throw new RequestConflictException("このGoogleアカウントは別の利用者と連携しています");}
    }
    /** パスワードによるログイン手段を残したまま連携を解除する。競合は認証行で直列化する。 */
    @Transactional
    public void unlink(UserIdentity user) {
        var c=credentials.lockByUserId(user.userId());
        if(c==null || c.authVersion()!=user.authVersion())
            throw new IllegalArgumentException("認証情報が変更されています。再度ログインしてください");
        if(c.passwordHash()==null)
            throw new RequestConflictException("パスワード未設定のため、Google連携を解除できません");
        google.deleteForUser(user.userId());
        credentials.incrementAuthVersion(user.userId());
        credentials.deleteSessions(user.loginId());
    }
}
