import { useId, useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import "./password-field.css";

/** 入力値を変更・送信せず、この入力欄だけ表示を切り替える。 */
export default function PasswordField({ label, ...inputProps }) {
  const id = useId();
  const [visible, setVisible] = useState(false);
  return <div className="password-field">
    <label htmlFor={id}>{label}</label>
    <div className="password-field-control">
      <input {...inputProps} id={id} type={visible ? "text" : "password"} />
      <button className="password-visibility" type="button"
        aria-label={`${label}を${visible ? "隠す" : "表示する"}`}
        aria-controls={id} aria-pressed={visible}
        onClick={() => setVisible(value => !value)}>
        {visible ? <EyeOff size={22} aria-hidden="true" /> : <Eye size={22} aria-hidden="true" />}
      </button>
    </div>
  </div>;
}
