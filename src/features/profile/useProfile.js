import { useState, useEffect, useRef, useCallback } from "react";
import { getProfile, saveProfile } from "../../api/client.js";
import { createProfileSaveQueue } from "./saveQueue.js";
import { profileError, profilePayload } from "./validation.js";

const EMPTY = { name: "", gender: "", age: "", bodyWeightKg: "" };

/** プロフィールの読込・検証・直列保存と、その画面状態を管理する。 */
export function useProfile(userId) {
  const [profile, setProfile] = useState(EMPTY);
  const [loadState, setLoadState] = useState("loading");
  const [saveState, setSaveState] = useState("idle");
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  const [retry, setRetry] = useState(0);
  const saved = useRef("");
  const [savedKey, setSavedKey] = useState("");
  const queue = useRef(null);
  const consumedRetry = useRef(0);

  useEffect(() => {
    const instance = createProfileSaveQueue(payload => saveProfile(userId, payload), event => {
      if (event.state === "persisted") {
        // 無効な入力で待機中の保存を取り消しても、通信中に成功したDB値を追跡する。
        saved.current = JSON.stringify(event.payload);
        setSavedKey(saved.current);
        return;
      }
      setSaveState(event.state);
      if (event.state === "saved") {
        saved.current = JSON.stringify(event.payload);
        setSavedKey(saved.current);
        setError("");
      } else if (event.state === "error") setError(event.message);
    });
    queue.current = instance;
    return () => instance.dispose();
  }, [userId]);

  useEffect(() => {
    const controller = new AbortController();
    getProfile(userId, controller.signal).then(user => {
      if (controller.signal.aborted) return;
      const data = { name: user.name ?? "", gender: ["male", "female"].includes(user.gender) ? user.gender : "", age: user.age ?? "", bodyWeightKg: user.bodyWeightKg ?? "" };
      saved.current = JSON.stringify(profilePayload(data));
      setSavedKey(saved.current);
      setProfile(data);
      setLoadState("ready");
      setSaveState("saved");
      setError("");
    }).catch(cause => {
      if (controller.signal.aborted) return;
      setError(cause.message);
      setLoadState("error");
    });
    return () => controller.abort();
  }, [userId, reload]);

  useEffect(() => {
    if (loadState !== "ready" || profileError(profile)) { queue.current?.invalidate(); return; }
    const payload = profilePayload(profile);
    if (JSON.stringify(payload) !== saved.current || queue.current?.hasWork()) {
      queue.current?.schedule(payload, retry > consumedRetry.current);
      consumedRetry.current = retry;
    }
    else Promise.resolve().then(() => setSaveState("saved"));
  }, [profile, loadState, retry]);

  const update = useCallback((field, value) => {
    setProfile(previous => ({ ...previous, [field]: value }));
    setSaveState("pending");
    setError("");
  }, []);
  const retryLoad = useCallback(() => { setLoadState("loading"); setError(""); setReload(n => n + 1); }, []);
  const retrySave = useCallback(() => { setError(""); setSaveState("pending"); setRetry(n => n + 1); }, []);
  const validationError = profileError(profile);
  const hasUnsavedChanges = JSON.stringify(profilePayload(profile)) !== savedKey;
  const status = validationError ? "invalid" : hasUnsavedChanges && saveState === "saved" ? "pending" : saveState;
  return { profile, update, loadState, saveState: status, error, validationError, retryLoad, retrySave };
}
