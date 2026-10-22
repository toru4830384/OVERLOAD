import test from "node:test";
import assert from "node:assert/strict";
import { createElement } from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { useWorkoutRest } from "../src/features/workouts/useWorkoutRest.js";

// 実画面で発生した「未開始のsessionIdと通知が両方未定義」の初期描画を再現する。
function RestProbe({ session }) {
  const rest = useWorkoutRest({ session, setSession: () => Promise.resolve(true) });
  return createElement("output", null, JSON.stringify({ remaining: rest.remaining,
    nextSet: rest.nextSet, justFinished: rest.justFinished }));
}

test("rest hook renders safely before a workout starts and after it is cleared", () => {
  for (const session of [undefined, null]) {
    const html = renderToStaticMarkup(createElement(RestProbe, { session }));
    assert.match(html, /remaining&quot;:null/);
    assert.match(html, /nextSet&quot;:null/);
    assert.match(html, /justFinished&quot;:false/);
  }
});
