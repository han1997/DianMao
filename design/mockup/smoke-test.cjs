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

  // 10b. custom reminder time: M3 TimePicker (dial + keyboard modes)
  await goChapter('add');
  await click('#content [data-action="quick-date"][data-offset="0"]');
  await click('#content [data-action="pick-time"]');
  assert.equal(await page.locator('#overlay:not([hidden]) .m3-tp-dial').count(), 1, 'M3 dial renders');
  const h0 = (await page.locator('.m3-tp-time').innerText()).replace(/\s/g, '');
  await click('#overlay [data-action="tp-hour"][data-hour="7"]');
  assert.notEqual((await page.locator('.m3-tp-time').innerText()).replace(/\s/g, ''), h0, 'hour mark updates time');
  await click('#overlay [data-action="tp-min"][data-min="45"]');
  assert.match((await page.locator('.m3-tp-time').innerText()).replace(/\s/g, ''), /07:45/, 'minute mark updates time');
  // keyboard mode
  await click('#overlay [data-action="tp-mode"][data-mode="input"]');
  assert.equal(await page.locator('#tpInput').count(), 1, 'keyboard input mode');
  await page.fill('#tpInput', '07:15');
  await click('#overlay [data-action="tp-apply"]');
  await page.waitForTimeout(60);
  assert.equal(await page.locator('.m3-tp-dial').count(), 1, 'returns to dial after apply');
  await click('#overlay [data-action="tp-confirm"]');
  await page.waitForTimeout(60);
  assert.match(await page.locator('#content').innerText(), /07:15/, 'custom time applied to form');

  // 10c. M3 dialogs stay inside the phone frame
  await click('#content [data-action="pick-date"]');
  await page.waitForTimeout(80);
  assert.equal(await page.locator('#overlay .m3-datepicker').count(), 1, 'M3 date picker renders');
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

  // 10d. repeat picker: presets + custom weekday editor
  await click('#content [data-action="pick-repeat"]');
  assert.equal(await page.locator('#overlay .m3-check-row').count(), 8, 'eight repeat presets');
  await click('#overlay [data-action="rp-preset"][data-preset="custom"]');
  assert.equal(await page.locator('#overlay .m3-int-btn').count(), 2, 'interval stepper');
  await click('#overlay [data-action="rp-int"][data-delta="1"]');
  assert.equal((await page.locator('#overlay .m3-int-val').innerText()).trim(), '2', 'interval increments');
  await click('#overlay [data-action="rp-day"][data-day="3"]');
  await click('#overlay [data-action="rp-confirm"]');
  await page.waitForTimeout(60);
  assert.match(await page.locator('#content').innerText(), /每 2 周 · 周/, 'custom repeat label applied');

  // 10e. reminder-lead multi-select
  await click('#content .set-row:has-text("开启到点提醒") .switch');
  await click('#content [data-action="pick-lead"]');
  assert.equal(await page.locator('#overlay .m3-check-row').count(), 4, 'four lead options');
  await click('#overlay [data-action="lead-toggle"][data-min="30"]');
  await click('#overlay [data-action="lead-confirm"]');
  await page.waitForTimeout(60);
  assert.match(await page.locator('#content').innerText(), /提前 30 分钟/, 'lead label applied');

  // 10f. multi-select batch operations (long-press works in list/quadrant-detail surfaces)
  await goChapter('home');
  await click('#appHeader [data-action="home-view"][data-view="today"]');
  await page.locator('#content .task-main[data-long="select-enter"]').first().dispatchEvent('pointerdown');
  await page.waitForTimeout(650);
  assert.match(await headerText(), /已选 1 项/, 'long-press enters multi-select');
  await click('#appHeader [data-action="select-all"]');
  assert.ok(await page.locator('#content .task-row.selected').count() > 1, 'select-all selects rows');
  await click('#content [data-action="batch-move"]');
  await click('#overlay [data-action="batch-move-confirm"][data-q="nu"]');
  await page.waitForTimeout(60);
  assert.equal(await page.locator('#content .task-row.selected').count(), 0, 'batch move exits selection');
  await click('#appHeader [data-action="home-view"][data-view="quadrants"]');

  // 10g. AppHeader settings icon opens the settings hub, not a theme toggle
  const settingsBtnIcon = await page.evaluate(() => document.querySelector('#appHeader [data-page="settings"] use').getAttribute('href'));
  assert.equal(settingsBtnIcon, '#i-settings', 'header settings button uses the settings icon');
  await click('#appHeader [data-page="settings"]');
  assert.match(await headerText(), /设置/, 'header settings icon opens settings');

  // 10h. two-level settings: hub rows, appearance page, and back routing
  assert.equal(await page.locator('#content .settings-row').count() >= 6, true, 'settings hub rows');
  await click('#content [data-action="settings-appearance"]');
  assert.match(await headerText(), /主题/, 'appearance page opens');
  assert.equal(await page.locator('#content .choice-row').count(), 3, 'three theme choices on appearance page');
  await click('#content [data-action="theme-mode"][data-mode="dark"]');
  assert.equal(await page.evaluate(() => document.documentElement.dataset.theme), 'dark', 'appearance page applies theme');
  assert.equal(await page.locator('#content .radio.on').count(), 1, 'selected radio reflects mode');
  await click('#appHeader [data-action="settings-back"]');
  assert.match(await headerText(), /设置/, 'back returns to settings hub');
  await click('#content [data-action="settings-backup"]');
  assert.match(await headerText(), /备份与恢复/, 'settings row opens backup');
  await click('#appHeader [data-action="back"]');
  assert.match(await headerText(), /设置/, 'backup back returns to settings');

  // 10i. about sheet: version token rendered via withVersion()
  await click('#content [data-action="settings-about"]');
  assert.equal(await page.locator('#overlay:not([hidden])').count(), 1, 'about sheet opens');
  assert.match(await page.locator('#overlay').innerText(), /关于 DianMao/, 'about sheet title');
  assert.match(await page.locator('#overlay').innerText(), new RegExp('v' + await page.evaluate(() => APP_VERSION)), 'about sheet shows APP_VERSION');
  await click('.overlay-card .sheet-heading [data-action="close"]');
  assert.equal(await page.locator('#overlay:not([hidden])').count(), 0, 'about sheet closes');

  // 11. dialogs chapter
  await goChapter('dialogs');
  assert.equal(await page.locator('.dialog-preview .dlg').count(), 8, 'eight dialog examples');

  // 11b. widget + notification chapter
  await goChapter('widget');
  assert.equal(await page.locator('.wg').count(), 2, 'wide + narrow widget');
  assert.equal(await page.locator('.wg.wide .wg-row').count() > 0, true, 'widget lists today rows');
  assert.equal(await page.locator('.notif').count(), 2, 'plain + recurring notification');
  assert.match(await page.locator('.notif').last().innerText(), /记一次完成/, 'recurring notification label');
  assert.equal(await page.locator('#content [data-action="open-notification-settings"]').count(), 1, 'notification settings row on widget page');
  await click('#content [data-action="open-notification-settings"]');
  assert.match(await page.locator('#toast').innerText(), /已打开系统通知设置/, 'notification settings row toasts');

  // 11d. long-press drag lift in quadrant detail (do this before swipes: swipes mutate the list)
  await goChapter('quadrant');
  await page.waitForTimeout(80);
  // earlier destructive tests (batch move) shuffled quadrants; reset the demo state
  await page.evaluate(() => { state = initial(); state.page = 'quadrant'; state.activeQ = 'iu'; render(); });
  await page.waitForTimeout(40);
  const dragRow = page.locator('#content [data-drag-row]').first();
  const db = await dragRow.boundingBox();
  await page.mouse.move(db.x + db.width / 2, db.y + db.height / 2);
  await page.mouse.down();
  await page.waitForTimeout(650);
  await page.mouse.move(db.x + db.width / 2, db.y + db.height / 2 - 30, { steps: 5 });
  assert.equal(await page.locator('#content .dragging').count() > 0, true, 'long-press lifts row for drag');
  await page.mouse.up();
  await page.waitForTimeout(60);

  // 11c. swipe gestures on home list rows (right = complete, left = delete)
  await goChapter('home');
  await click('#appHeader [data-action="home-view"][data-view="today"]');
  const row = page.locator('#content .swipe').first();
  const rb = await row.boundingBox();
  await page.mouse.move(rb.x + 30, rb.y + rb.height / 2);
  await page.mouse.down();
  await page.mouse.move(rb.x + rb.width - 40, rb.y + rb.height / 2, { steps: 8 });
  await page.mouse.up();
  await page.waitForTimeout(80);
  assert.equal(await page.locator('#overlay:not([hidden])').count(), 0, 'right swipe completes without a dialog');
  // left swipe opens the delete confirm
  const row2 = page.locator('#content .swipe').first();
  const rb2 = await row2.boundingBox();
  await page.mouse.move(rb2.x + rb2.width - 30, rb2.y + rb2.height / 2);
  await page.mouse.down();
  await page.mouse.move(rb2.x + 30, rb2.y + rb2.height / 2, { steps: 8 });
  await page.mouse.up();
  await page.waitForTimeout(80);
  assert.equal(await page.locator('#overlay:not([hidden])').count(), 1, 'left swipe opens delete confirm');
  await click('.overlay-card .sheet-heading [data-action="close"]');
  await click('#appHeader [data-action="home-view"][data-view="quadrants"]');

  // 12. appearance: system / light / dark (three-state, mirrors the in-app 主题 setting)
  await goChapter('settings');
  await click('#content [data-action="settings-appearance"]');
  assert.equal(await page.locator('#content [data-action="theme-mode"]').count(), 3, 'three appearance modes');
  await click('#content [data-action="theme-mode"][data-mode="dark"]');
  assert.equal(await page.evaluate(() => document.documentElement.dataset.theme), 'dark', 'dark mode applies');
  assert.equal(await page.evaluate(() => state.themeMode), 'dark', 'dark mode persists in state');
  await page.screenshot({ path: path.join(__dirname, 'preview-dark.png'), fullPage: true });
  await click('#content [data-action="theme-mode"][data-mode="light"]');
  assert.equal(await page.evaluate(() => document.documentElement.dataset.theme), 'light', 'light mode applies');
  await click('#content [data-action="theme-mode"][data-mode="system"]');
  const expected = await page.evaluate(() => matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
  assert.equal(await page.evaluate(() => document.documentElement.dataset.theme), expected, 'system mode follows the OS preference');
  // toolbar button cycles the same three states
  await click('#themeToggle');
  assert.equal(await page.evaluate(() => state.themeMode), 'light', 'toolbar cycles system → light');
  await click('#themeToggle');
  assert.equal(await page.evaluate(() => state.themeMode), 'dark', 'toolbar cycles light → dark');

  // 13. mobile full-screen breakpoint
  await page.setViewportSize({ width: 390, height: 844 });
  await page.waitForTimeout(80);
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  assert.ok(overflow <= 1, `no horizontal overflow on mobile (${overflow}px)`);
  await page.screenshot({ path: path.join(__dirname, 'preview-mobile.png'), fullPage: true });

  // 13b. zero-task first-run empty state (quadrants + smart views), then restore demo data
  await page.evaluate(() => { state = initial(); state.tasks = []; state.page = 'home'; state.homeView = 'quadrants'; render(); });
  await page.waitForTimeout(60);
  assert.match(await page.locator('#content').innerText(), /创建第一个任务/, 'quadrants empty state headline');
  assert.equal(await page.locator('#content [data-action="add"]').count(), 1, 'empty state add button');
  await page.screenshot({ path: path.join(__dirname, 'preview-empty-quadrants.png'), fullPage: true });
  await page.evaluate(() => { state.homeView = 'today'; render(); });
  await page.waitForTimeout(60);
  assert.match(await page.locator('#content').innerText(), /创建第一个任务/, 'smart list empty state headline');
  await page.evaluate(() => { state = initial(); state.page = 'home'; state.homeView = 'quadrants'; render(); });
  await page.waitForTimeout(60);
  assert.equal(await page.locator('.qboard .qpane').count(), 4, 'demo data restored after empty-state check');

  assert.deepEqual(errors, [], 'no page errors');
  await browser.close();
  console.log('✓ smoke test passed: all mockup screens, interactions and themes render cleanly');
})().catch(e => { console.error('✗ smoke test failed:', e.message); process.exit(1); });