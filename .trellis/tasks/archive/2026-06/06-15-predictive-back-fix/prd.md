# Fix predictive back gesture for overlay panels

## Goal

Make the Android 14+ predictive back gesture (left-swipe to go back) correctly close overlay panels (add/edit task, backup) instead of navigating away from the current screen. The previous `BackHandler` fix works for the system back button tap but not for the swipe-back gesture.

## What I already know

* `BackHandler` composables were added in the previous fix for `showAddSheet || taskForEdit != null` and `showBackupPanel`.
* The system back button tap works correctly with these handlers.
* Left-swipe back gesture on Android 14+ bypasses `BackHandler` because `activity-compose 1.8.1` does not fully support predictive back dispatch.
* `AddTaskPanel` and `BackupPanel` are implemented as `AnimatedVisibility` + full-screen `Box` overlays, not as `ModalBottomSheet`.
* Android 14 predictive back requires `activity-compose 1.9.0+` (which integrates with the new `OnBackPressedDispatcher` predictve back callback chain).
* The app uses `composeBom = "2024.02.00"` and `activityCompose = "1.8.1"`.

## Assumptions

* Upgrading `activity-compose` to `1.9.0+` is safe and does not break existing Compose behavior.
* `composeBom = "2024.02.00"` is compatible with `activity-compose 1.9.0+`; if not, BOM may need updating too.
* No other app-level `onBackPressed` overrides exist beyond the `BackHandler` composables.

## Requirements

* Upgrade `activity-compose` to at least `1.9.0` so `BackHandler` participates in the predictive back dispatch chain.
* Verify or upgrade `composeBom` if needed for compatibility.
* After the upgrade, the existing `BackHandler` composables should correctly intercept both the back button tap and the predictive back swipe gesture for overlay panels.
* Lint, tests, and debug build pass after the upgrade.

## Acceptance Criteria

* [ ] Predictive back swipe closes the add/edit task panel when it is open.
* [ ] Predictive back swipe closes the backup panel when it is open.
* [ ] System back button tap still works correctly for all panels.
* [ ] Quadrant detail back and app exit back still work.
* [ ] Lint, tests, and debug build pass.
* [ ] `CHANGELOG.md` updated.

## Definition of Done

* No new network/dependency download beyond the `activity-compose` and potential BOM upgrade.
* `CHANGELOG.md` is updated.
* Local git commit is created after user confirmation.

## Technical Approach

1. Upgrade `activityCompose` version in `gradle/libs.versions.toml` from `1.8.1` to `1.9.0` (or latest stable in the 1.9.x line).
2. Check if `composeBom` needs updating for compatibility; update if required.
3. Verify the existing `BackHandler` composables work with the upgraded library for both tap-back and swipe-back.
4. If `BackHandler` still does not intercept the predictive back gesture, consider wrapping overlay content in `ModalBottomSheet` (which natively handles predictive back) as a fallback.

## Decision (ADR-lite)

**Context**: `BackHandler` in `activity-compose 1.8.1` does not participate in Android 14's predictive back dispatch. The gesture callback reaches the Activity-level dispatcher before Compose's handlers.

**Decision**: Upgrade `activity-compose` to `1.9.0+`, which integrates `OnBackPressedDispatcher` with the predictive back callback chain. This is the standard Google-recommended path.

**Consequences**: Minimal code change (version bump only). If BOM incompatibility surfaces, a BOM bump may also be needed.

## Out of Scope

* Migrating panels to `ModalBottomSheet` (unless the upgrade alone does not fix the gesture).
* Predictive back animation customization.
* Saving draft state when back closes the edit panel.

## Technical Notes

* Relevant files: `gradle/libs.versions.toml`, `app/build.gradle.kts`, `MainActivity.kt`
* Current versions: `activityCompose = "1.8.1"`, `composeBom = "2024.02.00"`
* Android 14 predictive back was stabilized in `activity-compose 1.9.0-alpha01` and production-ready in `1.9.0`.
