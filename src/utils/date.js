

export function fmtDate(iso) {
  const d = new Date(iso);
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} (${"日月火水木金土"[d.getDay()]})`;
}
