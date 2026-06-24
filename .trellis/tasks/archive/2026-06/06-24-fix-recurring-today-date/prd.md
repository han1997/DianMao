# fix-recurring-today-date

## Goal

Fix the home time-focus lane so cycle-based recurring tasks show the current
cycle occurrence date/time instead of the original stored creation/reminder
date.

## What I Already Know

* User reported recurring items in today's reminders display the creation-time
  date/time, not today's occurrence time.
* `buildTimeFocusItems(tasks, nowMillis, zoneId, limit)` is the pure helper that
  classifies home focus rows.
* `TimeFocusRow` currently renders `item.task.dueLabel`, which is persisted when
  the task was created or last advanced.
* Alarm restore and reminder receivers advance persisted due time for
  scheduling, but the time-focus lane must not mutate task state.

## Requirements

* For `Daily`, `Weekly`, and `Monthly` recurring tasks, compute the focus row's
  display due time from the current repeat period.
* Preserve existing exclusions for completed, archived, and unscheduled tasks.
* Preserve sorting by focus kind rank, then effective due time.
* Keep row click behavior unchanged: tapping opens task detail and does not
  mutate reminders, repeat state, or persistence.

## Acceptance Criteria

* [ ] A daily recurring task created on a prior date but due later today shows
  today's date/time in the time-focus lane.
* [ ] A daily recurring task due earlier today is classified as overdue using
  today's occurrence date/time, not the original date.
* [ ] Existing overdue/today/upcoming ordering, exclusions, and row limit
  behavior still pass.

## Definition of Done

* Tests added/updated for the pure helper behavior.
* Relevant Gradle unit tests pass.
* `git diff --check` passes.
* Changelog updated for the visible bug fix.

## Out of Scope

* Room schema changes or data migration.
* Changing AlarmManager scheduling semantics.
* Changing task detail persistence display outside the time-focus lane.

## Technical Notes

* Relevant files:
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/RepeatReminderCalculator.kt`
  * `app/src/test/java/fun/han1997/dianmao/TimeFocusItemsTest.kt`
  * `app/src/test/java/fun/han1997/dianmao/RepeatReminderCalculatorTest.kt`
* Specs read:
  * `.trellis/spec/backend/compose-patterns.md`
  * `.trellis/spec/backend/quality-guidelines.md`
  * `.trellis/spec/guides/cross-layer-thinking-guide.md`
  * `.trellis/spec/guides/code-reuse-thinking-guide.md`
