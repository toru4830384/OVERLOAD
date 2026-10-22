import { useWorkoutData } from "./features/workouts/useWorkoutData.js";
import { useWorkoutRest } from "./features/workouts/useWorkoutRest.js";
import RestTimer from "./components/RestTimer.jsx";
import { addWorkoutSet, removeWorkoutEntry, removeWorkoutSet, updateWorkoutDraft } from "./features/workouts/sessionActions.js";
import WorkoutDraftNotice from "./components/WorkoutDraftNotice.jsx";
import { workoutStartError } from "./features/workouts/start.js";
import { useState, useMemo, useRef } from "react";
import { useProfile } from "./features/profile/useProfile.js";
import { ApiError, saveWorkout } from "./api/client.js";
import { summarizeCalories } from "./features/workouts/history.js";
import { useWorkoutDraft } from "./features/workouts/useWorkoutDraft.js";
import { getExerciseDetail } from "./features/exercises/exerciseDetails.js";
import { COLORS } from "./theme.js";
import { Dumbbell, History, ListChecks, User } from "lucide-react";
import { fmtDate } from "./utils/date.js";
import WorkoutScreen from "./screens/WorkoutScreen.jsx";
import HistoryScreen from "./screens/HistoryScreen.jsx";
import ExercisesScreen from "./screens/ExercisesScreen.jsx";
import ProfileScreen from "./screens/ProfileScreen.jsx";
import ExercisePicker from "./components/modals/ExercisePicker.jsx";
import ExerciseDetail from "./components/modals/ExerciseDetail.jsx";
import WorkoutSummary from "./components/modals/WorkoutSummary.jsx";

function beep() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.frequency.value = 880;
    gain.gain.setValueAtTime(0.15, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);
    osc.start();
    osc.stop(ctx.currentTime + 0.5);
  } catch { /* 音・振動が使えなくても記録操作は継続する。 */ }
}

function vibrate() {
  try {
    if (navigator.vibrate) navigator.vibrate([200, 100, 200]);
  } catch { /* 音・振動が使えなくても記録操作は継続する。 */ }
}

function notifyRestFinished() {
  beep();
  vibrate();
}

