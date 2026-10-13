'use strict';
/* DianMao UI preview — in-memory demo only.
   No calendar access, no notifications, no file or network reads.
   App UI follows DESIGN.md ("The Day Compass"); the surrounding studio /
   stage / notes chrome is only the presentation shell. */

const $ = s => document.querySelector(s);
const icon = (n, c = '') => `<svg class="icon ${c}" aria-hidden="true"><use href="#i-${n}"/></svg>`;
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const itile = (n, tint, small = false) => `<span class="itile${small ? ' sm' : ''}" style="--t:${tint}">${icon(n, small ? 's12' : 's18')}</span>`;
const meta = (n, t, tint = 'var(--secondary)') => `<span class="meta">${itile(n, tint, true)}${esc(t)}</span>`;
const metas = list => list.filter(Boolean).map(x => meta(x[0], x[1], x[2])).join('');
const statdot = (count, label, tint) => `<span class="statdot" style="--c:${tint}"><span class="dot6"></span>${count} ${esc(label)}</span>`;
const btnIcon = (n, tint, cls = '') => `<span class="itile ${cls}" style="--t:${tint};background:color-mix(in srgb,${tint} 12%,transparent)">${icon(n, 's15')}</span>`;

/* App version shown in the settings hero and the about sheet.
   Mirrors `gradle.properties` `app.versionName`; the `bumpVersion` Gradle task
   rewrites this constant in the same run so the prototype never drifts. */
const APP_VERSION = '1.2.4';

/* Markup writes the literal `__APP_VERSION__` placeholder; every render
   substitutes the constant above so the version has a single source. */
const withVersion = html => html.split('__APP_VERSION__').join(APP_VERSION);

const QUAD = {
  iu: { key: 'iu', label: '重要且紧急', short: '重急', desc: '今天必须推进', color: 'var(--q-iu)' },
  in: { key: 'in', label: '重要不紧急', short: '重要', desc: '长期价值任务', color: 'var(--q-in)' },
  nu: { key: 'nu', label: '不重要但紧急', short: '紧急', desc: '有时间压力', color: 'var(--q-nu)' },
  nn: { key: 'nn', label: '不重要不紧急', short: '低压', desc: '可延后处理', color: 'var(--q-nn)' },
};
const QUAD_ORDER = ['iu', 'in', 'nu', 'nn'];

/* ---------- demo clock: anchored to the design-preview date ---------- */
const NOW = new Date(2026, 9, 11, 9, 41); // 2026-10-11 09:41 (Sun)
const DAY = 86400000;
const midnight = d => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
const at = (d, h, m) => new Date(d.getFullYear(), d.getMonth(), d.getDate(), h, m).getTime();
const addDays = (d, n) => { const x = new Date(d); x.setDate(x.getDate() + n); return x; };
const sameDay = (a, b) => midnight(new Date(a)) === midnight(new Date(b));
const WEEK = ['日', '一', '二', '三', '四', '五', '六'];

function initial() {
  const t = (o) => Object.assign({ detail: null, useCalendar: false, useAlarm: false, useLocalReminder: false, repeat: null, subTasks: [], completed: false, completionNote: null, completionCount: 0, archived: false, dueAt: null, dueLabel: null }, o);
  return {
    page: 'home',
    from: null, // where a level-2 settings page was opened from
    homeView: 'quadrants',
    search: false, query: '',
    activeQ: 'iu',
    detailId: null,
    editingId: null,
    selection: [], // selected task ids in multi-select mode
    moveTaskId: null,
    calendar: { year: 2026, month: 9, selected: midnight(NOW) },
    form: null,
    dpMonth: new Date(NOW.getFullYear(), NOW.getMonth(), 1),
    dpSelected: midnight(NOW),
    tp: null,
    rp: null,
    lead: null,
    openGroups: {}, // label -> open (closed = collapsed)
    openHistory: false,
    tasks: [
      t({ id: 1, title: '回复合作方合同邮件', q: 'iu', dueAt: at(addDays(NOW, -1), 17, 0), dueLabel: '昨天 17:00', useAlarm: true, useCalendar: true }),
      t({ id: 2, title: '修复备份恢复的进度提示', q: 'iu', dueAt: at(NOW, 14, 0), dueLabel: '今天 14:00', useAlarm: true, useCalendar: true }),
      t({ id: 3, title: '整理季度 OKR 复盘草稿', q: 'in', detail: '把本季度三个关键结果的进展、偏差和下一步写清楚,周五例会上同步。', dueAt: at(addDays(NOW, 5), 9, 0), dueLabel: '每周五 09:00', useCalendar: true, useAlarm: true, useLocalReminder: true, repeat: '每 2 周', completionCount: 6, completionNote: '上次复盘发现“结果指标”比“动作清单”更能推进,这次先把指标写死再拆动作。', subTasks: [{ title: '列出三个关键结果', done: true }, { title: '补齐偏差原因', done: true }, { title: '写下季度下一步', done: false }, { title: '发送给协作同事', done: false }] }),
      t({ id: 4, title: '读完《卡片笔记》第 3 章', q: 'in' }),
      t({ id: 5, title: '学完线上课程第 2 讲', q: 'in', dueAt: at(addDays(NOW, 3), 20, 0), dueLabel: '周三 20:00' }),
      t({ id: 6, title: '预约牙医复查', q: 'nu', dueAt: at(addDays(NOW, 1), 9, 0), dueLabel: '明天 09:00', useLocalReminder: true, reminderLead: [30], lead: '提前 30 分钟' }),
      t({ id: 7, title: '取快递', q: 'nu', dueAt: at(NOW, 18, 30), dueLabel: '今天 18:30' }),
      t({ id: 8, title: '整理书桌抽屉', q: 'nn' }),
      t({ id: 9, title: '收藏夹清理', q: 'in', completed: true, completionNote: '顺手建了两个分组,以后好找。', completionCount: 1 }),
      t({ id: 10, title: '旧手机数据迁移', q: 'in', completed: true, archived: true, completionCount: 1 }),
      t({ id: 11, title: '提交 Q3 报销单', q: 'iu', completed: true, archived: true, completionNote: '保留发票原件,下月复用时省事。', completionCount: 1 }),
      t({ id: 12, title: '换洗窗帘', q: 'nn', completed: true, archived: true, completionCount: 1 }),
      t({ id: 13, title: '读完《置身事内》第 2 章', q: 'in', completed: true, archived: true, completionNote: '地方政府债务那节值得再读。', completionCount: 1 }),
    ],
    history: [
      { id: 1, taskId: 3, at: at(addDays(NOW, -7), 9, 12), note: '又拖到最后一天,下次把初稿拆到周三。' },
      { id: 2, taskId: 3, at: at(addDays(NOW, -21), 9, 5), note: null },
      { id: 3, taskId: 3, at: at(addDays(NOW, -35), 9, 8), note: null },
    ],
    backup: { url: 'https://dav.example.com/dianmao/', user: 'hanhu', pass: 'correct-horse', visible: false, auto: true },
    themeMode: 'system', // 'system' | 'light' | 'dark' — mirrors a persisted DataStore preference
    review: { last7: 12, last30: 38, daily: [[5, 2], [6, 4], [7, 1], [8, 3], [9, 2], [10, 6], [11, 4]], quad: { iu: 14, in: 9, nu: 10, nn: 5 } },
  };
}
let state = initial();

/* ---------- derived ---------- */
const active = () => state.tasks.filter(t => !t.archived);
const openTasks = () => active().filter(t => !t.completed);
const timedCount = () => openTasks().filter(t => t.useAlarm || t.useCalendar).length;
const doneCount = () => active().filter(t => t.completed).length;
const byId = id => state.tasks.find(t => t.id === +id);
const qTasks = q => active().filter(t => t.q === q);

function timeFocus() {
  const items = [];
  openTasks().filter(t => !state.query || (t.title + (t.detail || '')).toLowerCase().includes(state.query.toLowerCase())).forEach(t => {
    if (t.dueAt == null) return;
    let kind = null;
    if (t.dueAt < midnight(NOW)) kind = 'overdue';
    else if (sameDay(t.dueAt, NOW)) kind = 'today';
    else if (t.dueAt - midnight(NOW) <= 7 * DAY) kind = 'upcoming';
    if (kind) items.push({ t, kind });
  });
  const rank = { overdue: 0, today: 1, upcoming: 2 };
  items.sort((a, b) => rank[a.kind] - rank[b.kind] || a.t.dueAt - b.t.dueAt);
  return items.slice(0, 4);
}

function dayLabel(ms) {
  const d = new Date(ms);
  if (sameDay(d, NOW)) return '今天';
  if (sameDay(d, addDays(NOW, 1))) return '明天';
  return `${d.getMonth() + 1}月${d.getDate()}日 周${WEEK[d.getDay()]}`;
}

/* ---------- shared widgets ---------- */
const cm = (t, attrs = '') => `<span class="cm${t.completed ? ' done' : ''}${attrs ? ' clickable' : ''}" style="--c:${QUAD[t.q].color}" ${attrs}>${icon('check')}</span>`;

function taskMetaList(t, showQuadrant) {
  const list = [];
  if (showQuadrant) list.push(['inventory', QUAD[t.q].short, QUAD[t.q].color]);
  if (t.dueLabel) list.push(['schedule', t.dueLabel]);
  if (t.useCalendar) list.push(['calendar', '系统日历']);
  if (t.useAlarm) list.push(['alarm', '响铃']);
  if (t.useLocalReminder) list.push(['bell', '到点提醒']);
  if (t.repeat) list.push(['repeat', t.repeat]);
  if (t.subTasks.length) list.push(['check-circle', `${t.subTasks.filter(s => s.done).length}/${t.subTasks.length} 子事项`, QUAD[t.q].color]);
  if (t.completionNote) list.push(['edit-note', '有心得', 'var(--primary)']);
  return metas(list);
}

function taskRow(t, o = {}) {
  const showQ = o.showQuadrant !== false;
  const selected = state.selection.includes(t.id);
  const selectMode = state.selection.length > 0;
  const swipable = o.swipe !== false && !selectMode;
  const sub = t.subTasks.length ? `<div class="sub-prog" style="--c:${QUAD[t.q].color}">
      <div class="sp-head"><span>子事项</span><span>${t.subTasks.filter(s => s.done).length}/${t.subTasks.length}</span></div>
      <div class="sp-bar"><i style="width:${Math.round(t.subTasks.filter(s => s.done).length / t.subTasks.length * 100)}%"></i></div>
    </div>` : '';
  const mark = selectMode
    ? `<span class="checkzone" role="checkbox" aria-checked="${selected}">${icon('check-circle', 's20')}</span>`
    : `<button class="checkzone" data-action="toggle" data-id="${t.id}" aria-label="切换完成">${cm(t)}</button>`;
  const longPress = o.longPress || 'select';
  const longAttr = (!selectMode && longPress === 'select') ? 'data-long="select-enter"' : '';
  const card = `<div class="task-row${t.completed ? ' done' : ''}${selected ? ' selected' : ''}">
    ${mark}
    <button class="task-main" data-action="${selectMode ? 'select-toggle' : 'open'}" data-id="${t.id}" ${longAttr}>
      <span class="task-title">${esc(t.title)}</span>
      ${sub}
      <span class="metas">${taskMetaList(t, showQ)}</span>
    </button>
    ${selectMode ? '' : `<button class="icon-button sm more" data-action="menu" data-id="${t.id}" aria-label="更多操作">${icons('more', 's18')}</button>`}
  </div>`;
  if (!swipable) return card;
  // Swipe right → complete silently, swipe left → delete (mainstream to-do).
  return `<div class="swipe" data-swipe data-id="${t.id}" style="--c:${QUAD[t.q].color}"${longPress === 'drag' ? ' data-drag-row="1"' : ''}>
    <div class="swipe-bg swipe-bg-start">${icon('done-all', 's18')}<span>${t.completed ? '标为未完成' : '完成'}</span></div>
    <div class="swipe-bg swipe-bg-end">${icon('delete', 's18')}<span>删除</span></div>
    <div class="swipe-fore">${card}</div>
  </div>`;
}
const icons = (n, c) => `<span class="itile" style="background:transparent">${icon(n, c)}</span>`;

function collapseBox(key, headIcon, label, count, bodyHtml, open) {
  return `<div class="collapse ${open ? 'open' : ''} collapse-box">
    <button class="collapse-head" data-action="collapse" data-key="${key}">
      ${icon(headIcon, 's16')}
      <span class="ct">${label}</span><span class="cc">${count}</span>
      ${icon('chev-down', 's18 chev')}
    </button>
    <div class="collapse-body">${bodyHtml}</div>
  </div>`;
}

