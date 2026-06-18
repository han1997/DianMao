# Fix test suite and harden database migration safety

## Goal

Restore the Android unit test suite to a passing state and remove the current
Room destructive-migration fallback so future schema mistakes fail visibly
instead of risking silent user-data loss.

## What I already know

* `:app:compileDebugKotlin` passes.
* `:app:lintDebug` passes.
* `:app:testDebugUnitTest` fails to compile because `InAppBackTargetTest`
  calls `resolveInAppBackTarget` without the new `taskForDetail` parameter.
* `resolveInAppBackTarget` now has a `TaskDetail` state between overlays and
  quadrant detail.
* `TaskDatabase` has adjacent migrations from version 1 to 8 but still enables
  `fallbackToDestructiveMigration()`.

## Requirements

* Update `InAppBackTargetTest` for the current resolver signature.
* Add regression coverage for task detail back navigation priority.
* Keep existing overlay priority behavior covered:
  backup panel > archived panel > task panel > task detail > quadrant detail.
* Remove the destructive migration fallback from Room database creation.
* Do not change app behavior beyond the migration-safety hardening and tests.

## Acceptance Criteria

* [ ] `:app:testDebugUnitTest` passes.
* [ ] `:app:compileDebugKotlin` passes.
* [ ] `:app:lintDebug` passes.
* [ ] Back target tests include the task detail state.
* [ ] Database builder no longer uses `fallbackToDestructiveMigration()`.

## Definition of Done

* Tests updated for changed behavior.
* Lint and Kotlin compilation remain green.
* No unrelated refactors or UI changes.
* Any useful project convention learned during the work is considered for spec
  update.

## Technical Approach

Use the existing pure resolver test pattern in `InAppBackTargetTest`. Add the
new `taskForDetail` argument to all existing calls, add one or two focused
priority assertions for task detail, then remove the Room destructive migration
fallback because the explicit migration chain already covers every adjacent
version through `TASK_DATABASE_VERSION = 8`.

## Out of Scope

* Full Room migration instrumentation tests.
* Broader recurring-task state-model redesign.
* UI changes.
* Calendar provider behavior changes.

## Technical Notes

Relevant files:

* `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
* `app/src/test/java/fun/han1997/dianmao/InAppBackTargetTest.kt`
* `app/src/main/java/fun/han1997/dianmao/data/TaskDatabase.kt`
* `.trellis/spec/backend/database-guidelines.md`
* `.trellis/spec/backend/compose-patterns.md`
* `.trellis/spec/backend/error-handling.md`
