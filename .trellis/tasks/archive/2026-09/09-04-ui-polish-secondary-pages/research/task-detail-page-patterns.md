# Research: Task Detail Page & List/Quadrant Detail Patterns in Mainstream To-Do Apps

- **Query**: How do TickTick, Microsoft To Do, Todoist, Things 3, and Google Tasks design task detail pages and list/quadrant detail screens? Map findings onto DianMao's TaskDetailScreen and QuadrantDetailScreen.
- **Scope**: mixed (external web research + internal code/DESIGN.md analysis)
- **Date**: 2026-09-04

---

## Part 1: External Findings — Per-App Patterns

### 1. TickTick (滴答清单)

**Sources**: ticktick.com/about/features (fetched 2026-09-04); help.ticktick.com (help center exists but article fetches returned empty/404 — detail-layout specifics below are from the official features page + widely documented app behavior; treat fine-grained ordering as approximate).

- **Task detail layout (mobile)**: Full-screen detail page. Top: task title (inline editable), then a row of attribute shortcuts (date, priority, list/tag assignment). Below: subtasks checklist, then a free-form notes area (Markdown supported on desktop/web; images attachable). Comments section exists for shared lists. Bottom of the detail page carries a toolbar of quick actions (date, priority, tags, etc.).
- **Metadata display**: Attribute values render as small icon+text chips/rows directly under the title; tapping any opens a picker. Priority is a colored flag icon; date shows as "today/tomorrow/具体日期" text.
- **Subtasks**: Checklist rows with round checkboxes under a "子任务" section; progress is commonly surfaced as a small progress indicator (TickTick's signature is the progress ring on list headers and the calendar-view completion percentage).
- **Completion state**: Completed tasks get strikethrough + dimmed text; a "Completed" toggle in list options reveals completed items grouped at the bottom.
- **Signature patterns**: Progress ring (list-level completion %), Eisenhower Matrix view (four-quadrant board — direct competitor concept to DianMao's home board), three-column desktop layout where the third column is a persistent detail pane.
- **Empty states**: List views show a centered illustration + short line ("Nothing here yet" style); no heavy empty-state cards.

### 2. Microsoft To Do

**Sources**: support.microsoft.com/en-us/todo — "Add steps, importance, notes, tags, and categories to your tasks" and "My Day and suggestions" (both fetched 2026-09-04).

- **Task detail layout**: Tapping a task opens **detail view**. Order top-to-bottom: (1) task title with round completion checkbox to its left, (2) a **step counter line** beneath the title ("x of y steps completed" — official: "a counter beneath each task's name displays the total number of steps the task contains and how many have been completed so far"), (3) **+ Add step** inline input, (4) **Add note** area (rich text on web), (5) metadata rows: **Add to My Day**, **Add due date**, **Remind me**, **Repeat**, **Pick a category**, **Add file** — each a full-width tappable row with icon + label, (6) created/completed timestamps at the very bottom in small muted text.
- **Metadata display**: Full-width **rows with icons**, not chips. Each row is a verb phrase ("Add due date", "Remind me") that becomes the value once set ("Due Today"). This "verb becomes value" pattern is To Do's most distinctive detail-page trait.
- **Subtasks ("Steps")**: Inline input at top of the detail; each step is a checkbox row; completed steps stay visible with strikethrough; the x-of-y counter gives progress without a ring or bar.
- **Completion state**: Circle checkbox left of title; completed tasks dim + strikethrough and move to a "Completed" collapsed group at the bottom of lists. Completed timestamp is recorded and shown in detail view.
- **My Day**: A smart list that resets nightly; tasks are added via "Add to My Day" from detail view or suggestions (lightbulb icon). Unfinished My Day items roll back to Tasks list.
- **Empty states**: Simple centered text + subtle illustration; My Day empty state explicitly invites "add tasks for today".

### 3. Todoist

**Sources**: todoist.com/help — "Use the task view to manage tasks in Todoist" (eDeRDO0C), "Use sub-tasks in Todoist" (kMamDo), "View completed tasks in Todoist" (J19h2s), "Set a priority in Todoist" (Wy82Jp), plus "How to Use Todoist Effectively" guide (all fetched 2026-09-04).

- **Task view layout (Android, documented)**: Tapping a task opens task view with: (1) **breadcrumb** at top (project › section › parent task — tappable), (2) **task name** (inline editable), (3) **Date field** directly under the breadcrumb for date/time/duration, (4) **Sub-tasks section** (view/add/edit/reorder, with show/hide completed), (5) **Add a comment field**, then a row of **task chips**: Description, Assignee, Priority, Reminder, Labels, Deadline, Location, Move to…. Top-right: three-dot task menu (Edit, Duplicate, Copy link, Show/Hide completed sub-tasks, View activity, Delete).
- **Metadata display**: **Chips** — explicitly named "task chips" in the help doc. On web/desktop the same metadata lives in a right-hand sidebar as labeled rows (Project, Date, Deadline, Assignee, Priority, Labels, Reminders, Location).
- **Subtasks**: Own section inside task view; completed sub-tasks stay visibly crossed out until parent completes (guide: "completed sub-tasks will stay visibly crossed out until the parent task is completed"); "Show completed" toggle in the three-dot menu; sub-tasks can carry their own dates/priorities.
- **Completion state**: Circle checkbox left of task name; click circle or swipe right on mobile. Completed tasks vanish from active lists and appear in a "Completed tasks" group at the bottom of a section when toggled on via Display options; Reporting/Activity log keeps history (7 days free).
- **Signature patterns**: Breadcrumb navigation inside task view; chevron arrows to jump to next/previous task; Todoist Zero empty state ("You've reached Todoist Zero" celebratory but restrained illustration); priority as 4 colored flag levels (P1 red → P4 colorless) that also drive sort order.
- **Empty states**: "Todoist Zero" screen — centered illustration + encouraging line; per-view empty text otherwise minimal.

### 4. Things 3

**Sources**: culturedcode.com/things/support — "Scheduling To-Dos" (2803579), "Writing Notes in Things" (4438545), "An In-Depth Look at Today, Upcoming, Anytime, and Someday" (4001304) (fetched 2026-09-04).

- **To-do detail layout**: Detail is an expanded, sheet-like view of the to-do row itself (no separate page chrome): (1) **checkbox + title** at top, (2) **When** date field (calendar icon when unset; the assigned date text when set) — Things' "When" answers "When can I start working on this?", distinct from deadlines, (3) **reminder time** (clock) and **deadline** (flag) as inline fields, (4) **Notes** — a large always-present notes area directly under the title block (Markdown, bullets, checklists; grows with content), (5) **Checklist items** nested inside the to-do (collapsed behind a chevron until added), (6) tags row at the bottom.
- **Metadata display**: Inline **text fields in the detail body**, not chips — "When", reminder, deadline read as plain sentences ("Tomorrow", "Fri", flag icon + date). Extremely low-chrome.
- **Subtasks (checklists)**: Live inside the to-do body between title and notes; toggled with the same round checkbox as to-dos; can be reordered by drag.
- **Completion state**: Round checkbox fills with blue; completed to-dos move to **Logbook** (permanent archive) and can be shown at the bottom of a list temporarily ("x to-dos were moved out of the Inbox"-style transient notices; completed items fade out of Today automatically).
- **Signature patterns**: **Anytime grouping** — Anytime list shows loose to-dos floating at top, then grouped under their parent project/area headings; Today items reappear inside Anytime with a yellow star; **Upcoming** shows the next 7 days as separate day sections; inactive (future-start) tasks "hibernate" and visually take a back seat; **This Evening** section at the bottom of Today.
- **Empty states**: Very quiet — a single muted line per list; Things avoids illustrations in most empty lists.

### 5. Google Tasks

**Sources**: 9to5google.com — "Google Tasks redesign rolls out with cards, drops bottom app bar" (2024-08-26), "Google Tasks adding 'Completed' date to reminders" (2025-02-19), "Google Tasks adds incomplete counts to lists" (2025-03-27), guide index (fetched 2026-09-04). support.google.com Tasks articles were unreachable (transport errors) — detail-view specifics below combine 9to5Google reporting with well-established app behavior; marked approximate where uncertain.

- **Detail card layout**: Tapping a task opens a **bottom-sheet detail card** (Material 3): (1) **title** field at top, (2) **"Add details"** free-text area, (3) **"Add subtasks"** section with indented checkbox rows, (4) a bottom row of three actions: **date picker (Set date)**, **"…" overflow (delete, etc.)**, and a **sort/expand chevron**; list selector chip at top of the sheet. Everything lives in one flat card — no sections, no dividers.
- **Metadata display**: Minimal — date appears as a small chip/row under the title once set ("Today", "Tomorrow" + optional time); no priority, no labels, no repeat (by design, Google Tasks is deliberately basic).
- **Subtasks**: Indented rows under "Add subtasks" with the same round checkbox; one indent level; drag to reorder.
- **Completion state**: Round checkbox; completed tasks move into a separate **"Completed" card** at the bottom of the list (2024 redesign: "Active ones appear at the top, while there's another card for 'Completed'"). Since Feb 2025 each completed task shows a **"Completed" line with day + date** under its name inside that card.
- **Signature patterns**: Card motif for list sections (active card + completed card); bottom app bar removed in 2024 redesign (FAB only); incomplete-count pills next to list names in the switcher (2025); "Completed" date line as a lightweight completion-history substitute.
- **Empty states**: Single muted line ("No tasks" style) centered in the list; the Completed card simply doesn't render when empty.

### Cross-App Synthesis (the convergent pattern)

| Aspect | Convergent mainstream pattern |
|---|---|
| Hero | Task title is always the hero — large, inline-editable, completion checkbox beside it (To Do, Things, Todoist) or above the body (TickTick, Google Tasks) |
| Metadata | Either **full-width icon rows** (To Do, Things) or **compact chips** (Todoist, TickTick); never a dense form |
| Subtasks | Own section directly under title/metadata, checkbox rows, completed stay visible with strikethrough, progress surfaced as "x of y" counter (To Do) or ring (TickTick) |
| Notes | Always after subtasks, generous body text, no chrome |
| History/completed | Collapsed "Completed" group at bottom (Google Tasks, Todoist, To Do) or separate archive (Things Logbook); completion **timestamps** shown in muted small text (To Do, Google Tasks) |
| Bottom action bar | Rare — Google Tasks uses a 3-icon row inside the sheet; TickTick a quick-action toolbar; To Do/Todoist/Things put actions inline. A persistent bottom bar is a DianMao-specific choice (documented in DESIGN.md §4) |
| Empty states | One muted line or small illustration; never a heavy card |

---

## Part 2: Internal Baseline (what DianMao has today)

### Files Found

| File Path | Description |
|---|---|
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` (L4520–4853) | `TaskDetailScreen` — top bar, title, meta section, subtasks, calendar button, completion note, stats, history, bottom action bar |
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` (L1993–2116) | `QuadrantDetailHeader` + `QuadrantDetailEmptyState` + `InlineStatDot` |
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` (L1052–1096) | Quadrant detail list assembly (header item + task rows + empty state) |
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` (L2954–3017) | `SubTaskReadOnlySection` — checkbox rows in a bordered container, progress label |
| `DESIGN.md` | Design system: colors, elevation, motion, named rules |

### Current TaskDetailScreen structure (observed, L4520–4853)

1. Top bar: back + "事项详情" + edit button (edit tinted terracotta at 50% container alpha)
2. LazyColumn (18dp horizontal padding, 11dp gaps): title (headlineSmall SemiBold, dims when completed) → `TaskFormSection` "事项信息" with FlowRow of `CompactMeta` chips (quadrant, due, 系统日历, 响铃, repeat, 已完成 N 次) → `SubTaskReadOnlySection` (only if subtasks exist) → conditional "打开系统日历提醒" outlined button → "完成心得" section (if note) → stats FlowRow (`InlineStatDot` 总完成次数 / 历史记录 / 上次完成) → "完成历史" heading → empty-history card or `CompletionItem` list
3. Bottom action bar (the ONLY shadowed element, 8dp shadow + 2dp tonal per DESIGN.md L229): full-width 完成/标为未完成 filled button + 归档-or-hint row + delete icon button

### Current QuadrantDetailScreen structure (observed, L1052–1096, L1993–2116)

1. `QuadrantDetailHeader`: quadrant color at **0.08 alpha** tint, 14dp radius, quiet border 0.16 — back button + IconTile + quadrant title (titleLarge), one-line description, FlowRow of 3 `InlineStatDot`s (未完成 in quadrant color, 有提醒 in secondary, 已复盘 in teal)
2. Task list: `TaskRow` items with drag-to-reorder (8dp shadow + 1.02 scale only while dragging), `showQuadrantMeta = false`
3. `QuadrantDetailEmptyState`: 0.07 alpha tint card, IconTile + "这个象限还没有事项" + guidance line

---

## Part 3: Layout Options for DianMao

### Constraints that apply to every option (from DESIGN.md)

- **Ten Percent Accent Rule** (L191, L317): terracotta accent < ~10% of visible surface on any task screen. The 完成 button in the bottom bar is the main accent spend; keep everything else neutral or quadrant-tinted.
- **Quadrant Overlay Rule** (L195, L308): quadrant color only as low-alpha tint + 12dp dots — **4.5% (0.045) on home board panes, 8% (0.08) on quadrant detail headers and empty states** — never saturated backgrounds. Detail-screen options must not raise these alphas.
- **No shadows at rest** (L223–229, L247–249, L319): flat surfaces + quiet borders (outline at 0.10–0.22 alpha). Only exception on these screens: the TaskDetail bottom action bar (8dp shadow + 2dp tonal, documented).
- **Motion**: 120–220ms, state-change only, FastOutSlowInEasing (L232–255). Any new reveal (e.g., collapsible completed group) must fit: 150–220ms fade/slide, no choreography.
- **Typography**: Title Large for detail headings (L208), Body Large for notes (L210), Label Medium for meta chips (L213). One family, weight/scale only.
- **Component tokens**: TaskFormSection = PanelShape 18dp, surfaceVariant 0.26 alpha, quiet border 0.14 (L274); chips = ChipShape 8dp (L284); quadrant dots 12dp (L286).

### Option A — TaskDetailScreen: "Todoist-style hero + grouped cards" (minimal change)

- Keep top bar; promote the title into a true hero: title + a single-line quadrant chip (12dp dot + shortTitle) directly beneath, no section wrapper.
- Merge the current "事项信息" FlowRow chips into one compact meta row under the hero (chips stay ChipShape 8dp, surfaceVariant background per `chip-meta` token) — mirrors Todoist's chip row under the date field.
- Group remaining content into existing `TaskFormSection` cards: 子事项 (with To Do-style "x/y" progress label — already present via `subTaskProgressLabel`), 完成心得, 完成历史.
- Move completed history into a collapsed group with a "显示已完成记录" toggle (Todoist "Show completed" pattern), revealed with 150ms fade.
- Bottom action bar unchanged (it is already the documented elevation exception).
- **Fits**: quiet borders, flat elevation, accent stays in bottom bar + edit button only.

### Option B — TaskDetailScreen: "To Do-style verb rows" (medium change)

- Hero: checkbox + title on one line (completion toggle moves up beside the title, To Do/Things pattern), quadrant chip beneath.
- Replace the chip FlowRow with **full-width icon rows** in one TaskFormSection: 提醒/日期 (Schedule icon), 系统日历 (CalendarMonth), 响铃 (Alarm), 重复 (Repeat) — each row reads "添加提醒" when unset and the value when set (To Do's verb-becomes-value pattern). Calendar-blue reserved for the calendar row per the Time Has Its Own Voice Rule.
- Subtasks keep their bordered container but gain the To Do-style counter line ("已完成 x/y，共 y 项") as the section subtitle.
- Completion history becomes a "Completed"-card group at the bottom (Google Tasks pattern): collapsed card, count in the header, expandable; each entry keeps its timestamp line in Label Medium muted text (Google Tasks' "Completed" date line).
- Bottom action bar keeps 完成 primary; consider demoting the in-bar delete to the top-bar overflow to reduce bar density (structural note only).
- **Fits**: rows use quiet borders not shadows; accent still confined to primary button; quadrant color only in the chip dot.

### Option C — TaskDetailScreen: "Things-style flat detail" (largest change)

- Remove the top bar title ("事项详情") entirely; back + edit float on the app surface (Things has no detail-page chrome).
- Title hero with inline completion checkbox; then a single flat column separated by hairline dividers (Divider at 0.34 alpha per L181) instead of cards: When/提醒/重复 as plain text rows → subtasks → notes → history.
- Metadata reads as sentences ("明天 09:00", "每周重复"), no chips, no section headers — lowest-chrome option.
- History as a quiet "已完成 N 次 · 最近 <date>" summary line + expandable list.
- **Trade-off**: departs from the existing TaskFormSection card language; only worth it if the goal is a calmer, Things-like reading surface. Still satisfies flat-elevation and accent rules.

### Option A — QuadrantDetailScreen: "Keep tinted header, add completed group" (minimal change)

- Header unchanged (0.08 alpha is the documented home of the 8% tint).
- Below active tasks, add a collapsed "已完成 N" group (Google Tasks' Completed card): quiet-border card, count in Label Medium, expands with 150ms fade to show completed `TaskRow`s dimmed per existing completed-row treatment (surfaceVariant 0.38 alpha, border 0.12 per L270/294).
- Empty state unchanged.
- **Fits**: no new tint surfaces; stat dots already follow the dot pattern.

### Option B — QuadrantDetailScreen: "Sticky slim header + sectioned list" (medium change)

- Compress the header to a single row (back + dot + title + open-count) that can stick at the top on scroll; move the description and 3 stat dots into a non-tinted intro block above the list, or drop the description to one line under the title.
- List splits into "未完成" (default) and "已完成" (collapsed) sections with plain Label Medium section headers — Todoist section pattern.
- Header tint stays 0.08; the intro block uses app surface so the 8% tint area shrinks (keeps the Quadrant Overlay Rule comfortably).
- **Fits**: quiet borders for the sticky header (0.14–0.16 border, no shadow at rest — a sticky header with shadow would violate the rest-state rule; use border only).

### Option C — QuadrantDetailScreen: "Things Anytime-style grouping" (largest change)

- Keep a slim tinted header; group tasks within the quadrant by due status: 今天 / 已安排(未来) / 无日期 / 已完成 — echoing Things' Anytime list, which floats undated items and groups the rest under headings.
- Each group header is a Label Medium line with a 6dp dot in quadrant color at reduced alpha; completed group collapsed by default.
- Empty state per group only when the whole quadrant is empty (current card), otherwise quiet one-line hints per empty group (Things' muted-line pattern).
- **Trade-off**: most informative but adds hierarchy; ensure group headers don't read as "dense enterprise panels" (Feishu-style weight is explicitly rejected, L311).

### Recommendation-neutral summary

- All options keep: 0.08 alpha header tint (quadrant detail), 4.5% home panes untouched, quiet borders, no rest shadows, accent < 10%, 120–220ms state-change motion.
- The strongest cross-app convergences worth adopting regardless of option: **"x of y" subtask counter** (To Do), **collapsed Completed group with timestamps** (Google Tasks/Todoist), **verb-becomes-value metadata rows** (To Do) as an alternative to chips, and **breadcrumb/quadrant context chip** (Todoist) inside TaskDetail.

---

## Caveats / Not Found

- **TickTick**: help.ticktick.com articles returned empty or 404 on fetch; the detail-screen ordering (title → attribute chips → subtasks → notes → comments) is from the official features page plus widely documented app behavior, not a fetched help article. Treat exact ordering as approximate; the progress-ring and Matrix-view signatures are confirmed from the features page.
- **Google Tasks**: support.google.com consistently failed with transport errors; detail-card composition is from 9to5Google's redesign/completed-date reporting (which includes screenshots descriptions) plus established app behavior. The three-action bottom row and "Add details/Add subtasks" labels are stable across versions but not verified against a primary source today.
- **Microsoft To Do**: the "My Day and suggestions" and "Add steps…" articles were fetched successfully; the full detail-view field ordering (My Day / due date / remind / repeat / category / file rows) is from those articles' structure plus the documented screenshots — reliable, but the exact row order on Android may differ slightly from web.
- **Things 3**: checklist-inside-to-do placement (between title and notes) is from the support articles' descriptions; the visual claim that notes sit directly under the title block is consistent across the fetched docs but the articles don't include a layout diagram in text form.
- No app studied uses a persistent bottom action bar like DianMao's; this is a documented DianMao-specific elevation exception (DESIGN.md L229), so the external material offers no direct precedent for it — options above deliberately preserve it rather than remove it.
