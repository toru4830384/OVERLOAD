import { COLORS } from "../../theme.js";
import { CATEGORIES, EQUIPMENT_ORDER, EQUIPMENT_LABELS } from "../../data/exercises.js";
import { ChevronLeft } from "lucide-react";
import { MovementIcon } from "../MovementIcon.jsx";

/** 種目選択モーダル。部位・器具・種目の選択を表示する。 */
export default function ExercisePicker({
  setPickerOpen,
  setPickerCategory,
  setPickerEquipment,
  pickerCategory,
  exercises,
  pickerEquipment,
  setDetailExercise,
  addExerciseToSession,
}) {

  return (<div
          style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", display: "flex", alignItems: "flex-end", zIndex: 10 }}
          onClick={() => { setPickerOpen(false); setPickerCategory(null); setPickerEquipment(null); }}
        >
          <div onClick={(e) => e.stopPropagation()} style={{ background: COLORS.surface, borderRadius: "16px 16px 0 0", padding: 16, width: "100%", maxHeight: "75%", overflowY: "auto" }}>
            {!pickerCategory ? (
              <>
                <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 16, marginBottom: 12 }}>部位を選択</div>
                {CATEGORIES.map((cat) => {
                  const count = exercises.filter((e) => e.category === cat).length;
                  if (count === 0) return null;
                  return (
                    <button type="button"
                      key={cat} onClick={() => setPickerCategory(cat)}
                      style={{ width: "100%", textAlign: "left", background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "14px 14px", marginBottom: 6, color: COLORS.chalk, fontSize: 15, fontWeight: 600, display: "flex", justifyContent: "space-between", alignItems: "center" }}
                    >
                      <span>{cat}</span>
                      <span style={{ fontSize: 11, color: COLORS.steelDim, fontWeight: 400 }}>{count}種目</span>
                    </button>
                  );
                })}
              </>
            ) : !pickerEquipment ? (
              <>
                <button type="button"
                  onClick={() => setPickerCategory(null)}
                  style={{ background: "none", border: "none", color: COLORS.steel, fontSize: 13, display: "flex", alignItems: "center", gap: 4, padding: 0, marginBottom: 10 }}
                >
                  <ChevronLeft size={15} /> 部位一覧に戻る
                </button>
                <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 16, marginBottom: 12 }}>{pickerCategory} · 器具を選択</div>
                {EQUIPMENT_ORDER.map((eq) => {
                  const count = exercises.filter((e) => e.category === pickerCategory && e.equipment === eq).length;
                  if (count === 0) return null;
                  return (
                    <button type="button"
                      key={eq} onClick={() => setPickerEquipment(eq)}
                      style={{ width: "100%", textAlign: "left", background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "14px 14px", marginBottom: 6, color: COLORS.chalk, fontSize: 15, fontWeight: 600, display: "flex", justifyContent: "space-between", alignItems: "center" }}
                    >
                      <span>{EQUIPMENT_LABELS[eq]}</span>
                      <span style={{ fontSize: 11, color: COLORS.steelDim, fontWeight: 400 }}>{count}種目</span>
                    </button>
                  );
                })}
              </>
            ) : (
              <>
                <button type="button"
                  onClick={() => setPickerEquipment(null)}
                  style={{ background: "none", border: "none", color: COLORS.steel, fontSize: 13, display: "flex", alignItems: "center", gap: 4, padding: 0, marginBottom: 10 }}
                >
                  <ChevronLeft size={15} /> 器具一覧に戻る
                </button>
                <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 16, marginBottom: 12 }}>{pickerCategory} · {EQUIPMENT_LABELS[pickerEquipment]}</div>
                {exercises.filter((ex) => ex.category === pickerCategory && ex.equipment === pickerEquipment).map((ex) => (
                  <div
                    key={ex.id}
                    style={{ display: "flex", alignItems: "center", gap: 8, background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "6px 6px 6px 6px", marginBottom: 6 }}
                  >
                    <button
                      type="button"
                      onClick={() => setDetailExercise(ex)}
                      style={{ background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 6, padding: 3, display: "flex", flexShrink: 0 }}
                    >
                      <MovementIcon pattern={ex.pattern} size={22} />
                    </button>
                    <button
                      type="button"
                      onClick={() => addExerciseToSession(ex.id)}
                      style={{ flex: 1, textAlign: "left", background: "none", border: "none", padding: "4px 4px", color: COLORS.chalk, fontSize: 14, display: "flex", alignItems: "center", justifyContent: "space-between" }}
                    >
                      <span>{ex.name}</span>
                    </button>
                  </div>
                ))}
              </>
            )}
          </div>
        </div>);
}
