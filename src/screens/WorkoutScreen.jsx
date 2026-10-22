import { REST_SECONDS_MIN, REST_SECONDS_MAX } from "../features/workouts/restTimer.js";
import { useId } from "react";
import { Dumbbell, Plus, Timer } from "lucide-react";
import { COLORS } from "../theme.js";
import WorkoutExerciseCard from "../components/WorkoutExerciseCard.jsx";

/** ワークアウト全体の配置と休憩設定。種目別表示は専用カードへ委譲する。 */
export default function WorkoutScreen({
  activeSession,
  startWorkout,
  canStart = true,
  startLabel,
  gender,
  restInput,
  setRestInput,
  restError = "",
  exerciseMap = {},
  prMap = {},
  setDetailExercise,
  removeEntry,
  updateDraft,
  bodyWeightKg,
  removeSet,
  entryErrors = {},
  addSet,
  setPickerOpen,
  setPickerCategory,
  setPickerEquipment,
  finishWorkout,
  isSaving = false,
  isLocked = false,
  canAddExercise = true,
  canFinish = true,
  saveError = "",
  pendingSave = false,
  saveRecovery = pendingSave ? "retry" : "edit",
}) {
  const restId = useId();
  if (!activeSession) {
    return (
      <div style={{ textAlign: "center", padding: "44px 20px", border: `1.5px dashed ${COLORS.line}`, borderRadius: 16, margin: "8px 0" }}>
        <div style={{ display: "inline-block", fontFamily: "'Oswald', sans-serif", fontSize: 10, letterSpacing: 2, color: COLORS.amber, background: "rgba(240,168,46,0.1)", border: `1px solid ${COLORS.amber}`, borderRadius: 4, padding: "3px 10px", marginBottom: 16 }}>READY</div>
        <div><Dumbbell size={40} color={COLORS.steelDim} style={{ marginBottom: 14, filter: "drop-shadow(0 0 10px rgba(240,168,46,0.35))" }} /></div>
        <div style={{ color: COLORS.steel, fontSize: 14, marginBottom: 20 }}>今日のワークアウトを開始しましょう</div>
        <button type="button" className="btn-tactile" onClick={startWorkout} disabled={!canStart} style={{ background: COLORS.red, color: "#fff", border: "none", borderRadius: 10, padding: "14px 28px", fontSize: 15, fontWeight: 600, boxShadow: "0 4px 20px rgba(214,64,43,0.4)" }}>
          {startLabel ?? (gender === "" ? "プロフィールで性別を選択" : "ワークアウト開始")}
        </button>
      </div>
    );
  }

  return (
    <>
      <fieldset disabled={isLocked} style={{ border: 0, margin: 0, padding: 0, minWidth: 0 }}>
        <div style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: "10px 14px", marginBottom: 10 }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8 }}>
            <label htmlFor={restId} style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 12, color: COLORS.steel }}><Timer size={14} /> 休憩設定</label>
            <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <input id={restId} type="number" min={REST_SECONDS_MIN} max={REST_SECONDS_MAX} step="1" value={restInput} placeholder="休憩時間を入力"
                aria-invalid={Boolean(restError)} aria-describedby={restError ? `${restId}-error` : undefined}
                onChange={event => setRestInput(event.target.value)}
                style={{ width: 100, height: 30, background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 6, color: COLORS.chalk, textAlign: "center", fontFamily: "'JetBrains Mono', monospace", fontSize: 13 }} />
              <span style={{ fontSize: 12, color: COLORS.steel }}>秒</span>
            </div>
          </div>
          {restError && <div id={`${restId}-error`} role="alert" style={{ marginTop: 6, fontSize: 11, color: COLORS.red }}>{restError}</div>}
        </div>

        {activeSession.entries.map(entry => {
          const exercise = exerciseMap[entry.exerciseId] || { id: entry.exerciseId, name: entry.exerciseName || `種目 #${entry.exerciseId}（情報未取得）` };
          return <WorkoutExerciseCard key={entry.exerciseId} entry={entry} exercise={exercise} personalRecord={prMap[entry.exerciseId]}
            bodyWeightKg={bodyWeightKg} errors={entryErrors[entry.exerciseId]} setDetailExercise={setDetailExercise}
            removeEntry={removeEntry} updateDraft={updateDraft} removeSet={removeSet} addSet={addSet} />;
        })}

        <button type="button" disabled={!canAddExercise} onClick={() => { setPickerOpen(true); setPickerCategory(null); setPickerEquipment(null); }}
          style={{ width: "100%", background: COLORS.surface, border: `1px dashed ${COLORS.line}`, borderRadius: 10, padding: 12, color: COLORS.steel, fontSize: 13, display: "flex", alignItems: "center", justifyContent: "center", gap: 6, marginBottom: 14 }}>
          <Plus size={14} /> 種目を追加
        </button>
      </fieldset>

      {saveError && <div role="alert" style={{ color: COLORS.amber, marginBottom: 12 }}>
        保存・確認を完了できませんでした。{saveError}
        {saveRecovery === "retry" && " 同じ内容で再試行できます。"}
        {saveRecovery === "edit" && " 入力を修正して保存してください。"}
        {saveRecovery === "conflict" && " 保存済みの履歴を確認し、下書きを破棄する場合は内容を確認してください。"}
      </div>}
      <button type="button" disabled={isSaving || !canFinish} className="btn-tactile" onClick={finishWorkout} style={{ width: "100%", background: COLORS.green, border: "none", borderRadius: 10, padding: 13, color: "#fff", fontSize: 14, fontWeight: 600 }}>
        {isSaving ? "保存中..." : saveRecovery === "conflict" ? "保存状態を確認してください" : saveError ? saveRecovery === "retry" ? "保存を再試行" : "修正して保存" : "ワークアウトを終了して保存"}
      </button>
    </>
  );
}
