import { COLORS } from "../theme.js";
import { ChevronLeft, History } from "lucide-react";
import { fmtDate } from "../utils/date.js";
import { summarizeCalories } from "../features/workouts/history.js";

/** 履歴画面。月・日の選択とトレーニング記録を表示する。 */
export default function HistoryScreen({
  historyMonth,
  sessions,
  setSelectedDay,
  setHistoryMonth,
  prMap,
  selectedDay,
  exerciseMap,
}) {

  return (<>
            {(() => {
              const now = new Date();
              const isCurrentMonth = historyMonth.year === now.getFullYear() && historyMonth.month === now.getMonth();
              const monthKey = historyMonth.year * 12 + historyMonth.month;
              const earliestKey = sessions.length > 0
                ? Math.min(...sessions.map((s) => { const d = new Date(s.date); return d.getFullYear() * 12 + d.getMonth(); }))
                : monthKey;
              const isEarliestMonth = monthKey <= earliestKey;
              const monthSessions = sessions.filter((s) => {
                const d = new Date(s.date);
                return d.getFullYear() === historyMonth.year && d.getMonth() === historyMonth.month;
              });
              const workoutDays = new Set(monthSessions.map((s) => new Date(s.date).toDateString())).size;
              const { totalCalories: monthCalories, hasUnknownCalories } = summarizeCalories(monthSessions);
              const goPrev = () => {
                if (isEarliestMonth) return;
                setSelectedDay(null);
                setHistoryMonth((p) => (p.month === 0 ? { year: p.year - 1, month: 11 } : { year: p.year, month: p.month - 1 }));
              };
              const goNext = () => {
                if (isCurrentMonth) return;
                setSelectedDay(null);
                setHistoryMonth((p) => (p.month === 11 ? { year: p.year + 1, month: 0 } : { year: p.year, month: p.month + 1 }));
              };
              return (
                <>
                  <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 14 }}>
                    <button type="button" onClick={goPrev} disabled={isEarliestMonth} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 8, width: 34, height: 34, color: isEarliestMonth ? COLORS.steelDim : COLORS.chalk, display: "flex", alignItems: "center", justifyContent: "center", opacity: isEarliestMonth ? 0.4 : 1 }}>
                      <ChevronLeft size={16} />
                    </button>
                    <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 17 }}>{historyMonth.year}年{historyMonth.month + 1}月</div>
                    <button type="button" onClick={goNext} disabled={isCurrentMonth} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 8, width: 34, height: 34, color: isCurrentMonth ? COLORS.steelDim : COLORS.chalk, display: "flex", alignItems: "center", justifyContent: "center", opacity: isCurrentMonth ? 0.4 : 1 }}>
                      <ChevronLeft size={16} style={{ transform: "rotate(180deg)" }} />
                    </button>
                  </div>

                  <div style={{ display: "flex", gap: 10, marginBottom: 16 }}>
                    <div style={{ flex: 1, background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, padding: 14, textAlign: "center" }}>
                      <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 4 }}>ワークアウト日数</div>
                      <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 22, color: COLORS.red }}>{workoutDays}<span style={{ fontSize: 13, marginLeft: 2 }}>日</span></div>
                    </div>
                    <div style={{ flex: 1, background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, padding: 14, textAlign: "center" }}>
                      <div style={{ fontSize: 10, color: COLORS.steelDim, marginBottom: 4 }}>合計消費カロリー</div>
                      <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 22 }}>{monthCalories == null ? "—" : monthCalories.toFixed(1)}<span style={{ fontSize: 13, marginLeft: 2 }}>kcal</span></div>
                      {hasUnknownCalories && <div style={{ fontSize: 10 }}>記録済みの分のみ集計（未記録あり）</div>}
                    </div>
                  </div>

                  {(() => {
                    const firstOfMonth = new Date(historyMonth.year, historyMonth.month, 1);
                    const firstDow = firstOfMonth.getDay();
                    const daysInMonth = new Date(historyMonth.year, historyMonth.month + 1, 0).getDate();
                    const dayLabels = ["日", "月", "火", "水", "木", "金", "土"];
                    const cells = [];
                    for (let i = 0; i < firstDow; i++) cells.push(null);
                    for (let d = 1; d <= daysInMonth; d++) cells.push(d);

                    return (
                      <>
                        <div style={{ display: "grid", gridTemplateColumns: "repeat(7, 1fr)", gap: 4, marginBottom: 6 }}>
						  {dayLabels.map((l, i) => (
						    <div
						      key={l}
						      style={{
						        color: i === 0 ? "#E89A9A" :
						               i === 6 ? "#8CC9F2" :
						               "#B8B8B8",
						        fontSize: 11,
						        textAlign: "center"
						      }}
						    >
						      {l}
						    </div>
						  ))}
                        </div>
                        <div style={{ display: "grid", gridTemplateColumns: "repeat(7, 1fr)", gap: 4, marginBottom: 18 }}>
                          {cells.map((d, i) => {
                            if (d === null) return <div key={"blank" + i} />;
                            const daySessions = monthSessions.filter((s) => new Date(s.date).getDate() === d);
                            const hasSession = daySessions.length > 0;
                            const isPrDay = daySessions.some((s) =>
                              s.entries.some((e) => e.sets.some((set) => set.weight === prMap[e.exerciseId]))
                            );
                            const isSelected = selectedDay === d;
							const date = new Date(historyMonth.year, historyMonth.month, d);
							const dayOfWeek = date.getDay();
							const holidays = [
							  "2026-01-01",
							  "2026-01-12",
							  "2026-02-11",
							  "2026-02-23",
							  "2026-03-20",
							  "2026-04-29",
							  "2026-05-03",
							  "2026-05-04",
							  "2026-05-05",
							  "2026-07-20",
							  "2026-08-11",
							  "2026-09-21",
							  "2026-09-22",
							  "2026-10-12",
							  "2026-11-03",
							  "2026-11-23",
							];

							const dateKey =
							  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;

							const isHoliday = holidays.includes(dateKey);

							const dayColor =
							  dayOfWeek === 0 || isHoliday ? "#E89A9A" :
							  dayOfWeek === 6 ? "#8CC9F2" :
							  "#B8B8B8";
                            const accentColor = isPrDay ? COLORS.amber : COLORS.red;
                            const selectColor = hasSession ? accentColor : COLORS.steel;
                            return (
                              <button
                                type="button" key={d}
                                onClick={() => setSelectedDay((prev) => (prev === d ? null : d))}
                                style={{
                                  aspectRatio: "1", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center",
                                  borderRadius: 8, fontSize: 12, background: isSelected ? `${selectColor}22` : hasSession ? `${accentColor}1A` : "transparent",
                                  border: `1px solid ${isSelected ? selectColor : hasSession ? accentColor : "transparent"}`,
                                  color: dayColor, cursor: "pointer",
                                }}
                              >
                                <span>{d}</span>
                                {hasSession && <div style={{ width: 4, height: 4, borderRadius: "50%", background: accentColor, marginTop: 2 }} />}
                              </button>
                            );
                          })}
                        </div>
                        <div style={{ display: "flex", gap: 14, marginBottom: 18, paddingBottom: 14, borderBottom: `1px solid ${COLORS.line}` }}>
                          <div style={{ display: "flex", alignItems: "center", gap: 5 }}>
                            <div style={{ width: 8, height: 8, borderRadius: "50%", background: COLORS.red }} />
                            <span style={{ color: COLORS.steelDim, fontSize: 11 }}>記録した日</span>
                          </div>
                          <div style={{ display: "flex", alignItems: "center", gap: 5 }}>
                            <div style={{ width: 8, height: 8, borderRadius: "50%", background: COLORS.amber }} />
                            <span style={{ color: COLORS.steelDim, fontSize: 11 }}>自己ベスト達成</span>
                          </div>
                        </div>

                        {selectedDay === null ? (
                          monthSessions.length === 0 ? (
                            <div style={{ textAlign: "center", padding: "30px 20px", color: COLORS.steelDim, fontSize: 13 }}>
                              <History size={28} style={{ marginBottom: 10 }} />
                              <div>この月の記録はありません</div>
                            </div>
                          ) : (
                            <div style={{ textAlign: "center", padding: "10px 20px", color: COLORS.steelDim, fontSize: 12 }}>
                              日付をタップすると、その日の記録が見られます
                            </div>
                          )
                        ) : (() => {
                          const daySessions = monthSessions.filter((s) => new Date(s.date).getDate() === selectedDay);
                          if (daySessions.length === 0) {
                            return (
                              <div style={{ textAlign: "center", padding: "24px 20px", color: COLORS.steelDim, fontSize: 12 }}>
                                {historyMonth.month + 1}月{selectedDay}日はワークアウトの記録がありません
                              </div>
                            );
                          }
                          return daySessions.map((s) => {
                            const totalVolume = s.entries.reduce((sum, e) => sum + e.sets.reduce((ss, st) => ss + st.weight * st.reps, 0), 0);
                            return (
                              <div key={s.id} style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, marginBottom: 10, padding: 14 }}>
                                <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 15, marginBottom: 2 }}>{fmtDate(s.date)}</div>
                                <div style={{ fontSize: 11, color: COLORS.steelDim, marginBottom: 12 }}>
                                  {s.entries.length}種目 · 総量 {totalVolume.toLocaleString()}kg · 🔥{s.totalCalories == null ? "未記録" : `${s.totalCalories.toFixed(1)}kcal`}
                                </div>
                                {s.entries.map((entry) => {
                                  const ex = exerciseMap[entry.exerciseId];
                                  return (
                                    <div key={entry.exerciseId} style={{ marginBottom: 10 }}>
                                      <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 4 }}>{ex ? ex.name : "削除済み種目"}</div>
                                      <div style={{ display: "flex", flexWrap: "wrap", gap: 6, marginBottom: entry.note ? 6 : 0 }}>
                                        {entry.sets.map((set, i) => (
                                          <span key={i} style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: 12, background: COLORS.surfaceAlt, padding: "3px 8px", borderRadius: 6, color: COLORS.steel }}>
                                            {set.weight}kg×{set.reps}
                                          </span>
                                        ))}
                                      </div>
                                      {entry.note && (
                                        <div style={{ fontSize: 12, color: COLORS.amber, display: "flex", alignItems: "flex-start", gap: 5 }}>
                                          <span>📝</span><span style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}>{entry.note}</span>
                                        </div>
                                      )}
                                    </div>
                                  );
                                })}
                              </div>
                            );
                          });
                        })()}
                      </>
                    );
                  })()}
                </>
              );
            })()}
          </>);
}
