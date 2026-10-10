import { useMemo } from "react";
import { CATEGORIES, EQUIPMENT_ORDER, EQUIPMENT_LABELS } from "../data/exercises.js";
import { COLORS } from "../theme.js";
import { ExerciseRow } from "../components/ExerciseRow.jsx";
import { X } from "lucide-react";

/** 種目一覧画面。検索・部位・器具の条件に応じた表示を担当する。 */
export default function ExercisesScreen({
  exerciseSearch,
  filterCategory,
  filterEquipment,
  exercises,
  setDetailExercise,
  setExerciseSearch,
  setFilterCategory,
  setFilterEquipment,
}) {
  const exerciseListView = useMemo(() => {
    const q = exerciseSearch.trim().toLowerCase();
    const isFiltering = q || filterCategory !== "all" || filterEquipment !== "all";
    if (!isFiltering) {
      return CATEGORIES.map((cat) => {
        const list = exercises.filter((e) => e.category === cat);
        if (list.length === 0) return null;
        return (
          <div key={cat} style={{ marginBottom: 14 }}>
            <div style={{ fontSize: 11, color: COLORS.chalk, marginBottom: 6, letterSpacing: 1 }}>{cat.toUpperCase()}</div>
            {list.map((ex) => (
              <ExerciseRow key={ex.id} ex={ex} onDetail={setDetailExercise} showEquipment={false} />
            ))}
          </div>
        );
      });
    }
    const results = exercises.filter((e) =>
      (!q || e.name.toLowerCase().includes(q)) &&
      (filterCategory === "all" || e.category === filterCategory) &&
      (filterEquipment === "all" || e.equipment === filterEquipment)
    );
    if (results.length === 0) {
      return <div style={{ textAlign: "center", color: COLORS.steelDim, fontSize: 13, padding: "30px 0" }}>条件に一致する種目が見つかりません</div>;
    }
    return (
      <div style={{ marginBottom: 14 }}>
        <div style={{ fontSize: 11, color: COLORS.chalk, marginBottom: 6, letterSpacing: 1 }}>{results.length}件ヒット</div>
        {results.map((ex) => (
          <ExerciseRow key={ex.id} ex={ex} onDetail={setDetailExercise} showEquipment={true} />
        ))}
      </div>
    );
  }, [exercises, exerciseSearch, filterCategory, filterEquipment, setDetailExercise]);
  return (<>
            <div style={{ position: "relative", marginBottom: 10 }}>
              <input
                placeholder="種目名で検索" value={exerciseSearch} onChange={(e) => setExerciseSearch(e.target.value)}
                style={{ width: "100%", background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: "12px 14px", color: COLORS.chalk, fontSize: 14 }}
              />
              {exerciseSearch && (
                <button type="button" onClick={() => setExerciseSearch("")} style={{ position: "absolute", right: 10, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", color: COLORS.steelDim }}>
                  <X size={16} />
                </button>
              )}
            </div>

            <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
              <select value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)} style={{ flex: 1, background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: "10px 8px", color: COLORS.chalk, fontSize: 13 }}>
                <option value="all">すべての部位</option>
                {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
              <select value={filterEquipment} onChange={(e) => setFilterEquipment(e.target.value)} style={{ flex: 1, background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 10, padding: "10px 8px", color: COLORS.chalk, fontSize: 13 }}>
                <option value="all">すべての器具</option>
                {EQUIPMENT_ORDER.map((eq) => <option key={eq} value={eq}>{EQUIPMENT_LABELS[eq]}</option>)}
              </select>
            </div>

            {exerciseListView}
          </>);
}
