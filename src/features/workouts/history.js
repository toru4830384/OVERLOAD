/** 保存済みの計算結果を復元する。当時の値がない記録はnullを維持する。 */
export function restoreHistory(rows) {
  const grouped = new Map();
  for (const item of rows) {
    if (!grouped.has(item.sessionId)) grouped.set(item.sessionId, {
      id: item.sessionId, date: item.startedAt, finishedAt: item.finishedAt,
      bodyWeightKg: item.bodyWeightKg == null ? null : Number(item.bodyWeightKg),
      totalCalories: item.totalCalories == null ? null : Number(item.totalCalories), entries: [],
    });
    // 種目一覧が未取得でも、履歴APIが返した名称で記録を表示できるようにする。
    grouped.get(item.sessionId).entries.push({ exerciseId: item.exerciseId, exerciseName: item.exerciseName || "",
      note: item.note || "", sets: item.sets.map(set => ({ weight: Number(set.weightKg), reps: set.reps })) });
  }
  return [...grouped.values()].sort((a, b) => new Date(b.date) - new Date(a.date));
}

/** 保存済みカロリーだけを集計する。旧記録の未記録と0kcalを区別する。 */
export function summarizeCalories(sessions) {
  const recorded = sessions.filter(session => session.totalCalories != null);
  const totalHundredths = recorded.reduce((sum, session) => sum + Math.round(session.totalCalories * 100), 0);
  return {
    totalCalories: sessions.length > 0 && recorded.length === 0 ? null : totalHundredths / 100,
    hasUnknownCalories: recorded.length !== sessions.length,
  };
}
