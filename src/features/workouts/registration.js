/** 終了日時と入力を固定する。失敗時の再送ではこのオブジェクトをそのまま使う。 */
export function workoutPayload(session, finishedAt = new Date().toISOString(), userId = 1) {
  return { userId, clientRequestId: session.id, startedAt: session.date, finishedAt,
    bodyWeightKg: Number(session.bodyWeightKg),
    exercises: session.entries.filter(entry => entry.sets.length > 0).map(entry => ({
      exerciseId: entry.exerciseId, note: entry.note || "",
      sets: entry.sets.map((set, index) => ({ setNumber: index + 1, weightKg: set.weight, reps: set.reps })),
    })),
  };
}
