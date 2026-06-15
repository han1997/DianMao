# Fix back navigation when editing tasks

## Goal

Fix the Android back button behavior so that when a sheet/panel is open (add task, edit task, backup panel), pressing back closes that panel instead of navigating away from the current screen or exiting the app. This prevents the user from accidentally losing context while editing.

## What I already know

* The app uses `BackHandler` at line 435 only for the quadrant detail view (`activePriority != null`).
* `AddTaskPanel` opens when `showAddSheet == true` or `taskForEdit != null`.
* `BackupPanel` opens when `showBackupPanel == true`.
* None of these overlay panels register a `BackHandler`, so the system back dispatches to the single existing handler (quadrant detail) or the Activity default (exit app).
* The reported bug: editing a task inside a quadrant detail, then pressing back, dismisses the quadrant detail instead of closing the edit panel.

## Assumptions

* Overlays should close in reverse order of opening (most recent first), matching user expectation.
* Compose `BackHandler` with `enabled` lambda is the idiomatic way to intercept back when a panel is visible.
* `BackupPanel` is a bottom sheet with its own `onDismiss`; it may already handle back via the sheet dismiss, but this should be verified.

## Requirements

* When `AddTaskPanel` is visible (add or edit mode), pressing back closes the panel and returns focus to the underlying screen.
* When `BackupPanel` is visible, pressing back closes the panel.
* When quadrant detail is open and no overlay is visible, pressing back returns to the quadrant board (existing behavior, unchanged).
* When no overlay and no quadrant detail, system back exits the app (existing behavior, unchanged).

## Acceptance Criteria

* [ ] Editing a task, pressing back closes the edit panel, not the quadrant detail.
* [ ] Adding a task, pressing back closes the add panel.
* [ ] Backup panel open, pressing back closes it.
* [ ] Quadrant detail (no overlay), back returns to board.
* [ ] Home screen (no overlay, no detail), back exits app.
* [ ] Lint and debug build pass.

## Definition of Done

* Code follows existing Compose and Android patterns.
* No new dependencies.
* `CHANGELOG.md` updated.

## Technical Approach

Add `BackHandler` composables for the add/edit panel and backup panel states, with `enabled` set to the panel's visibility flag. Compose dispatches back to the most-recently-enabled `BackHandler`, so the overlay handlers will naturally take priority over the quadrant-detail handler.

Order in the composable tree (innermost/most-recent wins):
1. `showBackupPanel` handler
2. `taskForEdit != null || showAddSheet` handler
3. `activePriority != null` handler (existing)

## Decision (ADR-lite)

**Context**: Multiple overlays can be open simultaneously (edit task inside quadrant detail). The back button must dismiss the topmost overlay first.

**Decision**: Use Compose `BackHandler` with `enabled` on each overlay. Compose's built-in dispatch ensures the innermost enabled handler wins, which naturally produces the correct stack pop order.

**Consequences**: Simple, idiomatic. No manual back-stack management needed.

## Out of Scope

* Navigation component or router migration.
* Predictive back gesture animations (Android 14+).
* Saving draft state when back closes the edit panel.

## Technical Notes

* Relevant file: `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
* Existing `BackHandler` at line 435 handles quadrant detail only.
* `BackupPanel` is rendered at line 563; `AddTaskPanel` at line 513.
