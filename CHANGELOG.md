# Changelog

- Prototype v1.2 — 1:1 replicable UI (2026-10-11): the design mockup now only
  draws UI that Compose can reproduce. The self-invented time stepper is
  replaced by the Material 3 `TimePicker` (24-hour dial + keyboard mode) and the
  self-invented mini calendar by the M3 `DatePicker` grid, both matching
  `DianMaoDatePickerDialog` / `DianMaoTimePickerDialog`. Missing real dialogs are
  added (uncomplete-recurring confirm, repeat rule with custom frequency/
  interval/weekdays, reminder-lead multi-select, move-to-quadrant, batch delete);
  the dialog chapter grows from 3 to 8 examples. Missing real features are
  filled in: the reminder-lead setting row, long-press multi-select with
  select-all + batch move-quadrant / batch delete, and the calendar row's
  long-press「移动到...」. See `design/mockup/BASELINE.md`.

- Prototype v1.1 (2026-10-11): the design mockup gains a custom reminder-time
  picker (hour/minute steppers + free-form HH:MM input with validation +
  presets), matching the arbitrary time the Material 3 `TimePicker` already
  allows in the app (prototype gap, not new scope). Dialogs now render inside
  the phone frame via an in-device overlay instead of a `<dialog>` centered on
  the browser viewport, so they no longer overflow the device screen. The home
  header's backup button now uses a proper gear icon instead of a sun-like one
  that read as a theme toggle. Smoke test extended accordingly. See
  `design/mockup/BASELINE.md`.

- Prototype-first UI contract (2026-10-11): established a Frontend spec layer
  (`.trellis/spec/frontend/`) whose [prototype-first contract](../../.trellis/spec/frontend/prototype-first.md)
  makes a confirmed prototype mandatory before any Compose UI change, forbids
  deleting existing features that a prototype omits, and requires full state
  coverage, unified design tokens, per-UI change records, and light/dark +
  screen-size visual acceptance (compiling is not UI acceptance). The prototype
  baseline and pending items are registered in `design/mockup/BASELINE.md`.
  The former `backend/design-prototypes.md` moved into the frontend layer.

- Complete HTML design mockup (2026-10-11): added `design/mockup/`, a
  browser-openable, offline, in-memory prototype that mirrors the current
  Compose UI across nine surfaces — home (header, summary pills, smart-view
  switcher, time-focus lane, quadrant board, FAB), quadrant detail, task detail,
  add/edit, calendar month, review, backup, archive, and the dialog family.
  Light and dark themes use the exact `DianMaoTheme.kt` color schemes; the
  prototype ships a Playwright smoke test plus desktop/dark/mobile screenshots.
  Design-only; not bundled into the APK. See `design/mockup/README.md` and
  `.trellis/spec/frontend/design-prototypes.md`.

- Review stats & list polish (v1.1.2): the review day bars grow with a motion-
  contract-aware animation (snap under reduced motion), and the quadrant share
  rows gain share bars with percentages alongside the counts. Smart-view group
  headers (过期/今天/day groups) are now collapsible — tapping a header toggles
  its rows, with the count staying visible on the collapsed header.

- Calendar quick actions (v1.1.1): each task row in the selected-day panel
  gains an inline check target with the same completion semantics as the home
  rows (including the 写心得 chain), and a long-press opens a quick move dialog
  (今天/明天/后天) that preserves the task's time of day and re-syncs alarms,
  the system calendar event and the home widget. Home search now also matches
  task detail and completion notes.

- Review & safety improvements (v1.1.0): the calendar gains a 今天 button to
  jump the grid and selection back to today from any month. WebDAV restore now
  snapshots the current local data to dianmao-backup-before-restore.json on
  the same share before the destructive import — a wrong or corrupt restore
  source can be undone by restoring that snapshot, and the restore aborts if
  the snapshot itself fails. When reminders are armed but Android's exact-alarm
  permission is off (default on Android 14+), the app now surfaces a guidance
  notice instead of silently degrading to late inexact alarms.

