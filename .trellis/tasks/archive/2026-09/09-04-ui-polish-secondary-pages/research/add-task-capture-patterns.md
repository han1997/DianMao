# Research: Add-task / quick-capture flow & edit forms in mainstream to-do apps

- **Query**: How do mainstream to-do/task apps design their add-task / quick-capture flow and edit forms? Map findings onto DianMao's AddTaskPanel and propose 2-3 structural reorganization options.
- **Scope**: mixed (external web research + internal code/DESIGN.md analysis)
- **Date**: 2026-09-04

---

## Part 1 — External findings per app

### 1. Todoist (Quick Add) — richest reference

Sources: [Use Task Quick Add in Todoist](https://todoist.com/help/todoist/features/use-task-quick-add-in-todoist-va4Lhpzz), [Customize Quick Add](https://todoist.com/help/todoist/features/customize-quick-add-in-todoist-eqRRlZJNN), [How to Use Todoist Effectively](https://todoist.com/inspiration/how-to-use-todoist-effectively)

- **First-frame focus**: Quick Add opens as a compact dialog/sheet with the **task name field focused immediately**; typing is the first action. On mobile the Dynamic Add button can be dragged anywhere and opens the same capture surface.
- **Progressive disclosure**: The first frame is *name field + one row of task-action chips + Add button*. The **description field is hidden by default** and revealed by tapping `+` or pressing `↓` (can be pinned visible in settings). Task actions shown are user-configurable: priority, date, deadline, assignee, reminders, labels, location, attachment — each can be shown/hidden and reordered; labels can be icon-only.
- **Selector presentation**: All selectors are **inline chips in a single horizontal row** under the name field (e.g. calendar icon = schedule, flag icon = priority). Selecting one opens a popover/bottom-sheet picker (calendar grid with shortcuts "Today / Tomorrow / Next week"; priority dropdown p1–p3). Nothing expands the form vertically — pickers overlay.
- **Natural language parsing**: The name field itself parses dates (`tomorrow at 4 PM`, `every other Tuesday`), deadlines (`{march 30}`), priority (`p1`), labels (`%email`), reminders (`!14:00`), project (`#Work`), section (`/Admin`), assignee (`+Lucile`). Recognized tokens render as highlighted chips *inside the text field*; tapping a highlighted word converts it back to plain text. This means **most users never leave the title field** to set metadata.
- **Save affordances**: Primary button is **"Add task"** at the bottom of the quick-add surface. The critical pattern: after saving, **the sheet stays open and the name field clears, ready for the next task** — rapid serial capture. Enter submits. There is no separate "save and add another" button; *staying open is the default behavior* of quick add (the full task editor is a different surface).
- **Sheet height / scroll**: Compact — roughly a two-row sheet (name + chips + description slot + Add button). It never scrolls; the full editor (task view) is where description, sub-tasks, comments live.
- **Subtask capture**: Not in Quick Add. Sub-tasks are added inside the **task view** ("Add sub-task" under the parent), each sub-task being a full task with its own date/priority. Drag-and-drop (or long-press drag on mobile) nests a task under a parent.

### 2. Things 3 (Magic Plus / new-to-do) — keyboard-first, zero-chrome

Sources: [Getting Productive with Things](https://culturedcode.com/things/guide/), [Scheduling To-Dos](https://culturedcode.com/things/support/articles/2803579/), [Natural Language Input](https://culturedcode.com/things/support/articles/9780167/), [Adding To-Dos From Other Apps](https://culturedcode.com/things/support/articles/2803569/)

- **First-frame focus**: Tapping the Magic Plus (or starting to type on Mac) creates a **new to-do row inline in the list with the title field focused and keyboard up**. There is no modal form at all — the new to-do *is* a list row. Title first, everything else optional.
- **Progressive disclosure**: The new-to-do row shows only the title line. A **toolbar row appears beneath the keyboard** with small icon buttons: checklist, notes, When (calendar), deadline (flag), tags, move. Notes and checklist bodies are hidden until invoked. Metadata that is set appears as small inline badges on the row; unset metadata is invisible.
- **Selector presentation**: **When** is a popover date picker with shortcuts (Today / This Evening / Someday) plus a calendar grid; it answers "when can I start?" rather than "when is it due?". **Deadline** is a separate flag-button picker. **Reminders** attach to start dates only. Natural language works inside the When picker ("tues 8am", "17d", "3rd wed jul") — pull down in the picker to reveal the NL search field on iOS.
- **Save affordances**: **No save button.** The to-do is saved live as you type; pressing Return commits the title and (on Mac) creates the next to-do; tapping away or closing the keyboard dismisses. "Save and add another" is implicit — Return *is* that action.
- **Sheet height / scroll**: N/A — inline row expansion, not a sheet. The pattern's lesson: the capture surface can be as small as one text line plus a keyboard-accessory toolbar.
- **Subtask capture**: **Checklist** is one of the keyboard-toolbar buttons; tapping it turns the current to-do into a checklist and each Return adds a checklist item. Paste multi-line text into a checklist field and each row becomes an item (first row becomes title, rest cascade into notes when pasted into Title).

### 3. TickTick — quick add bar + detail expansion

Sources: [TickTick Features](https://ticktick.com/about/features), help center structure (help.ticktick.com)

- **First-frame focus**: The add-task entry is a **quick-add input bar** (bottom of list on mobile, top of list on desktop) with the text field focused. Web/desktop also has a global shortcut to open a capture window from anywhere.
- **Progressive disclosure**: The quick-add bar shows the title field plus a compact row of icon affordances (date, reminder, etc.); the **full detail editor opens only when you tap the expanded/detail icon** — there description, sub-tasks, tags, priority, list assignment appear as a full form. NLP is built into the title field: "Just type in your information, and it will be identified instantly" (dates/times parsed as you type).
- **Selector presentation**: Date/reminder/list/priority are **icon buttons in the quick-add row**; picking one opens a popover or bottom sheet. In the detail editor they are rows/sections. Priority is a flag-style picker with colored levels; the Eisenhower Matrix view exists but priority inside the add flow stays a simple picker.
- **Save affordances**: Send/enter on the keyboard commits the task; the quick-add bar **stays in place for the next entry** (serial capture). A dedicated "expanded view" arrow opens the full editor before saving.
- **Sheet height / scroll**: Quick add is a one-line bar (minimal); detail editor is a full-height page/sheet with scrolling sections.
- **Subtask capture**: Sub-tasks live in the detail editor as a checklist section; each item gets its own add row.

### 4. Microsoft To Do — two-step add + detail expansion

Sources: official support pages unreachable during research (404s / transport errors); below is well-established product behavior from general knowledge — **verify against the app before relying on specifics**.

- **First-frame focus**: Each list has an **"Add a task" input row** docked at the top of the list. First frame is a single-line text field with placeholder "Add a task"; keyboard opens on tap.
- **Progressive disclosure**: Step 1 is title only. Pressing Enter creates the task and keeps the input row active for the next one. **Step 2 (details)** opens by tapping the created task: a full detail page with note field, "Add step" (subtask) list, due date, reminder, repeat, and "My Day" toggle — each as its own row.
- **Selector presentation**: In the detail page, due date / reminder / repeat are **labeled rows with icons** (not chips); tapping opens a picker. "My Day" is a star/sun toggle. In the quick-add row, a small calendar icon and bell icon sit at the right edge of the input line for date/reminder without opening details.
- **Save affordances**: Enter on keyboard = create + keep input open (serial capture). Detail page auto-saves; back gesture returns to list. No explicit save button.
- **Sheet height / scroll**: Quick add is one line; detail is a full page with scrolling sections.
- **Subtask capture**: "Add step" rows under the task title in the detail page; steps are checked off inline.

### 5. Google Tasks / Any.do — minimal capture

Sources: support.google.com unreachable during research; general product knowledge — **verify before relying on specifics**.

- **Google Tasks**: Add is a **bottom-sheet with a single title field** ("Add a task") plus a "Add details" link and a Save button. Details (date, description, subtasks) are collapsed behind "Add details" — a second screen. Subtasks are added via "Add subtasks" inside the detail view. Save button is explicit (bottom of sheet); keyboard Done also commits.
- **Any.do**: A floating **"+" tab** opens a full-screen (or near-full) modal with a title field focused, and a horizontal row of type chips (Task / Grocery etc. depending on context); date/time/reminder/notes are rows revealed below the title. Keyboard action button commits; the modal closes on save (single capture, not serial).

### Cross-app synthesis (the consensus pattern)

| Dimension | Consensus |
|---|---|
| First frame | Title field focused, keyboard up, everything else optional or hidden |
| Metadata | One compact row of icon chips under the title; pickers overlay, never push content down |
| Serial capture | After save, the surface stays open with a cleared title field (Todoist, TickTick, MS To Do) — or Return creates the next item (Things) |
| Save button | One primary action; "add another" is a *behavior* (sheet stays open), not a second button |
| Full editor | Separate, heavier surface (detail page) — quick add and edit-detail are different surfaces |
| Subtasks | Live in the detail surface, not the quick-add surface (Todoist, TickTick, MS To Do, Google Tasks); Things is the exception (checklist button on keyboard toolbar) |
| NL parsing | Title field parses dates/priority so users rarely touch pickers (Todoist, TickTick, Things) |

---

## Part 2 — Internal findings: DianMao's current AddTaskPanel

### Files Found

| File Path | Description |
|---|---|
| `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` L3021–3413 | `AddTaskPanel` — 90%-height bottom sheet, add + edit modes |
| `MainActivity.kt` L2670–2733 | `TaskFormSection` — sectioned card (emphasized variant for title) |
| `MainActivity.kt` L2736–2790 | `FormSettingBlock` — small setting card (quadrant, time, repeat) |
| `MainActivity.kt` L2830–2901 | `SubTaskEditor` — subtask draft field + add button + rows |
| `MainActivity.kt` L3848–3875 | `RepeatRulePicker` — FilterChip FlowRow |
| `MainActivity.kt` L3879–3943 | `DateTimePickerRow` — date/time OutlinedButtons + clear |
| `MainActivity.kt` L4079+ | `ToggleRow` — calendar / alarm switch rows |
| `DESIGN.md` | Design system: colors, motion vocabulary, named rules |

### Current structure (top → bottom of the sheet)

1. Drag handle (38×4dp, outline 0.34) — L3129
2. Header row: IconTile (Add/Edit) + Title ("先记录一件事" / "编辑事项") + subtitle + close button — L3136–3166
3. Scrollable column (`weight(1f)`, `verticalScroll`) — L3167–3303:
   - `TaskFormSection` (emphasized, primaryContainer 0.32 tint, primary border 0.18): title OutlinedTextField (min 118dp, 4–6 lines, 18sp) + helper text — L3174–3208
   - `SubTaskEditor` (TaskFormSection "子事项") — L3209–3227
   - `FormSettingBlock` "象限": 4 FilterChips with ColorDot in FlowRow — L3228–3247
   - `DateTimePickerRow` "时间": 选择日期 / 选择时间 OutlinedButtons + 清除 — L3248–3275
   - `ToggleRow` "系统日历" (enabled only when date set) — L3276–3286
   - `ToggleRow` "响铃提醒" (enabled only when date set) — L3287–3297
   - `RepeatRulePicker` (enabled only when date+alarm set) — L3298–3302
4. Save-feedback pill (AnimatedVisibility, 120ms fade + 160ms slide, primaryContainer 0.54, auto-dismiss 2200ms) — L3304–3348
5. Primary save Button, full width, RowShape, "保存并继续记录" / "保存修改并关闭" — L3349–3408

### Behavioral facts

- Sheet: `fillMaxHeight(0.90f)`, SheetShape (22dp top), 6dp elevation, raised surface, scrim #211815 @ 0.16, `imePadding` — L3097–3120
- Add mode save: panel stays open, fields cleared (title, date, time, calendar, alarm, repeat, subtasks), feedback pill "已保存到 [象限]，可继续记录" — L3379–3391. **Priority/quadrant is preserved** across saves (kept, not reset).
- Edit mode save: panel dismisses — L3379–3380.
- **No auto-focus on the title field**: the panel opens without requesting focus and without showing the keyboard; the user must tap the field first. (No `FocusRequester` anywhere in AddTaskPanel.)
- Title field is oversized for capture: min 118dp / 4–6 lines for what is usually a one-line task.
- Every control is always visible: 7 stacked cards (title, subtask, quadrant, time, calendar, alarm, repeat) before the save button — the scroll column is ~2 screens tall on a phone.
- Subtask editor sits **above** quadrant/time — subtasks get higher visual priority than classification, unusual vs. all researched apps.
- IME action on title is `Done` → only clears focus (L3199–3200); it does not save.
- Dependencies: calendar/alarm require date; repeat requires date+alarm — enforced via `enabled` flags, not grouping.

### DESIGN.md constraints that bind any reorganization

- **10% accent rule**: saturated terracotta < ~10% of visible surface (DESIGN.md L191, L317)
- **Quiet borders**: outline 0.10–0.22 alpha; default 0.22, completed 0.12, time lanes 0.14 (L294)
- **Flat elevation**: shadows only for sheets/menus/drag/detail bottom bar (L223–230)
- **State-change-only motion, 120–220ms**, FastOutSlowInEasing only; save-pill reveal is 120ms fade + 160ms slide (L234–243, L306)
- **Capture-First Pattern** is a named input rule: quick-capture field visible immediately; quadrant/date/alarm/note "revealed progressively below" (L281)
- **Capture-First Save Pattern** is a named flow rule: panel does not dismiss on save; pill feedback; 2200ms auto-clear (L297)
- Sheet spec: 90% height, SheetShape 22dp, 6dp elevation, 38×4dp drag handle (L271)
- Task Form Section spec: PanelShape 18dp, surfaceVariant 0.26, quiet border 0.14 (L274)
- Don'ts: no Feishu-style operational weight, no entrance choreography, no unusual Android controls (L311–321)

---

## Part 3 — Structural reorganization options for AddTaskPanel

All options preserve: 90% sheet, capture-first save (stay-open + pill), edit mode dismiss-on-save, existing callbacks/state model, light-only M3, quiet borders, 120–220ms motion.

### Option A — "Sticky capture head + collapsed detail body" (Todoist/Things hybrid; recommended)

Structure the sheet as **two zones**:

- **Sticky head (never scrolls)**: drag handle → compact header (title + close, subtitle dropped or one-line) → title field (single-line growing to ~3 lines, auto-focused via `FocusRequester` on first open in add mode) → **one metadata chip row** → save button pinned at bottom of the head zone.
  - The chip row replaces the stacked cards for quick state: `[象限 chip w/ ColorDot] [日期 chip] [时间 chip] [日历 toggle-chip] [响铃 toggle-chip] [重复 chip]` — each chip shows current value ("未设置" when empty) and opens the existing picker/dialog on tap. Selected/active chips use terracotta container per DESIGN.md chip state rule; unset chips stay neutral with quiet border.
  - Save button stays full-width primary under the chips (or in the same row as a compact pill + expand affordance).
- **Scrolling body (below, collapsed by default)**: "子事项" and "备注/详情" as collapsed sections — a single tappable row each ("添加子事项 +", chevron) that expands in place (AnimatedVisibility, 180ms fade+slide, within the state-change rule). Subtask editor content is the existing `SubTaskEditor`.

Why it fits: this is exactly the Todoist quick-add shape (title + chip row + one primary action, pickers overlay) and the Things principle (title is the only thing always visible). It makes the first frame ~40% shorter, keeps every current capability one tap away, and the chip row reads as "quiet structure" rather than seven stacked cards. The 10% accent rule is easier to satisfy because the emphasized title card's primaryContainer tint can shrink or disappear — the focused field indicator becomes the accent moment.

Trade-offs: chips must handle the dependency chain (calendar/alarm disabled until date set — keep the existing `enabled` semantics as chip disabled state); quadrant as a chip opens a small menu rather than always-visible FlowRow (one extra tap to change quadrant, but quadrant is already "classify later" per the product principle).

### Option B — "Segmented groups: 记录 / 计划 / 细节" (MS To Do detail-page logic, sheet-sized)

Keep the current stacked-card architecture but **group the seven cards into three labeled segments** with a compact segmented header or anchor row:

- **记录** (capture): title field + subtask editor (subtasks are "more capture", matching the current placement)
- **计划** (plan): quadrant chips + date/time row (the two classification decisions)
- **提醒** (alerting): calendar toggle + alarm toggle + repeat picker (all three already share the date dependency)

Each segment is one `TaskFormSection` with a header; inside, the existing `FormSettingBlock`s remain. The segment headers act as a mini table of contents; optionally the header row is horizontally scrollable anchor chips that scroll the form to the section (scroll-driven, but it is a *user-initiated* state change, so allowed).

Why it fits: minimal structural risk — reuses every existing composable, only re-nests them; the dependency chain (date → calendar/alarm → repeat) becomes visually self-explanatory because the dependent controls live in one segment. Matches MS To Do's "details grouped in labeled rows" model.

Trade-offs: the sheet still shows all controls at once (no progressive disclosure gain); the anchor-chip row risks feeling like Feishu-style operational weight if over-decorated — keep it as plain label text or quiet chips.

### Option C — "Two-surface split: quick capture sheet + full editor" (Todoist literal model)

Split add and edit into different surfaces, as every researched app does:

- **Add mode** becomes a **compact capture sheet (~35–45% height)**: title field (auto-focused) + quadrant chip + date chip + save button. Nothing else. Save keeps the sheet open with the existing pill (capture-first save pattern unchanged). A single "更多选项" text/chevron affordance expands the sheet in place to the full 90% editor (AnimatedVisibility height animate, 220ms — a state change, allowed) or navigates to it.
- **Edit mode** keeps the current full 90% panel with all sections (possibly grouped per Option B), since editing is inherently a "refine" task.

Why it fits: the purest expression of "capture before classification" — the add surface finally matches the principle's weight class. Serial capture gets dramatically faster (less to scroll past, keyboard covers less). The expand affordance preserves access to subtasks/repeat during capture.

Trade-offs: largest implementation change (two layouts, height animation, state handoff between compact and expanded); the in-place height expansion is a new motion pattern that must be registered in DESIGN.md's motion vocabulary; risk of users not discovering subtasks during capture (mitigate: the pill or a hint row can surface "添加子事项" when relevant).

### Recommendation summary

- **Option A** gives the best capture-speed-to-risk ratio and directly mirrors the strongest consensus pattern from research (Todoist chip row + Things title-first) while keeping one surface.
- **Option B** is the low-risk fallback if chip-based pickers are judged too big a change.
- **Option C** is the most principled but heaviest; consider it as a follow-up evolution of Option A (the sticky head of A is already half of C's compact sheet).

Cross-cutting notes for any option:

- Auto-focus + keyboard on add-mode open (all five researched apps do this; DianMao currently does not) — use `FocusRequester` in a `LaunchedEffect(visible)` gated to `!isEditing`.
- Consider making the title field single-line-growing (min 1 line) instead of min 4 lines; 118dp of empty field is the single biggest first-frame cost.
- IME `Done` on the title field could trigger save in add mode (Todoist/Things behavior: Enter commits and continues) — but this changes interaction behavior, so flag it for the main agent to decide.
- Keep the save-feedback pill exactly as specified (120/160ms, 2200ms auto-dismiss) — it is already a named DESIGN.md pattern and matches the "sheet stays open" consensus.

## Caveats / Not Found

- **Microsoft To Do**: official support/product pages returned 404 or transport errors during research; its section is from general product knowledge and is flagged for verification.
- **Google Tasks / Any.do**: support.google.com unreachable; same caveat.
- **Things 3 "Magic Plus" naming**: the term is community shorthand for the "+" new-to-do affordance; Cultured Code's own docs describe the behavior (inline new to-do, keyboard toolbar) without using that name in the pages fetched.
- TickTick's help-center article pages did not render content via fetch (empty response); findings for TickTick come from its official features page plus well-known product behavior.
- No screenshot assets were fetched; all descriptions are text-derived.
