import { Component } from "react";
import { COLORS } from "../theme.js";

/** 想定外の表示エラーでも、白画面ではなく復旧操作を案内する。 */
export default class AppErrorBoundary extends Component {
  state = { failed: false };

  static getDerivedStateFromError() {
    return { failed: true };
  }

  render() {
    if (!this.state.failed) return this.props.children;
    return <main role="alert" style={{ padding: 24, background: COLORS.bg, color: COLORS.chalk, minHeight: "100dvh", fontFamily: "sans-serif" }}>
      <p>画面を表示できませんでした。再読み込みしてください。</p>
      <p>保存済みの下書きは、再読み込み後に復元できます。</p>
      <button type="button" onClick={() => window.location.reload()}>画面を再読み込み</button>
    </main>;
  }
}
