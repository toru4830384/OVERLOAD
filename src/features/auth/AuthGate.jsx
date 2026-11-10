import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { ApiError, createAccountApi, getAuthenticatedUser, logoutAccount } from "../../api/client.js";
import Overload from "../../App.jsx";
import AuthScreen from "./AuthScreen.jsx";

/** 認証の確認が終わるまで個人データや下書きのフックをマウントしない。 */
export default function AuthGate() {
  const [state, setState] = useState({ status: "loading", user: null, message: "" });
  const generation = useRef(0);
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    const observed = ++generation.current;
    getAuthenticatedUser(controller.signal).then(user => {
      if (!controller.signal.aborted && generation.current === observed) setState({ status: "ready", user, message: "" });
    }).catch(error => {
      if (controller.signal.aborted || generation.current !== observed) return;
      setState({ status: error instanceof ApiError && error.status === 401 ? "anonymous" : "error", user: null,
        message: error instanceof ApiError && error.status === 401 ? "" : error.message });
    });
    return () => controller.abort();
  }, [retry]);
  const authenticated = useCallback(user => { generation.current++; setState({ status: "ready", user, message: "" }); }, []);
  const lost = useCallback(accountKey => {
    // 古いAPIインスタンスの遅い401で、次にログインした本人をログアウトさせない。
    setState(previous => previous.user?.accountKey === accountKey
      ? { status: "anonymous", user: null, message: "ログイン状態が変わりました。下書きは端末に保持しています。同じアカウントでログインしてください。" } : previous);
  }, []);
  const api = useMemo(() => state.user ? createAccountApi(state.user.accountKey, () => lost(state.user.accountKey)) : null, [state.user, lost]);
  async function logout() {
    await logoutAccount(state.user.accountKey);
    generation.current++;
    setState({ status: "anonymous", user: null, message: "ログアウトしました。途中の下書きはこの端末に保持しています。" });
  }
  function passwordChanged() {
    generation.current++;
    setState({ status: "anonymous", user: null, message: "パスワードを変更しました。新しいパスワードでログインしてください。途中の下書きは保持しています。" });
  }
  if (state.status === "loading") return <main className="auth-page" role="status">ログイン状態を確認中…</main>;
  if (state.status === "error") return <main className="auth-page"><p role="alert">{state.message}</p><button onClick={() => { setState({ status: "loading", user: null, message: "" }); setRetry(n => n + 1); }}>再試行</button></main>;
  if (!state.user) return <AuthScreen onAuthenticated={authenticated} notice={state.message} />;
  return <Overload key={state.user.accountKey} user={state.user} api={api} onLogout={logout} onPasswordChanged={passwordChanged} />;
}
