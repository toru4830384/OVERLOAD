import { Pause, Play } from "lucide-react";
import { COLORS } from "../theme.js";
import { formatRestTime } from "../features/workouts/restTimer.js";

/** 本物のbuttonで、一時停止・再開と操作不能状態を支援技術へ伝える。 */
export default function RestTimer({ remaining, running, durationSeconds, disabled = false, onToggle }) {
  if (remaining == null) return null;
  const inactive = disabled || remaining <= 0;
  const color = !running ? COLORS.steelDim : remaining <= 5 ? COLORS.red : COLORS.amber;
  const radius = 21;
  const circumference = 2 * Math.PI * radius;
  const progress = durationSeconds > 0 ? Math.max(0, Math.min(1, remaining / durationSeconds)) : 0;
  const action = remaining <= 0 ? "休憩は終了しています" : running ? "休憩を一時停止" : "休憩を再開";
  return <button type="button" disabled={inactive} onClick={onToggle}
    aria-label={`${action}（残り${formatRestTime(remaining)}）`}
    style={{ position: "relative", width: 52, height: 52, flexShrink: 0, marginLeft: "auto",
      display: "flex", alignItems: "center", justifyContent: "center", border: 0,
      background: "transparent", padding: 0, cursor: inactive ? "default" : "pointer", borderRadius: "50%" }}>
    <svg aria-hidden="true" width="52" height="52" viewBox="0 0 52 52"
      style={{ position: "absolute", top: 0, left: 0, transform: "rotate(-90deg)" }}>
      <circle cx="26" cy="26" r={radius} fill="none" stroke={COLORS.line} strokeWidth="4" />
      <circle cx="26" cy="26" r={radius} fill="none" stroke={color} strokeWidth="4" strokeLinecap="round"
        strokeDasharray={circumference} strokeDashoffset={circumference * (1 - progress)}
        style={{ transition: "stroke-dashoffset 0.2s linear, stroke 0.3s" }} />
    </svg>
    <span style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 1 }}>
      {running ? <Pause size={9} color={COLORS.steelDim} aria-hidden="true" /> : <Play size={9} color={COLORS.steelDim} aria-hidden="true" />}
      <span role="timer" aria-live="off" style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 12, color }}>
        {formatRestTime(remaining)}
      </span>
    </span>
  </button>;
}