/* ---------- page: home ---------- */
function homeHeader() {
  const seg = [['quadrants', '四象限'], ['today', '今天'], ['upcoming', '近期'], ['inbox', '收集箱']]
    .map(([k, l]) => `<button class="seg ${state.homeView === k ? 'on' : ''}" data-action="home-view" data-view="${k}">${l}</button>`).join('');
  return `<div style="display:flex;align-items:center;gap:10px;width:100%">
      <span class="compass"><span class="grid">
        <span class="cell" style="--c:var(--q-iu)"></span><span class="cell" style="--c:var(--q-in)"></span>
        <span class="cell" style="--c:var(--q-nu)"></span><span class="cell" style="--c:var(--q-nn)"></span>
      </span><span class="hub"></span></span>
      <div class="header-title"><h2>DianMao</h2></div>
      <div class="header-actions">
        <button class="icon-button tile" data-action="search" aria-label="搜索">${itile(state.search ? 'close' : 'search', 'var(--on-surface-variant)')}</button>
        <button class="icon-button tile" data-page="calendar" aria-label="日历">${itile('calendar', 'var(--on-surface-variant)')}</button>
        <button class="icon-button tile" data-page="archive" aria-label="归档事项">${itile('archive', 'var(--on-surface-variant)')}</button>
        <button class="icon-button tile" data-page="settings" aria-label="设置">${itile('settings', 'var(--on-surface-variant)')}</button>
      </div>
    </div>
    <div class="pills">
      <span class="pill" style="--c:var(--primary)"><span class="dot6"></span><b>${openTasks().length}</b><span>未完成</span></span>
      <span class="pill" style="--c:var(--secondary)"><span class="dot6"></span><b>${timedCount()}</b><span>定时</span></span>
      <button class="pill" style="--c:var(--q-nn)" data-page="review"><span class="dot6"></span><b>${doneCount()}</b><span>已复盘</span></button>
    </div>
    ${state.search ? `<div class="field focus search-field" style="margin-top:2px"><span class="search-lead">${icon('search', 's18')}</span><input id="searchInput" placeholder="筛选当前视图" value="${esc(state.query)}" autocomplete="off">${state.query ? `<button class="search-clear" data-action="search-clear" aria-label="清除">${icon('close', 's18')}</button>` : ''}</div>` : ''}
    <div class="segmented" style="margin-top:4px">${seg}</div>`;
}

function homeContent() {
  if (state.homeView === 'quadrants') return quadrantsView();
  return smartList(state.homeView);
}

function quadrantsView() {
  const items = timeFocus();
  const lane = items.length ? `<div class="lane">
      <div class="lane-head">
        ${itile('schedule', 'var(--secondary)', false)}
        <div class="tx"><b>时间关注</b><span>过期、今天和最近安排</span></div>
        <span class="lane-count">${items.length} 项</span>
      </div>
      <div class="lane-rows">${items.map(({ t, kind }) => {
        const c = kind === 'overdue' ? 'var(--error)' : 'var(--secondary)';
        const lbl = kind === 'overdue' ? '过期' : kind === 'today' ? '今天' : '近期';
        return `<button class="lane-row" style="--c:${c}" data-action="open" data-id="${t.id}">
          <span class="lane-chip">${lbl}</span>
          <span class="lane-body">
            <span class="lane-task">${esc(t.title)}</span>
            <span class="metas">${metas([[ 'schedule', t.dueLabel ], [ 'inventory', QUAD[t.q].short, QUAD[t.q].color ]])}</span>
          </span>
        </button>`;
      }).join('')}</div>
    </div>` : '';

  const filtered = state.query ? active().filter(t => (t.title + (t.detail || '')).toLowerCase().includes(state.query.toLowerCase())) : null;
  if (filtered && !filtered.length) {
    return `${lane}<div class="qboard"><div class="empty" style="margin:auto">
      ${itile('search', 'var(--on-surface-variant)')}
      <h3>没有匹配“${esc(state.query)}”的事项</h3>
      <p>换个关键词,或关闭搜索查看全部象限。</p></div></div>`;
  }

  if (!active().length && !state.query) {
    return `<div class="qboard"><div class="empty" style="margin:auto">
      ${itile('compass', 'var(--primary)')}
      <h3>创建第一个任务</h3>
      <p>写下第一件事,四象限会帮你决定方向。</p>
      <button class="btn tonal" data-action="add" style="margin-top:14px">${btnIcon('add', 'var(--on-primary-container)')}新增事项</button></div></div>`;
  }

  const pane = q => {
    const all = filtered ? filtered.filter(t => t.q === q) : qTasks(q);
    const vis = all.slice(0, 5);
    const open = all.filter(t => !t.completed).length;
    const body = vis.length ? vis.map(t => `<div class="qtask${t.completed ? ' done' : ''}" style="--q:${QUAD[q].color}">
        ${cm(t, `data-action="toggle" data-id="${t.id}" role="checkbox" aria-checked="${t.completed}" aria-label="切换完成"`)}
        <span class="t">${esc(t.title)}</span>
        ${(t.dueLabel || t.useAlarm || t.useCalendar) ? icon('schedule', 's14 ic') : ''}
      </div>`).join('') + (all.length > vis.length ? `<span class="qmore">另有 ${all.length - vis.length} 项,点开查看</span>` : '')
      : `<div class="qempty">暂无${QUAD[q].short}事项</div>`;
    return `<div class="qpane" style="--q:${QUAD[q].color}" data-action="quadrant" data-q="${q}" role="button" tabindex="0">
      <span class="qpane-head"><span class="tx">
        <span class="qlabel"><span class="qdot"></span>${QUAD[q].label}</span>
        <span class="qdesc">${QUAD[q].desc}</span>
      </span><span class="qcount">${open} 项</span></span>
      ${body}
    </div>`;
  };
  return `${lane}<div class="qboard">
    <div class="qrow">${pane('iu')}<div class="qdiv"></div>${pane('in')}</div>
    <div class="qdiv-h"></div>
    <div class="qrow">${pane('nu')}<div class="qdiv"></div>${pane('nn')}</div>
  </div>`;
}

function smartList(view) {
  let items = openTasks();
  if (state.query) items = items.filter(t => (t.title + (t.detail || '')).toLowerCase().includes(state.query.toLowerCase()));
  if (view === 'today') items = items.filter(t => t.dueAt != null && t.dueAt < midnight(NOW) + DAY);
  if (view === 'upcoming') items = items.filter(t => t.dueAt != null && t.dueAt >= midnight(NOW) + DAY && t.dueAt <= midnight(NOW) + 14 * DAY);
  if (view === 'inbox') items = items.filter(t => t.dueAt == null);

  if (!items.length) {
    if (!openTasks().length && !state.query) {
      return `<div class="empty">${itile('compass', 'var(--primary)')}
        <h3>创建第一个任务</h3><p>写下第一件事,四象限会帮你决定方向。</p>
        <button class="btn tonal" data-action="add" style="margin-top:14px">${btnIcon('add', 'var(--on-primary-container)')}新增事项</button></div>`;
    }
    return `<div class="empty">${itile('inventory', 'var(--on-surface-variant)')}
      <h3>这里还没有事项</h3><p>${state.query ? `没有匹配“${esc(state.query)}”的事项` : '换个视图,或新增一条。'}</p>
      <button class="btn tonal" data-action="add" style="margin-top:14px">${btnIcon('add', 'var(--on-primary-container)')}新增事项</button></div>`;
  }
  if (view === 'inbox') return `<div class="group-body">${items.map(t => taskRow(t)).join('')}</div>`;

  const groups = {};
  items.forEach(t => { const l = dayLabel(t.dueAt); (groups[l] = groups[l] || []).push(t); });
  const order = Object.keys(groups).sort((a, b) => groups[a][0].dueAt - groups[b][0].dueAt);
  return order.map(label => {
    const closed = !!state.openGroups[view + ':' + label];
    return `<div class="group${closed ? ' closed' : ''}" style="margin-bottom:10px">
      <button class="group-head" data-action="group" data-key="${view}:${label}">
        <span class="grow">${label} · ${groups[label].length}</span>${icon(closed ? 'chev-down' : 'chev-up', 's16 chev')}
      </button>
      <div class="group-body">${groups[label].map(t => taskRow(t)).join('')}</div>
    </div>`;
  }).join('');
}

/* ---------- page: quadrant detail ---------- */
function quadrantHeader() {
  const q = QUAD[state.activeQ];
  const open = qTasks(state.activeQ).filter(t => !t.completed).length;
  return `<button class="icon-button tile" data-action="home" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    <span class="qdot" style="--q:${q.color}"></span>
    <div class="header-title"><h2>${q.label}</h2></div>
    <span style="font-size:12px;color:var(--on-surface-variant)">${open} 未完成</span>`;
}
function quadrantContent() {
  const q = QUAD[state.activeQ];
  const all = qTasks(state.activeQ);
  const open = all.filter(t => !t.completed);
  const done = all.filter(t => t.completed);
  const timed = open.filter(t => t.useAlarm || t.useCalendar).length;
  const intro = `<div class="qintro">
      <span class="qd">${q.desc}</span>
      <span class="statdots">
        ${statdot(open.length, '未完成', q.color)}
        ${statdot(timed, '有提醒', 'var(--secondary)')}
        ${statdot(done.length, '已复盘', 'var(--q-nn)')}
      </span>
    </div>`;
  if (!all.length) {
    return `${intro}<div class="form-section accent" style="background:color-mix(in srgb,${q.color} 8%,transparent);box-shadow:inset 0 0 0 1px color-mix(in srgb,var(--outline) 16%,transparent)">
      <div class="sec-head">${itile('inventory', q.color)}<div class="tx"><span class="sec-title">这个象限还没有事项</span></div></div>
      <span class="hint">点右下角新增事项,会默认放入“${q.label}”。</span></div>`;
  }
  return `${intro}
    <div class="section-label">未完成 ${open.length}</div>
    <div class="group-body">${open.map(t => taskRow(t, { showQuadrant: false, longPress: 'drag' })).join('')}</div>
    ${done.length ? collapseBox('qd-done', 'check-circle', '已完成', done.length, done.map(t => taskRow(t, { showQuadrant: false })).join(''), !!state.openGroups['qd-done']) : ''}`;
}

