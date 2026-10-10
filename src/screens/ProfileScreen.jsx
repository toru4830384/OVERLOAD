import { COLORS } from "../theme.js";
import { User } from "lucide-react";

/** プロフィール画面。必須入力の案内と編集欄を表示する。 */
export default function ProfileScreen({
  userName,
  setUserName,
  gender,
  setGender,
  age,
  setAge,
  bodyWeightKg,
  setBodyWeightKg,
  saveState,
  validationError,
  saveError,
  retrySave,
}) {

  return (<>
            <div style={{ textAlign: "center", marginBottom: 20 }}>
              <div style={{ width: 64, height: 64, borderRadius: "50%", background: COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 10px" }}>
                <User size={28} color={COLORS.steel} />
              </div>
              <div style={{ fontFamily: "'Oswald', sans-serif", fontSize: 18 }}>{userName.trim() ? userName : "マイプロフィール"}</div>
              <div style={{ fontSize: 11, color: COLORS.steelDim, marginTop: 2 }}>消費カロリー計算にはここで入力した体重が使われます</div>
            </div>

            <div style={{ background: COLORS.surface, border: `1px solid ${COLORS.line}`, borderRadius: 12, padding: 16, marginBottom: 14 }}>
              <div style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 12, color: COLORS.steel, marginBottom: 6 }}>名前</div>
                <input
                  type="text" maxLength="50" placeholder="未設定" value={userName}
                  onChange={(e) => setUserName(e.target.value)}
                  style={{ width: "100%", background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 10, color: COLORS.chalk, fontSize: 14 }}
                />
              </div>

              <div style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 12, color: COLORS.steel, marginBottom: 6 }}>性別（必須）</div>
                <div style={{ display: "flex", gap: 8 }}>
                  {[{ v: "male", l: "男性" }, { v: "female", l: "女性" }].map((g) => (
                    <button type="button" key={g.v} aria-pressed={gender === g.v} onClick={() => setGender(g.v)}
                      style={{ flex: 1, background: gender === g.v ? COLORS.blue : COLORS.surfaceAlt, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: "10px 0", color: gender === g.v ? "#fff" : COLORS.steel, fontSize: 13 }}
                    >
                      {g.l}
                    </button>
                  ))}
                </div>
                {gender === "" && <div role="alert" style={{ color: COLORS.steel, fontSize: 12, marginTop: 6 }}>男性または女性を選択してください。選択するまでプロフィールは保存されません。</div>}
              </div>

              <div style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 12, color: COLORS.steel, marginBottom: 6 }}>年齢（必須・0歳以上）</div>
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <input
                    type="number" inputMode="numeric" min="0" max="2147483647" step="1" required placeholder="年齢を入力（必須）" value={age}
                    onChange={(e) => setAge(e.target.value)}
                    style={{ flex: 1, background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 10, color: COLORS.chalk, fontSize: 14 }}
                  />
                  <span style={{ fontSize: 12, color: COLORS.steelDim }}>歳</span>
                </div>
                {age === "" && <div role="alert" style={{ color: COLORS.steel, fontSize: 12 }}>年齢を入力してください。入力するまでプロフィールは保存されません。</div>}
              </div>

              <div>
                <div style={{ fontSize: 12, color: COLORS.steel, marginBottom: 6 }}>体重(消費カロリー計算用)</div>
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <input
                    type="number" inputMode="decimal" min="0.01" max="999.99" step="0.01" required value={bodyWeightKg}
                    onChange={(e) => setBodyWeightKg(e.target.value)}
                    style={{ flex: 1, background: COLORS.bg, border: `1px solid ${COLORS.line}`, borderRadius: 8, padding: 10, color: COLORS.chalk, fontSize: 14 }}
                  />
                  <span style={{ fontSize: 12, color: COLORS.steelDim }}>kg</span>
                </div>
              </div>
            </div>

            {validationError && <div role="alert" style={{ color: COLORS.amber }}>{validationError}</div>}
            {!validationError && <div role={saveError ? "alert" : "status"} style={{ color: COLORS.steel, marginBottom: 12 }}>
              {saveError ? `保存に失敗しました。${saveError}` : saveState === "saved" ? "保存済み" : "保存中..."}
              {saveError && <button type="button" onClick={retrySave}>保存を再試行</button>}
            </div>}
            <div style={{ fontSize: 11, color: COLORS.steelDim, lineHeight: 1.6 }}>
              ※性別・年齢は現在プロフィール表示用として保存されます。消費カロリーの計算式(METs法)には体重のみが使われています。
            </div>
          </>);
}
