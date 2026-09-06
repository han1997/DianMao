# Unify top bars, title weight, and icon containers

## Goal

Polish cross-surface visual consistency after the full-screen destination
conversion: every full-screen destination's top bar should share one chrome
dialect, titles should use a consistent weight, and destructive icon containers
should use the same low-alpha semantic treatment as the rest of the app.

## What I already know

- Full-screen destinations (BackupPanel, ArchivedPanel, AddTaskPanel, recurring
  history) render through `FullScreenScaffold`, whose top bar shows a quiet
  surface row with SemiBold titleLarge title, optional subtitle + tonal
  leading IconTile, trailing actions, and a 0.10 alpha hairline border
  (`ui/components/FullScreenScaffold.kt`, `FullScreenTopBar` is private).
- `TaskDetailScreen` still hand-draws its own top bar (Surface + quietBorder +
  titleLarge at FontWeight.Medium) instead of reusing the shared bar — the one
  full-screen destination not on the shared chrome.
- TaskDetail bottom delete button uses `errorContainer` solid fill; the rest of
  the app uses low-alpha semantic containers (e.g. `error.copy(alpha=0.10f)`).
- An inline change already removed the tonal+shadow frame around the
  AddTaskPanel save button (uncommitted in the working tree).

## Decisions (user-confirmed)

1. Extract/reuse the shared full-screen top bar in TaskDetailScreen rather than
   wrapping TaskDetail in the FullScreenScaffold shell (which would add a 600dp
   centered width cap and regress the full-width detail layout).
2. Unify TaskDetail top-bar title weight to SemiBold (match FullScreenScaffold).
3. Change TaskDetail's delete icon container from `errorContainer` solid fill to
   the app-standard low-alpha semantic container.
4. Keep intentional exceptions: Home "DianMao" title stays Bold (brand),
   QuadrantDetail's tinted quadrant header stays as-is (a scrollable content
   header, not a top bar), TaskDetail's multi-action bottom bar frame stays.
5. Fold in the uncommitted AddTaskPanel save-button de-framing change and commit
   everything together.

## Requirements

- [ ] TaskDetail top bar reuses the shared top-bar composable (same chrome as
      other full-screen destinations).
- [ ] TaskDetail top-bar title weight matches the shared bar (SemiBold).
- [ ] TaskDetail delete button uses low-alpha semantic icon container.
- [ ] Full-screen layout width semantics are unchanged (no 600dp wrap on
      TaskDetail).

## Acceptance Criteria

- [ ] TaskDetail, AddTaskPanel, BackupPanel, ArchivedPanel, history panel show
      one consistent top-bar treatment.
- [ ] Lint / type-check / unit tests / assemble pass.
- [ ] git diff --check clean.

## Out of Scope

- Home title (brand), QuadrantDetail header (content block), TaskDetail bottom
  multi-action bar, all non-delete icon treatment changes, pickers, dialogs.

## Technical Notes

- `ui/components/FullScreenScaffold.kt` — make `FullScreenTopBar` internal.
- `ui/screens/TaskDetailScreen.kt` — replace hand-drawn top bar (lines ~134-166)
  with shared bar; title weight; delete icon container (lines ~439-451).
- Uncommitted AddTaskPanel save-bar de-framing change is part of this commit.
