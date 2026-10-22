import { useId } from "react";
import { Plus, Trash2, X } from "lucide-react";
import { COLORS } from "../theme.js";
import { BAR_WEIGHT } from "../data/exercises.js";
import { MovementIcon } from "./MovementIcon.jsx";
import { estimateSetCalories } from "../features/workouts/calories.js";
import { WORKOUT_REPETITIONS_MIN, WORKOUT_REPETITIONS_MAX } from "../features/workouts/validation.js";

const hiddenLabel = { position: "absolute", width: 1, height: 1, padding: 0, margin: -1, overflow: "hidden", clip: "rect(0, 0, 0, 0)", whiteSpace: "nowrap", border: 0 };
const draftInput = { width: "100%", boxSizing: "border-box", background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 10, color: COLORS.chalk, fontSize: 14 };
const iconButton = { background: "none", border: "none", color: COLORS.steelDim };

/** 種目単位の表示と入力。操作対象には並び順が変わらない種目IDを渡す。 */
export default function WorkoutExerciseCard({ entry, exercise, personalRecord, bodyWeightKg, errors = {}, setDetailExercise, removeEntry, updateDraft, removeSet, addSet }) {
  const id = useId();
  const name = exercise.name;
  const exerciseId = entry.exerciseId;
  return (
    <section aria-label={name} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, padding: 14, marginBottom: 12 }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <button type="button" aria-label={`${name}の詳細`} onClick={() => setDetailExercise(exercise)} style={{ background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 4, display: "flex" }}>
            <MovementIcon pattern={exercise.pattern} size={26} />
          </button>
          <div>
            <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 17, fontWeight: 500 }}>{name}</div>
            <div style={{ fontSize: 11, color: COLORS.steelDim }}>{exercise.category}{personalRecord ? ` · 自己ベスト ${personalRecord}kg` : ""}</div>
          </div>
        </div>
        <button type="button" aria-label={`${name}を削除`} onClick={() => removeEntry(exerciseId)} style={iconButton}><X size={16} /></button>
      </div>

      <div style={{ display: "flex", alignItems: "flex-start", gap: 6, marginTop: 10 }}>
        <label htmlFor={`${id}-note`} style={hiddenLabel}>{name}のメモ</label>
        <span aria-hidden="true" style={{ fontSize: 13, flexShrink: 0, marginTop: 6 }}>📝</span>
        <textarea id={`${id}-note`} rows={4} placeholder="今日のメモ(履歴に残ります)" maxLength={100} value={entry.note || ""}
          onChange={event => updateDraft(exerciseId, "note", event.target.value)}
          style={{ flex: 1, minWidth: 0, background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 6, padding: "6px 8px", color: COLORS.amber, fontSize: 12, fontFamily: "inherit", resize: "none", lineHeight: 1.5 }} />
      </div>

      {entry.sets.length > 0 && (
        <div style={{ marginTop: 10, display: "flex", flexDirection: "column", gap: 6 }}>
          {entry.sets.map((set, index) => {
            const isPr = set.weight === personalRecord;
            return (
              <div key={set.id} style={{ display: "flex", alignItems: "center", justifyContent: "space-between", background: isPr ? "rgba(240,168,46,0.08)" : COLORS.surfaceAlt, border: isPr ? `1px solid ${COLORS.amber}` : "1px solid transparent", borderRadius: 8, padding: "6px 10px" }}>
                <span style={{ fontSize: 11, color: COLORS.steelDim, width: 20 }}>#{index + 1}</span>
                <span style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 13, letterSpacing: 0.8, flex: 1, color: COLORS.amber, textShadow: "0 0 8px rgba(240,168,46,0.45)" }}>{set.weight}kg × {set.reps}回</span>
                <span style={{ fontSize: 11, color: COLORS.steelDim, marginRight: 8 }}>🔥{estimateSetCalories(set.reps, bodyWeightKg).toFixed(1)}kcal</span>
                {isPr && <span style={{ display: "flex", alignItems: "center", gap: 3, background: COLORS.amber, color: COLORS.bg, fontFamily: "'Oswald', sans-serif", fontSize: 10, fontWeight: 700, letterSpacing: 0.5, padding: "2px 7px", borderRadius: 5, marginRight: 8, boxShadow: "0 0 10px rgba(240,168,46,0.6)" }}>⚡PR</span>}
                <button type="button" aria-label={`${name}の${index + 1}セット目を削除`} onClick={() => removeSet(exerciseId, set.id)} style={iconButton}><Trash2 size={13} /></button>
              </div>
            );
          })}
        </div>
      )}

      <div style={{ display: "flex", gap: 8, marginTop: 10, alignItems: "flex-start" }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <label htmlFor={`${id}-weight`} style={hiddenLabel}>{name}の重量(kg)</label>
          <input id={`${id}-weight`} type="number" inputMode="decimal" min="0.01" max="9999.99" step="0.01" placeholder="重量(kg)" value={entry.draftWeight ?? ""}
            aria-invalid={Boolean(errors.weight)} aria-describedby={errors.weight ? `${id}-weight-error` : undefined}
            onChange={event => updateDraft(exerciseId, "draftWeight", event.target.value)} style={draftInput} />
          {errors.weight && <div id={`${id}-weight-error`} role="alert" style={{ marginTop: 4, fontSize: 11, color: COLORS.red }}>{errors.weight}</div>}
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <label htmlFor={`${id}-reps`} style={hiddenLabel}>{name}の回数</label>
          <input id={`${id}-reps`} type="number" inputMode="numeric" min={WORKOUT_REPETITIONS_MIN} max={WORKOUT_REPETITIONS_MAX} step={1} placeholder="回数" value={entry.draftReps ?? ""}
            aria-invalid={Boolean(errors.reps)} aria-describedby={errors.reps ? `${id}-reps-error` : undefined}
            onChange={event => updateDraft(exerciseId, "draftReps", event.target.value)} style={draftInput} />
          {errors.reps && <div id={`${id}-reps-error`} role="alert" style={{ marginTop: 4, fontSize: 11, color: COLORS.red }}>{errors.reps}</div>}
        </div>
        <button type="button" aria-label={`${name}のセットを追加`} className="btn-tactile" onClick={() => addSet(exerciseId)} style={{ background: COLORS.blue, border: "none", borderRadius: 8, padding: "0 16px", color: "#fff", height: 40 }}><Plus size={16} /></button>
      </div>
      {exercise.barbell && <div style={{ marginTop: 10, fontSize: 11, color: COLORS.steelDim }}>🏋 シャフト: {BAR_WEIGHT}kg</div>}
    </section>
  );
}
