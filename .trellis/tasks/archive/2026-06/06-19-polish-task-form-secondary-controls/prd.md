# Polish Task Form Secondary Controls

## Goal

Extend the recent task-content input polish to secondary task form surfaces so
subtasks, reminder settings, repeat settings, and completion-note inputs feel
like the same quiet Android product system instead of a mix of default controls.

## What I Already Know

* The user liked the stronger task-content input treatment and asked to apply
  the same direction more broadly, especially to subtasks, without being limited
  to only that area.
* The app is a single-module Jetpack Compose Android app.
* The relevant UI is concentrated in
  `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`.
* Product/design context says capture should come before classification, with
  restrained color, Android-native familiarity, and consistent affordances.
* Existing quiet UI helpers include `TaskFormSection`, `FormSettingBlock`,
  `RowShape`, `ChipShape`, and `quietBorder`.

## Requirements

* Keep the primary task title/content input as the strongest capture surface.
* Make subtask creation and editing visually consistent with the updated task
  content input, while preserving quick add and checkbox/delete behavior.
* Make secondary setting controls feel like one form system:
  * quadrant choice;
  * date/time actions;
  * calendar and alarm toggles;
  * repeat rule choice.
* Apply the same input vocabulary to completion-note editing surfaces where it
  fits:
  * completion note dialog;
  * inline completion-history note editor.
* Preserve existing task creation, editing, subtask, reminder, calendar, repeat,
  completion note, and back-navigation behavior.
* Avoid broad redesign of unrelated panels such as WebDAV/backup settings.

## Acceptance Criteria

* [x] Subtask draft entry uses a calm input row with stable height, rounded
      shape, subdued indicators, and a clear add action.
* [x] Existing subtask edit rows look like quiet editable rows instead of raw
      default text fields.
* [x] Empty subtask state is quiet and does not compete with the main title
      input.
* [x] Date/time and repeat controls stay compact and wrap safely on narrow
      screens.
* [x] Completion note dialog and inline completion-note editing use the same
      rounded, subdued input styling.
* [x] Accessibility-sized touch targets are preserved for add, delete, checkbox,
      and save actions.
* [x] `.\gradlew.bat :app:compileDebugKotlin --no-daemon` passes.
* [x] `.\gradlew.bat :app:lintDebug --no-daemon` passes.
* [x] `.\gradlew.bat :app:assembleDebug --no-daemon` passes.
* [x] `git diff --check` passes.

## Technical Approach

* Add or reuse small Compose helpers for quiet text-field colors and secondary
  form surfaces instead of duplicating styling across fields.
* Refine `SubTaskEditor` and `SubTaskEditRow` inside `MainActivity.kt`.
* Keep `FormSettingBlock` for compact settings and tune button/chip shapes only
  where the current defaults clash with the new form vocabulary.
* Update completion-note text fields to the same input style.
* Keep changes in Compose UI only. Do not touch Room schema, repository logic,
  platform calendar/alarm behavior, or backup code.

## Definition of Done

* Code follows the existing quiet task UI and Compose quality patterns.
* Build/lint checks pass.
* CHANGELOG is updated for visible UI polish.
* Spec update decision is made after implementation.
* Work is committed before finish-work.

## Out of Scope

* Database/schema changes.
* New task features or navigation destinations.
* Backup/WebDAV panel redesign.
* Exact TickTick cloning or brand asset copying.

## Technical Notes

* Impeccable register: product.
* Product context: fast capture, restrained color, native Android familiarity.
* Relevant specs:
  * `.trellis/spec/backend/compose-patterns.md`
  * `.trellis/spec/backend/quality-guidelines.md`
* Spec sync: `.trellis/spec/backend/compose-patterns.md` now documents shared
  quiet text-field styling for task workflow inputs.
