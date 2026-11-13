import { useEffect, useRef, useState } from "react";
import { beginGoogleLogin } from "../../api/client.js";
let sdkPromise;
let challengePromise;
function loginChallenge() {
  if (!challengePromise) challengePromise = beginGoogleLogin().finally(() => { challengePromise = undefined; });
  return challengePromise;
}
/** Google公式ボタンのライブラリ。失敗時に自動で認証を繰り返さない。 */
function loadSdk() {
  if (window.google?.accounts?.id) return Promise.resolve(window.google.accounts.id);
  if (!sdkPromise) sdkPromise = new Promise((resolve, reject) => {
    const script = document.createElement("script");
    const timeout = setTimeout(() => { script.remove(); reject(new Error("Googleログインを読み込めませんでした。")); }, 10000);
    script.src = "https://accounts.google.com/gsi/client";
    script.async = true;
    script.onload = () => { clearTimeout(timeout); window.google?.accounts?.id ? resolve(window.google.accounts.id) : reject(new Error("Googleログインを読み込めませんでした。")); };
    script.onerror = () => { clearTimeout(timeout); script.remove(); reject(new Error("Googleログインを読み込めませんでした。")); };
    document.head.appendChild(script);
  }).catch(error => { sdkPromise = undefined; throw error; });
  return sdkPromise;
}
/** nonceを受け取った公式ボタンだけを表示し、IDトークンを保存・URL化しない。 */
export default function GoogleSignInButton({ clientId, nonce, onCredential, onFailure }) {
  const target = useRef(null), callback = useRef(onCredential), failed = useRef(onFailure), sending = useRef(false);
  const [error, setError] = useState(""), [busy, setBusy] = useState(false), [attempt, setAttempt] = useState(0);
  useEffect(() => { callback.current = onCredential; failed.current = onFailure; }, [onCredential, onFailure]);
  useEffect(() => {
    let disposed = false;
    const element = target.current;
    Promise.all([loadSdk(), nonce ? Promise.resolve({ nonce }) : loginChallenge()]).then(([google, challenge]) => {
      if (disposed) return;
      google.initialize({ client_id: clientId, nonce: challenge.nonce, auto_select: false,
        callback: async response => {
          if (disposed || sending.current) return;
          sending.current = true; setBusy(true); setError("");
          try { await callback.current(response.credential); }
          catch (cause) { if (!disposed) { setError(cause.message); failed.current?.(cause); } }
          finally { sending.current = false; if (!disposed) setBusy(false); }
        } });
      google.renderButton(element, { type: "standard", theme: "outline", size: "large", text: "signin_with", locale: "ja", width: Math.max(200, Math.min(260, Math.floor(element.clientWidth))) });
    }).catch(cause => { if (!disposed) { setError(cause.message); failed.current?.(cause); } });
    return () => { disposed = true; element?.replaceChildren(); };
  }, [clientId, nonce, attempt]);
  return <div className="security-google">
    <div ref={target} inert={busy || Boolean(error)} aria-busy={busy} />
    {busy && <p role="status">Googleの本人確認中…</p>}
    {error && <><p role="alert" className="auth-error">{error}</p>{!nonce && <button type="button" onClick={() => { setError(""); setAttempt(n => n + 1); }}>Googleログインをやり直す</button>}</>}
  </div>;
}
