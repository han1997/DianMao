# Add dedicated archived tasks view

## Goal

Create a separate page for viewing archived tasks, remove archived tasks from main view, and add navigation entry to access the archive.

## Current Behavior

- Archived tasks are mixed with active tasks in the main view (actually filtered by `activeTasks` flow)
- `showArchivedPanel` state exists but may show archived tasks in overlay/sheet
- User needs to manually distinguish archived vs active tasks

## Requirements

### UI Structure

**Main view:**
- Show only active tasks (archived = false)
- Current quadrant-based layout unchanged
- Add navigation entry to archived view

**Archived view (new):**
- Dedicated screen/page for archived tasks
- List all archived tasks (no quadrant filtering needed)
- Each task shows: title, completion note if any, completion timestamp
- Actions: unarchive button, view details, delete

**Navigation entry:**
- Add button/icon in main view to navigate to archived view
- Placement: top app bar or bottom nav (depends on existing UI pattern)
- Icon: Archive/History related (Icons.Rounded.Archive or similar)

### Data Flow

- `repository.archivedTasks` already exists
- Main view uses `repository.activeTasks` (already filters archived)
- Archived view uses `repository.archivedTasks`

## Implementation Plan

1. **Create ArchivedTasksScreen composable**
   - List of archived tasks
   - Each item: TaskRow variant or simplified display
   - Unarchive action
   - Empty state if no archived tasks

2. **Add navigation state**
   - `var showArchivedView by rememberSaveable { mutableStateOf(false) }`
   - Or use proper navigation component if exists

3. **Add navigation entry in main view**
   - IconButton in TopAppBar or FloatingActionButton
   - Opens archived view on click

4. **Remove old showArchivedPanel logic** (if redundant)
   - Check if `showArchivedPanel` is still used
   - Clean up if archived view replaces it

## Acceptance Criteria

* [ ] Main view shows only active tasks
* [ ] Archived view accessible via navigation entry
* [ ] Archived view lists all archived tasks
* [ ] Unarchive action works from archived view
* [ ] Empty state shown when no archived tasks
* [ ] Navigation flow is intuitive

## UI Design Notes

**Archived view layout:**
- Simple list (no quadrant grouping needed)
- Each item: title + meta (completion date)
- Actions: unarchive icon button
- Subtle visual difference from active tasks (e.g., muted colors)

**Navigation entry:**
- Check existing top bar for icon placement
- Use consistent icon style with existing UI

## Technical Notes

Files to modify:
- `MainActivity.kt`: Add ArchivedTasksScreen composable, navigation state, entry point
- Verify `activeTasks` already filters archived (should be `observeActiveTasks()` from DAO)

## Out of Scope

- Search/filter in archived view (simple list only)
- Bulk unarchive operations
- Export archived tasks separately
