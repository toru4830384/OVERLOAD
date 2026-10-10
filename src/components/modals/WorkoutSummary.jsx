import { COLORS } from "../../theme.js";

/** ワークアウト完了モーダル。集計済みの結果を表示する。 */
export default function WorkoutSummary({
  setCompletionSummary,
  completionSummary,
  bodyWeightKg,
}) {

  return (<div
          style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.7)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 30, padding: 24 }}
          onClick={() => setCompletionSummary(null)}
        >
          <div onClick={(e) => e.stopPropagation()} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 16, padding: 24, width: "100%", maxWidth: 320, textAlign: "center" }}>
            <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 18, marginBottom: 4 }}>お疲れ様でした!</div>
            <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 18 }}>今日のワークアウト結果</div>

            <div style={{ background: COLORS.surfaceAlt, borderRadius: 12, padding: 18, marginBottom: 14 }}>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 4 }}>推定消費カロリー</div>
              <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 32, fontWeight: 700, color: COLORS.red }}>
                {completionSummary.calories.toFixed(1)}<span style={{ fontSize: 15, marginLeft: 4 }}>kcal</span>
              </div>
            </div>

            <div style={{ display: "flex", gap: 10, marginBottom: 18 }}>
              <div style={{ flex: 1, background: COLORS.surfaceAlt, borderRadius: 10, padding: 12 }}>
                <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 2 }}>総重量</div>
                <div style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 15 }}>{completionSummary.volume.toLocaleString()}kg</div>
              </div>
              <div style={{ flex: 1, background: COLORS.surfaceAlt, borderRadius: 10, padding: 12 }}>
                <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 2 }}>種目数</div>
                <div style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 15 }}>{completionSummary.exerciseCount}種目</div>
              </div>
            </div>

            <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 14 }}>
              ※体重{bodyWeightKg}kgとMETs法による概算値です(挙上中・休憩中の時間を含む)
            </div>

            <button type="button" onClick={() => setCompletionSummary(null)} style={{ width: "100%", background: COLORS.red, border: "none", borderRadius: 10, padding: 12, color: "#fff", fontSize: 14, fontWeight: 600 }}>
              閉じる
            </button>
          </div>
        </div>);
}
