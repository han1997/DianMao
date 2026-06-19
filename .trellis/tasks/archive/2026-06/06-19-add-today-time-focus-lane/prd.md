# Add Today Time Focus Lane

## Goal

Continue the humane workflow optimization by adding a compact time-sensitive
focus lane to the home screen. DianMao should still be organized by four
quadrants, but opening the app should immediately surface the few timed tasks
that need attention now: overdue, today, then nearest upcoming.

## What I Already Know

* The user asked to continue optimizing app usage logic after the capture-first
  workflow was completed.
* The previous task identified "Today/Time-Sensitive Focus" as the next major
  workflow direction.
* Current home screen shows `AppHeader` and then the full `QuadrantBoard`.
* Task rows and compact metadata already expose due time, calendar, alarm, and
  repeat information.
* Time data is available on `TaskUiModel.dueAtMillis` and `dueLabel`.
* Existing product direction says time sensitivity should feel first-class, but
  the app must not become a noisy dashboard.

## Requirements

* Add a compact focus lane between the header and four-quadrant board on the
  home screen.
* Only show the lane when there are unfinished tasks with `dueAtMillis`.
* Show at most three tasks to keep the surface calm.
* Prioritize tasks by:
  * overdue first;
  * today second;
  * upcoming third;
  * due time order within each group.
* Let users tap a focused task to open task detail directly.
* Keep the four-quadrant board as the main model below the focus lane.
* Do not change task persistence, reminder scheduling, calendar sync, repeat
  behavior, or completion behavior.
* Keep the lane visually restrained and consistent with quiet task UI patterns.

## Acceptance Criteria

* [x] Home screen shows a time focus lane when at least one unfinished task has
      a due time/date.
* [x] The focus lane is hidden when there are no unfinished timed tasks.
* [x] The lane shows no more than three tasks.
* [x] Overdue tasks appear before today tasks, and today tasks appear before
      upcoming tasks.
* [x] Tasks are ordered by due time within each priority group.
* [x] Completed tasks are excluded.
* [x] Tapping a focus item opens the task detail screen.
* [x] Four-quadrant board remains visible and usable below the lane.
* [x] Unit tests cover focus item classification/order/limit behavior.
* [x] `.\gradlew.bat :app:testDebugUnitTest --no-daemon` passes.
* [x] `.\gradlew.bat :app:compileDebugKotlin --no-daemon` passes.
* [x] `.\gradlew.bat :app:lintDebug --no-daemon` passes.
* [x] `.\gradlew.bat :app:assembleDebug --no-daemon` passes.
* [x] `git diff --check` passes.

## Technical Approach

* Add a small pure helper that classifies and orders `TaskUiModel` values into
  focus items.
* Add unit tests for overdue/today/upcoming order, completed exclusion, and the
  three-item limit.
* Add a `TimeFocusLane` composable in `MainActivity.kt`, inserted between
  `AppHeader` and `QuadrantBoard`.
* Use existing `RowShape`, `ChipShape`, `quietBorder`, `CompactMeta`, and
  restrained Material 3 surfaces.

## Definition of Done

* Requirements are implemented without data-model changes.
* Relevant tests and Gradle checks pass.
* CHANGELOG is updated for visible workflow change.
* Spec update decision is made after implementation.
* Work is committed and task is archived.

## Out of Scope

* Full Today page or new navigation destination.
* Editing/snoozing due dates from the focus lane.
* Undo for completion/archive/delete.
* Database schema changes.
* Exact TickTick UI cloning.

## Technical Notes

* Relevant code:
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskModels.kt`
* Relevant specs:
  * `.trellis/spec/backend/compose-patterns.md`
  * `.trellis/spec/backend/quality-guidelines.md`
