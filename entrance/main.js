import { waitForReadiness } from "./readiness.js";
import { appUrl, healthUrl } from "./config.js";

const status = document.querySelector("#status");
const title = document.querySelector("#loading-title");
const elapsed = document.querySelector("#elapsed");
const meter = document.querySelector("#activity");
const retry = document.querySelector("#retry");
let active;

async function connect() {
  active?.abort();
  const controller = new AbortController();
  active = controller;
  const started = performance.now();
  const updateElapsed = () => { elapsed.textContent = `${Math.floor((performance.now() - started) / 1000)}秒`; };
  updateElapsed();
  const timer = setInterval(updateElapsed, 250);
  retry.hidden = true;
  meter.hidden = false;
  title.textContent = "〜 NOW LOADING 〜";
  status.textContent = "あなたのトレーニング記録へ接続しています。";
  try {
    await waitForReadiness({ url: healthUrl, signal: controller.signal });
    if (controller.signal.aborted) return;
    updateElapsed();
    title.textContent = "準備ができました";
    status.textContent = "OVERLOADを開きます。";
    window.location.replace(appUrl);
  } catch (error) {
    if (controller.signal.aborted) return;
    updateElapsed();
    title.textContent = "接続を確認できませんでした";
    status.textContent = error.message;
    retry.hidden = false;
  } finally {
    clearInterval(timer);
    if (active === controller) meter.hidden = true;
  }
}

retry.addEventListener("click", connect);
window.addEventListener("pagehide", () => active?.abort());
// 戻る操作で復元されても、以前の停止済み状態を放置しない。
window.addEventListener("pageshow", event => { if (event.persisted) connect(); });
connect();
