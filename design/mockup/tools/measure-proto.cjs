/**
 * measure-proto.cjs -- dump precise element geometry + computed typography from
 * the DianMao prototype so it can be diffed against the running app.
 *
 * Usage:
 *   PLAYWRIGHT_MODULE=<path/to/playwright> node measure-proto.cjs <page> <theme> [selectorsJson]
 *
 * Output: JSON { page, theme, phone: {w,h}, elements: [...] }
 *   each element: { sel, index, x, y, w, h, fontSize, fontWeight, color,
 *                   background, borderRadius, padding, gap }
 */
const path = require('path');
const fs = require('fs');

const PW = process.env.PLAYWRIGHT_MODULE || 'playwright';
const { chromium } = require(PW);

const ROOT = path.resolve(__dirname, '..');
const page = process.argv[2] || 'home';
const theme = process.argv[3] || 'light';
const selectors = process.argv[4]
  ? JSON.parse(process.argv[4])
  : ['.header', '.header-actions', '.itile'];

(async () => {
  const browser = await chromium.launch({ channel: 'chrome' });
  const ctx = await browser.newContext({
    viewport: { width: 1400, height: 1000 },
    deviceScaleFactor: 3,
    colorScheme: theme === 'dark' ? 'dark' : 'light',
  });
  const pg = await ctx.newPage();
  await pg.goto('file://' + path.join(ROOT, 'index.html').replace(/\\/g, '/'));
  await pg.waitForTimeout(300);
  await pg.evaluate(
    ([p, t]) => {
      window.__setPage && window.__setPage(p);
      window.__setTheme && window.__setTheme(t);
    },
    [page, theme]
  );
  await pg.waitForTimeout(400);

  const data = await pg.evaluate(
    ([sels, leafOnly]) => {
    const phone = document.querySelector('.phone');
    const pr = phone.getBoundingClientRect();
    const cs = getComputedStyle(phone);
    const bw = parseFloat(cs.borderLeftWidth) || 0;
    // prototype inner screen origin (device pixel space, /3 for logical)
    const originX = pr.left + bw;
    const originY = pr.top + bw;

    const px = (v) => Math.round((parseFloat(v) || 0) * 100) / 100;
    const out = [];
    for (const sel of sels) {
      const nodes = [...document.querySelectorAll(sel)];
      nodes.forEach((el, i) => {
        if (leafOnly) {
          const hasElementChild = [...el.children].some(
            (c) => c.tagName !== 'INPUT' && c.tagName !== 'SVG' && c.tagName !== 'PATH'
          );
          if (hasElementChild) return;
        }
        const r = el.getBoundingClientRect();
        const s = getComputedStyle(el);
        out.push({
          sel,
          index: i,
          x: px(r.left - originX),
          y: px(r.top - originY),
          w: px(r.width),
          h: px(r.height),
          fontSize: s.fontSize,
          fontWeight: s.fontWeight,
          lineHeight: s.lineHeight,
          color: s.color,
          background: s.backgroundColor,
          radius: s.borderRadius,
          padding: s.padding,
          gap: s.gap,
          text: (el.textContent || '').trim().slice(0, 24),
        });
      });
    }
    return {
      phone: { w: px(pr.width - bw * 2), h: px(pr.height - bw * 2), border: bw },
      elements: out,
    };
    },
    [selectors, process.env.LEAF === '1']
  );

  await browser.close();
  const res = { page, theme, ...data };
  if (process.env.OUT) fs.writeFileSync(process.env.OUT, JSON.stringify(res, null, 2));
  console.log(JSON.stringify(res, null, 2));
})();