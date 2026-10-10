import { COLORS } from "../theme.js";
import { Dumbbell, Timer, X, Trash2, Plus } from "lucide-react";
import { MovementIcon } from "../components/MovementIcon.jsx";
import { estimateSetCalories } from "../features/workouts/calories.js";
import { BAR_WEIGHT } from "../data/exercises.js";
import { WORKOUT_REPETITIONS_MIN, WORKOUT_REPETITIONS_MAX } from "../features/workouts/validation.js";

/** ワークアウト画面。入力や開始・完了の操作は親へ通知する。 */
export default function WorkoutScreen({
  activeSession,
  startWorkout,
  canStart = true,
  startLabel,
  gender,
  restSeconds,
  setRestSeconds,
  exerciseMap,
  prMap,
  setDetailExercise,
  removeEntry,
  updateDraft,
  bodyWeightKg,
  removeSet,
  weightError,
  repsError,
  addSet,
  setPickerOpen,
  setPickerCategory,
  setPickerEquipment,
  finishWorkout,
  isSaving = false,
  isLocked = false,
  canAddExercise = true,
  saveError = "",
}) {

  return (<>
            {!activeSession ? (
              <div style={{ textAlign: "center", padding: "44px 20px", border: `1.5px dashed ${COLORS.line}`, borderRadius: 16, margin: "8px 0" }}>
                <div style={{ display: "inline-block", fontFamily: "'Oswald', sans-serif", fontSize: 10, letterSpacing: 2, color: COLORS.amber, background: "rgba(240,168,46,0.1)", border: `1px solid ${COLORS.amber}`, borderRadius: 4, padding: "3px 10px", marginBottom: 16 }}>READY</div>
                <div>
                  <Dumbbell size={40} color={COLORS.steelDim} style={{ marginBottom: 14, filter: `drop-shadow(0 0 10px rgba(240,168,46,0.35))` }} />
                </div>
                <div style={{ color: COLORS.steel, fontSize: 14, marginBottom: 20 }}>今日のワークアウトを開始しましょう</div>
                <button type="button" className="btn-tactile" onClick={startWorkout} disabled={!canStart} style={{ background: COLORS.red, color: "#fff", border: "none", borderRadius: 10, padding: "14px 28px", fontSize: 15, fontWeight: 600, boxShadow: `0 4px 20px rgba(214,64,43,0.4)` }}>
                  {startLabel ?? (gender === "" ? "プロフィールで性別を選択" : "ワークアウト開始")}
                </button>
              </div>
            ) : (
              <>
                <fieldset disabled={isLocked} style={{ border: 0, margin: 0, padding: 0, minWidth: 0 }}>
			    {/* rest timer settings */}
			    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: "10px 14px", marginBottom: 10 }}>

			      <div style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 12, color: COLORS.steel }}>

			        <Timer size={14} /> 休憩設定

			      </div>

			      <div style={{ display: "flex", alignItems: "center", gap: 6 }}>

				    <input
				      type="number"
				      min="1"
				      value={restSeconds === 0 ? "" : restSeconds}
				      placeholder="休憩時間を入力"
				      onChange={(e) => setRestSeconds(Number(e.target.value))}
				      style={{ width: 100, height: 30, background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 6, color: COLORS.chalk, textAlign: "center", fontFamily: "'JetBrains Mono', monospace", fontSize: 13 }}
				    />

			        <span style={{ fontSize: 12, color: COLORS.steel }}>秒</span>

			      </div>

			    </div>

                {activeSession.entries.map((entry, idx) => {
                  const ex = exerciseMap[entry.exerciseId] || { id: entry.exerciseId, name: entry.exerciseName || `種目 #${entry.exerciseId}（情報未取得）` };
                  const pr = prMap[ex.id];
                  return (
                    <div key={entry.exerciseId} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, padding: 14, marginBottom: 12 }}>
                      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                          <button type="button" onClick={() => setDetailExercise(ex)} style={{ background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 4, display: "flex" }}>
                            <MovementIcon pattern={ex.pattern} size={26} />
                          </button>
                          <div>
                            <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 17, fontWeight: 500 }}>{ex.name}</div>
                            <div style={{ fontSize: 11, color: COLORS.steelDim }}>{ex.category}{pr ? ` · 自己ベスト ${pr}kg` : ""}</div>
                          </div>
                        </div>
                        <button type="button" onClick={() => removeEntry(idx)} style={{ background: "none", border: "none", color: COLORS.steelDim }}><X size={16} /></button>
                      </div>

                      <div style={{ display: "flex", alignItems: "flex-start", gap: 6, marginTop: 10 }}>
                        <span style={{ fontSize: 13, flexShrink: 0, marginTop: 6 }}>📝</span>
                        <textarea
                          rows={4} placeholder="今日のメモ(履歴に残ります)" maxLength={100}
                          value={entry.note || ""}
                          onChange={(e) => updateDraft(idx, "note", e.target.value)}
                          style={{ flex: 1, background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 6, padding: "6px 8px", color: COLORS.amber, fontSize: 12, fontFamily: "inherit", resize: "none", lineHeight: 1.5 }}
                        />
                      </div>

                      {entry.sets.length > 0 && (
                        <div style={{ marginTop: 10, display: "flex", flexDirection: "column", gap: 6 }}>
                          {entry.sets.map((s, si) => {
                            const isPr = s.weight === pr;
                            return (
                            <div key={s.id} style={{ display: "flex", alignItems: "center", justifyContent: "space-between", background: isPr ? "rgba(240,168,46,0.08)" : COLORS.surfaceAlt, border: isPr ? `1px solid ${COLORS.amber}` : "1px solid transparent", borderRadius: 8, padding: "6px 10px" }}>
                              <span style={{ fontSize: 11, color: COLORS.steelDim, width: 20 }}>#{si + 1}</span>
                              <span style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 13, letterSpacing: 0.8, flex: 1, color: COLORS.amber, textShadow: `0 0 8px rgba(240,168,46,0.45)` }}>{s.weight}kg × {s.reps}回</span>
                              <span style={{ fontSize: 11, color: COLORS.steelDim, marginRight: 8 }}>🔥{estimateSetCalories(s.reps, bodyWeightKg).toFixed(1)}kcal</span>
                              {isPr && (
                                <span style={{
                                  display: "flex", alignItems: "center", gap: 3, background: COLORS.amber, color: COLORS.bg,
                                  fontFamily: "'Oswald', sans-serif", fontSize: 10, fontWeight: 700, letterSpacing: 0.5,
                                  padding: "2px 7px", borderRadius: 5, marginRight: 8, boxShadow: `0 0 10px rgba(240,168,46,0.6)`,
                                }}>
                                  ⚡PR
                                </span>
                              )}
                              <button type="button" onClick={() => removeSet(idx, s.id)} style={{ background: "none", border: "none", color: COLORS.steelDim }}><Trash2 size={13} /></button>
                            </div>
                          );})}
                        </div>
                      )}

					  <div style={{ display: "flex", gap: 8, marginTop: 10, alignItems: "flex-start" }}>
					  <div style={{ flex: 1 }}>
					    <input
					      type="number"
					      inputMode="decimal"
					      placeholder="重量(kg)"
					      value={entry.draftWeight}
					      onChange={(e) => updateDraft(idx, "draftWeight", e.target.value)}
					      style={{
					        width: "100%",
					        boxSizing: "border-box",
					        background: COLORS.bg,
					        border: `1px solid ${COLORS.line}`,
					        borderRadius: 8,
					        padding: "10px",
					        color: COLORS.chalk,
					        fontSize: 14
					      }}
					    />

					    {weightError && (
					      <div style={{ marginTop: 4, fontSize: 11, color: COLORS.red }}>
					        {weightError}
					      </div>
					    )}
					  </div>
					  <div style={{ flex: 1 }}>
					    <input
					      type="number"
					      inputMode="numeric"
					      min={WORKOUT_REPETITIONS_MIN}
					      max={WORKOUT_REPETITIONS_MAX}
					      step={1}
					      placeholder="回数"
					      value={entry.draftReps}
					      onChange={(e) => updateDraft(idx, "draftReps", e.target.value)}
					      style={{
					        width: "100%",
					        boxSizing: "border-box",
					        background: COLORS.bg,
					        border: `1px solid ${COLORS.line}`,
					        borderRadius: 8,
					        padding: "10px",
					        color: COLORS.chalk,
					        fontSize: 14
					      }}
					    />

					    {repsError && (
					      <div style={{ marginTop: 4, fontSize: 11, color: COLORS.red }}>
					        {repsError}
					      </div>
					    )}
					  </div>
                        <button type="button" className="btn-tactile" onClick={() => addSet(idx)} style={{ background: COLORS.blue, border: "none", borderRadius: 8, padding: "0 16px", color: "#fff", height: 40 }}>
                          <Plus size={16} />
                        </button>
                      </div>

                      {ex.barbell && (
                        <div style={{ marginTop: 10, fontSize: 11, color: COLORS.steelDim }}>
                          🏋 シャフト: {BAR_WEIGHT}kg
                        </div>
                      )}
                    </div>
                  );
                })}

                <button type="button"
                  disabled={!canAddExercise} onClick={() => { setPickerOpen(true); setPickerCategory(null); setPickerEquipment(null); }}
                  style={{ width: "100%", background: COLORS.surface, border: `1px dashed ${COLORS.line}`, borderRadius: 10, padding: "12px", color: COLORS.steel, fontSize: 13, display: "flex", alignItems: "center", justifyContent: "center", gap: 6, marginBottom: 14 }}
                >
                  <Plus size={14} /> 種目を追加
                </button>

                </fieldset>
                {saveError && <div role="alert" style={{ color: COLORS.amber, marginBottom: 12 }}>保存・確認を完了できませんでした。{saveError} 同じ内容で再試行できます。</div>}
                <button type="button" disabled={isSaving} className="btn-tactile" onClick={finishWorkout} style={{ width: "100%", background: COLORS.green, border: "none", borderRadius: 10, padding: "13px", color: "#fff", fontSize: 14, fontWeight: 600 }}>
                  {isSaving ? "保存中..." : saveError ? "保存を再試行" : "ワークアウトを終了して保存"}
                </button>
              </>
            )}
          </>);
}
