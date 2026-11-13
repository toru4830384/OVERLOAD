import { useEffect, useState } from "react";
import { credentialsError, registrationError, registrationPayload, googleRegistrationError, googleRegistrationPayload } from "./validation.js";
import { registerAccount, loginAccount, getGoogleConfiguration, googleLogin, registerGoogleAccount } from "../../api/client.js";
import "./auth.css";
import GoogleSignInButton from "./GoogleSignInButton.jsx";
import PasswordField from "./PasswordField.jsx";

/** 登録とログイン。既存の匿名記録を新規アカウントへ自動的に引き継がない。 */
export default function AuthScreen({ onAuthenticated, notice = "" }) {
  const [register, setRegister] = useState(false);
  const [input, setInput] = useState({ loginId: "", password: "", name: "", gender: "", age: "", bodyWeightKg: "" });
  const [google, setGoogle] = useState({ enabled: false });
  const [googleMode, setGoogleMode] = useState(false);
  useEffect(() => {
    let disposed = false;
    getGoogleConfiguration().then(config => {
      if (!disposed) { setGoogle(config); if (config.registrationPending) { setGoogleMode(true); setRegister(true); } }
    }).catch(() => { /* Googleが利用できなくても既存のログインを維持する。 */ });
    return () => { disposed = true; };
  }, []);
  async function authenticateGoogle(credential) {
    setBusy(true); setError("");
    try {
      const result = await googleLogin(credential);
      if (result.status === "authenticated") onAuthenticated(result.identity);
      else { setGoogleMode(true); setRegister(true); setInput(previous => ({ ...previous, password: "" })); }
    } finally { setBusy(false); }
  }
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const update = (field, value) => setInput(previous => ({ ...previous, [field]: value }));
  async function submit(event) {
    event.preventDefault();
    if (busy) return;
    const invalid = googleMode ? googleRegistrationError(input) : register ? registrationError(input) : credentialsError(input);
    if (invalid) { setError(invalid); return; }
    setBusy(true); setError("");
    let created = false;
    try {
      if (googleMode) { onAuthenticated(await registerGoogleAccount(googleRegistrationPayload(input))); return; }
      if (register) { await registerAccount(registrationPayload(input)); created = true; }
      const user = await loginAccount({ loginId: input.loginId, password: input.password });
      setInput(previous => ({ ...previous, password: "" }));
      onAuthenticated(user);
    } catch (cause) {
      if (created) { setRegister(false); setError(`登録は完了しました。ログインを再試行してください。${cause.message}`); }
      else setError(cause.message);
    } finally { setBusy(false); }
  }
  return <main className="auth-page">
    <div className="auth-brand">OVERLOAD</div>
    <p className="auth-tagline">積み重ねたトレーニングを、あなたの記録に。</p>
    <section className="auth-card" aria-labelledby="auth-heading">
      <h1 id="auth-heading">{googleMode ? "Googleでアカウントを作成" : register ? "アカウントを作成" : "ログイン"}</h1>
      {notice && <p role="status" className="auth-notice">{notice}</p>}
      <form onSubmit={submit}>
        <fieldset disabled={busy}>
          <label>ログインID<input name="username" autoComplete="username" autoCapitalize="none" spellCheck={false} aria-describedby="login-id-help" required minLength={3} maxLength={32} pattern="[a-z0-9_]{3,32}" value={input.loginId} onChange={e => update("loginId", e.target.value)} /></label>
          <p id="login-id-help" className="auth-help">
            {register ? "好きなIDを決めてください。" : "登録時に決めたIDを入力してください。"}<br />
            例：taro_123（3〜32文字。半角の小文字 a〜z、数字 0〜9、アンダーバー「_」が使えます）
          </p>
          {!googleMode && <>
          <PasswordField key={register ? "register-password" : "login-password"} label="パスワード" name="password" autoComplete={register ? "new-password" : "current-password"} aria-describedby="password-help" required minLength={12} maxLength={128} value={input.password} onChange={e => update("password", e.target.value)} />
          <p id="password-help" className="auth-help">
            {register ? "12〜128文字で、ご自身だけが分かるものを決めてください。" : "登録時に決めたパスワードを入力してください（12〜128文字）。"}<br />
            英字・数字でも、文章でも作れます。<br />
            英数字の例：training2026<br />
            文章の例：朝から筋トレを続けるぞ！<br />
            この例をそのままパスワードに使わないでください。
          </p>
          </>}
          {googleMode && <p className="auth-help">Googleの本人確認が完了しました。プロフィールを入力すると登録できます。既存アカウントの記録は自動では引き継がれません。既存の記録がある方はログイン後にGoogleを連携してください。</p>}
          {register && <>
            <label>プロフィール名<input name="displayName" autoComplete="nickname" aria-describedby="profile-name-help" maxLength={50} value={input.name} onChange={e => update("name", e.target.value)} /></label>
            <label>性別<select required value={input.gender} onChange={e => update("gender", e.target.value)}><option value="">選択してください</option><option value="male">男性</option><option value="female">女性</option></select></label>
            <div className="auth-columns">
              <label>年齢<input required type="number" min="0" max="2147483647" step="1" value={input.age} onChange={e => update("age", e.target.value)} /></label>
              <label>体重（kg）<input required type="number" min="0.01" max="999.99" step="0.01" value={input.bodyWeightKg} onChange={e => update("bodyWeightKg", e.target.value)} /></label>
            </div>
            <p id="profile-name-help" className="auth-help">プロフィール名、性別、年齢、体重はログイン後いつでも変更可能です。</p>
            <p className="auth-help">プロフィールとトレーニング記録は本人だけが利用できます。実施中の下書きはこの端末にも保存されます。</p>
          </>}
          {error && <p role="alert" className="auth-error">{error}</p>}
          <button className="auth-primary" type="submit">{busy ? "確認中…" : googleMode ? "Googleで登録して始める" : register ? "登録して始める" : "ログイン"}</button>
        </fieldset>
      </form>
      <button type="button" disabled={busy} className="auth-switch" onClick={() => { setRegister(value => !value); setGoogleMode(false); setError(""); }}>{register ? "アカウントをお持ちの方はログイン" : "初めての方はアカウントを作成"}</button>
      {google.enabled && !googleMode && <>
        <GoogleSignInButton clientId={google.clientId} onCredential={authenticateGoogle} />
        <p className="auth-help">すでに記録がある方は、先にログインしてプロフィールからGoogleを連携してください。</p>
      </>}
      <p className="auth-help">{googleMode
        ? "Googleで登録した場合、OVERLOAD用のパスワードは作成しません。次回も「Googleでログイン」からログインしてください。"
        : <>パスワードはログイン後のプロフィールから変更できます。メールによる再設定には対応していません。ログインIDとパスワードを安全に保管してください。{google.enabled && "Google連携もご利用ください。"}</>}</p>
      <p className="auth-help"><a href="/privacy.html">プライバシーについて</a></p>
    </section>
  </main>;
}
