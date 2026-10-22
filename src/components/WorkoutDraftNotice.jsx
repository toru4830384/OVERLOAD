import { COLORS } from "../theme.js";

/** 保存・復元失敗を隠さず、入力を保持したまま再試行できるようにする。 */
export default function WorkoutDraftNotice({ draft }) {
  if (!draft.error) return null;
  return <div role="alert" style={{ padding: 12, color: COLORS.amber }}>
    {draft.error}
    {draft.errorKind === "conflict" ? <button type="button" onClick={() => {
      if (window.confirm("このタブの未保存の入力を破棄し、別タブの最新の下書きを読み込みます。")) draft.reload();
    }}>最新の下書きを読み込む</button> : <button type="button" onClick={draft.retry}>{draft.errorKind === "clear" ? "下書きの削除を再試行" : "途中保存を再試行"}</button>}
    {draft.errorKind === "load" && <button type="button" onClick={() => {
      if (window.confirm("読み込めない下書きを削除します。元に戻せません。")) draft.discard();
    }}>読み込めない下書きを破棄</button>}
  </div>;
}
