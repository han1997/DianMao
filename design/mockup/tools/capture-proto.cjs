// Capture each prototype screen at the phone-frame width for pixel comparison.
// Emits the .phone-screen element at 1x (410x880), matching a 410dp-wide device.
// Usage: PLAYWRIGHT_MODULE=<path> node tools/capture-proto.cjs
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const { pathToFileURL } = require('node:url');
const path = require('node:path');
const fs = require('node:fs');

const OUT = path.resolve(__dirname, '..', '..', '..', '.tmp-shots');
const PAGES = ['home', 'quadrant', 'detail', 'add', 'calendar', 'review', 'settings', 'appearance', 'backup', 'archive', 'dialogs', 'widget'];

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch({ channel: 'chrome', headless: true });
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, deviceScaleFactor: 1 });
  await page.goto(pathToFileURL(path.resolve(__dirname, '..', 'index.html')).href);
  await page.waitForTimeout(200);
  for (const theme of ['light', 'dark']) {
    for (const pg of PAGES) {
      await page.evaluate((p) => { state = initial(); state.themeMode = p.theme; if (p.pg === 'appearance') state.from = 'settings'; applyTheme(); navigate(p.pg); }, { pg, theme });
      await page.waitForTimeout(60);
      const el = await page.$('#phone');
      await el.screenshot({ path: path.join(OUT, `proto-${pg}-${theme}-frame.png`) });
    }
  }
  await browser.close();
  // The phone frame adds a 6px border all around; trim it so each image is
  // exactly the 390x808 app screen (1px == 1dp).
  const { execSync } = require('node:child_process');
  for (const f of fs.readdirSync(OUT).filter((n) => n.endsWith('-frame.png'))) {
    const src = path.join(OUT, f);
    const dst = path.join(OUT, f.replace('-frame.png', '.png'));
    try { execSync(`magick "${src}" -shave 6x6 "${dst}"`, { stdio: 'ignore' }); } catch { fs.copyFileSync(src, dst); }
    fs.unlinkSync(src);
  }
  console.log('prototype screens captured (390x808 app area) →', OUT);
})().catch(e => { console.error(e); process.exit(1); });