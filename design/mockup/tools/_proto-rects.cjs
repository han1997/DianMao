const { chromium } = require(process.env.PLAYWRIGHT_MODULE);
const { pathToFileURL } = require('node:url');
const path = require('node:path');
(async () => {
  const b = await chromium.launch({ channel: 'chrome', headless: true });
  const p = await b.newPage({ viewport: { width: 1440, height: 1000 } });
  await p.goto(pathToFileURL(path.resolve(__dirname, '..', 'index.html')).href);
  await p.waitForTimeout(150);
  await p.evaluate(() => { state.themeMode = 'dark'; applyTheme(); render(); });
  const rects = await p.evaluate(() => {
    const root = document.querySelector('.phone').getBoundingClientRect();
    const rel = (el) => { const r = el.getBoundingClientRect(); return { x: +(r.x - root.x - 6).toFixed(1), y: +(r.y - root.y - 6).toFixed(1), w: +r.width.toFixed(1), h: +r.height.toFixed(1) }; };
    const out = {};
    const sel = { pills: '.pills', pill0: '.pill', segmented: '.segmented', appHeader: '.app-header', lane: '.lane', laneRow: '.lane-row', qboard: '.qboard', qpane: '.qpane', qtask: '.qtask', fab: '.fab', title: '.app-title', compass: '.compass' };
    for (const [k, s] of Object.entries(sel)) { const el = document.querySelector(s); if (el) out[k] = rel(el); }
    return out;
  });
  console.log(JSON.stringify(rects, null, 1));
  await b.close();
})().catch(e => { console.error(e); process.exit(1); });
