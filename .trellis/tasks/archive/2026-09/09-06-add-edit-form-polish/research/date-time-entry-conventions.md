# Research: Date / Time / Reminder Entry Conventions in Mainstream Task Apps

- **Query**: How do TickTick, Todoist, Things 3, Google Tasks, and Microsoft To Do present date, time, calendar, alarm, and repeat settings when capturing/editing a task? Map findings onto DianMao's Compose form (chips FlowRow + M3 pickers + fixed save bar).
- **Scope**: external (official help centers, live app documentation) + internal (DianMao code/spec/DESIGN.md audit)
- **Date**: 2026-09-06

---

## 1. Summary of Findings per App

### Things 3 (Cultured Code) — iOS/Mac
Official sources fetched: [Scheduling To-Dos](https://culturedcode.com/things/support/articles/2803579/), [Setting a Reminder](https://culturedcode.com/things/support/articles/2803585/), [Repeating To-Dos](https://culturedcode.com/things/support/articles/2803564/), [Natural Language Dates](https://culturedcode.com/things/support/articles/9780167/)

| Aspect | Behavior |
|---|---|
| Picker metaphor | A single **"When" popover/date picker** reached by tapping the calendar icon on an undated to-do or the date badge on a dated one. It is an **anchored popover** (not a full-screen editor, not a bottom sheet). Shortcuts **Today / This Evening / Someday**, a calendar grid, and a **Clear** button that returns the to-do to "Anytime". Reminder time is added *inside the same picker*. |
| Natural language | The When popover has a **pull-down/type-to-search natural-language field** ("tues 8am" sets start date + reminder at once). Explicitly recommended for combined date+reminder ("if the date popover keeps closing on you, use natural language"). |
| Setting explanation | Rows in the to-do show **verbs when unset** and values when set: "+ Add Reminder", "+ Add Deadline", the date itself. Reminder semantics are explained in help ("reminds you to *start* a task", "not persistent, not an alarm"), not inline in the UI. |
| Repeat | Repeating to-dos are a separate creation path (New Repeating To-Do / Items→Repeat / More→Repeat), and every repeating item is backed by an editable **template**. Repeat does not live in the same chip surface as date; it is deeper. |
| Save/confirm | Save is a **checkmark/OK in the popover or top-right**, plus keyboard Return commits natural language. Clear removes scheduling. |
| Grouping | The When popover **groups date + reminder time in one place**; repeat is elsewhere (template/More menu). |

### TickTick (滴答清单) — Android/iOS/Web
Source: help.dida365.com (Chinese help center, extracted article bodies) + help.ticktick.com

| Aspect | Behavior |
|---|---|
| Picker metaphor | Add/edit form is a **full detail page (desktop) or a compose "save page" (mobile)**; after capture there is a **"保存页面"/detail editor** with rows. Tapping a row ("日期&时间", "提醒", "重复") opens the respective **inline picker or menu**; dates/times also accept **natural language typed in the task title** ("明天早上 9 点开会" auto-schedules). Picker chrome is native per-platform; the app's own identity shows in the surrounding row surfaces, not in custom-drawn calendars. |
| Setting explanation | The capture form has **two grouped areas** — one for "设置任务日期&时间" and another for "提醒&重复". Reminder settings ("提醒") list concrete trigger options. Settings do **not** explain each option with a sentence in the form; explanation lives in help articles and in the values themselves. |
| Repeat/alarm | "提醒&重复" are grouped together and opened from a shared entry; repeat options are rich (every day/week/month, weekdays, lunar repeat, etc.) behind a menu/list. |
| Save/confirm | TickTick's signature is a **prominent "保存" button at the bottom-right / bottom bar** of the compose page. Natural-language time typed into the title is confirmed when the task is saved. |

### Todoist — Android/iOS/Web
Official sources fetched: [Schedule a date and time](https://todoist.com/help/todoist/features/schedule-a-date-and-time-for-your-todoist-tasks-q7VobO), [Quick Add](https://todoist.com/help/todoist/features/use-task-quick-add-in-todoist-va4Lhpzz), [Recurring dates](https://todoist.com/help/todoist/features/introduction-to-recurring-dates-YUYVJJAV), [Reminders](https://todoist.com/help/todoist/features/introduction-to-reminders-9PezfU)

| Aspect | Behavior |
|---|---|
| Picker metaphor | A **"scheduler"** popup/dialog opens when the user taps the Date row (desktop) or Date chip/field (mobile). Android flow per official help: tap a task → tap **Date** → scheduler appears → tap **Type a date** and type natural language **or** select a day on the calendar. Time is a separate control inside the scheduler (**Time / Add time**), plus **Repeat** inside the same scheduler. A colored per-project workload bar spans the top. Native date/time wheels are used where the platform expects them; the "scheduler" chrome is app-owned. |
| Natural language | First-class. Quick Add parses the date in the task title, **highlights it**, and stores it on save; the scheduler also has a date text field. `!30min before`, `!tomorrow 3pm`, `every 3rd Tuesday` work in the same field. |
| Setting explanation | Task view (detail) exposes **rows with verbs when unset and values when set**: "Date", "Deadline", "Reminders", plus a **Reminders chip** that expands to "suggested reminders" (e.g., At time of task, 30 min before) and Custom Date & Time. Disabled/dependency states are avoided rather than explained: reminders are relative to the task's time, so help says "To add an automatic reminder, always add a date and time to the task". Todoist rarely disables controls; it hides or makes the prerequisite obvious by phrasing. |
| Save/confirm | Scheduler has an explicit **Save** (top-right "Done"/"Save"/checkmark). Quick Add commits when the task is added (keyboard return / Add button). The task view detail edits save implicitly; removing a date is **No Date**. |
| Grouping | Date and time and repeat share one **scheduler**; reminders are a **separate chip/section** with their own mini-list. Multiple chips appear together in the task view as a row of labeled controls, but each opens its own surface. |

### Google Tasks — Android/iOS/Web
Sources: support.google.com/tasks (blocked in this environment; behavior cross-referenced from official product pages and long-standing UI) — treat as "not directly re-verified today"

| Aspect | Behavior |
|---|---|
| Picker metaphor | Mobile add task page is a **full-screen form**: title, then a row of **chip-like buttons** (📅 Due date, 🔁 Repeat, etc.). Tapping due date opens a **native dialog date picker**; a second tap area adds **time** (native time picker or keyboard). Repeat opens a **bottom sheet / menu list** ("Daily / Weekdays / Weekly / Monthly / Yearly / Custom…"). |
| Setting explanation | Controls are **bare chips with values** ("Today", "Tomorrow", a date). There are no inline descriptions of what each option does. Add/notifications depend on the due date existing; the app uses **presence/absence and menu ordering** rather than disabling. |
| Save/confirm | Primary action is a **checkmark at top-right / "Save"** (desktop bottom). Back/close discards. |
| Grouping | Due date and repeat are **adjacent chips in one row** but not wrapped in a labeled "schedule" section; My Day/reminders live on the same task-edit screen. |

### Microsoft To Do — Android/iOS/Web/Windows
Source: support.microsoft.com search + product UI (pages mostly JS-rendered and not directly fetchable here); note as partially re-verified

| Aspect | Behavior |
|---|---|
| Picker metaphor | Add task field is an **inline quick-add row** under the list; the detail/"step" view has a **"+ Add due date" row** and a **"+ Add reminder" row**. Tapping opens native or in-app pickers (date calendar popup + a time selector; reminders offer quick choices "Later today / Tomorrow / Next week / Custom" plus date+time). No full-screen date editor; everything is row-driven. |
| Setting explanation | Rows **switch from verb to value**: "+ Add due date" becomes "Tomorrow", "Due tomorrow 09:00"; "+ Add reminder" becomes the reminder text. Repeat ("Repeat") is another row toggling options "Daily / Weekdays / Weekly / Monthly / Yearly". No inline paragraph descriptions. |
| Save/confirm | To Do is a **persistent capture bar**: typed task + inline chips for due/reminder/repeat/my-day, and the item commits when you press **Add/Enter** (or tapping elsewhere). Detail-pane edits are implicit (no save button). |
| Grouping | Due date, reminder, repeat, and My Day are **independent rows/chips** inside the task detail; no single grouped "time settings" card. |

---

## 2. Answers to the Four Focus Questions (cross-app synthesis)

### 2.1 Picker metaphor
- **The dominant metaphor in every app is a modal or anchored surface opened from a *row or chip* that shows the current value or an unset-verb.** No mainstream app keeps the calendar permanently expanded inline inside the capture form.
  - Full-page capture forms (TickTick mobile, Google Tasks) still open date/time in a **dialog/separate layer**.
  - Things anchors a **popover** to the date badge; Todoist opens a **scheduler dialog**; Google Tasks uses **native dialogs**; MS To Do uses **row-triggered pickers**.
- **Picker chrome relationship to identity:**
  - Apps with a strong visual identity (Things, Todoist) **dress the picker in their own containers** (Todoist scheduler: app bar, workload strip, natural-language field; Things: custom When popover) yet still reuse **native wheels/calendars** for the actual day/time selection.
  - Apps closest to platform stock (Google Tasks, MS To Do) accept **native dialog chrome** and put identity into the surrounding rows.
  - TickTick on mobile likewise leans on native pickers surfaced from app-styled rows.
- **Implication for DianMao:** raw M3 `DatePickerDialog`/`TimePicker` chrome *inside a plain card* looks incongruous because the form rows carry all the app's identity. The mainstream fix is **not** to hand-draw a calendar; it is to (a) keep M3 `DatePicker`/`TimePicker` as the underlying control, but (b) wrap them in the app's own dialog container (warm scrim `#211815@16%`, `PanelShape`, app typography, quiet-border surface, primary tonal confirm), and (c) make the **row/chip that opens them explain what is being set**. Native controls + branded chrome is exactly what TickTick/Things/Todoist do.

### 2.2 Setting explanation
- **Most apps do not put long explanations inline.** They rely on three mechanisms:
  1. **Verb-to-value row labels** ("+ Add reminder" → "Tomorrow 09:00"; "添加提醒" → "已同步系统日历") — used by MS To Do and by DianMao's own `TaskDetailScreen`.
  2. **Concrete trigger option lists** (reminders: "At time of task / 30 min before / Custom…") instead of abstract toggles — Todoist, MS To Do.
  3. **Help text outside the form** for semantics — Things explains in help that reminders are not alarms, etc.
- **Disabled/dependency states:** the leading apps **avoid disabling**. Todoist phrases the prerequisite ("always add a date and time" to get automatic reminders) and hides/removes the irrelevant. Google Tasks and TickTick keep controls enabled and let the underlying action no-op or prompt. Things uses a **Clear** affordance instead of disabled.
- **Implication for DianMao:** bare "未设置" chips with a dimmed icon do not match what mainstream apps do. Two viable lanes: (a) a **grouped settings section** with section header + rows where each row is verb→value + a one-line subtitle ("到期日决定时间/日历/响铃/重复是否可用") — closest to Todoist/Things grouping; or (b) keep chips but give each chip an **expanded description beneath**, and replace silent disabled-dimming with an explicit dependency note ("需先设置日期"). The app already owns the vocabulary in `TaskFormSection` and `FormSettingBlock` (title + subtitle rows) — using them to group the five settings under one labeled card mirrors Todoist's scheduler grouping and the detail-screen "事项信息" card.

### 2.3 Save / confirm affordance
- **Quick-capture:** full-width, bottom-anchored primary **Add/Save/Return-to-continue** is the TickTick pattern (prominent bottom 保存) and the current DianMao add-mode pattern. Todoist/Google Tasks commit with a **keyboard/Add** action at top or bottom; MS To Do's inline capture bar commits on Add.
- **Wording distinguishes continue vs close:**
  - TickTick's compose page labels simply **保存** (does not describe next behavior).
  - DianMao's "保存并继续记录" (add) vs "保存修改并关闭" (edit) is more explicit than the mainstream apps — that is a defensible copy choice. Mainstream apps express "continue vs close" by **behavior after save** (form stays open vs navigates back) rather than by long button text.
- **Implication for DianMao:** the primary full-width bottom bar is consistent with TickTick and with the app's own DESIGN.md (fixed bottom action bar, terracotta filled button, RowShape). "Ugly" is likely a **styling** issue (full-width terracotta block, no quiet container, icon+text weight) rather than the bar metaphor. Mainstream alternatives for styling: keep full-width primary but pair it in a quiet tonal bar; or right-align a compact primary next to a cancel (desktop pattern); or commit via IME + a secondary "保存" text action (TickTick mobile) with the bottom bar only when meaningful. Copy can stay action-focused; if the user dislikes the long sentence, "保存" + behavior is the mainstream norm, but the current wording already encodes behavior more clearly than mainstream.

### 2.4 Grouping
- **All the strongest apps group related time settings.**
  - Things: one "When" popover holds date + reminder (repeat is a template concept elsewhere).
  - Todoist: one scheduler holds date + time + repeat; reminders sit in a separate but clearly-labeled section.
  - TickTick: capture page groups "设置任务日期&时间" and "提醒&重复" as two labeled blocks.
  - The **scatter-only** approach (bare chips with no enclosing label, like current Google Tasks and the current DianMao FlowRow) is the weakest grouping and the reason the settings feel unexplained.
- **Implication for DianMao:** wrap the five chips (date/time/calendar/alarm/repeat) into one labeled **TaskFormSection** ("时间与提醒" / "安排") and, inside it, use `FormSettingBlock` rows or chip rows whose header/subtitle explains semantics and dependencies. Quadrant stays outside as its own section (priority is not a time setting — consistent with PRD's assumption).

---

## 3. Mapping onto DianMao's Constraints (app-specific)

### 3.1 The current form (verified in code)
- `AddTaskPanel` (`app/src/main/java/fun/han1997/dianmao/ui/panels/AddTaskPanel.kt`) : a full-screen form (`FullScreenScaffold`) with:
  - title `OutlinedTextField` (RowShape, `quietTextFieldColors`), auto-focus in add mode;
  - one `FlowRow` of `FilterChip`s: 4 quadrant chips (ColorDot) + **date** (`Icons.Rounded.CalendarMonth`, blue when enabled) + **time** (`Schedule`) + **calendar sync** (`CalendarMonth`) + **alarm** (`Alarm`) + **repeat** (`Repeat`);
  - chips show "未设置" or a short value; disabled dependencies: time/calendar/alarm `enabled = dueDateMillis != null`; repeat `enabled = dueDateMillis != null && useAlarm`;
  - disabled chips only dim the icon/container — no reason text;
  - `DropdownMenu` for repeat; `DianMaoDatePickerDialog`/`DianMaoTimePickerDialog` in `DateTimePickers.kt`;
  - fixed bottom `Surface(tonal 2dp, shadow 8dp)` bar with a full-width filled primary `Button` (RowShape, `InlineButtonIcon(Check)` + text), `imePadding()`.
- `DateTimePickers.kt`: M3 `DatePicker` inside M3 `DatePickerDialog` (default chrome + default container); M3 `TimePicker` in a `Dialog`+`Card`(PanelShape, surface, 6dp elevation). Both inherit theme colors but keep stock M3 layout chrome and default scrim.
- The **detail screen already implements the mainstream verb→value pattern** (`TaskDetailScreen.kt:239-275`): a `TaskFormSection` ("事项信息") with `DetailMetaRow` rows — due = `dueLabel` or "添加提醒"; calendar = "已同步系统日历" / "未同步系统日历"; alarm = "响铃提醒已开启" / "未设置响铃"; repeat = rule label / "不重复". Form and detail currently use **two different dialects** for the same five settings.

### 3.2 Constraint fit summary
- **Grouping:** the app has `TaskFormSection` (PanelShape, surfaceVariant 0.26, quietBorder) and `FormSettingBlock` (RowShape, header icon+tile+subtitle). These already match the mainstream grouped-row pattern and the detail screen's own card.
- **Explanation:** DESIGN.md's typography (labelMedium/bodySmall for metadata/explanation) supports one-line subtitles per setting and per group.
- **Pickers:** the app's rule set already says form dialogs = freeform `Dialog` + branded Card (see DESIGN.md §5 Dialogs and Date & Time Pickers). The current time picker card is themed; the **date picker dialog is still stock M3 chrome** — that mismatch is the "incongruous" complaint. There is **no requirement to abandon M3 DatePicker/TimePicker**; the requirement is to put them in the app's own chrome, matching what TickTick/Todoist/Things do (native control + branded surface).
- **Disabled state:** DESIGN.md and the five-setting dependency chain are unique to DianMao's semantics; mainstream precedent says the app should *explain* the chain rather than silently disable. `FormSettingBlock(enabled=…)` already has a disabled visual, but has no subtitle slot for the reason while disabled (its title/subtitle dim; a reason line can be added to `content`).
- **Save:** DESIGN.md §4/§5 already endorses the fixed bottom action bar + terracotta filled primary; the mainstream apps confirm the metaphor. Styling refinements should stay within the existing button vocabulary (RowShape, IconTile tonal chip, no `Color.White`/`Color.Black`, letterSpacing 0).

---

## 4. Caveats / Not Found

- **Google Tasks and Microsoft To Do**: official help pages are JS-rendered and were not directly fetchable from this network (support.google.com and support.microsoft.com search/support both blocked or JS-only). Their row behaviors above are based on the long-standing product UI and are marked as less freshly verified than Things/Todoist/TickTick.
- **TickTick**: international help article bodies are JS-rendered; extracted content came from the Chinese help center (help.dida365.com) with mojibake encoding; the readable fragments confirm the "设置任务日期&时间 / 提醒&重复" grouped-block structure and the prominent bottom 保存 page.
- **No screenshots** were captured; the report relies on official help-step text and app UI description, not pixels.
- App-specific "exact Chinese microcopy" for mainstream apps (beyond DianMao) was not the goal; keep DianMao's Chinese copy.
