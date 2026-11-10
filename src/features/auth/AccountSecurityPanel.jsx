import { useEffect, useState } from "react";
import GoogleSignInButton from "./GoogleSignInButton.jsx";
import PasswordField from "./PasswordField.jsx";
import { getGoogleConfiguration, RequestTimeoutError } from "../../api/client.js";
import { passwordChangeError } from "./securityValidation.js";
import "./security.css";
/** プロフィールとは別の明示的な送信操作で、パスワード変更とGoogle連携を行う。 */
export default function AccountSecurityPanel({ api, onPasswordChanged, disabled = false }) {
  const [status, setStatus] = useState(null), [google, setGoogle] = useState(null), [error, setError] = useState("");
  const [input, setInput] = useState({ currentPassword: "", newPassword: "", confirmation: "" });
  const [linkPassword, setLinkPassword] = useState(""), [nonce, setNonce] = useState(null), [busy, setBusy] = useState(false);
  const [message, setMessage] = useState(""), [reload, setReload] = useState(0);
  useEffect(() => {
    let disposed = false;
    api.getSecurityStatus().then(security => { if (!disposed) setStatus(security); })
      .catch(cause => { if (!disposed) setError(cause.message); });
    getGoogleConfiguration().then(config => { if (!disposed) setGoogle(config); })
      .catch(() => { if (!disposed) setGoogle({ enabled: false }); });
    return () => { disposed = true; };
  }, [api, reload]);
  async function changePassword(event) {
    event.preventDefault(); if (busy || disabled) return;
    const invalid = passwordChangeError(input); if (invalid) { setError(invalid); return; }
    setBusy(true); setError("");
    try {
      await api.changePassword({ currentPassword: input.currentPassword, newPassword: input.newPassword });
      setInput({ currentPassword: "", newPassword: "", confirmation: "" });
      onPasswordChanged();
    } catch (cause) {
      setError(cause instanceof RequestTimeoutError
        ? "変更結果を確認できませんでした。自動では再送していません。新しいパスワードでログインできるか確認してください。下書きは保持しています。"
        : cause.message);
    }
    finally { setBusy(false); }
  }
  async function prepareLink(event) {
    event.preventDefault(); if (busy || disabled) return;
    if (!linkPassword) { setError("Google連携には現在のパスワードを入力してください。"); return; }
    setBusy(true); setError(""); setMessage("");
    try { const challenge = await api.beginGoogleLink({ currentPassword: linkPassword }); setLinkPassword(""); setNonce(challenge.nonce); }
    catch (cause) { setError(cause.message); }
    finally { setBusy(false); }
  }
  async function link(credential) {
    await api.linkGoogle({ credential }); setNonce(null);
    setStatus(previous => ({ ...previous, googleLinked: true }));
    setMessage("Google連携が完了しました。次回からGoogleでも同じ記録へログインできます。");
  }
  return <section className="account-security" aria-labelledby="account-security-heading">
    <h2 id="account-security-heading">ログインとセキュリティ</h2>
    {!status && !error && <p role="status">設定を読み込み中…</p>}
    {error && <p role="alert" className="auth-error">{error}</p>}
    {!status && error && <button type="button" onClick={() => { setError(""); setReload(n => n + 1); }}>設定を再読み込み</button>}
    {message && <p role="status">{message}</p>}
    {status?.hasPassword && <form onSubmit={changePassword}>
      <h3>パスワードを変更</h3>
      <fieldset disabled={busy || disabled}>
        <PasswordField label="現在のパスワード" autoComplete="current-password" required minLength={12} maxLength={128} value={input.currentPassword} onChange={e => setInput(p => ({ ...p, currentPassword: e.target.value }))} />
        <PasswordField label="新しいパスワード" autoComplete="new-password" required minLength={12} maxLength={128} value={input.newPassword} onChange={e => setInput(p => ({ ...p, newPassword: e.target.value }))} />
        <PasswordField label="新しいパスワード（確認）" autoComplete="new-password" required minLength={12} maxLength={128} value={input.confirmation} onChange={e => setInput(p => ({ ...p, confirmation: e.target.value }))} />
        <p>12〜128文字。変更後はすべての端末で再ログインが必要です。途中の下書きはこの端末に保持します。</p>
        <button type="submit">{busy ? "確認中…" : "パスワードを変更する"}</button>
      </fieldset>
    </form>}
    {status && !status.hasPassword && <p>Googleで登録したアカウントです。OVERLOAD用のパスワードは設定されていません。</p>}
    {status && <div>
      <h3>Googleログイン</h3>
      {status.googleLinked ? <p>Google連携済みです。Googleで同じ記録にログインできます。</p> : google?.enabled ? <>
        <p>IDやパスワードを忘れた場合に備え、今のアカウントへGoogleを連携できます。</p>
        {!nonce ? <form onSubmit={prepareLink}><fieldset disabled={busy || disabled}>
          <PasswordField label="連携確認用の現在のパスワード" autoComplete="current-password" required minLength={12} maxLength={128} value={linkPassword} onChange={e => setLinkPassword(e.target.value)} />
          <button type="submit">Google連携に進む</button>
        </fieldset></form> : <GoogleSignInButton clientId={google.clientId} nonce={nonce} onCredential={link} onFailure={cause => { setNonce(null); setError(cause.message); }} />}
      </> : <p>Googleログインは準備中です。利用可能になったら、ここから連携できます。</p>}
    </div>}
  </section>;
}