export default function Overload() {
  const profileState = useProfile(1);
  const { name: userName, gender, age, bodyWeightKg } = profileState.profile;
  const setUserName = value => profileState.update("name", value);
  const setGender = value => profileState.update("gender", value);
  const setAge = value => profileState.update("age", value);
  const setBodyWeightKg = value => profileState.update("bodyWeightKg", value);
  const data = useWorkoutData(1);
  const { exercises, sessions, loadHistory, ready: dataReady, error: dataError } = data;
  const [workoutError, setWorkoutError] = useState("");
  const [isSavingWorkout, setIsSavingWorkout] = useState(false);
  const savingWorkout = useRef(false);
  const draft = useWorkoutDraft(1);
  const activeSession = draft.session;
  const setActiveSession = draft.setSession;
  const [saveConflict, setSaveConflict] = useState(false);
  const [tab, setTab] = useState("workout");
  const [pickerOpen, setPickerOpen] = useState(false);
  const [pickerCategory, setPickerCategory] = useState(null);
  const [pickerEquipment, setPickerEquipment] = useState(null);
  const [detailExercise, setDetailExercise] = useState(null);

  const workoutLocked = isSavingWorkout || Boolean(draft.pendingPayload) || saveConflict
    || ["load", "clear", "conflict"].includes(draft.errorKind);
  const rest = useWorkoutRest({ session: activeSession, setSession: setActiveSession,
    locked: workoutLocked, onFinish: notifyRestFinished });
  const [completionSummary, setCompletionSummary] = useState(null);
  const [entryErrors, setEntryErrors] = useState({});
  const [exerciseSearch, setExerciseSearch] = useState("");
  const [filterCategory, setFilterCategory] = useState("all");
  const [filterEquipment, setFilterEquipment] = useState("all");
  const [historyMonth, setHistoryMonth] = useState(() => {
    const now = new Date();
    return { year: now.getFullYear(), month: now.getMonth() };
  });
  const [selectedDay, setSelectedDay] = useState(null);

  const exerciseMap = useMemo(() => {
    const m = {};
    exercises.forEach((e) => (m[e.id] = e));
    return m;
  }, [exercises]);

  const prMap = useMemo(() => {
    const all = [...sessions, ...(activeSession ? [activeSession] : [])];
    const m = {};
    all.forEach((s) => {
      (s.entries || []).forEach((entry) => {
        entry.sets.forEach((set) => {
          if (!m[entry.exerciseId] || set.weight > m[entry.exerciseId]) {
            m[entry.exerciseId] = set.weight;
          }
        });
      });
    });
    return m;
  }, [sessions, activeSession]);



  const detailExerciseInfo = useMemo(() => {
    return detailExercise ? getExerciseDetail(detailExercise) : null;
  }, [detailExercise]);

  // 描画後に保存・競合状態が変わっても、古いイベントで編集しない。
  function editingBlocked() {
    const current = draft.getSnapshot();
    return savingWorkout.current || Boolean(current.pendingPayload) || saveConflict
      || ["load", "clear", "conflict"].includes(current.errorKind);
  }

  function startWorkout() {
    if (editingBlocked() || draft.getSnapshot().session || !dataReady || dataError) return;
    if (workoutStartError(profileState, { dataReady, dataError })) {
      setTab("profile");
      return;
    }
    setWorkoutError("");
    setSaveConflict(false);
    setEntryErrors({});
    void setActiveSession({ id: crypto.randomUUID(), date: new Date().toISOString(),
      bodyWeightKg: Number(bodyWeightKg), entries: [], restSeconds: rest.input, restTimer: null });
  }

  function addExerciseToSession(exId) {
    if (editingBlocked() || !data.catalogReady) return;
    void setActiveSession(previous => {
      if (!previous || previous.entries.some(entry => entry.exerciseId === exId)) return previous;
      return { ...previous, entries: [...previous.entries, { exerciseId: exId,
        exerciseName: exerciseMap[exId]?.name || "", sets: [], draftWeight: "", draftReps: "" }] };
    });
    setPickerOpen(false);
    setPickerCategory(null);
    setPickerEquipment(null);
  }

  function updateDraft(exerciseId, field, value) {
    if (editingBlocked()) return;
    void setActiveSession(previous => updateWorkoutDraft(previous, exerciseId, field, value));
    const errorField = { draftWeight: "weight", draftReps: "reps" }[field];
    if (errorField) setEntryErrors(previous => ({ ...previous,
      [exerciseId]: { ...previous[exerciseId], [errorField]: "" } }));
  }

  function addSet(exerciseId) {
    if (editingBlocked()) return;
    let outcome;
    void setActiveSession(previous => {
      outcome = addWorkoutSet(previous, exerciseId);
      return rest.error ? previous : outcome.session;
    });
    if (!outcome) return;
    setEntryErrors(previous => ({ ...previous, [exerciseId]: outcome.errors }));
    if (outcome.added && !rest.error) rest.start({ ...outcome.nextSet,
      exerciseName: exerciseMap[exerciseId]?.name || outcome.nextSet.exerciseName });
  }

  function removeSet(exerciseId, setId) {
    if (editingBlocked()) return;
    if (rest.nextSet?.exerciseId === exerciseId) void rest.clear();
    void setActiveSession(previous => removeWorkoutSet(previous, exerciseId, setId));
  }

  function removeEntry(exerciseId) {
    if (editingBlocked()) return;
    if (rest.nextSet?.exerciseId === exerciseId) void rest.clear();
    void setActiveSession(previous => removeWorkoutEntry(previous, exerciseId));
    setEntryErrors(previous => {
      const next = { ...previous };
      delete next[exerciseId];
      return next;
    });
  }

  async function finishWorkout() {
    const current = draft.getSnapshot();
    if (!current.session || savingWorkout.current || saveConflict || ["load", "clear", "conflict"].includes(current.errorKind)) return;
    // 下書きの保存待ちがあっても、終了ボタンを押した時刻を固定する。
    const finishedAt = new Date().toISOString();
    savingWorkout.current = true;
    setIsSavingWorkout(true);
    setWorkoutError("");
    let accepted = false;
    try {
      if (!current.pendingPayload && !await rest.clear()) {
        throw new Error(draft.getSnapshot().error || "下書きを保存できませんでした。再試行してください。");
      }
      const payload = await draft.freezeForSave(finishedAt);
      if (payload.exercises.length === 0) {
        await draft.complete();
        return;
      }
      const result = await saveWorkout(payload);
      accepted = true;
      setSaveConflict(false);
      // 保存後はサーバーが確定した計算値を表示し、再読込時と一致させる。
      const history = await loadHistory();
      const savedSession = history.find(session => session.id === result[0]?.sessionId);
      if (!savedSession) throw new Error("保存結果を取得できませんでした。同じ内容で再試行してください。");
      setCompletionSummary({
        calories: savedSession.totalCalories,
        bodyWeightKg: savedSession.bodyWeightKg,
        volume: savedSession.entries.reduce((sum, entry) => sum + entry.sets.reduce((total, set) => total + set.weight * set.reps, 0), 0),
        exerciseCount: savedSession.entries.length,
      });
      await draft.complete();
    } catch (error) {
      // 入力拒否と、POST成功後の確認失敗・通信断を区別する。
      if (!accepted && error instanceof ApiError) {
        if ([400, 404, 422].includes(error.status)) await draft.resumeAfterRejection(error.status);
        setSaveConflict(error.status === 409);
      }
      setWorkoutError(error.message || "保存できませんでした。同じ内容で再試行してください。");
    } finally {
      savingWorkout.current = false;
      setIsSavingWorkout(false);
    }
  }

  if (profileState.loadState === "loading" && !activeSession) {
    return <><WorkoutDraftNotice draft={draft} />
<div role="status" style={{ padding: 24, color: COLORS.steel }}>読み込み中...</div></>;
  }
  if (profileState.loadState === "error" && !activeSession) {
    return <><WorkoutDraftNotice draft={draft} /><div role="alert" style={{ padding: 24, color: COLORS.steel }}>
      プロフィールを読み込めませんでした。{profileState.error}
      <button type="button" onClick={profileState.retryLoad}>再読み込み</button>
    </div></>;
  }

  return (
    <div className="overload-root" style={{ background: COLORS.bg, fontFamily: "Inter, sans-serif", color: COLORS.chalk, display: "flex", flexDirection: "column", width: "100%", maxWidth: 480, margin: "0 auto", position: "relative" }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Oswald:wght@500;700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap');
        * { box-sizing: border-box; }
        input, select { font-family: inherit; }
        input::placeholder { color: ${COLORS.steelDim}; }
        input[type=number]::-webkit-inner-spin-button, input[type=number]::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
        input[type=number] { -moz-appearance: textfield; appearance: textfield; }
        button { font-family: inherit; cursor: pointer; }
        .btn-tactile { transition: transform 0.08s ease, filter 0.08s ease; }
        .btn-tactile:active { transform: translateY(1.5px); filter: brightness(0.9); }
        .overload-root { min-height: 100vh; min-height: 100dvh; overscroll-behavior: contain; }
        html, body { overscroll-behavior: contain; }
        * { -webkit-tap-highlight-color: transparent; }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-thumb { background: ${COLORS.line}; border-radius: 3px; }
        @keyframes pulseGlow { 0%,100% { box-shadow: 0 0 0 0 rgba(214,64,43,0.4);} 50% { box-shadow: 0 0 0 8px rgba(214,64,43,0);} }
        @keyframes restFlash { 0%, 100% { opacity: 0; } 15%, 45% { opacity: 1; } 30%, 60% { opacity: 0; } }
        @keyframes restMsgPop { 0% { opacity: 0; transform: translate(-50%, -50%) scale(0.9); } 10% { opacity: 1; transform: translate(-50%, -50%) scale(1); } 85% { opacity: 1; transform: translate(-50%, -50%) scale(1); } 100% { opacity: 0; transform: translate(-50%, -50%) scale(0.95); } }
      `}</style>

      {/* rest timer complete flash + next-set message */}
      {rest.justFinished && (
        <>
          <div style={{ position: "fixed", inset: 0, background: COLORS.red, opacity: 0, pointerEvents: "none", zIndex: 40, animation: "restFlash 1.2s ease-in-out 2" }} />
          <div
            style={{
              position: "fixed", top: "50%", left: "50%", zIndex: 41, pointerEvents: "none",
              animation: "restMsgPop 2.5s ease-in-out forwards",
              background: COLORS.surface, border: `2px solid ${COLORS.red}`, borderRadius: 16,
              padding: "22px 28px", textAlign: "center", maxWidth: "80%", boxShadow: "0 8px 30px rgba(0,0,0,0.5)",
            }}
          >
            <div style={{ fontSize: 26, marginBottom: 6 }}>💪</div>
            {rest.nextSet && rest.nextSet.exerciseName && (
              <div style={{ fontSize: 12, color: COLORS.steelDim, marginBottom: 4 }}>{rest.nextSet.exerciseName}</div>
            )}
            <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 20, color: COLORS.chalk }}>
              {rest.nextSet ? `${rest.nextSet.setNumber}セット目を始めましょう!` : "次のセットを始めましょう!"}
            </div>
          </div>
        </>
      )}

	  {/* Header */}
	  <div style={{ padding: "16px 18px 12px", borderBottom: `1px solid ${COLORS.line}`, display: "flex", alignItems: "flex-start" }}>
	    <div style={{ flex: 1, minWidth: 0, textAlign: "left" }}>
	      <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 22, letterSpacing: 1.5, fontWeight: 700, color: COLORS.chalk, textShadow: `0 0 18px rgba(214,64,43,0.45)` }}>OVERLOAD</div>
		  <div style={{ display: "flex", alignItems: "center", gap: 5, fontSize: 11, color: COLORS.steelDim, marginTop: 2, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
		  {tab === "workout" && <Dumbbell size={11} strokeWidth={1.8} style={{ flexShrink: 0 }} />}
		  {tab === "history" && <History size={11} strokeWidth={1.8} style={{ flexShrink: 0 }} />}
		  {tab === "exercises" && <ListChecks size={11} strokeWidth={1.8} style={{ flexShrink: 0 }} />}
		  {tab === "profile" && <User size={11} strokeWidth={1.8} style={{ flexShrink: 0 }} />}
		  {tab === "history"
		    ? selectedDay !== null
		      ? (() => {
		          const selectedDateSessions = sessions.filter((s) => {
		            const d = new Date(s.date);
		            return (
		              d.getFullYear() === historyMonth.year &&
		              d.getMonth() === historyMonth.month &&
		              d.getDate() === selectedDay
		            );
		          });

		          const exerciseIds = new Set(
		            selectedDateSessions.flatMap((s) =>
		              s.entries.map((e) => e.exerciseId)
		            )
		          );

		          const totalExercises = exerciseIds.size;

		          const totalVolume = selectedDateSessions.reduce(
		            (sum, s) =>
		              sum +
		              s.entries.reduce(
		                (entrySum, e) =>
		                  entrySum +
		                  e.sets.reduce(
		                    (setSum, st) => setSum + st.weight * st.reps,
		                    0
		                  ),
		                0
		              ),
		            0
		          );

		          const calorieSummary = summarizeCalories(selectedDateSessions);
          const calorieLabel = calorieSummary.totalCalories == null ? "カロリー未記録"
            : `${calorieSummary.totalCalories.toFixed(1)}kcal${calorieSummary.hasUnknownCalories ? "（未記録あり）" : ""}`;

          return `${fmtDate(
		            new Date(historyMonth.year, historyMonth.month, selectedDay)
		          )} · ${totalExercises}種目 · 総量 ${totalVolume.toLocaleString()}kg · 🔥${calorieLabel}`;
		        })()
		      : `${historyMonth.year}年${historyMonth.month + 1}月 · 日付を選択してください`
		    : tab === "workout"
		    ? "今日のトレーニングを始めよう"
		    : tab === "exercises"
		    ? "種目を選択すると詳細を確認できます"
		    : tab === "profile"
		    ? "あなたの情報を管理"
		    : ""}
	      </div>
	    </div>
        <RestTimer remaining={rest.remaining} running={rest.running} durationSeconds={rest.durationSeconds}
          disabled={workoutLocked} onToggle={() => { if (!editingBlocked()) rest.toggle(); }} />
	  </div>

      {dataError && <div role="alert" style={{ padding: 12, color: COLORS.amber }}>
        記録の読み込みに失敗しました。{dataError}
        <button type="button" onClick={data.retry}>再読み込み</button>
      </div>}
      <WorkoutDraftNotice draft={draft} />
      {saveConflict && <div role="alert" style={{ padding: 12, color: COLORS.amber }}>
        同じ識別子で別の内容が保存されています。履歴を確認してください。現在の下書きは保持しています。
        <button type="button" onClick={async () => {
          try { await loadHistory(); setTab("history"); } catch (error) { setWorkoutError(error.message); }
        }}>履歴を確認</button>
        <button type="button" onClick={async () => {
          if (!window.confirm("現在の下書きを削除します。サーバーの保存済み記録は残ります。元に戻せません。")) return;
          if (await draft.complete()) { setSaveConflict(false); setWorkoutError(""); }
        }}>現在の下書きを破棄</button>
      </div>}
      {activeSession && profileState.loadState !== "ready" && <div role="alert" style={{ padding: 12, color: COLORS.amber }}>
        実施中の記録は復元しました。プロフィールの{profileState.loadState === "loading" ? "読み込み中です。" : `読み込みに失敗しました。${profileState.error}`}
        {profileState.loadState === "error" && <button type="button" onClick={profileState.retryLoad}>再読み込み</button>}
      </div>}
      {/* Body */}
      <div style={{ flex: 1, overflowY: (pickerOpen || detailExercise || completionSummary) ? "hidden" : "auto", scrollbarGutter: "stable", padding: 16, paddingBottom: 90 }}>
        {tab === "workout" && (
          <WorkoutScreen
            activeSession={activeSession}
            startWorkout={startWorkout}
            canAddExercise={data.catalogReady}
            canStart={dataReady && !dataError && !draft.isPersisting && draft.errorKind !== "load" && draft.errorKind !== "clear" && draft.errorKind !== "conflict"}
            startLabel={!dataReady || dataError ? "記録を読み込んでいます" : profileState.validationError ? "プロフィールを入力" : profileState.saveState !== "saved" ? "プロフィールの保存を確認" : "ワークアウト開始"}
            gender={gender}
            restInput={rest.input}
            setRestInput={rest.setInput}
            restError={rest.error}
            exerciseMap={exerciseMap}
            prMap={prMap}
            setDetailExercise={setDetailExercise}
            removeEntry={removeEntry}
            updateDraft={updateDraft}
            bodyWeightKg={activeSession?.bodyWeightKg ?? bodyWeightKg}
            isSaving={isSavingWorkout}
            isLocked={workoutLocked}
            canFinish={!saveConflict && !["load", "clear", "conflict"].includes(draft.errorKind)}
            pendingSave={Boolean(draft.pendingPayload)}
            saveRecovery={saveConflict ? "conflict" : draft.pendingPayload ? "retry" : "edit"}
            saveError={isSavingWorkout ? "" : workoutError || (draft.pendingPayload ? "保存結果の確認が必要です。" : "")}
            removeSet={removeSet}
            entryErrors={entryErrors}
            addSet={addSet}
            setPickerOpen={setPickerOpen}
            setPickerCategory={setPickerCategory}
            setPickerEquipment={setPickerEquipment}
            finishWorkout={finishWorkout}
          />
        )}

        {tab === "history" && (
          <HistoryScreen
            historyMonth={historyMonth}
            sessions={sessions}
            setSelectedDay={setSelectedDay}
            setHistoryMonth={setHistoryMonth}
            prMap={prMap}
            selectedDay={selectedDay}
            exerciseMap={exerciseMap}
          />
        )}

        {tab === "exercises" && (
          <ExercisesScreen
            exerciseSearch={exerciseSearch}
            filterCategory={filterCategory}
            filterEquipment={filterEquipment}
            exercises={exercises}
            setDetailExercise={setDetailExercise}
            setExerciseSearch={setExerciseSearch}
            setFilterCategory={setFilterCategory}
            setFilterEquipment={setFilterEquipment}
          />
        )}

        {tab === "profile" && (profileState.loadState !== "ready" ? <div role="alert">プロフィールの読み込み完了後に編集できます。</div> :
          <ProfileScreen
            userName={userName}
            setUserName={setUserName}
            gender={gender}
            setGender={setGender}
            age={age}
            setAge={setAge}
            bodyWeightKg={bodyWeightKg}
            setBodyWeightKg={setBodyWeightKg}
            saveState={profileState.saveState}
            validationError={profileState.validationError}
            saveError={profileState.error}
            retrySave={profileState.retrySave}
          />
        )}
      </div>

      {/* exercise picker modal */}
      {pickerOpen && (
          <ExercisePicker
            setPickerOpen={setPickerOpen}
            setPickerCategory={setPickerCategory}
            setPickerEquipment={setPickerEquipment}
            pickerCategory={pickerCategory}
            exercises={exercises}
            pickerEquipment={pickerEquipment}
            setDetailExercise={setDetailExercise}
            addExerciseToSession={addExerciseToSession}
          />
        )}

      {/* exercise detail modal */}
      {detailExercise && (
          <ExerciseDetail
            setDetailExercise={setDetailExercise}
            detailExercise={detailExercise}
            detailExerciseInfo={detailExerciseInfo}
          />
        )}

      {/* workout completion summary */}
      {completionSummary && (
          <WorkoutSummary
            setCompletionSummary={setCompletionSummary}
            completionSummary={completionSummary}
            bodyWeightKg={completionSummary.bodyWeightKg}
          />
        )}

      {/* bottom nav */}
      <div style={{ position: "fixed", bottom: 0, left: 0, right: 0, maxWidth: 480, margin: "0 auto", background: COLORS.surface, borderTop: `1px solid ${COLORS.line}`, display: "flex", zIndex: 5 }}>
        {[
          { id: "workout", label: "ワークアウト", icon: Dumbbell },
          { id: "history", label: "履歴", icon: History },
          { id: "exercises", label: "種目", icon: ListChecks },
          { id: "profile", label: "プロフィール", icon: User },
        ].map(({ id, label, icon: Icon }) => (
          <button type="button" className="btn-tactile"
            key={id} onClick={() => setTab(id)}
            style={{ position: "relative", flex: 1, background: "none", border: "none", padding: "12px 0 10px", display: "flex", flexDirection: "column", alignItems: "center", gap: 3, color: tab === id ? COLORS.red : COLORS.steelDim }}
          >
            {tab === id && <div style={{ position: "absolute", top: 0, left: "30%", right: "30%", height: 2, background: COLORS.red, boxShadow: `0 0 8px ${COLORS.red}`, borderRadius: 2 }} />}
            <Icon size={18} style={tab === id ? { filter: `drop-shadow(0 0 4px rgba(214,64,43,0.7))` } : undefined} />
            <span style={{ fontSize: 10 }}>{label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
