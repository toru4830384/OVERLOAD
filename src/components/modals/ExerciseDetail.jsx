import { COLORS } from "../../theme.js";
import { MovementIcon } from "../MovementIcon.jsx";
import { EQUIPMENT_LABELS, PATTERN_LABELS, BAR_WEIGHT } from "../../data/exercises.js";
import { X } from "lucide-react";
import { CATEGORY_MUSCLE } from "../../features/exercises/exerciseDetails.js";

/** 種目詳細モーダル。動作説明と注意点を表示する。 */
export default function ExerciseDetail({
  setDetailExercise,
  detailExercise,
  detailExerciseInfo,
}) {

  return (<div
          style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.65)", display: "flex", alignItems: "flex-end", zIndex: 20 }}
          onClick={() => setDetailExercise(null)}
        >
          <div onClick={(e) => e.stopPropagation()} style={{ background: COLORS.surface, borderRadius: "16px 16px 0 0", padding: 18, width: "100%", maxHeight: "80%", overflowY: "auto" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 14 }}>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <div style={{ background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: 8, display: "flex" }}>
                  <MovementIcon pattern={detailExercise.pattern} size={40} />
                </div>
                <div>
                  <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 19, fontWeight: 600 }}>{detailExercise.name}</div>
                  <div style={{ fontSize: 11, color: COLORS.steelDim, marginTop: 2 }}>
                    {detailExercise.category} · {EQUIPMENT_LABELS[detailExercise.equipment] || ""} · {PATTERN_LABELS[detailExercise.pattern]}
                  </div>
                </div>
              </div>
              <button type="button" onClick={() => setDetailExercise(null)} style={{ background: "none", border: "none", color: COLORS.steelDim }}><X size={18} /></button>
            </div>

            <div style={{ background: COLORS.surfaceAlt, borderRadius: 10, padding: 12, marginBottom: 12 }}>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 4 }}>主に鍛えられる部位</div>
              <div style={{ fontSize: 14, fontWeight: 600 }}>{detailExercise.muscle || CATEGORY_MUSCLE[detailExercise.category] || detailExercise.category}</div>
            </div>

            <div style={{ marginBottom: 12 }}>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 6 }}>動作の説明</div>
              <div style={{ fontSize: 14, lineHeight: 1.7 }}>{detailExerciseInfo.how}</div>
            </div>

            <div style={{ marginBottom: 6 }}>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 6 }}>フォームのポイント</div>
              {detailExerciseInfo.tips.map((tip, i) => (
                <div key={i} style={{ display: "flex", gap: 8, fontSize: 13, lineHeight: 1.6, marginBottom: 6, color: COLORS.chalk }}>
                  <span style={{ color: COLORS.green, flexShrink: 0 }}>✓</span>
                  <span>{tip}</span>
                </div>
              ))}
            </div>

            {detailExercise.barbell && (
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginTop: 10, paddingTop: 10, borderTop: `1px solid ${COLORS.line}` }}>
                🏋 この種目はシャフト({BAR_WEIGHT}kg)を使うバーベル種目です
              </div>
            )}
          </div>
        </div>);
}
