# TickTick-Inspired Product/UI Patterns

## Sources

* User request: "参考嘀嗒清单，完善软件功能、页面UI".
* TickTick official features page: `https://ticktick.com/features`.
* Local product/design context: `PRODUCT.md`, `DESIGN.md`.
* Current implementation scan: `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`,
  `app/src/main/java/fun/han1997/dianmao/data/TaskModels.kt`,
  `app/src/main/java/fun/han1997/dianmao/data/TaskRepository.kt`.

## TickTick Patterns Worth Borrowing

* Fast capture: task entry should be the fastest path, with details added after
  the task exists.
* Smart views: Today, calendar/reminder, lists, tags, filters, recurring tasks,
  and overview surfaces help users find the right next action quickly.
* Task density: compact list rows, clear metadata, and predictable action
  placement make repeated daily scanning efficient.
* Time planning: calendar/reminder/repeat handling is central, not secondary.
* Cross-entry convenience: widgets/integrations are part of TickTick's capture
  system, but Android widget/integration work should be a later DianMao task.
* Focus/Pomodoro/habits exist in TickTick, but they are distinct product modules
  and would pull DianMao away from its current task/review scope if included in
  the first iteration.

## Current DianMao Capabilities

* Home screen already has an app header, compact stats, a time-focus lane, and a
  four-quadrant board.
* Task model already supports due date/time, system calendar, AlarmManager,
  repeat rules, subtasks, completion count/history, completion note, archiving,
  and display order.
* Add/edit sheet already supports capture-first saving, quadrant selection,
  date/time, calendar sync, alarm, repeat, and subtasks.
* Task detail already supports metadata, subtask toggles, completion history,
  calendar open action, edit/delete/archive, and completion notes.

## Gaps Relative To TickTick-Style Daily Use

* No first-class smart view switcher beyond the four-quadrant board and limited
  time-focus lane.
* No search or quick filter for active tasks.
* No "inbox/unplanned" planning surface for tasks without time or a refined
  quadrant.
* No compact upcoming/scheduled overview beyond the top three time-focus rows.
* No tags/list folders. Adding them requires persisted schema and backup changes.
* No natural-language date parsing. Adding it requires parser design and more
  failure cases.
* No Android home-screen widget, Pomodoro, or habit module. These are larger
  independent product areas.

## Recommended MVP

Build a home "task command center" layer without changing the persisted model:

* Add a restrained segmented control above the main content for smart views:
  `四象限`, `今天`, `近期`, `收集箱` or similar.
* Add lightweight search/filter state over existing active tasks.
* Expand time-aware browsing from three focus rows into a scannable view when
  the user selects Today/Upcoming.
* Treat tasks without due time as an Inbox/Unplanned planning surface.
* Keep task rows, detail, add/edit sheet, and existing reminder behavior intact.

This provides visible TickTick-style functionality and UI polish while avoiding
Room migrations in the first pass.

## Deferred Candidates

* Tags/list folders: useful, but requires `TaskEntity`, backup codec, schema
  migration, filtering UI, and import/export compatibility.
* Natural-language quick add: useful, but needs parsing rules, date/time
  ambiguity handling, tests, and UX copy.
* Android widget: aligns with TickTick capture, but touches platform widget
  lifecycle and should be scoped separately.
* Pomodoro/habits: not aligned with DianMao's current "task plus review log"
  core and risks feature sprawl.
