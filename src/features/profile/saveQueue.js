/** 同時PUTを避け、待機中の変更は最新の1件にまとめて順番に保存する。 */
export function createProfileSaveQueue(save, notify, delay = 500) {
  let running = false;
  let pending = null;
  let timer;
  let revision = 0;
  let disposed = false;

  async function drain() {
    if (running || disposed || !pending) return;
    running = true;
    while (pending && !disposed) {
      const job = pending;
      pending = null;
      notify({ state: "saving", revision: job.revision });
      try {
        await save(job.payload);
        if (!disposed) notify({ state: "persisted", payload: job.payload, revision: job.revision });
        if (!disposed && job.revision === revision) notify({ state: "saved", payload: job.payload, revision });
      } catch (error) {
        if (!disposed && job.revision === revision) notify({ state: "error", message: error.message, revision });
      }
    }
    running = false;
  }

  return {
    schedule(payload, immediate = false) {
      if (disposed) return;
      revision++;
      pending = { payload, revision };
      clearTimeout(timer);
      timer = setTimeout(drain, immediate ? 0 : delay);
    },
    hasWork() { return running || pending !== null; },
    invalidate() { revision++; pending = null; clearTimeout(timer); },
    dispose() { disposed = true; pending = null; clearTimeout(timer); },
  };
}
