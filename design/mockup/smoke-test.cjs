// Optional smoke test for the DianMao HTML mockup.
// Requires Playwright; point PLAYWRIGHT_MODULE at an installed module if needed.
// Run: PLAYWRIGHT_MODULE="C:/Users/hanhu/Code/Collection_of_wedding_photos/node_modules/playwright" node smoke-test.cjs
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const { pathToFileURL } = require('node:url');
const path = require('node:path');
const assert = require('node:assert/strict');

(async () => {
  const browser = await chromium.launch({ channel: 'chrome', headless: true });
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
  const errors = [];
  page.on('pageerror', e => errors.push(e.message));
  page.on('console', m => { if (m.type() === 'error') errors.push('console: ' + m.text()); });

  await page.goto(pathToFileURL(path.join(__dirname, 'index.html')).href);
  await page.waitForTimeout(150);

  const click = async (sel) => { await page.locator(sel).first().click(); await page.waitForTimeout(60); };
  const headerText = () => page.locator('#appHeader').innerText();
  const goChapter = async (p) => click(`#chapterNav [data-page="${p}"]`);

  // 1. home renders with header, pills, switcher, board
  assert.match(await headerText(), /DianMao/);
  assert.equal(await page.locator('.qboard .qpane').count(), 4, 'four quadrant panes');
  assert.equal(await page.locator('.lane-row').count(), 4, 'time focus lane rows');
  assert.equal(await page.locator('.pill').count(), 3, 'summary pills');
  assert.equal(await page.locator('#fab:not(.hidden)').count(), 1, 'FAB visible on home');
  await page.screenshot({ path: path.join(__dirname, 'preview-desktop.png'), fullPage: true });

  // 2. smart view switching
  await click('#appHeader [data-action="home-view"][data-view="today"]');
  assert.equal(await page.locator('.group').count(), 2, 'today groups: overdue + today');
  await click('#appHeader [data-action="home-view"][data-view="upcoming"]');
  assert.ok(await page.locator('.group').count() >= 1, 'upcoming has groups');
  await click('#appHeader [data-action="home-view"][data-view="quadrants"]');

  // 3. search filters
  await click('#appHeader [data-action="search"]');
  await page.fill('#searchInput', '快递');
  await page.waitForTimeout(80);
  assert.equal(await page.locator('.qtask').count(), 1, 'search narrows board to one task');
  await page.fill('#searchInput', '不存在的任务');
  await page.waitForTimeout(80);
  assert.match(await page.locator('.qboard').innerText(), /没有匹配/);
  await page.fill('#searchInput', '');
  await click('#appHeader [data-action="search"]');

  // 4. quadrant detail
  await click('.qpane[data-q="in"]');
  assert.match(await headerText(), /重要不紧急/);
  assert.match(await headerText(), /未完成/);
  assert.equal(await page.locator('#content .collapse').count() >= 1, true, 'completed group present');
  await click('.collapse-head[data-key="qd-done"]');
  assert.equal(await page.locator('#content .collapse.open').count(), 1, 'completed group expands');
  await click('#content [data-action="open"]');

  // 5. task detail
  assert.match(await headerText(), /事项详情/);
  assert.equal(await page.locator('#content .meta-row').count() >= 5, true, 'verb-value meta rows');
  await click('.collapse-head[data-key="history"]');
  assert.ok(await page.locator('#content .collapse.open').count() >= 1, 'history expands');
  await click('#content [data-action="toggle"]');
  await page.waitForTimeout(60);

  // 6. add form: edits, priority switch, dependency gating, save
  await goChapter('add');
  assert.match(await headerText(), /新增事项/);
  await page.fill('#formTitle', '测试新增事项');
  await click('#content [data-action="form-q"][data-q="nn"]');
  // alarm is disabled until calendar sync is on
  const alarmRow = page.locator('#content .set-row').filter({ hasText: '开启日历响铃' });
  await click('#content .set-row:has-text("同步到系统日历") .switch');
  await click('#content [data-action="quick-date"][data-offset="0"]');
  const dateRowText = await page.locator('#content .set-row').first().innerText();
  assert.match(dateRowText, /月.*日/);
  await click('#content [data-action="form-sub-add"]');
  await click('#content [data-action="form-save"]');
  await page.waitForTimeout(80);
  assert.match(await headerText(), /DianMao/, 'back on home after save');
  assert.match(await page.locator('#content').innerText(), /测试新增事项/, 'new task appears on home');

  // 7. calendar
  await goChapter('calendar');
  assert.equal(await page.locator('.cal-weekrow').count() >= 5, true, 'calendar grid rows');
  assert.equal(await page.locator('.cal-cell').count(), 35, '5 rows x 7');
  const month0 = await page.locator('.mtitle').innerText();
  await click('#content [data-action="cal-next"]');
  assert.notEqual(await page.locator('.mtitle').innerText(), month0, 'month advances');
  await click('#content [data-action="cal-today"]');
  assert.match(await page.locator('.mtitle').innerText(), /2026年10月/);

  // 8. review
  await goChapter('review');
  assert.equal(await page.locator('.bar-row').count(), 7, '7-day distribution');
  assert.equal(await page.locator('.share-row').count(), 4, 'quadrant share rows');

  // 9. backup
  await goChapter('backup');
  assert.match(await page.locator('.sync-head').innerText(), /已连接/);
  const passType0 = await page.locator('#davPass').getAttribute('type');
  await click('#content [data-action="toggle-pass"]');
  const passType1 = await page.locator('#davPass').getAttribute('type');
  assert.notEqual(passType0, passType1, 'password visibility toggles');
  assert.equal(await page.locator('[data-action="upload"]').isDisabled(), false, 'upload enabled when configured');
  await click('#content [data-action="toggle-auto"]');

  // 10. archive
  await goChapter('archive');
  const archCount = await page.locator('.arch-row').count();
  assert.ok(archCount >= 1, 'archived rows');
  await click('#content [data-action="unarchive"]');
  await page.waitForTimeout(60);
  assert.equal(await page.locator('.arch-row').count(), archCount - 1, 'unarchive removes row');

  // 10b. custom reminder time: steppers + free-form input
  await goChapter('add');
  await click('#content [data-action="quick-date"][data-offset="0"]');
  await click('#content [data-action="pick-time"]');
  assert.equal(await page.locator('#overlay:not([hidden]) .stepper').count(), 2, 'hour + minute steppers');
  const h0 = await page.locator('#timeH').innerText();
  await click('#overlay [data-action="time-step"][data-unit="h"][data-delta="1"]');
  assert.notEqual(await page.locator('#timeH').innerText(), h0, 'hour stepper changes value');
  await page.fill('#timeInput', '07:45');
  await click('#overlay [data-action="time-apply"]');
  await page.waitForTimeout(60);
  assert.match(await page.locator('#content').innerText(), /07:45/, 'custom time applied to form');
  // invalid input is rejected and keeps the dialog open
  await click('#content [data-action="pick-time"]');
  await page.fill('#timeInput', '99:99');
  await click('#overlay [data-action="time-apply"]');
  await page.waitForTimeout(60);
  assert.equal(await page.locator('#overlay:not([hidden])').count(), 1, 'invalid time keeps dialog open');
  await click('.overlay-card .sheet-heading [data-action="close"]');
  assert.equal(await page.locator('#overlay[hidden]').count(), 1, 'dialog closes');

  // 10c. overlay (dialog) stays inside the phone frame
  await click('#content [data-action="pick-date"]');
  await page.waitForTimeout(80);
  const contained = await page.evaluate(() => {
    const card = document.querySelector('#overlay .overlay-card');
    const phone = document.querySelector('#phone');
    const c = card.getBoundingClientRect(), p = phone.getBoundingClientRect();
    return { inside: c.top >= p.top - 1 && c.bottom <= p.bottom + 1 && c.left >= p.left - 1 && c.right <= p.right + 1,
             dialog: { top: Math.round(c.top), bottom: Math.round(c.bottom) }, phone: { top: Math.round(p.top), bottom: Math.round(p.bottom) } };
  });
  assert.equal(contained.inside, true, `dialog stays inside phone (${JSON.stringify(contained)})`);
  await page.screenshot({ path: path.join(__dirname, 'preview-date-dialog.png') });
  await click('.overlay-card .sheet-heading [data-action="close"]');

  // 10d. AppHeader settings icon opens backup, not a theme toggle
  await goChapter('home');
  const settingsBtnIcon = await page.evaluate(() => document.querySelector('#appHeader [data-page="backup"] use').getAttribute('href'));
  assert.equal(settingsBtnIcon, '#i-settings', 'header settings button uses the settings icon');
  await click('#appHeader [data-page="backup"]');
  assert.match(await headerText(), /备份与恢复/, 'header settings icon opens backup');

  // 11. dialogs chapter
  await goChapter('dialogs');
  assert.equal(await page.locator('.dialog-preview .dlg').count(), 3, 'three dialog examples');

  // 12. dark theme
  await click('#themeToggle');
  assert.equal(await page.evaluate(() => document.documentElement.dataset.theme), 'dark');
  await page.screenshot({ path: path.join(__dirname, 'preview-dark.png'), fullPage: true });

  // 13. mobile full-screen breakpoint
  await page.setViewportSize({ width: 390, height: 844 });
  await page.waitForTimeout(80);
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  assert.ok(overflow <= 1, `no horizontal overflow on mobile (${overflow}px)`);
  await page.screenshot({ path: path.join(__dirname, 'preview-mobile.png'), fullPage: true });

  assert.deepEqual(errors, [], 'no page errors');
  await browser.close();
  console.log('✓ smoke test passed: all mockup screens, interactions and themes render cleanly');
})().catch(e => { console.error('✗ smoke test failed:', e.message); process.exit(1); });