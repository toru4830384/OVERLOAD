import React from "react";
import { COLORS } from "../theme.js";

export const MovementIcon = React.memo(function MovementIcon({ pattern, size = 26, color = COLORS.steel }) {
  const s = { stroke: color, strokeWidth: 2.4, strokeLinecap: "round", strokeLinejoin: "round", fill: "none" };
  let body;
  switch (pattern) {
    case "push_horizontal":
      body = (<>
        <circle cx="8" cy="23" r="3" {...s} />
        <line x1="11" y1="23" x2="24" y2="23" {...s} />
        <path d="M24,23 L29,28 L34,23" {...s} />
        <line x1="16" y1="23" x2="16" y2="13" {...s} />
        <line x1="9" y1="13" x2="23" y2="13" {...s} />
        <circle cx="9" cy="13" r="1.8" {...s} />
        <circle cx="23" cy="13" r="1.8" {...s} />
      </>);
      break;
    case "push_vertical":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="20" y1="11" x2="20" y2="24" {...s} />
        <line x1="20" y1="24" x2="15" y2="35" {...s} />
        <line x1="20" y1="24" x2="25" y2="35" {...s} />
        <line x1="20" y1="14" x2="12" y2="5" {...s} />
        <line x1="20" y1="14" x2="28" y2="5" {...s} />
        <line x1="10" y1="5" x2="30" y2="5" {...s} />
      </>);
      break;
    case "pull_vertical":
      body = (<>
        <line x1="8" y1="4" x2="32" y2="4" {...s} />
        <circle cx="20" cy="10" r="3" {...s} />
        <line x1="20" y1="13" x2="10" y2="4" {...s} />
        <line x1="20" y1="13" x2="30" y2="4" {...s} />
        <line x1="20" y1="13" x2="20" y2="27" {...s} />
        <line x1="20" y1="27" x2="17" y2="36" {...s} />
        <line x1="20" y1="27" x2="23" y2="36" {...s} />
      </>);
      break;
    case "pull_horizontal":
      body = (<>
        <circle cx="10" cy="12" r="3" {...s} />
        <line x1="10" y1="15" x2="26" y2="25" {...s} />
        <line x1="26" y1="25" x2="24" y2="36" {...s} />
        <line x1="26" y1="25" x2="33" y2="29" {...s} />
        <line x1="17" y1="19" x2="11" y2="24" {...s} />
        <circle cx="11" cy="24" r="1.8" {...s} />
      </>);
      break;
    case "squat":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="12" y1="11" x2="28" y2="11" {...s} />
        <line x1="20" y1="11" x2="20" y2="20" {...s} />
        <line x1="20" y1="20" x2="12" y2="26" {...s} />
        <line x1="20" y1="20" x2="28" y2="26" {...s} />
        <line x1="12" y1="26" x2="14" y2="36" {...s} />
        <line x1="28" y1="26" x2="26" y2="36" {...s} />
      </>);
      break;
    case "hinge":
      body = (<>
        <circle cx="9" cy="14" r="3" {...s} />
        <line x1="9" y1="17" x2="28" y2="24" {...s} />
        <line x1="28" y1="24" x2="26" y2="35" {...s} />
        <line x1="28" y1="24" x2="32" y2="34" {...s} />
        <line x1="15" y1="19" x2="15" y2="31" {...s} />
        <line x1="8" y1="31" x2="22" y2="31" {...s} />
        <circle cx="8" cy="31" r="1.8" {...s} />
        <circle cx="22" cy="31" r="1.8" {...s} />
      </>);
      break;
    case "lunge":
      body = (<>
        <circle cx="16" cy="8" r="3" {...s} />
        <line x1="16" y1="11" x2="19" y2="22" {...s} />
        <path d="M19,22 L25,25 L25,35" {...s} />
        <path d="M19,22 L10,27 L8,35" {...s} />
      </>);
      break;
    case "curl":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="20" y1="11" x2="20" y2="26" {...s} />
        <line x1="20" y1="26" x2="16" y2="36" {...s} />
        <line x1="20" y1="26" x2="24" y2="36" {...s} />
        <line x1="20" y1="15" x2="26" y2="19" {...s} />
        <line x1="26" y1="19" x2="22" y2="10" {...s} />
        <circle cx="22" cy="10" r="2" {...s} />
      </>);
      break;
    case "extension":
      body = (<>
        <circle cx="13" cy="8" r="3" {...s} />
        <line x1="13" y1="11" x2="13" y2="26" {...s} />
        <line x1="13" y1="26" x2="9" y2="36" {...s} />
        <line x1="13" y1="26" x2="17" y2="36" {...s} />
        <line x1="13" y1="14" x2="21" y2="11" {...s} />
        <line x1="21" y1="11" x2="21" y2="22" {...s} />
        <circle cx="21" cy="22" r="1.8" {...s} />
      </>);
      break;
    case "fly":
      body = (<>
        <circle cx="20" cy="10" r="3" {...s} />
        <line x1="20" y1="13" x2="20" y2="26" {...s} />
        <line x1="20" y1="26" x2="16" y2="36" {...s} />
        <line x1="20" y1="26" x2="24" y2="36" {...s} />
        <path d="M9,17 Q20,25 31,17" {...s} />
      </>);
      break;
    case "raise":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="20" y1="11" x2="20" y2="26" {...s} />
        <line x1="20" y1="26" x2="16" y2="36" {...s} />
        <line x1="20" y1="26" x2="24" y2="36" {...s} />
        <line x1="20" y1="15" x2="8" y2="13" {...s} />
        <line x1="20" y1="15" x2="32" y2="13" {...s} />
      </>);
      break;
    case "core":
      body = (<>
        <path d="M9,31 L20,27 L27,33" {...s} />
        <path d="M9,31 Q8,20 15,13" {...s} />
        <circle cx="15" cy="10" r="3" {...s} />
      </>);
      break;
    case "shrug":
      body = (<>
        <circle cx="20" cy="9" r="3" {...s} />
        <line x1="20" y1="12" x2="20" y2="26" {...s} />
        <line x1="13" y1="13" x2="27" y2="13" {...s} />
        <line x1="13" y1="13" x2="11" y2="25" {...s} />
        <line x1="27" y1="13" x2="29" y2="25" {...s} />
        <circle cx="11" cy="25" r="1.8" {...s} />
        <circle cx="29" cy="25" r="1.8" {...s} />
        <line x1="20" y1="26" x2="16" y2="36" {...s} />
        <line x1="20" y1="26" x2="24" y2="36" {...s} />
      </>);
      break;
    case "calf":
      body = (<>
        <circle cx="20" cy="8" r="3" {...s} />
        <line x1="20" y1="11" x2="20" y2="24" {...s} />
        <line x1="20" y1="14" x2="13" y2="19" {...s} />
        <line x1="20" y1="14" x2="27" y2="19" {...s} />
        <line x1="20" y1="24" x2="18" y2="32" {...s} />
        <line x1="18" y1="32" x2="23" y2="34" {...s} />
      </>);
      break;
    default:
      body = <circle cx="20" cy="20" r="10" {...s} />;
  }
  return (
    <svg viewBox="0 0 40 40" width={size} height={size} style={{ flexShrink: 0 }}>
      {body}
    </svg>
  );
});
