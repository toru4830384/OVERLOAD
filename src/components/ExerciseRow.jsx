import React from "react";
import { COLORS } from "../theme.js";
import { MovementIcon } from "./MovementIcon.jsx";
import { EQUIPMENT_LABELS } from "../data/exercises.js";

export const ExerciseRow = React.memo(function ExerciseRow({ ex, onDetail, showEquipment }) {
  return (
    <div
      style={{
        display: "flex", justifyContent: "space-between", alignItems: "center", background: COLORS.surface,
        border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "8px 12px", marginBottom: 6,
        contentVisibility: "auto", containIntrinsicSize: "0 46px",
      }}
    >
      <button type="button" onClick={() => onDetail(ex)} style={{ flex: 1, display: "flex", alignItems: "center", gap: 10, background: "none", border: "none", textAlign: "left", padding: 0, color: COLORS.chalk}}>
        <div style={{ background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 6, padding: 3, display: "flex", flexShrink: 0 }}>
          <MovementIcon pattern={ex.pattern} size={20} />
        </div>
        {showEquipment ? (
          <div>
            <div style={{ fontSize: 14 }}>{ex.name}</div>
            <div style={{ fontSize: 10, color: COLORS.steelDim }}>{ex.category} · {EQUIPMENT_LABELS[ex.equipment]}</div>
          </div>
        ) : (
          <span style={{ fontSize: 14 }}>{ex.name}</span>
        )}
      </button>
    </div>
  );
});