- Deep code review hardening (v1.0.9): fixed the ISO-weekly cycle bug where the
  week key paired weekOfWeekBasedYear with the calendar year — the ISO week
  spanning New Year's Eve split into two periods, letting a weekly task be
  completed twice in one cycle. resetExpiredCycleTasks now loads only completed
  cycle tasks via a DAO-level filter and resets in one transactional bulk
  update. The backup panel no longer wraps its setting blocks inside the status
  header card (double tint + missing spacing). A shared snackbar error guard
  replaces 13 identical try/catch blocks, 34 dead imports are gone, and the
  home day buckets (review stats, smart views, time focus, list grouping)
  refresh at local midnight so they no longer go stale in long sessions.

- Redesigned the launcher icon as the Day Compass: a thin terracotta dial ring
  with a slender needle pointing 45° up-right and an ink hub at center, replacing
  the four-quadrant paw-print mark. The monochrome layer mirrors the same ring +
  needle + hub silhouette for themed icons; the warm paper background is now pure.

- Stability polish: reminder and widget completion actions now ignore stale or
  duplicate task snapshots, so concurrent entry points cannot advance a
  recurring task twice or recreate an obsolete reminder. Batch selection now
  drops tasks that disappear from the active list, and batch-delete cleanup
  only targets entities that were actually deleted. Added regression coverage
  for completion idempotency, selection reconciliation, and calendar
  midnight/stable-order boundaries.

- Added quick actions on reminder notifications (v1.0.8): a 完成 button (记一次完成 for
  recurring tasks) completes the task with the same silent semantics as in-app swipe
  completion — alarms cancelled, recurring cycles advanced, home widget refreshed —
  and a 稍后提醒 button re-fires the reminder after 10 minutes. Notification action
  pendingIntents use dedicated request codes so they never collide with the alarm
  pendingIntents.

- Made the home widget interactive (v1.0.8): each task row gains a circular check
  target that toggles completion in place via a Glance action callback (reusing the
  shared silent-completion helper) and refreshes the widget immediately; rows keep
  deep-linking into the task detail. Responsive layout shows up to 8 rows on wide
  widgets (narrow keeps 4).

- Added a calendar month view (v1.0.8): a calendar icon beside the home search opens
  a full-screen page with ‹ › month navigation, a Monday-first 7-column grid, today
  highlight, selection fill, and dimmed out-of-month days. Each day shows up to three
  quadrant dots plus a +n overflow; selecting a day lists that day's tasks (completed
  dimmed) with rows deep-linking into details, and a 记一笔 entry pre-fills the add
  panel with the selected day as the due date. The view joins the in-app back chain.

- Added batch operations (v1.0.8): long-press a task row in the home smart list or a
  quadrant detail to enter multi-select (checkbox per row, tap toggles, swipe
  disabled), with a bottom bar showing 已选 N 项, 全选/取消全选, and three actions:
  批量完成 (silent completion per row + widget refresh), 批量移动象限 (quadrant picker
  dialog; targeted DAO update of quadrant + updatedAt only), and 批量删除 (confirm
  dialog; per-row reminder cancel + delete, one 撤销 restore for all, calendar event
  cleanup when the undo window lapses). Single-select behavior is unchanged.

- Added a home-screen widget (Glance): a warm-paper "DianMao · 今天" card
  showing up to four of today's open tasks (overdue first, via the existing
  today smart-view logic), a 记一笔 quick-add action that opens the app with
  the add sheet focused, and deep links from task rows into the task detail.
  The widget refreshes whenever the active task data changes plus a 30-minute
  system period; day/night color pairs match the app's themes.

