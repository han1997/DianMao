# Redesign secondary surfaces and task editing

## Goal
Replace the visually heavy, inconsistent overlay experience with coherent, polished native Android secondary surfaces. The user explicitly authorizes changing presentation patterns instead of retaining modal sheets and allows mainstream productivity-app references.

## Known Context
- The user dislikes all dialogs/panels, especially archive, settings, add task, and edit task.
- DianMao is an Android Compose task app with a warm, restrained visual identity.
- Recent safe-area and FAB layering fixes must not regress.
- User delegated aesthetic decisions; no blocking preference question is required.
- **User correction (2026-09-05):** secondary surfaces must inherit the home screen's warm, soft-paper visual identity — NOT vanilla Material3 defaults. Style continuity with the home screen is a hard requirement.

## Requirements
- Audit every app-owned modal/panel/dialog, not only the named four.
- Prefer full-screen secondary destinations for long settings/archive/editor workflows; reserve compact dialogs for short, focused decisions.
- **Style continuity with home (hard requirement):** secondary surfaces reuse the home screen's visual system — warm paper background (`#F8F8F5`), terracotta primary + muted blue secondary, near-flat `PanelShape`/`RowShape`/`ChipShape` cards with soft hairline borders (`quietBorder`) and very-low-alpha tinted fills, no heavy elevation/shadows. Extract shared `IconTile`/`SummaryPill`/`CompactMeta`/`ColorDot`-family treatments where needed. This is a redesign in the app's own visual language, not a vanilla MD3 pass.
- Establish shared typography, spacing, navigation, action hierarchy, and safe-area treatment.
- Preserve task fields, subtask workflows, reminders/calendar, backup/restore, archive/recovery, history/notes, confirmations, and keyboard interactions.
- Keep irreversible actions explicit and avoid accidental data changes or network activity.
- Provide loading, error, empty, long-content, and small-screen behavior.

## Acceptance Criteria
- [ ] Settings, archive, add, and edit have a visibly coherent new presentation.
- [ ] New surfaces read as DianMao-native (warm palette, soft hairlines, low-alpha tints, near-flat shapes) rather than default Material3 — confirmed visually.
- [ ] All app-owned dialogs and history/note/selector surfaces are reviewed and brought into the same visual system.
- [ ] Back navigation closes the correct top layer and restores the originating screen.
- [ ] Status bars, cutouts, navigation bars, and IME do not obscure content/actions.
- [ ] Existing persistence/platform behavior is preserved; relevant regression tests pass.
- [ ] Unit tests, lint, debug build, and diff checks pass; visual validation and limitations are recorded.

## Definition of Done
- Product/design/spec documentation reflects the new presentation contract.
- Code review and verification are complete; user commit confirmation is requested before any work commit.

## Out of Scope
- Home/task-list redesign, new stored fields, database migrations, external sync providers, and unrelated IDE/tooling files.

## Open Questions
None currently. Resolve technical/layout questions by inspecting the app first.

## Design Contract (visual system shared across all secondary surfaces)

Audited surfaces (see Technical Notes for file map): BackupPanel, ArchivedPanel,
AddTaskPanel, RecurringTaskHistoryPanel (ModalBottomSheet), CompletionNoteDialog,
DeleteTaskDialog, UncompleteRecurringTaskDialog, TaskDetailScreen in-detail
dialogs/note editors, and the date/time picker dialogs.

Base visual tokens (already on home, MUST be reused on secondary surfaces):
- Container surface = `MaterialTheme.colorScheme.surface` / `background`; warm paper
  scrim `Color(0xFF211815).copy(alpha = 0.16f)` (not black).
- Shape language: `PanelShape` 18dp / `SheetShape` 22dp top / `RowShape` 12dp /
  `ChipShape` 8dp / `IconTileShape` 14dp.
- Borders = `quietBorder()` hairline, not heavy outlines. Low-alpha tinted fills
  (quadrant/semantic color at ~5–10% alpha) instead of saturated surfaceVariant.
- Icons live in tonal `IconTile` containers; header actions are 48dp `IconButton`.
- Motion vocabulary: enter `fadeIn(150–200ms) + slideInVertically(220–280ms, 1/4
  travel)`; exit `fadeOut(120–160ms) + slideOutVertically(180–220ms, 1/4 travel)`.