/* ---------- page: task detail ---------- */
function detailHeader() {
  const t = byId(state.detailId);
  return `<button class="icon-button tile" data-action="back" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile('inventory', QUAD[t.q].color)}
    <div class="header-title"><div class="eyeline">${QUAD[t.q].short}</div><h2>事项详情</h2></div>
    <button class="icon-button tile" data-action="edit" data-id="${t.id}" aria-label="编辑">${itile('edit', 'var(--primary)')}</button>`;
}
function detailContent() {
  const t = byId(state.detailId);
  const q = QUAD[t.q];
  const subsDone = t.subTasks.filter(s => s.done).length;
  const metaRow = (n, label, tint = 'var(--secondary)', onClick = '') => `<${onClick ? `button data-action="${onClick}" data-id="${t.id}"` : 'div'} class="meta-row">
      ${itile(n, tint, true)}<span class="mtx">${esc(label)}</span>${onClick ? icon('chev-right', 's18 chev') : ''}
    </${onClick ? 'button' : 'div'}>`;
  const hist = state.history.filter(h => h.taskId === t.id);

  return `<div style="display:flex;align-items:flex-start;gap:10px">
      <button class="checkzone" style="margin:0;padding-top:0;display:grid;place-items:start center" data-action="toggle" data-id="${t.id}" aria-label="${t.completed ? '标记为未完成' : '标记为完成'}">${cm(t)}</button>
      <h1 style="flex:1;font-size:24px;font-weight:600;line-height:1.25;${t.completed ? 'color:var(--on-surface-variant)' : ''}">${esc(t.title)}</h1>
    </div>
    <div style="display:flex;align-items:center;gap:8px;padding-left:58px">
      <span class="qdot" style="--q:${q.color}"></span>
      <span style="font-size:12px;font-weight:500;color:var(--on-surface-variant)">${q.short}</span>
    </div>
    ${t.detail ? `<p style="padding-left:58px;font-size:16px;line-height:24px;color:var(--on-surface-variant)">${esc(t.detail)}</p>` : ''}

    <div class="form-section">
      <div class="sec-head">${itile('inventory', q.color)}<div class="tx"><span class="sec-title">事项信息</span></div></div>
      <div class="meta-list">
        ${metaRow('schedule', t.dueLabel || '添加提醒')}
        ${metaRow('calendar', t.useCalendar ? '已同步系统日历' : '未同步系统日历')}
        ${metaRow('alarm', t.useAlarm ? '响铃提醒已开启' : '未设置响铃')}
        ${metaRow('bell', t.useLocalReminder ? '到点提醒已开启' : '未开启到点提醒')}
        ${metaRow('repeat', t.repeat || '不重复')}
        ${t.completionCount ? metaRow('check-circle', `已完成 ${t.completionCount} 次`, q.color, 'open') : ''}
      </div>
    </div>

    ${t.subTasks.length ? `<div class="stack tight">
      <div style="display:flex;align-items:center"><span style="flex:1;font-size:16px;font-weight:600">子事项</span>
        <span style="font-size:12px;color:var(--on-surface-variant)">已完成 ${subsDone}/${t.subTasks.length}</span></div>
      <div class="block" style="gap:0;padding:8px">
        ${t.subTasks.map((s, i) => `<button class="meta-row" style="min-height:48px;padding-right:6px" data-action="subtask" data-id="${t.id}" data-index="${i}">
          <span class="cm${s.done ? ' done' : ''}" style="--c:${q.color};margin:0;pointer-events:none">${icon('check')}</span>
          <span class="mtx" style="${s.done ? 'color:var(--on-surface-variant)' : ''}">${esc(s.title)}</span></button>`).join('')}
      </div></div>` : ''}

    ${t.useCalendar ? `<button class="btn outlined full" style="padding:11px 14px">${btnIcon('calendar', 'var(--secondary)')}打开系统日历提醒</button>` : ''}

    ${t.completionNote ? `<div class="form-section">
      <div class="sec-head">${itile('edit-note', 'var(--primary)')}<div class="tx"><span class="sec-title">完成心得</span></div></div>
      <p style="font-size:16px;line-height:24px;color:var(--on-surface-variant)">${esc(t.completionNote)}</p></div>` : ''}

    ${(t.repeat || hist.length) ? `<div class="statdots">
      ${statdot(t.completionCount, '总完成次数', q.color)}
      ${statdot(hist.length, '历史记录', 'var(--on-surface-variant)')}
    </div>` : ''}

    ${collapseBox('history', 'history', '完成历史', hist.length,
      hist.length ? hist.map(h => `<div class="task-row" style="flex-direction:column;gap:6px;padding:12px;align-items:stretch">
          <div style="display:flex;align-items:center"><span style="flex:1;font-size:14px;line-height:20px">${stamp(h.at)}</span>
            <button class="icon-button sm" data-action="edit-note" data-id="${t.id}">${icons('edit', 's16')}</button>
            <button class="icon-button sm" data-action="delete-completion" data-id="${h.id}" style="color:var(--error)">${icons('delete', 's16')}</button>
          </div>${h.note ? `<div style="font-size:12px;line-height:16px;color:var(--on-surface-variant)">${esc(h.note)}</div>` : ''}
        </div>`).join('') : `<span class="hint" style="padding:4px">${t.repeat ? '完成循环事项后,这里会记录每次完成' : '暂无完成记录'}</span>`,
      state.openHistory)}

    <div class="stack tight" style="padding-top:4px;margin-top:auto">
      <button class="btn filled full" style="padding:13px 18px" data-action="toggle" data-id="${t.id}">
        ${btnIcon(t.completed ? 'replay' : 'done-all', 'var(--on-primary)')}${t.completed ? '标为未完成' : '完成'}</button>
      <div style="display:flex;align-items:center;gap:10px">
        ${t.completed ? `<button class="btn tonal" style="flex:1" data-action="archive" data-id="${t.id}">${btnIcon('archive', 'var(--on-primary-container)')}归档</button>`
          : `<span class="hint" style="flex:1">删除会先要求确认</span>`}
        <button class="icon-button tile" data-action="delete" data-id="${t.id}" aria-label="删除" style="background:color-mix(in srgb,var(--error) 10%,transparent)">${itile('delete', 'var(--error)')}</button>
      </div>
    </div>`;
}
function stamp(ms) { const d = new Date(ms); return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`; }

/* ---------- page: add / edit form ---------- */
function addHeader() {
  const editing = !!state.editingId;
  return `<button class="icon-button tile" data-action="back" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile(editing ? 'edit' : 'add', 'var(--primary)')}
    <div class="header-title"><div class="eyeline">${editing ? '更新事项内容与安排' : '先记录一件事,稍后再分类整理'}</div><h2>${editing ? '编辑事项' : '新增事项'}</h2></div>
    <button class="icon-button tile" data-action="back" aria-label="关闭">${itile('close', 'var(--on-surface-variant)')}</button>`;
}
function addContent() {
  const f = state.form;
  const hasDate = !!f.dueDate;
  const q = QUAD[f.q];
  const dateText = hasDate ? `${f.dueDate.getMonth() + 1}月${f.dueDate.getDate()}日 周${WEEK[f.dueDate.getDay()]}` : null;
  const setting = (n, title, sub, opts = {}) => `<${opts.disabled ? 'div' : `button data-action="${opts.action || ''}"`} class="set-row${opts.disabled ? ' disabled' : ''}${opts.tint ? '' : ' off'}">
      ${itile(n, opts.tint || 'color-mix(in srgb,var(--on-surface-variant) 62%,transparent)', true)}
      <span class="tx"><span class="st">${esc(title)}</span><span class="ss">${esc(sub)}</span></span>
      ${opts.trailing === 'switch'
        ? `<span class="switch${opts.on ? ' on' : ''}" data-action="form-switch" data-field="${opts.field}" role="switch" aria-checked="${!!opts.on}"><span class="thumb"></span></span>`
        : icon('chev-right', 's18 chev')}
    </${opts.disabled ? 'div' : 'button'}>`;
  const chip = (l, on, action, data) => `<button class="lane-chip" data-action="${action}" ${data} style="--c:var(--primary);${on ? 'background:var(--primary-container);color:var(--on-primary-container)' : ''}">${l}</button>`;

  return `<div class="field focus"><input id="formTitle" class="fval title" placeholder="要记录什么?" value="${esc(f.title)}" autocomplete="off"></div>
    <div class="field"><textarea id="formDetail" placeholder="补充说明(可选)" rows="2">${esc(f.detail || '')}</textarea></div>

    <div class="form-section">
      <div class="sec-head">${itile('inventory', q.color)}<div class="tx"><span class="sec-title">优先级</span><span class="sec-sub">放入四象限,决定它的颜色与归类</span></div></div>
      <div class="segmented">${QUAD_ORDER.map(k => `<button class="seg ${f.q === k ? 'on' : ''}" data-action="form-q" data-q="${k}">
        <span class="qdot q8" style="--q:${QUAD[k].color}"></span>${QUAD[k].short}</button>`).join('')}</div>
    </div>

    <div class="form-section">
      <div class="sec-head">${itile('schedule', 'var(--secondary)')}<div class="tx"><span class="sec-title">时间与提醒</span><span class="sec-sub">到期日决定时间、日历、响铃与重复是否可用</span></div></div>
      ${setting('calendar', dateText || '添加到期日', dateText ? '点按修改到期日' : '为该事项选一天', { action: 'pick-date', tint: hasDate ? 'var(--secondary)' : null, disabled: false })}
      <div style="display:flex;gap:8px;padding-left:4px">
        ${chip('今天', hasDate && sameDay(f.dueDate, NOW), 'quick-date', `data-offset="0"`)}
        ${chip('明天', hasDate && sameDay(f.dueDate, addDays(NOW, 1)), 'quick-date', `data-offset="1"`)}
        ${chip('下周一', false, 'quick-date', `data-offset="2"`)}
      </div>
      <div class="set-div"></div>
      ${setting('schedule', f.dueTime || '添加提醒时间', f.dueTime ? '点按修改提醒时间' : (hasDate ? '选择当天提醒的几点几分' : '需先设置到期日'), { action: 'pick-time', tint: f.dueTime ? 'var(--secondary)' : null, disabled: !hasDate })}
      <div class="set-div"></div>
      ${setting('calendar', f.useCalendar ? '已同步到系统日历' : '同步到系统日历', f.useCalendar ? '到期日会写入系统日历' : '在系统日历中创建日程', { trailing: 'switch', field: 'useCalendar', on: f.useCalendar, disabled: !hasDate, tint: f.useCalendar ? 'var(--secondary)' : 'color-mix(in srgb,var(--on-surface-variant) 62%,transparent)' })}
      <div class="set-div"></div>
      ${setting('alarm', f.useAlarm ? '日历响铃已开启' : '开启日历响铃', f.useAlarm ? '日程到点在系统日历响铃' : (f.useCalendar ? '开启后系统日历日程带响铃' : '需先同步到系统日历'), { trailing: 'switch', field: 'useAlarm', on: f.useAlarm, disabled: !(hasDate && f.useCalendar), tint: f.useAlarm ? 'var(--secondary)' : null })}
      <div class="set-div"></div>
      ${setting('bell', f.useLocalReminder ? '到点提醒已开启' : '开启到点提醒', f.useLocalReminder ? (f.reminderLead && f.reminderLead.length ? `已开启 ${f.reminderLead.length} 个提醒时点` : '准时提醒我') : '在 DianMao 内发出通知,不依赖日历', { trailing: 'switch', field: 'useLocalReminder', on: f.useLocalReminder, disabled: !hasDate, tint: f.useLocalReminder ? 'var(--secondary)' : null })}
      <div class="set-div"></div>
      ${setting('schedule', f.lead || '准时提醒', f.useLocalReminder ? '点按选择多个提醒提前量' : '需先开启到点提醒', { action: 'pick-lead', tint: f.useLocalReminder ? 'var(--secondary)' : null, disabled: !(hasDate && f.useLocalReminder) })}
      <div class="set-div"></div>
      ${setting('repeat', f.repeat || '不重复', f.repeat ? '点按修改重复规则' : (hasDate ? '可设为每天、工作日、每年或自定义' : '需先设置到期日'), { action: 'pick-repeat', tint: f.repeat ? 'var(--secondary)' : null, disabled: !hasDate })}
    </div>

    ${f.feedback ? `<div class="save-pill">${itile('check', 'var(--on-primary-container)', true)}${esc(f.feedback)}</div>` : ''}

    <div class="form-section">
      <div class="sec-head">${itile('check-circle', 'var(--primary)')}<div class="tx"><span class="sec-title">子事项</span><span class="sec-sub">${f.subTasks.length ? `${f.subTasks.filter(s => s.done).length}/${f.subTasks.length} 已完成` : '可选'}</span></div></div>
      <div style="display:flex;gap:10px;align-items:center">
        <div class="field" style="flex:1"><input id="formSub" placeholder="添加一个小任务" autocomplete="off"></div>
        <button class="btn tonal" data-action="form-sub-add">${btnIcon('add', 'var(--on-primary-container)')}添加</button>
      </div>
      ${f.subTasks.length ? `<div class="stack tight">${f.subTasks.map((s, i) => `<div class="task-row" style="padding:6px;align-items:center;gap:6px">
        <span class="cm${s.done ? ' done' : ''} clickable" style="--c:var(--primary);margin-left:6px" data-action="form-sub-toggle" data-index="${i}" role="checkbox" aria-checked="${s.done}">${icon('check')}</span>
        <span style="flex:1;font-size:14px;line-height:20px;${s.done ? 'color:var(--on-surface-variant)' : ''}">${esc(s.title)}</span>
        <button class="icon-button sm" data-action="form-sub-del" data-index="${i}" style="color:var(--error)">${icons('delete', 's16')}</button>
      </div>`).join('')}</div>` : `<span class="hint">还没有子事项,可在上方添加</span>`}
    </div>

    <button class="btn filled full" style="padding:13px 18px;min-height:50px" data-action="form-save">
      ${btnIcon('check', 'var(--on-primary)')}${state.editingId ? '保存修改并关闭' : '保存并关闭'}</button>`;
}

/* ---------- page: calendar ---------- */
function calendarHeader() {
  return `<button class="icon-button tile" data-action="back" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile('calendar', 'var(--primary)')}
    <div class="header-title"><div class="eyeline">按月浏览事项</div><h2>日历</h2></div>`;
}
function calendarContent() {
  const c = state.calendar;
  const monthStart = new Date(c.year, c.month, 1);
  const startPad = (monthStart.getDay() + 6) % 7; // Monday-first
  const cells = [];
  for (let i = 0; i < startPad; i++) cells.push({ date: new Date(c.year, c.month, 1 - (startPad - i)), out: true });
  const daysInMonth = new Date(c.year, c.month + 1, 0).getDate();
  for (let d = 1; d <= daysInMonth; d++) cells.push({ date: new Date(c.year, c.month, d), out: false });
  while (cells.length % 7 !== 0) cells.push({ date: new Date(c.year, c.month + 1, cells.length - startPad - daysInMonth + 1), out: true });

  const dayTasks = ms => active().filter(t => t.dueAt != null && sameDay(t.dueAt, new Date(ms)));
  const weeks = [];
  for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7));
  const grid = weeks.map(w => `<div class="cal-weekrow">${w.map(cell => {
    const ms = midnight(cell.date);
    const list = dayTasks(ms);
    const colors = [...new Set(list.map(t => QUAD[t.q].color))].slice(0, 3);
    const extra = new Set(list.map(t => QUAD[t.q].color)).size - colors.length;
    const sel = ms === c.selected;
    return `<button class="cal-cell${cell.out ? ' out' : ''}${sameDay(cell.date, NOW) ? ' today' : ''}${sel ? ' sel' : ''}" data-action="cal-pick" data-day="${ms}">
      <span class="dn">${cell.date.getDate()}</span>
      <span class="dd">${colors.map(x => `<i style="--c:${x}"></i>`).join('')}${extra > 0 ? `<b>+${extra}</b>` : ''}</span>
    </button>`;
  }).join('')}</div>`).join('');

  const selected = new Date(c.selected);
  const list = dayTasks(c.selected);
  return `<div class="cal-month">
      <button class="icon-button" data-action="cal-prev" aria-label="上个月">${icon('chev-left')}</button>
      <span class="mtitle">${c.year}年${c.month + 1}月</span>
      <button class="icon-button" data-action="cal-next" aria-label="下个月">${icon('chev-right')}</button>
      <button class="text-button" data-action="cal-today">今天</button>
    </div>
    <div class="cal-week">${['一', '二', '三', '四', '五', '六', '日'].map(x => `<span>${x}</span>`).join('')}</div>
    <div>${grid}</div>
    <div class="cal-daylist">
      <div class="dh"><b>${selected.getMonth() + 1}月${selected.getDate()}日 · ${list.filter(t => !t.completed).length} 项待办</b>
        <button class="text-button" data-action="cal-add">${icon('add', 's16')}记一笔</button></div>
      ${list.length ? list.map(t => `<div class="cal-task">
          <button class="checkzone" data-action="toggle" data-id="${t.id}" aria-label="切换完成">${cm(t)}</button>
          <button class="t" data-action="open" data-id="${t.id}" data-long="move-to-date" style="border:0;background:none;text-align:left;font:inherit;color:inherit;cursor:pointer">${esc(t.title)}</button>
          ${t.dueLabel ? `<span class="due">${esc(t.dueLabel.split(' ').pop())}</span>` : ''}
        </div>`).join('') : `<span class="hint" style="padding:4px">当天没有事项</span>`}
    </div>`;
}

/* ---------- page: review ---------- */
function reviewHeader() {
  return `<button class="icon-button tile" data-action="back" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile('chart', 'var(--primary)')}
    <div class="header-title"><div class="eyeline">完成回顾与心得</div><h2>复盘</h2></div>`;
}
function reviewContent() {
  const r = state.review;
  const max = Math.max(...r.daily.map(d => d[1]));
  const total = Object.values(r.quad).reduce((a, b) => a + b, 0);
  const events = state.tasks.filter(t => t.completed && t.completionNote).slice(0, 3);
  return `<div class="block">
      <div class="sec-head">${itile('chart', 'var(--primary)', true)}<div class="tx"><span class="sec-title sm">完成概览</span><span class="sec-sub">按本地时间统计完成次数</span></div></div>
      <div style="display:flex;gap:16px">${statdot(r.last7, '近 7 天', 'var(--primary)')}${statdot(r.last30, '近 30 天', 'var(--secondary)')}</div>
    </div>
    <div class="block">
      <div class="sec-head">${itile('chart', 'var(--primary)', true)}<div class="tx"><span class="sec-title sm">近 7 天每日分布</span></div></div>
      <div style="display:flex;flex-direction:column;gap:6px">
        ${r.daily.map(([d, n]) => `<div class="bar-row">
          <span class="bl${d === 11 ? ' today' : ''}">${d === 11 ? '今天' : `10/${d}`}</span>
          <span class="bar-track"><i style="width:${Math.round(n / max * 100)}%"></i></span>
          <span class="bc">${n}</span></div>`).join('')}
      </div>
    </div>
    <div class="block">
      <div class="sec-head">${itile('chart', 'var(--primary)', true)}<div class="tx"><span class="sec-title sm">四象限占比</span><span class="sec-sub">近 30 天各象限完成次数</span></div></div>
      <div style="display:flex;flex-direction:column;gap:8px">
        ${QUAD_ORDER.map(k => { const n = r.quad[k]; const pct = Math.round(n / total * 100);
          return `<div class="share-row" style="--c:${QUAD[k].color}"><span class="qdot" style="--q:${QUAD[k].color}"></span>
            <span class="sl">${QUAD[k].short}</span><span class="share-track"><i style="width:${pct}%"></i></span>
            <span class="sv">${n} 次 · ${pct}%</span></div>`; }).join('')}
      </div>
    </div>
    <div class="block">
      <div class="sec-head">${itile('chart', 'var(--primary)', true)}<div class="tx"><span class="sec-title sm">最近完成</span><span class="sec-sub">最新 ${events.length} 条记录</span></div></div>
      <div style="display:flex;flex-direction:column;gap:10px">
        ${events.length ? events.map((t, i) => `<div class="event" style="--c:${QUAD[t.q].color}"><span class="qdot" style="--q:${QUAD[t.q].color}"></span>
          <span class="tx"><span class="et">${esc(t.title)}</span><span class="ed">${['今天 10:24', '昨天 22:10', '9月28日 20:15'][i] || '已完成'}</span>
          ${t.completionNote ? `<span class="en">${esc(t.completionNote)}</span>` : ''}</span></div>`).join('')
          : `<span class="hint">暂无完成记录</span>`}
      </div>
    </div>`;
}

/* ---------- page: backup ---------- */
/* ---------- page: settings (level 1) ---------- */
function settingsHeader() {
  return `<button class="icon-button tile" data-action="back" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile('settings', 'var(--primary)')}
    <div class="header-title"><div class="eyeline">偏好与数据</div><h2>设置</h2></div>`;
}
const THEME_MODE_LABEL = { system: '跟随系统', light: '浅色', dark: '深色' };
function settingsContent() {
  const b = state.backup;
  const host = b.url.trim() ? (b.url.match(/^https?:\/\/([^/]+)/) || [, ''])[1] : null;
  const doneCount = active().filter(t => t.completed).length;
  const group = (title, rows) => `<div class="section-label" style="padding:0 4px 6px">${title}</div><div class="settings-group">${rows}</div>`;
  const row = (a, ic, tint, title, sub, trailing) => `<button class="settings-row" data-action="${a}">
      ${itile(ic, tint)}<span class="sr-tx"><b>${title}</b><span>${sub}</span></span>${trailing || icon('chev-right', 's18 set-chev')}</button>`;
  const tag = (label, tone = '') => `<span class="set-tag${tone ? ' ' + tone : ''}">${label}</span>`;
  return `
    <div class="settings-hero">
      <span class="brand-mark">D</span>
      <div><h2>DianMao</h2><span class="hint">v__APP_VERSION__ · The Day Compass</span></div>
    </div>
    ${group('外观', row('settings-appearance', 'moon', 'var(--primary)', '主题', '浅色、深色或跟随系统', tag(THEME_MODE_LABEL[state.themeMode])) + row('settings-widget', 'inventory', 'var(--secondary)', '小组件与通知', '桌面小组件与到点提醒样式', ''))}
    ${group('数据', row('settings-backup', 'cloud-up', 'var(--secondary)', '备份与恢复', host ? `${host} · 已连接` : '本地导出与 WebDAV 同步', tag(host ? '已连接' : '未配置', host ? '' : 'warn')) + row('settings-archive', 'archive', 'var(--q-nn)', '归档事项', `${state.tasks.filter(t => t.archived).length} 项已归档`, '') + row('settings-review', 'chart', 'var(--primary)', '复盘', `${doneCount} 项已完成可复盘`, ''))}
    ${group('关于', row('settings-about', 'compass', 'var(--primary)', '关于 DianMao', '版本、设计语言与隐私说明', ''))}
    <span class="hint" style="padding:4px">所有数据与偏好仅保存在本机;备份由你自行指定目标。</span>`;
}

/* ---------- page: appearance (level 2) ---------- */
function appearanceHeader() {
  return `<button class="icon-button tile" data-action="settings-back" aria-label="返回设置">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile('moon', 'var(--primary)')}
    <div class="header-title"><div class="eyeline">浅色、深色或跟随系统</div><h2>主题</h2></div>`;
}
function appearanceContent() {
  const modes = [['system', '跟随系统', '随系统设置自动切换', 'moon'], ['light', '浅色', '始终使用暖纸亮色主题', 'sun'], ['dark', '深色', '始终使用深暖色主题', 'moon']];
  const list = modes.map(([k, label, sub, ic]) => `<button class="choice-row${state.themeMode === k ? ' on' : ''}" data-action="theme-mode" data-mode="${k}" role="radio" aria-checked="${state.themeMode === k}">
      ${itile(ic, 'var(--primary)')}
      <span class="sr-tx"><b>${label}</b><span>${sub}</span></span>
      <span class="radio${state.themeMode === k ? ' on' : ''}" aria-hidden="true"></span>
    </button>`).join('');
  const preview = `<div class="theme-preview">
      <div class="tp-card dark"><span class="tp-swatch"></span><span class="tp-line"></span><span class="tp-line short"></span></div>
      <div class="tp-card light"><span class="tp-swatch"></span><span class="tp-line"></span><span class="tp-line short"></span></div>
    </div>`;
  return `<div class="settings-group choice-list">${list}</div>
    ${preview}
    <span class="hint" style="padding:0 4px">${state.themeMode === 'system' ? `当前跟随系统:${isDarkNow() ? '深色' : '浅色'}` : `已固定为${THEME_MODE_LABEL[state.themeMode]}主题`}${state.themeMode === 'system' ? ',系统主题变化时自动生效。' : '。切回跟随系统即可自动跟随。'}</span>
    <span class="hint" style="padding:0 4px">主题偏好保存在本机,不随备份上传。</span>`;
}

/* ---------- page: backup (level 2) ---------- */
function backupHeader() {
  return `<button class="icon-button tile" data-action="back" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile('settings', 'var(--primary)')}
    <div class="header-title"><div class="eyeline">本地文件与 WebDAV 同步</div><h2>备份与恢复</h2></div>
    <button class="icon-button tile" data-action="back" aria-label="关闭">${itile('close', 'var(--on-surface-variant)')}</button>`;
}
function backupContent() {
  const b = state.backup;
  const ready = !!b.url.trim();
  return `<div class="sync-head">
      ${itile('cloud-up', 'var(--on-surface-variant)')}
      <div class="tx">
        <span class="h1">${ready ? esc((b.url.match(/^https?:\/\/([^/]+)/) || [, '未配置云端同步'])[1]) : '未配置云端同步'}</span>
        <span class="h2">${ready ? 'WebDAV 云端备份目标 · dianmao-backup-latest.json' : '配置下方地址后可上传与恢复'}</span>
        <span class="chip${ready ? '' : ' muted'}">${ready ? '已连接' : '未配置'}</span>
      </div>
    </div>
    <div class="block">
      <div class="sec-head"><div class="tx"><span class="sec-title sm">本地备份</span><span class="sec-sub">导出当前数据,或从文件恢复</span></div></div>
      <div class="row2">
        ${ready
          ? `<button class="btn outlined" style="padding:11px 14px" data-action="export">${btnIcon('file-up', 'var(--primary)')}导出本地</button>`
          : `<button class="btn filled" style="padding:13px 14px" data-action="export">${btnIcon('file-up', 'var(--on-primary)')}导出本地</button>`}
        <button class="btn outlined" style="padding:11px 14px" data-action="revert-local">${btnIcon('file-down', 'var(--primary)')}本地恢复</button>
      </div>
    </div>
    <div class="block">
      <div class="sec-head"><div class="tx"><span class="sec-title sm">WebDAV 配置</span><span class="sec-sub">填目录地址自动建目录备份;以 .json 结尾则视为文件</span></div></div>
      <div class="fields">
        <div class="field-group">
          <div class="field"><div class="flabel">备份目录 URL</div><input id="davUrl" value="${esc(b.url)}" placeholder="https://dav.example.com/dianmao/"></div>
          <div class="hint" style="padding:6px 0 0 4px">备份写入目录内 dianmao-backup-latest.json;目录不存在会自动创建</div>
        </div>
        <div class="field"><div class="flabel">用户名</div><input id="davUser" value="${esc(b.user)}"></div>
        <div class="field" style="display:flex;align-items:center;gap:8px">
          <div style="flex:1;min-width:0"><div class="flabel">密码</div><input id="davPass" type="${b.visible ? 'text' : 'password'}" value="${esc(b.pass)}"></div>
          <button class="icon-button sm" data-action="toggle-pass" aria-label="显示或隐藏密码" style="color:var(--on-surface-variant)">${icons(b.visible ? 'eye' : 'eye-off', 's18')}</button>
        </div>
      </div>
      <button class="btn text" style="align-self:flex-end" data-action="save-dav">${btnIcon('save', 'var(--primary)')}保存设置</button>
    </div>
    <div class="block">
      <div class="sec-head"><div class="tx"><span class="sec-title sm">云端同步</span><span class="sec-sub">上传当前数据到 WebDAV,或从云端恢复</span></div></div>
      <button class="btn filled full" style="padding:13px 18px" data-action="upload" ${ready ? '' : 'disabled'}>${btnIcon('cloud-up', 'var(--on-primary)')}上传备份</button>
      <button class="btn outlined full" style="padding:11px 14px" data-action="download" ${ready ? '' : 'disabled'}>${btnIcon('cloud-down', 'var(--primary)')}下载恢复</button>
    </div>
    <div class="block">
      <div class="sec-head"><div class="tx"><span class="sec-title sm">自动备份</span><span class="sec-sub">启动应用时自动上传到 WebDAV,最多每天一次</span></div></div>
      <div style="display:flex;align-items:center;gap:12px">
        <div class="tx" style="flex:1"><span style="font-size:16px;line-height:24px">${b.auto ? '自动备份已开启' : '开启自动备份'}</span>
          <span class="hint">${b.auto ? '上次成功:今天 08:30' : '需要先配置 WebDAV'}</span></div>
        <span class="switch${b.auto ? ' on' : ''}" data-action="toggle-auto" role="switch" aria-checked="${b.auto}"><span class="thumb"></span></span>
      </div>
    </div>`;
}

/* ---------- page: archive ---------- */
function archiveHeader() {
  const n = state.tasks.filter(t => t.archived).length;
  return `<button class="icon-button tile" data-action="back" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile('archive', 'var(--primary)')}
    <div class="header-title"><div class="eyeline">查看已归档的旧事项,恢复后回到待办</div><h2>归档事项${n ? ' · ' + n : ''}</h2></div>`;
}
function archiveContent() {
  const list = state.tasks.filter(t => t.archived);
  if (!list.length) return `<div class="empty">${itile('inventory', 'var(--on-surface-variant)')}<h3>暂无归档事项</h3><p>完成并归档的事项会出现在这里。</p></div>`;
  return list.map(t => `<div class="arch-row">
      <span class="qdot" style="--q:${QUAD[t.q].color}"></span>
      <div class="tx"><span class="at">${esc(t.title)}</span>
        <span class="am">${[QUAD[t.q].short, t.dueLabel, t.completionNote].filter(Boolean).map(esc).join(' · ')}</span></div>
      <button class="icon-button tile" data-action="unarchive" data-id="${t.id}" aria-label="取消归档" style="background:color-mix(in srgb,var(--primary-container) 50%,transparent)">${itile('unarchive', 'var(--primary)')}</button>
    </div>`).join('');
}

/* ---------- page: dialogs showcase ---------- */
function dialogsHeader() {
  return `<button class="icon-button tile" data-action="back" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile('edit-note', 'var(--primary)')}
    <div class="header-title"><div class="eyeline">同一 18dp 圆角卡 + 暖纸 scrim + 静默边框 + 6dp 阴影</div><h2>对话框</h2></div>`;
}

/* ---------- page: widget + notifications ---------- */
function widgetHeader() {
  return `<button class="icon-button tile" data-action="back" aria-label="返回">${itile('arrow-back', 'var(--on-surface-variant)')}</button>
    ${itile('inventory', 'var(--primary)')}
    <div class="header-title"><div class="eyeline">桌面小组件与到点提醒通知</div><h2>小组件与通知</h2></div>`;
}
function widgetContent() {
  const now = new Date(NOW);
  const today = openTasks().filter(t => t.dueAt != null && (t.dueAt < midnight(NOW) + DAY)).slice(0, 8);
  const rows = (limit) => today.slice(0, limit).map(t => `<div class="wg-row">
      <button class="wg-check" data-action="widget-toggle" data-id="${t.id}" aria-label="完成「${esc(t.title)}」">${icon('check-circle', 's20')}</button>
      <span class="wg-title">${esc(t.title)}</span>
      ${t.dueLabel ? `<span class="wg-due">${esc(t.dueLabel.split(' ').pop())}</span>` : ''}
    </div>`).join('');
  const widget = (limit, cls) => `<div class="wg ${cls}">
      <div class="wg-head"><span>DianMao · 今天</span><span class="wg-count">${Math.min(limit, today.length)}</span></div>
      ${today.length ? rows(limit) : `<div class="wg-empty">今天没有待办</div>`}
      <div class="wg-add" data-action="add">+ 记一笔</div>
    </div>`;
  const notif = (recurring) => `<div class="notif">
      <div class="notif-head">${itile('bell', 'var(--primary)', true)}<span>DianMao 提醒</span><span class="notif-time">现在</span></div>
      <div class="notif-title">${esc(today[0] ? today[0].title : '整理季度 OKR 复盘草稿')}</div>
      <div class="notif-actions">
        <button class="notif-btn">${icon('done-all', 's18')}${recurring ? '记一次完成' : '完成'}</button>
        <button class="notif-btn">${icon('schedule', 's18')}稍后提醒</button>
      </div>
    </div>`;
  return `<div class="stack">
      <div class="section-label">宽小组件 · 最多 8 条</div>
      ${widget(8, 'wide')}
      <div class="section-label">窄小组件 · 最多 4 条</div>
      <div style="display:flex;justify-content:center">${widget(4, 'narrow')}</div>
      <div class="section-label">到点提醒通知</div>
      ${notif(false)}
      <div class="section-label">重复事项提醒 · 完成文案变为「记一次完成」</div>
      ${notif(true)}
      <div class="section-label" style="padding:0 4px 6px">系统</div>
      <div class="settings-group"><button class="settings-row" data-action="open-notification-settings">
        ${itile('bell', 'var(--primary)')}<span class="sr-tx"><b>通知设置</b><span>打开系统的应用通知设置</span></span>${icon('chev-right', 's18 set-chev')}</button></div>
      <span class="hint">小组件与通知均为演示:勾选/按钮不会真正修改数据或发送通知。</span>
    </div>`;
}
function dialogsContent() {
  const preview = (inner, h) => `<div class="dialog-preview"><div class="backdrop"></div><div class="wrap">
    <div class="dlg"><h4>${h}</h4>${inner}</div></div></div>`;
  const dpCell = (d, opts='') => `<span class="m3-dp-cell${opts}">${d}</span>`;
  const datePreview = `<div class="m3-datepicker" style="pointer-events:none">
      <div class="m3-dp-head"><span class="m3-dp-nav">${icon('chev-left','s18')}</span><span class="m3-dp-title">2026年10月</span><span class="m3-dp-nav">${icon('chev-right','s18')}</span></div>
      <div class="m3-dp-week">${['一','二','三','四','五','六','日'].map(x=>`<span>${x}</span>`).join('')}</div>
      <div class="m3-dp-grid">
        <div class="m3-dp-row">${dpCell('')}${dpCell('')}${dpCell('')}${dpCell(1)}${dpCell(2)}${dpCell(3)}${dpCell(4)}</div>
        <div class="m3-dp-row">${dpCell(5)}${dpCell(6)}${dpCell(7)}${dpCell(8)}${dpCell(9)}${dpCell(10)}${dpCell(11,' today sel')}</div>
        <div class="m3-dp-row">${dpCell(12)}${dpCell(13)}${dpCell(14)}${dpCell(15)}${dpCell(16)}${dpCell(17)}${dpCell(18)}</div>
        <div class="m3-dp-row">${dpCell(19)}${dpCell(20)}${dpCell(21)}${dpCell(22)}${dpCell(23)}${dpCell(24)}${dpCell(25)}</div>
      </div>
    </div>
    <div class="dlg-actions"><button class="btn text">取消</button><button class="btn text">确定</button></div>`;
  const checkRow = (label, on=false) => `<span class="m3-check-row${on?' on':''}" style="pointer-events:none"><span class="m3-check">${on?icon('check','s16'):''}</span>${label}</span>`;
  return preview(`<div style="font-size:14px;line-height:20px;color:var(--on-surface-variant)">确定删除“取快递”?删除后不会保留提醒和完成备注。</div>
      <div class="dlg-actions"><button class="btn text">取消</button><button class="btn text" style="color:var(--error)">删除</button></div>`, '删除事项')
    + preview(`<div style="font-size:14px;line-height:20px;color:var(--on-surface-variant)">这是一个重复事项,取消完成后将重新进入待办状态。是否继续?</div>
      <div class="dlg-actions"><button class="btn text">取消</button><button class="btn text">确认取消完成</button></div>`, '取消完成重复事项?')
    + preview(`<div style="font-size:14px;line-height:20px;color:var(--on-surface-variant)">整理季度 OKR 复盘草稿</div>
      <div class="field" style="min-height:88px"><div class="flabel">心得或复盘</div><div class="fph">这件事完成后,我学到或确认了什么?</div></div>
      <div class="dlg-actions"><button class="btn text">稍后再写</button><button class="btn text">保存备注</button></div>`, '完成并写备注')
    + preview(datePreview, '选择到期日')
    + preview(`<div class="m3-repeat-picker" style="pointer-events:none">${checkRow('不重复')}${checkRow('每天')}${checkRow('工作日')}${checkRow('每周')}${checkRow('每月')}${checkRow('每年')}${checkRow('计数')}${checkRow('自定义', true)}</div>
      <div class="dlg-actions"><button class="btn text">取消</button><button class="btn text">确定</button></div>`, '重复规则')
    + preview(`<div class="m3-repeat-picker" style="pointer-events:none">${checkRow('准时提醒', true)}${checkRow('提前 5 分钟', true)}${checkRow('提前 30 分钟')}${checkRow('提前 1 天')}</div>
      <div class="dlg-actions"><button class="btn text">取消</button><button class="btn text">确定</button></div>`, '提醒提前量')
    + preview(`<div class="m3-repeat-picker" style="pointer-events:none"><span class="m3-check-row"><span class="m3-check" style="background:var(--q-iu);border-color:var(--q-iu)"></span>重要且紧急</span><span class="m3-check-row"><span class="m3-check" style="background:var(--q-in);border-color:var(--q-in)"></span>重要不紧急</span><span class="m3-check-row"><span class="m3-check" style="background:var(--q-nu);border-color:var(--q-nu)"></span>不重要但紧急</span><span class="m3-check-row"><span class="m3-check" style="background:var(--q-nn);border-color:var(--q-nn)"></span>不重要不紧急</span></div>
      <div class="dlg-actions"><button class="btn text">取消</button></div>`, '移动到象限')
    + preview(`<div style="font-size:14px;line-height:20px;color:var(--on-surface-variant)">确定删除选中的 3 个事项?删除后不会保留提醒和完成备注。</div>
      <div class="dlg-actions"><button class="btn text">取消</button><button class="btn text" style="color:var(--error)">删除</button></div>`, '批量删除');
}

/* ---------- navigation + notes ---------- */
const PAGES = [['home', '首页'], ['quadrant', '象限详情'], ['detail', '事项详情'], ['add', '新增事项'], ['calendar', '日历'], ['review', '复盘'], ['settings', '设置'], ['backup', '备份'], ['archive', '归档'], ['dialogs', '对话框'], ['widget', '小组件与通知']];
const NOTES = {
  home: ['01', '先捕捉,<br>再分类。', '四象限不是分类表单,而是一眼看清今天该往哪走。时间关注把过期与今天提到最上层。', ['切到「今天 / 近期 / 收集箱」看分组列表', '右滑完成 / 左滑删除任务行', '长按任务进入多选,可全选与批量操作', '点一个象限进入详情']],
  quadrant: ['02', '一个象限,<br>一种节奏。', '象限色只做低透明度底纹(8%),头部与空态用同一层 tint,不抢正文。', ['长按任务行拖拽排序(抬起态)', '点「已完成」展开收起', '点标题进事项详情']],
  detail: ['03', '细节留档,<br>完成有记忆。', '事项信息用「动词变值」的行式表达:未设置时是动作,已设置时是值。完成历史折叠在底部。', ['点「完成」切换状态', '点「完成历史」展开', '点右上角铅笔进入编辑']],
  add: ['04', '记录要快,<br>整理要轻。', '标题默认聚焦;优先级与时间按区块排列,未满足依赖的设置在行内说明原因而不是静默变灰。', ['切换优先级与各开关', '选到期日(M3 日历)/ 提醒时间(M3 表盘)', '选重复规则与提醒提前量', '添加子事项']],
  calendar: ['05', '时间有它<br/>自己的声音。', '日历相关的蓝色只用于时间语义;月格里的点表示当天事项的象限。', ['切换月份', '点某一天看当天列表', '长按任务「移动到...」']],
  review: ['06', '完成之后,<br>留下一点什么。', '统计只用现有事实:近 7/30 天次数、每日分布、象限占比与完成心得。', ['滚动查看各区', '点顶部「已复盘」pill 也能到这里']],
  backup: ['07', '数据在手里,<br>也在云上。', '表单用静默边框,唯一的主按钮属于「上传备份」;未配置时本地导出接管主按钮。', ['切换密码可见性', '开关自动备份', '点「保存设置」']],
  settings: ['07', '先看全局,<br>再进细节。', '一级设置页只列出分组入口:外观、数据、关于。每行一句现状,不堆控件。', ['点「主题」进二级页', '点「备份与恢复」进备份页', '点「小组件与通知」看样式']],
  appearance: ['07', '亮或暗,<br>你说了算。', '二级主题页用单选列表 + 双主题小预览,选择即时生效;跟随系统时自动跟随 OS。', ['切换跟随系统 / 浅色 / 深色', '看下方明暗对比预览']],
  archive: ['08', '旧的完成,<br>不必消失。', '归档行保留象限色点、标题、meta 与一个明确的恢复按钮。', ['点「恢复」把它放回待办']],
  dialogs: ['09', '同一家族,<br>同一套语气。', '所有紧凑对话框共享同一个容器:暖纸 scrim、18dp 圆角卡、发丝级边框与 6dp 阴影。', ['删除 / 取消完成重复 / 完成心得', '日期 / 重复规则 / 提醒提前量', '移动到象限 / 批量删除']],
  widget: ['10', '不打开应用,<br>也知道今天。', '桌面小组件展示今天的待办并可勾选完成;到点提醒的通知带「完成」与「稍后提醒」。', ['看宽/窄两种小组件尺寸', '勾选小组件里的任务(演示)', '看提醒通知的两个动作按钮']],
};
const TITLES = Object.fromEntries(PAGES);
const HEADERS = {
  home: homeHeader, quadrant: quadrantHeader, detail: detailHeader, add: addHeader,
  calendar: calendarHeader, review: reviewHeader, backup: backupHeader, archive: archiveHeader, dialogs: dialogsHeader, widget: widgetHeader,
  settings: settingsHeader, appearance: appearanceHeader,
};
const CONTENTS = {
  home: homeContent, quadrant: quadrantContent, detail: detailContent, add: addContent,
  calendar: calendarContent, review: reviewContent, backup: backupContent, archive: archiveContent, dialogs: dialogsContent, widget: widgetContent,
  settings: settingsContent, appearance: appearanceContent,
};
const SCAFFOLD = { quadrant: 1, detail: 1, add: 1, calendar: 1, review: 1, backup: 1, archive: 1, dialogs: 1, widget: 1, settings: 1, appearance: 1 };

function navigate(page) {
  if (page === 'add' && !state.form) { state.editingId = null; state.form = blankForm('iu'); }
  if (page === 'detail' && state.detailId == null) { state.detailId = openTasks()[0] ? openTasks()[0].id : (active()[0] || {}).id; }
  if (page !== 'backup' && page !== 'archive' && page !== 'review' && page !== 'widget') state.from = null;
  state.page = page;
  if (page !== 'home') state.search = false;
  if (page !== 'home' && page !== 'quadrant') state.selection = [];
  render();
  $('#content').scrollTop = 0;
}

function render() {
  const page = state.page;
  const selecting = state.selection.length > 0 && (page === 'home' || page === 'quadrant');
  if (selecting) {
    $('#appHeader').className = 'app-header scaffold';
    $('#appHeader').innerHTML = `<button class="icon-button tile" data-action="select-exit" aria-label="退出多选">${itile('close', 'var(--on-surface-variant)')}</button>
      <div class="header-title"><h2>已选 ${state.selection.length} 项</h2></div>
      <button class="text-button" data-action="select-all">全选</button>`;
  } else {
    $('#appHeader').className = 'app-header' + (page === 'home' ? ' home' : '') + (SCAFFOLD[page] ? ' scaffold' : '');
    $('#appHeader').innerHTML = HEADERS[page]();
  }
  const body = SCAFFOLD[page] ? `<div class="page-body">${CONTENTS[page]()}</div>` : CONTENTS[page]();
  const bar = selecting ? `<div class="select-bar">
      <button class="btn tonal" style="flex:1" data-action="batch-move">${btnIcon('inventory', 'var(--on-primary-container)')}移动象限</button>
      <button class="btn" style="flex:1;color:var(--error);background:color-mix(in srgb,var(--error) 10%,transparent)" data-action="batch-delete">${btnIcon('delete', 'var(--error)')}删除所选</button>
    </div>` : '';
  $('#content').innerHTML = withVersion(body + bar);
  $('#fab').classList.toggle('hidden', selecting || !(page === 'home' || page === 'quadrant'));

  $('#chapterNav').innerHTML = PAGES.map(([p, n], i) => `<button data-page="${p}" class="${page === p ? 'active' : ''}" ${page === p ? 'aria-current="page"' : ''}><span>0${i + 1}</span>${n}</button>`).join('');
  const note = NOTES[page] || NOTES.home;
  $('#noteNumber').textContent = note[0];
  $('#noteTitle').innerHTML = note[1];
  $('#noteBody').textContent = note[2];
  $('#noteList').innerHTML = note[3].map(x => `<li>${esc(x)}</li>`).join('');
  $('#pageCaption').textContent = TITLES[page];
  if (page === 'home' && state.search) { const el = $('#searchInput'); if (el) el.focus(); }
}

/* ---------- sheet / toast ---------- */
let toastTimer, returnFocus;
function toast(msg) {
  clearTimeout(toastTimer);
  const el = $('#toast'); el.textContent = msg; el.classList.add('visible');
  toastTimer = setTimeout(() => el.classList.remove('visible'), 2600);
}
function sheet(title, body, opts = {}) {
  returnFocus = document.activeElement;
  $('#sheetTitle').textContent = title;
  const iconHost = $('#sheetIcon');
  if (opts.icon) {
    const tint = opts.tint || 'var(--primary)';
    iconHost.innerHTML = `<span class="itile" style="--t:${tint}">${icon(opts.icon, 's18')}</span>`;
    iconHost.style.display = '';
  } else {
    iconHost.innerHTML = '';
    iconHost.style.display = 'none';
  }
  $('#sheetBody').innerHTML = withVersion(body);
  const overlay = $('#overlay');
  overlay.hidden = false;
  const card = overlay.querySelector('.overlay-card');
  if (card) card.scrollTop = 0;
  const first = overlay.querySelector('#sheetBody input, #sheetBody button, .overlay-card > .sheet-heading button');
  if (first) first.focus();
}
function closeSheet() {
  const overlay = $('#overlay');
  if (overlay.hidden) return;
  overlay.hidden = true;
  $('#sheetBody').innerHTML = '';
  if (returnFocus && returnFocus.isConnected) returnFocus.focus();
}
function menuItem(action, label, ico, data = '', danger = false) {
  return `<button class="menu-item${danger ? ' danger' : ''}" data-action="${action}" ${data}>${icon(ico)}${label}</button>`;
}

/* ---------- actions ---------- */
const actions = {
  'home': () => navigate('home'),
  'settings-back': () => navigate('settings'),
  'settings-appearance': () => navigate('appearance'),
  'settings-backup': () => { state.from = 'settings'; navigate('backup'); },
  'settings-archive': () => { state.from = 'settings'; navigate('archive'); },
  'settings-review': () => { state.from = 'settings'; navigate('review'); },
  'settings-widget': () => { state.from = 'settings'; navigate('widget'); },
  'settings-about': () => sheet('关于 DianMao', `<div style="font-size:14px;line-height:22px;color:var(--on-surface-variant)">
      <b style="color:var(--on-surface)">DianMao v__APP_VERSION__</b><br>个人任务指挥台。四象限决定方向,时间决定什么时候看它。<br><br>
      设计语言:The Day Compass。数据与偏好仅保存在本机;日历、提醒与备份均由你显式授权后才发生。
    </div>
    <div class="dlg-actions"><button class="btn text" data-action="close">关闭</button></div>`, { icon: 'compass', tint: 'var(--primary)' }),
  'open-notification-settings': () => toast('演示:已打开系统通知设置'),
  'back': () => {
    if (state.page === 'detail') return navigate('home');
    if (state.page === 'quadrant') return navigate('home');
    if (state.page === 'add') return navigate(state.editingId ? 'detail' : 'home');
    if (state.page === 'appearance') return navigate('settings');
    if (['backup', 'archive', 'review', 'widget'].includes(state.page) && state.from === 'settings') return navigate('settings');
    navigate('home');
  },
  'add': () => { state.editingId = null; state.form = blankForm(state.homeView === 'quadrants' ? 'iu' : 'iu'); navigate('add'); },
  'edit': el => { const t = byId(el.dataset.id); state.editingId = t.id; state.form = formFrom(t); navigate('add'); },
  'open': el => { state.detailId = +el.dataset.id; navigate('detail'); },
  'quadrant': el => { state.activeQ = el.dataset.q; navigate('quadrant'); },
  'search': () => { state.search = !state.search; if (!state.search) state.query = ''; render(); },
  'search-clear': () => { state.query = ''; render(); const i = $('#searchInput'); if (i) i.focus(); },
  'home-view': el => { state.homeView = el.dataset.view; render(); },
  'group': el => { const k = el.dataset.key; state.openGroups[k] = !state.openGroups[k]; render(); },
  'collapse': el => {
    const k = el.dataset.key;
    if (k === 'history') state.openHistory = !state.openHistory;
    else state.openGroups[k] = !state.openGroups[k];
    render();
  },
  'toggle': el => { const t = byId(el.dataset.id); t.completed = !t.completed; if (t.completed) t.completionCount = Math.max(1, t.completionCount); toast(t.completed ? '已标记完成' : '已标为未完成'); render(); },
  'select-enter': el => { const id = +el.dataset.id; if (!state.selection.includes(id)) state.selection.push(id); render(); },
  'select-toggle': el => { const id = +el.dataset.id; const i = state.selection.indexOf(id); if (i >= 0) state.selection.splice(i, 1); else state.selection.push(id); render(); },
  'swipe-complete': el => { const t = byId(el.dataset.id); if (!t) return; t.completed = !t.completed; if (t.completed) t.completionCount = Math.max(1, t.completionCount); toast(t.completed ? '已静默完成(可在“写心得”补备注)' : '已标为未完成'); render(); },
  'swipe-delete': el => { const t = byId(el.dataset.id); if (!t) return; sheet('删除事项', `<p class="dlg-body" style="margin:0">确定删除“${esc(t.title)}”?删除后不会保留提醒和完成备注。</p>
      <div class="dlg-actions"><button class="btn text" data-action="close">取消</button>
      <button class="btn text" style="color:var(--error)" data-action="confirm-delete" data-id="${t.id}">删除</button></div>`, { icon: 'delete', tint: 'var(--error)' }); },
  'select-all': () => { const ids = (state.page === 'quadrant' ? qTasks(state.activeQ) : openTasks()).filter(t => !t.completed).map(t => t.id); state.selection = state.selection.length === ids.length ? [] : ids; render(); },
  'select-exit': () => { state.selection = []; render(); },
  'batch-move': () => sheet('移动到象限', `<div class="m3-repeat-picker">${QUAD_ORDER.map(k => `<button class="m3-check-row" data-action="batch-move-confirm" data-q="${k}"><span class="m3-check" style="background:${QUAD[k].color};border-color:${QUAD[k].color}"></span>${QUAD[k].label}</button>`).join('')}</div>
      <div class="dlg-actions"><button class="btn text" data-action="close">取消</button></div>`, { icon: 'inventory', tint: 'var(--primary)' }),
  'batch-move-confirm': el => { const q = el.dataset.q; state.tasks.forEach(t => { if (state.selection.includes(t.id)) t.q = q; }); closeSheet(); state.selection = []; toast(`已移动到${QUAD[q].short}象限`); render(); },
  'batch-delete': () => sheet('批量删除', `<p class="dlg-body" style="margin:0">确定删除选中的 ${state.selection.length} 个事项?删除后不会保留提醒和完成备注。</p>
      <div class="dlg-actions"><button class="btn text" data-action="close">取消</button>
      <button class="btn text" style="color:var(--error)" data-action="batch-delete-confirm">删除</button></div>`, { icon: 'delete', tint: 'var(--error)' }),
  'batch-delete-confirm': () => { state.tasks = state.tasks.filter(t => !state.selection.includes(t.id)); state.selection = []; closeSheet(); toast('演示:已批量删除'); navigate('home'); },
  'move-to-date': el => { const t = byId(el.dataset.id); state.moveTaskId = t.id; sheet('移动到...', `<div class="m3-repeat-picker">
      ${[['今天', 0], ['明天', 1], ['后天', 2]].map(([l, off]) => { const d = addDays(NOW, off); return `<button class="m3-check-row" data-action="move-to-date-confirm" data-offset="${off}"><span class="m3-check"></span>${l} · ${d.getMonth() + 1}/${d.getDate()}</button>`; }).join('')}
      </div>
      <span class="hint" style="display:block;margin-top:8px">保留原提醒时刻;重复事项请在编辑中调整</span>
      <div class="dlg-actions"><button class="btn text" data-action="close">取消</button></div>`, { icon: 'calendar', tint: 'var(--secondary)' }); },
  'move-to-date-confirm': el => { const t = byId(state.moveTaskId); const off = +el.dataset.offset; const base = t.dueAt ? new Date(t.dueAt) : new Date(NOW); const d = addDays(NOW, off); t.dueAt = new Date(d.getFullYear(), d.getMonth(), d.getDate(), base.getHours(), base.getMinutes()).getTime(); t.dueLabel = labelFor(new Date(t.dueAt), `${String(base.getHours()).padStart(2, '0')}:${String(base.getMinutes()).padStart(2, '0')}`); closeSheet(); toast('已移动到期日'); render(); },
  'subtask': el => { const t = byId(el.dataset.id); const s = t.subTasks[+el.dataset.index]; s.done = !s.done; render(); },
  'archive': el => { const t = byId(el.dataset.id); t.archived = true; toast('已归档'); navigate('home'); },
  'unarchive': el => { const t = byId(el.dataset.id); t.archived = false; toast('已恢复到待办'); render(); },
  'delete': el => {
    const t = byId(el.dataset.id);
    sheet('删除事项', `<p class="dlg-body" style="margin:0">确定删除“${esc(t.title)}”?删除后不会保留提醒和完成备注。</p>
      <div class="dlg-actions"><button class="btn text" data-action="close">取消</button>
      <button class="btn text" style="color:var(--error)" data-action="confirm-delete" data-id="${t.id}">删除</button></div>`, { icon: 'delete', tint: 'var(--error)' });
  },
  'confirm-delete': el => { state.tasks = state.tasks.filter(t => t.id !== +el.dataset.id); closeSheet(); toast('演示:事项已删除'); navigate('home'); },
  'edit-note': el => {
    const t = byId(el.dataset.id);
    sheet(t.completed ? '编辑完成备注' : '完成并写备注', `<p style="margin:0 0 12px;font-size:14px;line-height:20px;color:var(--on-surface-variant)">${esc(t.title)}</p>
      <div class="field"><div class="flabel">心得或复盘</div><textarea id="noteText" placeholder="这件事完成后,我学到或确认了什么?">${esc(t.completionNote || '')}</textarea></div>
      <div class="dlg-actions"><button class="btn text" data-action="close">稍后再写</button>
      <button class="btn text" data-action="save-note" data-id="${t.id}">保存备注</button></div>`, { icon: 'edit-note', tint: 'var(--primary)' });
  },
  'save-note': el => { const t = byId(el.dataset.id); const v = $('#noteText').value.trim(); t.completionNote = v || null; closeSheet(); toast('备注已保存'); render(); },
  'delete-completion': el => { state.history = state.history.filter(h => h.id !== +el.dataset.id); render(); toast('演示:完成记录已删除'); },
  'menu': el => {
    const t = byId(el.dataset.id);
    let body = menuItem('edit-note', t.completed ? '编辑完成备注' : '完成并写备注', t.completed ? 'edit-note' : 'done-all', `data-id="${t.id}"`);
    body += menuItem('edit', '编辑事项', 'edit', `data-id="${t.id}"`);
    if (!t.completed) body += menuItem('move-to-date', '移动到...', 'calendar', `data-id="${t.id}"`);
    if (t.completed) body += menuItem('archive', '归档', 'archive', `data-id="${t.id}"`);
    body += menuItem('delete', '删除事项', 'delete', `data-id="${t.id}"`, true);
    sheet('事项操作', body, { icon: 'inventory', tint: QUAD[t.q].color });
  },
  'close': () => closeSheet(),

  /* add / edit form */
  'form-q': el => { state.form.q = el.dataset.q; collectForm(); render(); },
  'quick-date': el => { const off = +el.dataset.offset; state.form.dueDate = off === 2 ? nextMonday() : addDays(NOW, off); if (!state.form.dueTime) state.form.dueTime = '09:00'; render(); },
  'form-switch': el => {
    const f = state.form, field = el.dataset.field;
    f[field] = !f[field];
    if (field === 'useCalendar' && !f.useCalendar) f.useAlarm = false;
    render();
  },
  'form-sub-add': () => { collectForm(); const v = ($('#formSub').value || '').trim(); if (!v) return; state.form.subTasks.push({ title: v, done: false }); render(); },
  'form-sub-toggle': el => { collectForm(); const s = state.form.subTasks[+el.dataset.index]; s.done = !s.done; render(); },
  'form-sub-del': el => { collectForm(); state.form.subTasks.splice(+el.dataset.index, 1); render(); },
  'form-save': () => {
    collectForm();
    const f = state.form;
    if (!f.title.trim()) { toast('先写点什么'); $('#formTitle').focus(); return; }
    if (state.editingId) {
      const t = byId(state.editingId);
      Object.assign(t, { title: f.title, detail: f.detail || null, q: f.q, dueAt: f.dueDate ? f.dueDate.getTime() : null, dueLabel: f.dueDate ? labelFor(f.dueDate, f.dueTime) : null, useCalendar: f.useCalendar, useAlarm: f.useAlarm, useLocalReminder: f.useLocalReminder, repeat: f.repeat, subTasks: f.subTasks });
      state.editingId = null; toast('事项已更新'); navigate('home');
    } else {
      const id = Math.max(0, ...state.tasks.map(t => t.id)) + 1;
      state.tasks.push({ id, title: f.title, detail: f.detail || null, q: f.q, dueAt: f.dueDate ? f.dueDate.getTime() : null, dueLabel: f.dueDate ? labelFor(f.dueDate, f.dueTime) : null, useCalendar: f.useCalendar, useAlarm: f.useAlarm, useLocalReminder: f.useLocalReminder, repeat: f.repeat, subTasks: f.subTasks, completed: false, completionNote: null, completionCount: 0, archived: false });
      toast(`已保存到${QUAD[f.q].short}象限`); navigate('home');
    }
  },
  'pick-date': () => { state.dpMonth = new Date((state.form.dueDate || new Date(NOW)).getFullYear(), (state.form.dueDate || new Date(NOW)).getMonth(), 1); state.dpSelected = state.form.dueDate ? midnight(state.form.dueDate) : midnight(NOW); sheet('选择到期日', m3DatePicker()); },
  'dp-prev': el => { const m = +el.dataset.shift; state.dpMonth = addMonths(state.dpMonth, m); renderOverlay(); },
  'dp-next': el => { const m = +el.dataset.shift; state.dpMonth = addMonths(state.dpMonth, m); renderOverlay(); },
  'dp-pick': el => { state.dpSelected = +el.dataset.day; renderOverlay(); },
  'dp-confirm': () => { state.form.dueDate = new Date(state.dpSelected); if (!state.form.dueTime) state.form.dueTime = '09:00'; closeSheet(); render(); },
  'pick-time': () => {
    const cur = state.form.dueTime || '09:00';
    const [h, m] = cur.split(':').map(Number);
    state.tp = { hour: h, minute: m, mode: 'dial', amp: h < 12 ? 'AM' : 'PM' };
    sheet('选择提醒时间', m3TimePicker());
  },
  'tp-hour': el => { state.tp.hour = (+el.dataset.hour) % 24; renderOverlay(); },
  'tp-min': el => { state.tp.minute = (+el.dataset.min) % 60; renderOverlay(); },
  'tp-amp': el => { const want = el.dataset.amp; if ((state.tp.hour < 12 ? 'AM' : 'PM') !== want) state.tp.hour = (state.tp.hour + 12) % 24; renderOverlay(); },
  'tp-mode': el => { state.tp.mode = el.dataset.mode; renderOverlay(); const i = $('#tpInput'); if (i) i.focus(); },
  'tp-apply': () => {
    const raw = ($('#tpInput').value || '').trim();
    const m = raw.match(/^(\d{1,2}):(\d{2})$/);
    if (!m || +m[1] > 23 || +m[2] > 59) { toast('请输入 HH:MM,例如 07:45'); return; }
    state.tp.hour = +m[1]; state.tp.minute = +m[2]; state.tp.mode = 'dial'; renderOverlay();
  },
  'tp-confirm': () => {
    state.form.dueTime = `${String(state.tp.hour).padStart(2, '0')}:${String(state.tp.minute).padStart(2, '0')}`;
    if (!state.form.dueDate) state.form.dueDate = new Date(NOW);
    closeSheet(); render();
  },
  'pick-repeat': () => {
    const f = state.form;
    state.rp = { preset: f.repeatPreset || 'none', customRule: f.repeatCustomRule || 'weekly', interval: f.repeatInterval || 1, weekdays: new Set(f.repeatWeekdays && f.repeatWeekdays.length ? f.repeatWeekdays : [1]) };
    sheet('重复规则', m3RepeatPicker());
  },
  'rp-preset': el => { state.rp.preset = el.dataset.preset; renderOverlay(); },
  'rp-rule': el => { state.rp.customRule = el.dataset.rule; renderOverlay(); },
  'rp-int': el => { const d = +el.dataset.delta; state.rp.interval = Math.max(1, Math.min(99, state.rp.interval + d)); renderOverlay(); },
  'rp-day': el => { const d = +el.dataset.day; if (state.rp.weekdays.has(d)) state.rp.weekdays.delete(d); else state.rp.weekdays.add(d); renderOverlay(); },
  'rp-confirm': () => {
    const f = state.form, rp = state.rp;
    if (rp.preset === 'custom') {
      f.repeatPreset = 'custom'; f.repeatCustomRule = rp.customRule; f.repeatInterval = rp.interval;
      f.repeatWeekdays = rp.customRule === 'weekly' ? [...rp.weekdays].sort() : [];
    } else {
      f.repeatPreset = rp.preset; f.repeatWeekdays = rp.preset === 'workdays' ? [1, 2, 3, 4, 5] : [];
      f.repeatInterval = 1; f.repeatCustomRule = 'weekly';
    }
    f.repeat = repeatLabel(f.repeatPreset, f.repeatInterval, f.repeatWeekdays || []);
    closeSheet(); render();
  },
  'pick-lead': () => {
    state.lead = new Set(state.form.reminderLead && state.form.reminderLead.length ? state.form.reminderLead : [0]);
    sheet('提醒提前量', m3LeadPicker());
  },
  'lead-toggle': el => { const v = +el.dataset.min; if (state.lead.has(v)) state.lead.delete(v); else state.lead.add(v); renderOverlay(); },
  'lead-confirm': () => {
    state.form.reminderLead = [...state.lead].sort((a, b) => a - b);
    if (!state.form.reminderLead.length) state.form.reminderLead = [0];
    state.form.lead = leadLabel(state.form.reminderLead);
    closeSheet(); render();
  },

  /* calendar */
  'cal-prev': () => { const c = state.calendar; const m = new Date(c.year, c.month - 1, 1); c.year = m.getFullYear(); c.month = m.getMonth(); render(); },
  'cal-next': () => { const c = state.calendar; const m = new Date(c.year, c.month + 1, 1); c.year = m.getFullYear(); c.month = m.getMonth(); render(); },
  'cal-today': () => { const c = state.calendar; c.year = NOW.getFullYear(); c.month = NOW.getMonth(); c.selected = midnight(NOW); render(); },
  'cal-pick': el => { state.calendar.selected = +el.dataset.day; render(); },
  'cal-add': () => { state.editingId = null; state.form = blankForm('iu'); state.form.dueDate = new Date(state.calendar.selected); state.form.dueTime = '09:00'; navigate('add'); },

  /* backup */
  'toggle-pass': () => { state.backup.visible = !state.backup.visible; render(); },
  'save-dav': () => { state.backup.url = $('#davUrl').value; state.backup.user = $('#davUser').value; state.backup.pass = $('#davPass').value; toast('演示:WebDAV 设置已保存'); render(); },
  'toggle-auto': () => { state.backup.auto = !state.backup.auto; render(); },
  'theme-mode': el => { state.themeMode = el.dataset.mode; applyTheme(); render(); },
  'export': () => toast('演示:已导出本地备份文件'),
  'widget-toggle': () => toast('演示:小组件勾选完成任务(真实应用中会写入 Room 并刷新小组件)'),
  'revert-local': () => toast('演示:已从本地文件恢复'),
  'upload': () => toast('演示:已上传备份到 WebDAV'),
  'download': () => toast('演示:已从 WebDAV 下载恢复'),
};

function m3DatePicker() {
  const month = state.dpMonth || new Date(NOW.getFullYear(), NOW.getMonth(), 1);
  const selected = state.dpSelected || midnight(NOW);
  const monthStart = new Date(month.getFullYear(), month.getMonth(), 1);
  const pad = (monthStart.getDay() + 6) % 7; // Monday-first weekday header
  const days = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < pad; i++) cells.push(null);
  for (let d = 1; d <= days; d++) cells.push(d);
  while (cells.length % 7 !== 0) cells.push(null);
  const rows = [];
  for (let i = 0; i < cells.length; i += 7) rows.push(cells.slice(i, i + 7));
  const todayMs = midnight(NOW);
  return `<div class="m3-datepicker">
    <div class="m3-dp-head">
      <button class="m3-dp-nav" data-action="dp-prev" data-shift="-1" aria-label="上个月">${icon('chev-left', 's18')}</button>
      <span class="m3-dp-title">${month.getFullYear()}年${month.getMonth() + 1}月</span>
      <button class="m3-dp-nav" data-action="dp-next" data-shift="1" aria-label="下个月">${icon('chev-right', 's18')}</button>
    </div>
    <div class="m3-dp-week">${['一', '二', '三', '四', '五', '六', '日'].map(x => `<span>${x}</span>`).join('')}</div>
    <div class="m3-dp-grid">${rows.map(w => `<div class="m3-dp-row">${w.map(d => {
      if (d == null) return `<span class="m3-dp-cell empty"></span>`;
      const ms = new Date(month.getFullYear(), month.getMonth(), d).getTime();
      const isSel = ms === selected, isToday = ms === todayMs;
      return `<button class="m3-dp-cell${isSel ? ' sel' : ''}${isToday && !isSel ? ' today' : ''}" data-action="dp-pick" data-day="${ms}">${d}</button>`;
    }).join('')}</div>`).join('')}</div>
  </div>
  <div class="dlg-actions"><button class="btn text" data-action="close">取消</button>
  <button class="btn text" data-action="dp-confirm">确定</button></div>`;
}

function addMonths(date, n) { return new Date(date.getFullYear(), date.getMonth() + n, 1); }

/** M3 TimePicker: 24-hour dial + keyboard mode toggle, matching DianMaoTheme. */
function m3TimePicker() {
  const t = state.tp || { hour: 9, minute: 0, mode: 'dial' };
  const hh = String(t.hour).padStart(2, '0'), mm = String(t.minute).padStart(2, '0');
  if (t.mode === 'input') {
    return `<div class="m3-timepicker input-mode">
      <div class="m3-tp-inputrow">
        <div class="m3-tp-field"><input id="tpInput" class="m3-tp-input" inputmode="numeric" value="${hh}" data-field="h"><span class="m3-tp-sep">:</span><input id="tpInputM" class="m3-tp-input" inputmode="numeric" value="${mm}" data-field="m"></div>
      </div>
      <button class="m3-tp-mode" data-action="tp-mode" data-mode="dial" aria-label="切换到表盘">${icon('schedule', 's20')}</button>
    </div>
    <div class="dlg-actions"><button class="btn text" data-action="close">取消</button>
      <button class="btn text" data-action="tp-apply">确定</button></div>`;
  }
  const hourMarks = [12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];
  const minuteMarks = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];
  const hourAngle = ((t.hour % 12) / 12) * 360;
  const minuteAngle = (t.minute / 60) * 360;
  return `<div class="m3-timepicker">
    <div class="m3-tp-headline"><span class="m3-tp-time"><b>${hh}</b>:<b>${mm}</b></span></div>
    <div class="m3-tp-dial" style="--ha:${hourAngle}deg;--ma:${minuteAngle}deg">
      <div class="m3-tp-hand h"></div>
      <div class="m3-tp-center"></div>
      ${hourMarks.map((h, i) => {
        const ang = (i / 12) * 2 * Math.PI - Math.PI / 2;
        const x = 50 + 42 * Math.cos(ang), y = 50 + 42 * Math.sin(ang);
        const val = h === 12 ? 0 : h;
        const on = val === (t.hour % 12);
        return `<button class="m3-tp-mark${on ? ' on' : ''}" style="left:calc(${x}% - 16px);top:calc(${y}% - 16px)" data-action="tp-hour" data-hour="${val}">${h}</button>`;
      }).join('')}
    </div>
    <div class="m3-tp-min-row">
      ${minuteMarks.map(mn => `<button class="m3-tp-min${mn === t.minute ? ' on' : ''}" data-action="tp-min" data-min="${mn}">:${String(mn).padStart(2, '0')}</button>`).join('')}
    </div>
    <button class="m3-tp-mode" data-action="tp-mode" data-mode="input" aria-label="切换到键盘输入">${icon('keyboard', 's20')}</button>
  </div>
  <div class="dlg-actions"><button class="btn text" data-action="close">取消</button>
    <button class="btn text" data-action="tp-confirm">确定</button></div>`;
}

function m3RepeatPicker() {
  const rp = state.rp;
  const presets = [['none', '不重复'], ['daily', '每天'], ['workdays', '工作日'], ['weekly', '每周'], ['monthly', '每月'], ['yearly', '每年'], ['count', '计数'], ['custom', '自定义']];
  const rows = presets.map(([k, label]) => `<button class="m3-check-row${rp.preset === k ? ' on' : ''}" data-action="rp-preset" data-preset="${k}">
      <span class="m3-check">${rp.preset === k ? icon('check', 's16') : ''}</span>${label}</button>`).join('');
  let custom = '';
  if (rp.preset === 'custom') {
    const rules = [['daily', '每 天'], ['weekly', '每 周'], ['monthly', '每 月'], ['yearly', '每 年']];
    custom = `<div class="m3-rp-div"></div>
      <span class="hint">频率</span>
      <div class="m3-chiprow">${rules.map(([k, l]) => `<button class="lane-chip" data-action="rp-rule" data-rule="${k}" style="--c:var(--primary);${rp.customRule === k ? 'background:var(--primary-container);color:var(--on-primary-container)' : ''}">${l}</button>`).join('')}</div>
      <div class="m3-int-row"><span>间隔</span><span class="grow"></span>
        <button class="m3-int-btn" data-action="rp-int" data-delta="-1" ${rp.interval <= 1 ? 'disabled' : ''} aria-label="减少间隔">${icon('minus', 's18')}</button>
        <span class="m3-int-val">${rp.interval}</span>
        <button class="m3-int-btn" data-action="rp-int" data-delta="1" ${rp.interval >= 99 ? 'disabled' : ''} aria-label="增加间隔">${icon('add', 's18')}</button></div>`;
    if (rp.customRule === 'weekly') {
      custom += `<span class="hint">重复周几</span>
        <div class="m3-chiprow">${['一', '二', '三', '四', '五', '六', '日'].map((d, i) => `<button class="lane-chip" data-action="rp-day" data-day="${i + 1}" style="--c:var(--primary);${rp.weekdays.has(i + 1) ? 'background:var(--primary-container);color:var(--on-primary-container)' : ''}">${d}</button>`).join('')}</div>`;
    }
  }
  return `<div class="m3-repeat-picker">${rows}${custom}</div>
    <div class="dlg-actions"><button class="btn text" data-action="close">取消</button>
    <button class="btn text" data-action="rp-confirm">确定</button></div>`;
}

function m3LeadPicker() {
  const options = [[0, '准时提醒'], [5, '提前 5 分钟'], [30, '提前 30 分钟'], [1440, '提前 1 天']];
  return `<div class="m3-repeat-picker">${options.map(([v, label]) => `<button class="m3-check-row${state.lead.has(v) ? ' on' : ''}" data-action="lead-toggle" data-min="${v}">
      <span class="m3-check">${state.lead.has(v) ? icon('check', 's16') : ''}</span>${label}</button>`).join('')}</div>
    <div class="dlg-actions"><button class="btn text" data-action="close">取消</button>
    <button class="btn text" data-action="lead-confirm">确定</button></div>`;
}

function leadLabel(minutes) {
  const map = { 0: '准时提醒', 5: '提前 5 分钟', 30: '提前 30 分钟', 1440: '提前 1 天' };
  const valid = minutes.filter(x => x >= 0).sort((a, b) => a - b);
  if (!valid.length) return '准时提醒';
  return valid.map(x => map[x] || '准时提醒').join('、');
}

/** Mirrors data/RepeatRuleLabel.kt for the prototype. */
function repeatLabel(preset, interval, weekdays) {
  const names = ['一', '二', '三', '四', '五', '六', '日'];
  if (preset === 'none') return '不重复';
  if (preset === 'count') return '计数';
  if (preset === 'daily') return interval === 1 ? '每天' : `每 ${interval} 天`;
  if (preset === 'monthly') return interval === 1 ? '每月' : `每 ${interval} 个月`;
  if (preset === 'yearly') return interval === 1 ? '每年' : `每 ${interval} 年`;
  // weekly family
  const wd = (weekdays || []).filter(d => d >= 1 && d <= 7);
  const isWork = wd.length === 5 && [1, 2, 3, 4, 5].every(d => wd.includes(d));
  const isWeekend = wd.length === 2 && [6, 7].every(d => wd.includes(d));
  if (isWork) return interval === 1 ? '工作日' : `每 ${interval} 周 · 工作日`;
  if (isWeekend) return interval === 1 ? '周末' : `每 ${interval} 周 · 周末`;
  if (!wd.length) return interval === 1 ? '每周' : `每 ${interval} 周`;
  const list = [...wd].sort((a, b) => a - b).map((d, i) => (i === 0 && interval > 1 ? '周' : '') + names[d - 1]).join('、');
  return interval === 1 ? '每周' + list : `每 ${interval} 周 · ` + list;
}

/** Re-renders only the open overlay body (dialogs stay in place while editing). */
function renderOverlay() {
  if ($('#overlay').hidden) return;
  const title = $('#sheetTitle').textContent;
  if (title === '选择到期日') $('#sheetBody').innerHTML = m3DatePicker();
  else if (title === '选择提醒时间') $('#sheetBody').innerHTML = m3TimePicker();
  else if (title === '重复规则') $('#sheetBody').innerHTML = m3RepeatPicker();
  else if (title === '提醒提前量') $('#sheetBody').innerHTML = m3LeadPicker();
}

/* ---------- form helpers ---------- */
function blankForm(q) { return { title: '', detail: '', q, dueDate: null, dueTime: null, useCalendar: false, useAlarm: false, useLocalReminder: false, reminderLead: [0], lead: '准时提醒', repeat: null, repeatPreset: 'none', repeatCustomRule: 'weekly', repeatInterval: 1, repeatWeekdays: [], subTasks: [], feedback: null }; }
function formFrom(t) {
  const d = t.dueAt != null ? new Date(t.dueAt) : null;
  return { title: t.title, detail: t.detail || '', q: t.q, dueDate: d ? new Date(d.getFullYear(), d.getMonth(), d.getDate()) : null, dueTime: d ? `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}` : null, useCalendar: t.useCalendar, useAlarm: t.useAlarm, useLocalReminder: t.useLocalReminder, reminderLead: t.reminderLead || [0], lead: t.lead || '准时提醒', repeat: t.repeat, repeatPreset: t.repeatPreset || 'none', repeatCustomRule: t.repeatCustomRule || 'weekly', repeatInterval: t.repeatInterval || 1, repeatWeekdays: t.repeatWeekdays || [], subTasks: t.subTasks.map(s => ({ ...s })), feedback: null };
}
function collectForm() {
  state.form.title = $('#formTitle') ? $('#formTitle').value : state.form.title;
  state.form.detail = $('#formDetail') ? $('#formDetail').value : state.form.detail;
}
function labelFor(date, time) {
  const d = new Date(date);
  const base = sameDay(d, NOW) ? '今天' : sameDay(d, addDays(NOW, 1)) ? '明天' : `${d.getMonth() + 1}月${d.getDate()}日`;
  return time ? `${base} ${time}` : base;
}
function nextMonday() { const d = new Date(NOW); const add = ((8 - d.getDay()) % 7) || 7; return addDays(d, add); }

/* ---------- delegated events + setup ---------- */
document.addEventListener('click', e => {
  const nav = e.target.closest('[data-page]');
  if (nav) { e.preventDefault(); navigate(nav.dataset.page); return; }
  const el = e.target.closest('[data-action]');
  if (!el) return;
  const fn = actions[el.dataset.action];
  if (fn) { e.preventDefault(); fn(el); }
});
// Long-press a task row to enter multi-select; long-press a calendar row to move its date.
let pressTimer = null;
document.addEventListener('pointerdown', e => {
  const move = e.target.closest('[data-long="move-to-date"]');
  const sel = e.target.closest('[data-long="select-enter"]');
  const el = move || sel;
  if (!el) return;
  if (sel && state.selection.length) return;
  const action = move ? 'move-to-date' : 'select-enter';
  pressTimer = setTimeout(() => { actions[action](el); pressTimer = null; }, 500);
});
['pointerup', 'pointerleave', 'pointercancel', 'scroll'].forEach(ev =>
  document.addEventListener(ev, () => { if (pressTimer) { clearTimeout(pressTimer); pressTimer = null; } }, true));

/* ---------- swipe-to-action ---------- */
// Horizontal drag past 35% of the row width commits: right = complete, left = delete.
let swipe = null;
document.addEventListener('pointerdown', e => {
  const wrap = e.target.closest('[data-swipe]');
  if (!wrap || state.selection.length) return;
  if (e.target.closest('[data-action="menu"], .checkzone, .task-main, .icon-button')) return;
  const fore = wrap.querySelector('.swipe-fore');
  swipe = { wrap, fore, startX: e.clientX, startY: e.clientY, dx: 0, decided: false, active: false };
});
document.addEventListener('pointermove', e => {
  if (!swipe) return;
  const dx = e.clientX - swipe.startX, dy = e.clientY - swipe.startY;
  if (!swipe.decided) {
    if (Math.abs(dx) < 8 && Math.abs(dy) < 8) return;
    if (Math.abs(dy) > Math.abs(dx)) { swipe = null; return; } // vertical scroll wins
    swipe.decided = true; swipe.active = true; swipe.wrap.classList.add('dragging');
  }
  swipe.dx = dx;
  swipe.fore.style.transform = `translateX(${dx}px)`;
});
function endSwipe() {
  if (!swipe) return;
  const { wrap, fore, dx, active } = swipe; const id = +wrap.dataset.id;
  swipe = null;
  if (!active) return;
  wrap.classList.remove('dragging');
  const threshold = wrap.clientWidth * 0.35;
  fore.style.transform = '';
  if (dx >= threshold) { actions['swipe-complete']({ dataset: { id } }); }
  else if (dx <= -threshold) { actions['swipe-delete']({ dataset: { id } }); }
}
['pointerup', 'pointercancel'].forEach(ev => document.addEventListener(ev, endSwipe));

/* ---------- drag reorder (quadrant-detail list) ---------- */
// Long-press first (mirrors detectDragGesturesAfterLongPress), then a vertical
// move lifts the row to 8dp / 1.02. Drop persists via 50ms debounce in the real
// app; the prototype shows the lifted state and treats drop as a no-op.
let drag = null;
let dragTimer = null;
document.addEventListener('pointerdown', e => {
  const row = e.target.closest('[data-drag-row]');
  if (!row || state.selection.length) return;
  if (e.target.closest('[data-action="menu"], .checkzone, .icon-button')) return;
  dragTimer = setTimeout(() => {
    drag = { row, startY: e.clientY, active: true };
    row.classList.add('dragging');
    dragTimer = null;
  }, 500);
});
document.addEventListener('pointermove', e => {
  if (drag) { drag.lastY = e.clientY; }
});
function endDrag() {
  if (dragTimer) { clearTimeout(dragTimer); dragTimer = null; }
  if (!drag) return;
  drag.row.classList.remove('dragging');
  drag = null;
  toast('演示:拖拽抬起态(真实应用按 50ms 防抖持久化顺序)');
}
['pointerup', 'pointercancel', 'pointerleave'].forEach(ev => document.addEventListener(ev, endDrag));
document.addEventListener('input', e => {
  if (e.target.id === 'searchInput') { state.query = e.target.value; const pos = e.target.selectionStart; render(); const i = $('#searchInput'); if (i) { i.focus(); i.setSelectionRange(pos, pos); } }
  else if (e.target.id === 'formTitle' || e.target.id === 'formDetail') { collectForm(); }
});
document.addEventListener('keydown', e => {
  if (e.key === 'Enter' && e.target.id === 'formTitle') { e.preventDefault(); actions['form-save'](); }
  if (e.key === 'Enter' && e.target.id === 'tpInput') { e.preventDefault(); actions['tp-apply'](); }
  if (e.key === 'Escape' && !$('#overlay').hidden) { e.preventDefault(); closeSheet(); }
});

/* theme toggle */
/* ---------- theme: system / light / dark preference ---------- */
const systemDark = matchMedia('(prefers-color-scheme: dark)');
function isDarkNow() { return state.themeMode === 'dark' || (state.themeMode === 'system' && systemDark.matches); }
function applyTheme() {
  const dark = isDarkNow();
  document.documentElement.dataset.theme = dark ? 'dark' : 'light';
  const btn = $('#themeToggle');
  if (btn) btn.innerHTML = `${icon(dark ? 'sun' : 'moon', 's18')}${dark ? '浅色' : '深色'}`;
  if (!btn) return;
  btn.setAttribute('aria-label', state.themeMode === 'system'
    ? `跟随系统(当前${dark ? '深色' : '浅色'}),点击切换`
    : `当前${dark ? '深色' : '浅色'}主题,点击切换`);
}
// The preview toolbar button cycles system → light → dark → system, mirroring the
// in-app 外观 setting rather than acting as a separate preview-only control.
const THEME_CYCLE = ['system', 'light', 'dark'];
$('#themeToggle').addEventListener('click', () => {
  state.themeMode = THEME_CYCLE[(THEME_CYCLE.indexOf(state.themeMode) + 1) % THEME_CYCLE.length];
  applyTheme();
  toast(state.themeMode === 'system' ? `外观:跟随系统(${isDarkNow() ? '深色' : '浅色'})` : `外观:${state.themeMode === 'dark' ? '深色' : '浅色'}`);
  if (state.page === 'backup') render();
});
// Live-follow the OS while in system mode.
systemDark.addEventListener('change', () => { if (state.themeMode === 'system') applyTheme(); });
applyTheme();
render();