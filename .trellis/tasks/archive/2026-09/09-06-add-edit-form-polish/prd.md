# Polish add/edit form pickers, setting explanations, and save action

## Goal

Make the full-screen add/edit task form feel cohesive, legible, and native to
DianMao's warm paper visual language. Three specific pain points from the user:

1. The date and time pickers opened from the form look incongruous (raw M3
   picker chrome inside a plain card — does not match the app).
2. The five setting chips (date / time / calendar / alarm / repeat) and their
   enable/disable chains have no explanation of what each setting does.
3. The save button ("保存并继续记录" / "保存修改并关闭") reads as ugly and
   style-inconsistent with the rest of the form.

## What I already know

- Add/edit is now a full-screen destination (FullScreenScaffold): title field,
  a FlowRow of FilterChips (quadrant + date/time/calendar/alarm/repeat), save
  feedback pill, subtask editor in a scrolling column, fixed save bar at the
  bottom (from the previous task).
- Chips currently show "未设置" or a short value; leading icons use
  InlineButtonIcon tonal treatment. Quadrant chips use ColorDot. There is no
  per-chip caption explaining what the control does.
- Chips are disabled in dependency chains: calendar/alarm need a date, repeat
  needs date + alarm. Disabled state currently only dims the icon — no reason
  shown.
- Pickers: `DianMaoDatePickerDialog` (M3 DatePicker in DatePickerDialog) and
  `DianMaoTimePickerDialog` (M3 TimePicker in a freeform PanelShape Card), in
  `ui/components/DateTimePickers.kt`. Both inherit theme colors but use stock M3
  layout chrome.
- Save bar: full-width filled primary Button, RowShape, InlineButtonIcon +
  text, `.imePadding()`. Text differs add vs edit.
- DESIGN.md governs: warm scrim `0xFF211815@16%`, PanelShape/RowShape/ChipShape,
  quietBorder hairlines, tonal IconTile, ten-percent accent rule, letter
  spacing 0.sp, motion 140-280ms state-change-only, no Color.White/Black in new
  code.

## Assumptions (temporary)

- User wants to keep the current metadata-as-chips interaction (chip tap opens a
  picker / toggles / menu) but improve legibility and explanation, rather than
  replace it with a different input metaphor.
- The five "settings" the user refers to are date, time, calendar-sync, alarm,
  repeat. (Quadrant is a priority choice, not a "setting".)

## Decisions (user-confirmed 2026-09-06)

1. **Grouped settings block (not bare chips).** The five time settings move
   into one labeled `TaskFormSection` ("时间与提醒"/"安排") whose rows use
   verb→value labels + one-line subtitles (the dialect already used by the
   TaskDetail "事项信息" card). Quadrant stays its own priority section above.
2. **Native control + app-owned container.** Keep M3 `DatePicker`/`TimePicker`
   as the control; wrap both in DianMao's own dialog chrome (warm scrim
   `#211815@16%`, `PanelShape` surface, quiet border, app typography, tonal
   confirm). No hand-drawn calendar.
3. **Refined bottom save bar (not replaced).** Keep the fixed full-width
   primary save bar; refine styling within the app's button vocabulary
   (RowShape, tonal IconTile, letter-spacing 0, quiet container).
4. **Scope = form + ALL picker containers.** Unify every date/time picker
   dialog container app-wide (date and time pickers share the same branded
   container). TaskDetail's read-only "事项信息" card already uses the target
   vocabulary — no change there.

## Open Questions

None.

## Requirements (evolving)

- [ ] Add/edit form presents the five settings as a grouped, labeled section
      with verb→value rows and one-line subtitles; disabled rows show the
      reason ("需先设置日期") instead of silent dimming.
- [ ] Date and time pickers open in a shared, app-branded dialog container
      (warm scrim, PanelShape, quiet border, themed buttons) — applied
      consistently to every picker invocation app-wide.
- [ ] Save action looks native, primary, and consistent with the form's visual
      language (refined bottom bar).
- [ ] Pickers and settings rows read as one coherent control family.

## Acceptance Criteria (evolving)

