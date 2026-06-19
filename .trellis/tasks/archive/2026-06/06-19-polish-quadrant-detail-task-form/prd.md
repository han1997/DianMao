# Polish Quadrant Detail and Task Form

## Goal

Refine the quadrant detail task list and add/edit task panel to better match
TickTick-style calm scanning. In quadrant detail pages, task rows should not
repeat the current quadrant label below each item. The add/edit sheet should
feel more polished, capture-first, and visually consistent with the recent
quiet task UI direction.

## What I Already Know

* User explicitly requested:
  * no "important", "urgent-important", or equivalent quadrant tags under
    items on the quadrant secondary page;
  * beautify the add/edit task page with TickTick as reference.
* The app is a single-module Jetpack Compose Android app.
* Relevant code is in `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`.
* Quadrant detail currently renders `TaskRow` for each item.
* `TaskRow` currently always emits a quadrant metadata chip:
  `CompactMeta(icon = Icons.Rounded.Inventory2, text = task.quadrant.shortTitle)`.
* `AddTaskPanel` owns the add/edit sheet, title field, priority chips, subtask
  editor, date/time controls, calendar/alarm toggles, repeat picker, and save
  button.
* Product/design context requires fast capture before classification, restrained
  color, native Android familiarity, and consistent affordances.

## Requirements

* Hide the quadrant metadata chip in quadrant detail task rows because the page
  context already communicates the quadrant.
* Preserve the quadrant chip anywhere it is still useful outside a quadrant
  detail context.
* Improve the add/edit sheet visual hierarchy:
  * make the title input feel like the primary capture surface;
  * make classification, subtasks, time, reminder, and repeat controls quieter;
  * keep save action fixed and clear;
  * keep native controls and existing behavior.
* Preserve existing task creation, editing, reminder, calendar, repeat, subtask,
  and back-navigation behavior.

## Acceptance Criteria

* [x] On quadrant detail pages, task rows no longer show the current quadrant
      short tag below the task title.
* [x] Other task-row contexts can still show quadrant context when useful.
* [x] Add/edit task panel looks cleaner and more TickTick-like without adding
      new behavior.
* [x] Text and controls do not overlap on narrow mobile screens.
* [x] Accessibility-sized action targets are preserved.
* [x] `.\gradlew.bat :app:compileDebugKotlin --no-daemon` passes.
* [x] `.\gradlew.bat :app:lintDebug --no-daemon` passes.
* [x] `.\gradlew.bat :app:assembleDebug --no-daemon` passes.
* [x] `git diff --check` passes.

## Out of Scope

* Changing Room schema or task data behavior.
* Adding new navigation destinations.
* Reworking the whole home screen again.
* Copying TickTick brand assets or exact UI.

## Technical Approach

* Add a `showQuadrantMeta` parameter to `TaskRow`, defaulting to true.
* Pass `showQuadrantMeta = false` from the quadrant detail list.
* Refine `AddTaskPanel`, `TaskFormSection`, priority selection, date/time
  controls, repeat picker, and toggle rows using existing Compose components.
* Reuse existing quiet UI helpers (`PanelShape`, `RowShape`, `ChipShape`,
  `quietBorder`) instead of introducing a new component system.

## Definition of Done

* Code follows existing Compose and quiet task UI patterns.
* Build/lint checks pass.
* CHANGELOG is updated for visible UI change.
* Commit plan is prepared for user confirmation.

## Technical Notes

* Design register: product.
* Impeccable context loaded from `PRODUCT.md` and `DESIGN.md`.
* Relevant specs:
  * `.trellis/spec/backend/compose-patterns.md`
  * `.trellis/spec/backend/quality-guidelines.md`
