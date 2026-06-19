# Auto-archive completed non-recurring tasks

## Goal

Automatically archive completed tasks that have no repeat rule, keeping the main view focused on active and recurring responsibilities.

## Current Behavior

- All completed tasks stay in the active task list
- User must manually archive each completed task
- Main view becomes cluttered with old completed tasks

## Requirements

### Auto-Archive Logic

**When completion happens:**
- If `repeatRule == None` AND task becomes `completed == true`
- Automatically set `archived = true`
- Task moves to archived panel immediately

**What should NOT auto-archive:**
- Recurring tasks (Daily/Weekly/Monthly/Count) - user may want to see completion history
- Tasks that are already archived

### User Experience

**Completion flow:**
1. User marks non-recurring task as complete (with or without note)
2. Task disappears from main view immediately
3. Task appears in archived panel
4. User can unarchive if needed

**Recurring tasks:**
- Cycle-based with alarm: advance to next cycle, stay active
- Cycle-based without alarm: mark completed, stay active until auto-reset
- Count-based: mark completed with counter, stay active
- All recurring tasks remain visible to show progress/history

## Implementation

### TaskRepository Changes

Update `toggleCompleted` and `saveCompletionNote`:
- After setting `completed = true`
- Check if `repeatRule == RepeatRule.None`
- If true, also set `archived = true` in same update

### No UI Changes Needed

- Archived panel already exists
- `activeTasks` flow already filters by `archived = false`
- Badge/display logic unchanged

## Acceptance Criteria

* [ ] Completing a non-recurring task auto-archives it
* [ ] Task disappears from main view immediately
* [ ] Task appears in archived panel
* [ ] Recurring tasks (all types) do NOT auto-archive
* [ ] Manual archive/unarchive still works
* [ ] Completion note flow triggers auto-archive

## Out of Scope

- Batch auto-archive of existing completed tasks (manual cleanup only)
- Auto-archive delay/timer (immediate is fine)
- User setting to disable auto-archive (always on for now)

## Technical Notes

Files to modify:
- `TaskRepository.kt`: `toggleCompleted`, `saveCompletionNote` methods
- Add `archived = true` when `completed = true && repeatRule == None`