- [ ] The five settings sit in one grouped section with clear labels/subtitles.
- [ ] Disabled setting rows state their dependency reason inline.
- [ ] Date and time pickers share one branded container style (warm scrim,
      PanelShape, quiet border, tonal confirm) everywhere they appear.
- [ ] Save button matches the app's button vocabulary (primary slot, tonal
      icon, RowShape, quiet spacing) and reads as native.
- [ ] Lint / type-check / unit tests / assemble pass.

## Definition of Done (team quality bar)

- Lint / typecheck / tests green.
- DESIGN.md / README updated if the interaction or component vocabulary changes.
- CHANGELOG updated.

## Out of Scope (explicit)

- Changing persisted task fields or time/reminder behavior.
- Quadrant picker redesign (already color-dot + shortTitle; only restyle if
  needed for cohesion).
- Other surfaces (backup/archive/history).

## Technical Notes

- `app/src/main/java/fun/han1997/dianmao/ui/panels/AddTaskPanel.kt` — form body
  (chips FlowRow, feedback, save bar).
- `app/src/main/java/fun/han1997/dianmao/ui/components/DateTimePickers.kt` —
  picker dialogs (unify container here for both date + time).
- `app/src/main/java/fun/han1997/dianmao/ui/components/SubTaskComponents.kt` —
  `TaskFormSection`/`FormSettingBlock` grouping primitives.
- DESIGN.md §5 chips/inputs/dialogs and §4 elevation/motion.

## Research References

- [`research/date-time-entry-conventions.md`](research/date-time-entry-conventions.md)
  — cross-app conventions (Things/Todoist/TickTick/Google Tasks/MS To Do):
  grouped labeled time-settings blocks + verb→value rows; native picker control
  in app-owned branded chrome; fixed bottom primary save bar is the TickTick
  pattern; disabled dependencies should be explained, not silently dimmed.

## Design Contract

Form body order (full-screen destination): title field → priority section
(quadrant chips — own `TaskFormSection` or bordered block, unchanged control) →
"时间与提醒" `TaskFormSection` holding five `FormSettingBlock`-style rows:

| Row | Unset verb | Set value | Subtitle / disabled note |
|---|---|---|---|
| 到期日 | 添加到期日 | M月d日(周?) | 决定时间/日历/响铃/重复是否可用 |
| 提醒时间 | 添加提醒时间 | HH:mm | 需先设置到期日(disabled note) |
| 系统日历 | 同步到系统日历 | 已开启同步 | 到期日当天同步到系统日历;需先设置到期日 |
| 响铃 | 开启响铃提醒 | 到期日 HH:mm 响铃 | 需先设置到期日 |
| 重复 | 不重复 | 每天/每周/每月 | 需先设置到期日并开启响铃 |

Interactions preserved: tapping date/time opens picker; calendar/alarm are
toggle rows (row tap toggles when enabled; disabled shows reason); repeat opens
the existing DropdownMenu (anchored to the row). Add-mode save keeps
capture-first behavior (feedback pill + stay open). Color semantics follow
DESIGN.md: calendar-blue only for calendar/time semantics; terracotta for
selected/primary; quadrant ColorDot for priority.

Picker container (shared by date + time, in `DateTimePickers.kt`): freeform
`Dialog` + `PanelShape` `Surface`/`Card` on `surface`, warm scrim
`Color(0xFF211815).copy(alpha = 0.16f)` via transparent-scrim Dialog
properties, quiet border, app title/actions row ("确定"/"取消" as TextButton or
tonal primary). Both pickers use the SAME wrapper so they read as one family.

Save bar refinement: keep fixed bottom tonal+shadow Surface; primary Button
full-width RowShape with `InlineButtonIcon(Check)` tonal chip + text; enable
only when title non-blank; copy unchanged ("保存并继续记录" / "保存修改并关闭").
Remove visual heaviness (the bar should sit as a quiet anchored action, not a
second card) — tune elevation/spacing to match TaskDetail's bottom bar.

## Out of Scope (explicit)

- Changing persisted task fields or time/reminder behavior.
- Quadrant selection behavior (visual only if needed for cohesion).
- TaskDetail read-only card (already target dialect).
- Backup/archive/history surfaces.
- Hand-drawn calendar/time controls or natural-language date parsing.