- Letter spacing `0.sp`; no `Color.White`/`Color.Black` in new UI code.
- Full-screen destinations own safe area via a single
  `windowInsetsPadding(WindowInsets.safeDrawing)` on their content column
  (mirror TaskDetailScreen). Compact dialogs keep `windowInsetsPadding`.

Surface-specific presentation contract (user-confirmed 2026-09-05):
1. Backup (设置/备份与恢复), Archive (归档), and Task Add/Edit become FULL-SCREEN
   destinations that render like TaskDetailScreen — same root `Surface` +
   `safeDrawing` column + quiet top bar (back `IconButton` + title). Not
   bottom-sheet cards over the dimmed board.
2. Full-screen task form layout: single-column form, content width capped at
   ~600dp and centered on large/expanded windows. Subsection grouping with
   `TaskFormSection`/`FormSettingBlock`-style tinted group surfaces.
3. Compact `AlertDialog`/`Card` dialogs REMAIN the home for short irreversible
   confirmations (delete task, un-complete recurring, delete completion record)
   and for the completion-note + date/time pickers. They must inherit theme via
   `PanelShape`/`RowShape`, warm scrim, tonal icon headers — no vanilla M3 chrome.
4. `RecurringTaskHistoryPanel`'s `ModalBottomSheet` becomes a full-screen history
   surface consistent with 1 (task info header + stats + history list).

## Technical Approach

State-machine navigation (no Navigation-Compose). Back model is a pure
`resolveInAppBackTarget` resolver + `InAppBackTarget` enum in
`ui/logic/HomeModels.kt`, dispatched once from the composition-time snapshot.
Converting panels to full-screen destinations keeps this resolver model but
changes each branch from an overlay card to a full-screen sibling surface under
the root `Box`, plus a per-layer top-bar back that routes through the same
centralized dismiss mutation. Every dismiss path (app-bar back, system
back-handler branch) must call the same mutation; never re-read state inside
back callbacks.

## Decision (ADR-lite)

**Context**: User dislikes every modal/panel surface. Wants continuity with the
home's warm soft-paper identity, not vanilla MD3.
**Decision**: 
- Present Backup / Archived / Add / Edit as full-screen destinations (like the
  existing TaskDetailScreen), not floating cards/sheets over the dimmed home.
- Keep short confirmations + completion-note + date/time pickers as compact
  themed dialogs.
- Convert the recurring-task history bottom sheet into a full-screen surface.
- Everything reuses the home visual tokens (quiet surfaces, tonal icons,
  hairline borders, warm scrim, `safeDrawing`, shared motion vocabulary).
**Consequences**: Consistent, lighter secondary surfaces; larger per-file
refactors across panels; a new shared full-screen scaffold is introduced to
avoid drift between the four+ destinations.

## Technical Notes
- Work inline, without subagents, per project workflow.
- Use UTF-8-safe Python edits and preserve unrelated .idea/.impeccable/.opencode changes.
- File map of audited surfaces (all `app/src/main/java/fun/han1997/dianmao/`):
  - `MainActivity.kt` — `DianMaoApp` state machine + root Box + Scaffold; owns
    `showBackupPanel/showArchivedPanel/showAddSheet/taskForEdit/taskForDetail`.
  - `ui/logic/HomeModels.kt` — `InAppBackTarget` enum + `resolveInAppBackTarget`.
  - `ui/theme/DianMaoTheme.kt` — theme tokens, shapes, `quietBorder`.
  - `ui/screens/TaskDetailScreen.kt` — reference full-screen surface + dialogs +
    `RecurringTaskHistoryPanel` (bottom sheet to convert) + note/detail dialogs.
  - `ui/panels/AddTaskPanel.kt`, `ui/panels/BackupPanel.kt` (also ArchivedPanel
    + ArchivedTaskItem), `ui/panels/Dialogs.kt` — overlay cards/sheets/dialogs to
    convert/restyle.
  - `ui/components/*` — `CommonComponents`, `DateTimePickers`, `SubTaskComponents`
    (`TaskFormSection`, `FormSettingBlock`), `TaskRow`, `SubTaskEditor`.
  - Tests: `app/src/test/.../InAppBackTargetTest.kt` covers resolver priority —
    update alongside nav changes.
