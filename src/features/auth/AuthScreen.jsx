import { useState } from "react";
import { credentialsError, registrationError, registrationPayload } from "./validation.js";
import { registerAccount, loginAccount } from "../../api/client.js";
import "./auth.css";

/** 登録とログイン。既存の匿名記録を新規アカウントへ自動的に引き継がない。 */
export default function AuthScreen({ onAuthenticated, notice = "" }) {
  const [register, setRegister] = useState(false);
  const [input, setInput] = useState({ loginId: "", password: "", name: "", gender: "", age: "", bodyWeightKg: "" });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const update = (field, value) => setInput(previous => ({ ...previous, [field]: value }));
  async function submit(event) {
    event.preventDefault();
    if (busy) return;
    const invalid = register ? registrationError(input) : credentialsError(input);
    if (invalid) { setError(invalid); return; }
    setBusy(true); setError("");
    let created = false;
    try {
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
      <h1 id="auth-heading">{register ? "アカウントを作成" : "ログイン"}</h1>
      {notice && <p role="status" className="auth-notice">{notice}</p>}
      <form onSubmit={submit}>
        <fieldset disabled={busy}>
          <label>ログインID<input name="username" autoComplete="username" autoCapitalize="none" spellCheck={false} aria-describedby="login-id-help" required minLength={3} maxLength={32} pattern="[a-z0-9_]{3,32}" value={input.loginId} onChange={e => update("loginId", e.target.value)} /></label>
          <p id="login-id-help" className="auth-help">
            {register ? "好きなIDを決めてください。" : "登録時に決めたIDを入力してください。"}<br />
            例：taro_123（3〜32文字。半角の小文字 a〜z、数字 0〜9、アンダーバー「_」が使えます）
          </p>
          <label>パスワード<input name="password" type="password" autoComplete={register ? "new-password" : "current-password"} aria-describedby="password-help" required minLength={12} maxLength={128} value={input.password} onChange={e => update("password", e.target.value)} /></label>
          <p id="password-help" className="auth-help">
            {register ? "12〜128文字で、ご自身だけが分かるものを決めてください。" : "登録時に決めたパスワードを入力してください（12〜128文字）。"}<br />
            英字・数字でも、文章でも作れます。<br />
            英数字の例：training2026<br />
            文章の例：朝から筋トレを続けるぞ！<br />
            この例をそのままパスワードに使わないでください。
          </p>
          {register && <>
            <label>表示名<input name="displayName" autoComplete="nickname" maxLength={50} value={input.name} onChange={e => update("name", e.target.value)} /></label>
            <label>性別<select required value={input.gender} onChange={e => update("gender", e.target.value)}><option value="">選択してください</option><option value="male">男性</option><option value="female">女性</option></select></label>
            <div className="auth-columns">
              <label>年齢<input required type="number" min="0" max="2147483647" step="1" value={input.age} onChange={e => update("age", e.target.value)} /></label>
              <label>体重（kg）<input required type="number" min="0.01" max="999.99" step="0.01" value={input.bodyWeightKg} onChange={e => update("bodyWeightKg", e.target.value)} /></label>
            </div>
            <p className="auth-help">プロフィールとトレーニング記録は本人だけが利用できます。実施中の下書きはこの端末にも保存されます。</p>
          </>}
          {error && <p role="alert" className="auth-error">{error}</p>}
          <button className="auth-primary" type="submit">{busy ? "確認中…" : register ? "登録して始める" : "ログイン"}</button>
        </fieldset>
      </form>
      <button type="button" disabled={busy} className="auth-switch" onClick={() => { setRegister(value => !value); setError(""); }}>{register ? "アカウントをお持ちの方はログイン" : "初めての方はアカウントを作成"}</button>
      <p className="auth-help">ログインIDとパスワードを安全に保管してください。現在、メールによるパスワード再設定には対応していません。</p>
    </section>
  </main>;
}
