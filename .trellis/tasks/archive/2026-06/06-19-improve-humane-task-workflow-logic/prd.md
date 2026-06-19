# Improve Humane Task Workflow Logic

## Goal

Optimize DianMao's daily task flow using TickTick-like product logic as a
reference while preserving DianMao's four-quadrant model. The app should feel
less like a collection of features and more like a considerate daily tool:
quick to capture, forgiving after actions, clear about what changed, and
helpful about what needs attention next.

## What I Already Know

* User requested a comprehensive optimization of software usage logic, making
  the app more humane, with TickTick-like behavior as the reference.
* DianMao is a personal Android task app centered on one task model, four
  quadrants, time sensitivity, system calendar/alarm integration, and
  completion notes.
* Product/design context says:
  * capture before classification;
  * time sensitivity is first-class;
  * completion creates memory;
  * native Android familiarity and calm polish matter more than decoration.
* Current code is mostly concentrated in
  `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` and task behavior in
  `app/src/main/java/fun/han1997/dianmao/data/TaskRepository.kt`.
* Existing app already has:
  * four-quadrant board and quadrant detail pages;
  * add/edit sheet;
  * subtask editing;
  * date/time picker;
  * calendar and alarm toggles;
  * repeat rules;
  * completion note dialog;
  * task detail page;
  * archived task panel;
  * backup panel.

## Current Workflow Findings

* Add flow is capture-capable, but saving a new task closes the sheet, which
  makes consecutive capture slower than TickTick-like quick entry.
* Quadrant detail empty state says the user can add from the lower-right button,
  but the floating add button is hidden while inside a quadrant detail page.
* New tasks created from quadrant detail already have the current quadrant as
  the initial priority when the add sheet is opened, but the page does not
  expose the add action.
* Non-recurring tasks are automatically archived when completed. This keeps the
  active list clean, but it can feel abrupt if the user expects visible feedback
  or an undo path.
* Time-sensitive tasks exist, but the home screen is still dominated by the
  quadrant board. There is no dedicated "today / overdue / upcoming" focus
  lane yet.
* Calendar/alarm/repeat controls are functional, but the relationship between
  date, alarm, calendar, and repeat is still somewhat mechanical.

## Requirements (Evolving)

* Keep the four-quadrant model. Do not turn DianMao into a generic to-do app.
* Make daily capture faster and more forgiving.
* Make actions more explainable: after save, complete, archive, delete, or
  reminder changes, the user should understand what happened.
* First MVP focuses on capture-first workflow:
  * show the floating add action while browsing a quadrant detail page;
  * new tasks created from quadrant detail default to that quadrant;
  * saving a new task keeps the sheet open for consecutive capture;
  * after saving a new task, clear task content, subtasks, date, time, alarm,
    calendar, and repeat fields, while preserving the selected quadrant;
  * editing an existing task still closes after save.
* Show clear save feedback after creating a task so the user knows the capture
  succeeded.
* Prefer incremental, testable improvements over a large risky rewrite.
* Preserve existing Room data and platform integration behavior unless a
  specific workflow change is explicitly chosen.

## Feasible MVP Directions

### Approach A: Capture-First Workflow (Recommended)

Make adding tasks feel more like a fast capture inbox:

* Show a floating add action inside quadrant detail pages.
* Keep the add sheet open after saving a new task, reset the title/subtasks, and
  preserve the selected quadrant so multiple items can be captured quickly.
* Add a clear "saved" state or brief status text so the user knows the item was
  created.
* Keep edit flow unchanged: editing an existing task still closes after save.

Pros:
* Directly supports the product principle "capture before classification".
* Low data risk because task persistence behavior stays the same.
* Fixes the current mismatch where quadrant detail empty state mentions adding
  but the FAB is hidden.

Cons:
* Does not yet solve Today/overdue prioritization.

### Approach B: Today/Time-Sensitive Focus

Add a compact focus lane above the quadrant board for overdue/today/timed tasks:

* Surface overdue and today items before quadrant browsing.
* Keep quadrant board as the main organization model below.
* Add quick access from the focus lane into task detail.

Pros:
* Helps the user decide what matters now.
* Makes calendar/alarm features feel more useful.

Cons:
* More UI and sorting complexity.
* Requires careful responsive layout so home screen does not become dashboard-like.

### Approach C: Forgiving Completion and Undo

Make completion/archive/delete behavior clearer and easier to recover from:

* Show a feedback surface after completion, especially when non-recurring tasks
  auto-archive.
* Offer undo or a quick way to open archived items.
* Clarify completion note flow and repeat-task behavior.

Pros:
* Reduces surprise and data-loss anxiety.
* Good for trust and daily use.

Cons:
* Needs action feedback architecture and possibly snackbar/undo state.

## Open Questions

* Resolved: user chose Approach A, Capture-First Workflow, as the first MVP.

## Acceptance Criteria (Evolving)

* [x] The chosen workflow improves a complete daily path, not just isolated
      styling.
* [x] Floating add action is available on quadrant detail pages when no task
      detail overlay is open.
* [x] Adding from a quadrant detail page defaults the new task to that quadrant.
* [x] Saving a new task keeps the add sheet open for continued capture.
* [x] Saving a new task clears content/subtasks/time/reminder/repeat fields
      while preserving the current quadrant choice.
* [x] Saving an edited task still closes the sheet.
* [x] Task creation shows clear feedback after save.
* [x] Existing data model remains compatible.
* [x] Existing calendar/alarm/repeat behavior is preserved unless explicitly
      changed.
* [x] Back navigation remains deterministic across panels and detail views.
* [x] Compose UI remains compact, accessible, and consistent with the quiet
      task UI pattern.
* [x] `.\gradlew.bat :app:compileDebugKotlin --no-daemon` passes.
* [x] `.\gradlew.bat :app:lintDebug --no-daemon` passes.
* [x] `.\gradlew.bat :app:assembleDebug --no-daemon` passes.
* [x] `git diff --check` passes.

## Definition of Done

* Requirements are confirmed before implementation.
* Applicable Trellis specs are loaded before code changes.
* Build/lint checks pass.
* CHANGELOG is updated for visible behavior changes.
* Spec update decision is made after implementation.
* Work is committed and task is archived.

## Out of Scope (Temporary)

* Replacing the four-quadrant model.
* Copying TickTick UI or brand assets exactly.
* Large navigation rewrite.
* Today/overdue focus lane. This remains a later workflow direction.
* Undo for completion/archive/delete. This remains a later workflow direction.
* New cloud sync or collaboration features.
* Database schema changes unless a chosen workflow absolutely requires them.

## Technical Notes

* Impeccable register: product.
* Relevant design context:
  * `PRODUCT.md`
  * `DESIGN.md`
  * `C:/Users/hanhu/.agents/skills/impeccable/reference/product.md`
* Likely touched files:
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
  * maybe `app/src/main/java/fun/han1997/dianmao/data/TaskRepository.kt`
    depending on chosen workflow.
* Relevant specs:
  * `.trellis/spec/backend/compose-patterns.md`
  * `.trellis/spec/backend/quality-guidelines.md`
* Spec sync: `.trellis/spec/backend/compose-patterns.md` now documents the
  capture-first task entry pattern: new saves stay in capture flow, edit saves
  close the sheet.