- Added the review surface ("复盘"): tapping the home header's 已复盘 pill
  opens a full-screen review page aggregating completion data — 7/30-day
  completion totals, the last 7 days' distribution as quiet inline bars,
  quadrant share over the last 30 days, and the most recent completions with
  their notes (recurring per-cycle completions joined with non-recurring
  tasks' last completions). The review panel joins the in-app back priority
  chain; the room DAO gains a cross-task completions-with-task JOIN query.

- Added WebDAV auto backup: a backup-panel switch (default off) makes the app
  upload the latest data to the configured WebDAV directory on launch, at most
  once per day. Failed attempts stay silent and retry on the next start; the
  panel shows the last auto-backup time and result. Manual upload/restore and
  the WebDAV transport layer are unchanged — no new dependencies.

- Extended 到点提醒 to multiple lead times: a task can now fire at several
  points at once (e.g. 准时 + 提前 5 分钟 + 提前 1 天) — the 提前量 dropdown
  became a branded multi-select dialog, each lead time schedules its own exact
  alarm with a distinct notification, and cancelling a task's reminder cancels
  every alarm. Reminder state survives the upgrade (single lead → same value),
  recurring tasks reschedule all lead times per cycle, and backup format was
  upgraded to version 7 with the extra lead times included (v3–v6 still
  import). Database migrated to version 13.

- Extended the repeat-rule model beyond the plain frequency enum: tasks can now
  repeat 每年 (with Feb 29 clamping to 2/28 in non-leap years), on a custom
  interval (每 N 天/周/月/年), and on selected weekdays (指定周几, with a
  工作日 preset for Monday–Friday). The add/edit form's repeat row is no longer
  gated behind the calendar ring toggle — repeat only requires a due date;
  calendar ring controls RRULE sync and local reminders control per-cycle
  rescheduling independently. The repeat dropdown became a branded picker
  dialog (presets + inline custom editor with frequency, interval stepper, and
  weekday chips), task rows/detail show dynamic labels (每周一、三, 每 2 周 ·
  周一、三, 每 3 个月), and the generated calendar RRULE now carries
  INTERVAL/BYDAY/YEARLY. Database migrated to version 12, backup format
  upgraded to version 6 (v3/v4/v5 still import).

- Reworked the smart-view row swipes to the settle-back pattern: the row
  always returns to place after a swipe — completing a task lets it leave the
  list through the data flow, and a left swipe opens the delete confirmation
  with the row staying put until deletion is confirmed. A 40% width threshold
  stops accidental triggers while scrolling, and gesture state resets cleanly
  when a task's completion state changes.

- The quadrant-detail pages now offer the add-task action, and saving a new
  task closes the form instead of staying open for continuous capture — the
  save button reads 保存, and both add and edit are single-shot.

- Added a hand-tuned dark theme that follows the system: the warm paper
  identity carries into a deep warm brown-grey palette with lightened
  terracotta and calendar-blue accents, inverted tinted containers, and the
  same quiet hairline language. The system navigation bar matches the dark
  background at night; light mode is unchanged.

- Added row swipe gestures to the home smart-view lists: swipe right to
  complete (or un-complete) a task, swipe left to request deletion — with a
  tinted action hint behind the row while dragging. Deletion still runs
  through the confirm dialog and undo chain, and long-press drag reorder is
  unaffected.

- Restored local due-time reminders with a lead-time option: tasks can now
  enable 到点提醒 (DianMao's own notification, independent of calendar sync)
  and choose how early it fires — 准时 / 提前 5 分钟 / 提前 30 分钟 / 提前 1 天.
  Reminders survive reboots and backup restores, recurring tasks reschedule
  per cycle, and the task detail page shows the reminder state. Database
  migrated to version 10 with the new fields included in backups.

- Restored the home smart views: a segmented switcher (四象限 / 今天 / 近期 /
  收集箱) above the board browses the pure smart-view projections — today
  (overdue + timed today), the next 7 days, and unscheduled inbox — reusing the
  shared task row and all row actions, with the quadrant board remaining the
  default primary surface.

- Added undo to destructive actions, matching mainstream task apps: deleting a
  task or a completion record now shows the top notice with a 撤销 action for
  a few seconds — tapping it restores the item with its original id, order,
  and fields (the linked calendar event is only cleaned up once the undo
  window closes).

- Replaced the four quadrant priority chips in the add/edit form with a
  connected segmented-button row: each segment is evenly weighted with a small
  quadrant color dot and a centered two-character label, so nothing clips on
  phone widths, and the selected segment uses the terracotta container.

- Cross-surface visual-consistency pass over every screen: non-scheduling
  tonal buttons and selected chips now use the terracotta container instead of
  inheriting calendar blue from Material defaults (subtask add, archive,
  priority chips); cloud-sync status and completion-history accents moved to
  neutral ink; a warm error pair (#9C3B2E / #F3DAD5) replaced the stock
  Material red; all compact dialogs (delete, uncomplete-recurring, completion
  note, delete-completion) share one branded container with the warm paper
  scrim, panel card, hairline border, and red destructive confirms; the
  task-detail screen rides the shared full-screen scaffold (600dp centered
  cap); duplicated icon buttons, quadrant dots, and empty-state patterns were
  unified; the theme typography now defines the full scale with zero letter
  spacing everywhere; task rows dropped their saturated side stripe; the time
  focus lane returned to a raised neutral container with per-row tints; home
  stat pills were neutralized; shape tokens (InlineShape/PillShape) and
  spacing rhythm were normalized; DESIGN.md was updated to match.

- Laid the four quadrant priority chips in the add/edit form out on a single
  evenly spaced row instead of letting them wrap.

- Removed the tonal frame around the task-detail bottom action bar: the
  complete/archive/delete actions now sit directly on the page background,
  matching the add/edit form's frameless save button.

- Unified operation feedback across every page: the global notice is now an
  overlay anchored to the same top position on all screens instead of the
  Material 3 Scaffold snackbar slot, which shifted with the FAB's presence
  (home-only) and made notices land at different heights per page. Tapping a
  notice now dismisses it immediately.

- Reworked the calendar-alarm linkage: 响铃提醒 is now 开启日历响铃, a
  sub-switch of 同步到系统日历 that only controls the system-calendar event's
  audible reminder (including OPPO/ColorOS's per-event 响铃 switch via the
  force_reminder column); the master 同步到系统日历 switch creates a quiet
  calendar event on its own. DianMao no longer schedules its own AlarmManager
  notifications — the local reminder pipeline (receivers, notification
  channel, boot restore, exact-alarm permission) was removed, and repeat
  cycles now advance on app start via the existing expired-cycle reset.
  Repeat options stay dependent on the calendar ring toggle.

- Fixed three add/edit workflow issues: the date picker now preselects today
  when no due date is set yet (instead of opening unselected); the date picker
  dialog gives the Material 3 calendar enough horizontal room (its design width
  is 360dp) so the date grid no longer renders cramped on phone screens; and
  system-calendar events only carry an alert reminder when the task's 响铃提醒
  is enabled — syncing to the calendar with the alarm off now writes a quiet
  event (HAS_ALARM=0, no Reminders row), and turning the alarm off on an
  existing synced task clears its calendar reminder.

- Unified the full-screen destination chrome: the task-detail top bar now
  reuses the shared `FullScreenTopBar` used by backup/archive/add-edit/history
  (same quiet surface row, SemiBold title, subtitle slot, tonal leading icon)
  instead of a hand-drawn variant, and the task-detail delete control now uses
  the app's low-alpha semantic icon container instead of a solid error fill.
  The add/edit form's save button sits directly on the page background without
  a tonal frame, matching the other floating primary actions.

- Polished the add/edit task form and picker chrome: the five time settings
  (date, time, system-calendar, alarm, repeat) now sit in a grouped
  "时间与提醒" section with verb→value rows, a one-line explanation each, and
  explicit dependency reasons when disabled ("需先设置到期日"), matching the
  task-detail "事项信息" dialect; priority keeps its own grouped chip section.
  Date and time pickers both open in a shared branded dialog container (warm
  paper scrim, panel card, quiet hairline border) instead of stock Material 3
  chrome, and the bottom save bar was tightened to the detail screen's action
  vocabulary.

- Converted secondary overlay panels into full-screen destinations consistent
  with the home screen's warm paper visual language: backup/settings, archive,
  add/edit task, and recurring completion history now render as full-screen
  surfaces with a quiet anchored top bar, single safe-area column, and centered
  600dp content cap on large windows — replacing the dimmed bottom-sheet cards
  and drag-handle chrome. A shared `FullScreenScaffold` component owns the
  top-bar, safe-area, and width-cap contract so the destinations cannot drift.
  Short confirmations (delete, uncomplete-recurring, delete completion record),
  the completion-note form, and the date/time pickers remain compact themed
  dialogs. Back priority and task behavior are unchanged; DESIGN.md now
  documents the full-screen presentation contract.

- Fixed task detail content overlapping the status bar: the full-screen
  detail surface now applies system safe-area insets to its header, scrolling
  content, and bottom actions while keeping its background edge-to-edge.

- Replaced the system date and time dialogs with Material 3 Compose pickers
  that inherit the app theme: the add-task sheet now opens a themed calendar
  date picker and a 24-hour time picker in a matching card dialog, and the
  legacy android.app picker helpers were removed. A consistency pass also
  aligned the recurring-task complete button with the shared button shape and
  padding.

- WebDAV backups now treat the configured URL as a backup folder: the app
  writes `dianmao-backup-latest.json` inside the directory, creates missing
  directory levels with WebDAV MKCOL before uploading, and keeps legacy
  full-file URLs (ending in `.json`) working unchanged. The backup format was
  upgraded to version 4 with human-readable ISO-8601 timestamps, app and
  device metadata, a summary block, and readable quadrant/repeat labels next
  to the machine fields; version 3 backup files still import.

- Relaxed the visual rhythm and motion for a calmer feel: spacing tokens
  across screens, rows, sections, and sheets grew by 2-4dp (screen padding
  20dp, row gaps 12dp, section gaps 14dp, sheet padding 16dp) and all
  state-change animations lengthened into a 140-280ms comfort band (sheets
  enter at 200/280ms, exits at 160/220ms) while keeping the
  state-change-only, single-easing motion rules.

- Split the single-file UI into a layered structure: `ui/theme` (theme,
  shapes, quiet helpers), `ui/components` (shared composables and task rows),
  `ui/logic` (pure models, resolvers, and platform helpers), `ui/screens`
  (home, quadrant detail, task detail), and `ui/panels` (add/edit, backup,
  archive, dialogs). `MainActivity.kt` now holds only the Activity entry and
  app assembly, with no behavior change.

- Restructured secondary and capture surfaces to match mainstream to-do apps:
  the task detail page now opens with a completion checkbox beside the title,
  reads metadata as full-width verb rows, and collapses completion history
  into an expandable group; the quadrant detail page uses a single-row tinted
  header with an intro block and splits the list into open and completed
  sections; the add-task sheet pins a capture head (auto-focused title,
  metadata chip row, save button) above a collapsed subtask section, and the
  keyboard Done key saves in add mode; the backup panel became a scrolling
  settings page with a sync status card, a pure WebDAV form with a password
  visibility toggle, and a single primary upload action; the archive panel
  shows richer rows with counts and dates and drops its decorative drag
  handle; dialogs now use text-button dismiss actions and a freeform card for
  the completion-note form.

- Continued core task workflow hardening: date, calendar, alarm, and repeat
  settings now normalize into valid combinations; completion, subtask,
  archive, delete, and completion-history actions show success or failure
  feedback while keeping local saves separate from reminder rescheduling
  failures.

- Improved the main task workflow for narrow screens and failure recovery:
  search with no matches now shows a clear board-level empty state, task rows
  keep one compact overflow action instead of two permanent trailing buttons,
  and task-detail actions wrap into a safer vertical hierarchy with 48dp note
  history controls.

- Task creation and editing now wait for the Room save result before clearing
  or closing the form. Failed saves keep the full draft visible with an inline
  retry message, while reminder or calendar side-effect failures are reported
  separately without treating the durable local save as lost.

- Simplified the home screen layout by removing the browse module (browse title,
  result count, and view-switching filter chips) and moving the search button into
  the app header title row. The floating search panel now expands from the header
  with auto-focus and clear-on-close, the quadrant board gains maximum vertical
  space, and the DianMao title sits closer to the status bar with reduced top
  padding. Today/Upcoming/Inbox smart views are no longer reachable from the home
  screen; the four-quadrant model remains the primary home surface.

- Freed vertical space for the home quadrant board: the app header summary pills
  are now more compact, the home column uses tighter top padding and section
  spacing, and the always-visible search field is replaced by a search button
  that expands a floating search panel with auto-focus and a clear action.

- Polished the task detail, add/edit, quadrant detail, archive, and backup
  surfaces to match the home screen's quiet visual language: the detail top bar
  now has a surface anchor, meta info and completion notes sit inside form
  sections, stats use quiet inline dots, and the primary complete action uses
  the terracotta accent. The archive panel now shares the same sheet shape,
  elevation, and drag handle as the add and backup panels, archived items keep
  their quadrant color dot, backup status switches icons by operation type, and
  the uncomplete-recurring dialog uses a replay glyph.

- Polished the home/list browsing hierarchy: the header now anchors the page
  without another heavy card, smart view controls read more like navigation and
  filtering, the time-focus lane uses calendar-blue timing emphasis, and
  quadrant/list rows are quieter while preserving existing task behaviors.

- Further refined the add/edit task sheet around capture-first entry, making the
  primary task field more prominent while keeping quadrant, time, calendar,
  alarm, and repeat controls visually secondary and behaviorally unchanged.

- Polished the shared add/edit task sheet with a stronger capture-first header,
  clearer primary task input, section icons, quieter date/repeat controls, and
  consistent save feedback/action icon treatment.

- Continued UI polish across backup, archive, and task-detail surfaces: headers,
  form fields, status messages, empty states, and bottom actions now use the
  same quiet icon container and rounded-control language as the home screen.

- Redesigned the in-app icon treatment and launcher icon around a four-quadrant
  compass mark, with quieter icon containers for header actions, metadata chips,
  quadrant detail headers, and reminder/calendar controls.

- Added TickTick-style home smart views for four-quadrant browsing, today,
  upcoming, and unscheduled inbox tasks, with search over task and subtask
  titles.

- Fixed recurring tasks in the home time-focus lane so their displayed reminder
  date/time uses the current repeat cycle instead of the original stored date.

- Added a compact home time-focus lane that surfaces up to three unfinished
  overdue, today, or nearest upcoming timed tasks above the quadrant board.

- Improved capture-first workflow: quadrant detail pages now expose quick add,
  and saving a new task keeps the sheet open with confirmation feedback while
  resetting fields for the next capture.

- Extended the quiet task form polish to secondary controls: subtasks, date/time
  actions, repeat chips, and completion-note editors now share the same rounded
  low-noise input vocabulary.

- Refined quadrant detail and task form polish: quadrant detail rows no longer
  repeat the current quadrant tag, and add/edit controls now use a stronger
  capture field plus quieter quadrant, time, reminder, and repeat setting rows.

- TickTick-inspired visual polish: refine the warm-neutral theme, quieter home
  header, lighter quadrant board, list-like task rows, compact metadata chips,
  and more consistent sheet/detail/archive surfaces.

## 2026-06-15

- 修复二级页面编辑事项时返回行为：BackHandler 现在使用预计算的 backTarget 而非在回调中重复计算，确保返回时优先关闭编辑面板再回到二级页面，避免直接跳回主页面。

- 修复 Android 14+ 预测性返回手势（左滑返回）无法关闭新增/编辑事项面板和备份面板的问题；将 activity-compose 从 1.8.1 升级到 1.9.3，使 BackHandler 正确参与预测性返回调度链。

- 修复编辑/新增事项面板和备份面板打开时，按返回键会关闭底层页面而非面板的问题；现在返回键会先关闭最顶层的面板，再关闭象限详情，最后退出应用。

- 为开启日历同步的事项记录 Calendar Provider 事件 ID，编辑标题、时间或重复规则时更新既有系统日历事件，避免重复插入。
- 关闭事项日历同步或删除事项时，会尝试清理 DianMao 已托管的系统日历事件。
- 新增 Room 2→3 迁移，为任务表添加可空 `calendarEventId` 字段。
- 提醒通知发送前增加 Android 13+ 通知权限检查，避免无权限时触发 lint/运行时风险。
- 新增设备重启后的提醒恢复 receiver，重新调度未完成事项的未来本机提醒。
- 应用启动时会补偿已过期的重复提醒，将每天/每周/每月提醒推进到下一次未来时间后再调度。
- 新增重复提醒时间补偿的本地单元测试。
- 启用 Room schema 导出，提交当前数据库 schema，明确后续迁移基准。
- 新增 JSON 备份格式与本地文件导出/恢复入口，恢复时会清空不可跨设备复用的日历事件 ID。
- 新增 WebDAV 备份设置、手动上传当前备份和下载恢复能力。
- 收紧首页顶部留白，改为更轻量的标题区和统计状态带。
- 首页改为非滚动视口布局，四象限面板随剩余高度自适应。
- 压缩备份与恢复面板布局，常态下不需要滚动即可完成本地和 WebDAV 操作。
- 允许用户配置的局域网 HTTP WebDAV 地址，修复 Android 明文流量拦截导致的上传失败。
- 进一步压缩象限详情页、任务行和首页预览密度，减少大面积留白。
- 重构新增/编辑事项面板，表单内容可按需滚动，底部保存按钮固定显示。
- 更新 README 当前状态。

## 2026-06-14

- 为已添加事项新增编辑入口，可修改内容、象限、日期时间、日历同步、响铃和重复提醒。
- 为已添加事项新增删除入口和确认对话框，删除时同步取消本机提醒。
- 编辑事项时复用新增事项面板，保存后会按最新提醒设置重新调度 AlarmManager。
- 修复新增事项面板内点击层级，确保“事项内容”输入框可正常获得焦点和编辑。
- 新增重复提醒设置，支持不重复、每天、每周、每月。
- 为重复提醒新增 Room 字段、显式 TypeConverter 和 1→2 数据库迁移。
- 提醒广播触发后会按重复规则推进下一次时间并重新调度 AlarmManager。
- 系统日历写入和插入兜底会携带重复规则。
- 接入 Room 本地持久化，事项不再只保存在内存中。
- 新增任务数据层、Repository、DAO 与数据库初始化逻辑。
- 新增原生日期选择器和时间选择器，替换手写时间标签输入。
- 接入系统 Calendar Provider；无可写日历或权限不足时打开系统日历插入页兜底。
- 接入 AlarmManager 本机提醒、通知渠道、提醒广播接收器和 Android 13+ 通知权限请求。
- 更新新增事项面板的日历/提醒开关文案和禁用态，避免未设置时间时保存无效提醒。
- 更新 README 当前状态和剩余待办。

## 2026-06-13

- 初始化 git 仓库。
- 将 Android 模板工程改造为 Kotlin + Jetpack Compose + Material 3。
- 新增 DianMao 首屏原型：
  - 四象限任务筛选。
  - 今日关注摘要。
  - 快速新增事项面板。
  - 时间标签、系统日历意图、响铃意图。
  - 完成事项并记录完成备注。
- 新增 `PRODUCT.md` 和 `DESIGN.md`，记录产品方向和 seed 版设计方向。
- 新增 `README.md`，记录技术栈、当前状态、运行方式和开发约定。
- 将 Gradle wrapper 下载超时时间调整为 60 秒，降低首次同步失败概率。
- 将 Gradle/AGP 调整为本机已缓存组合，并指定 Android Studio JBR，避免首次构建自动下载 JDK。
- 移除 Gradle daemon 自动 toolchain 下载配置，使用本机 Android Studio JBR 构建。
- 启用 AndroidX 与 Jetifier，满足 Compose/AndroidX 依赖要求。
- 修正 Kotlin 包名中 `fun` 关键字的转义，匹配现有 Android namespace。
- 移除不必要的已弃用窗口颜色 API 调用，保留主题层系统栏配置。
