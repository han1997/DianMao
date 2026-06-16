# Add confirmation for uncompleting recurring tasks and preserve notes

## Goal

Add a confirmation dialog when unmarking recurring tasks as complete, and preserve completion notes when uncompleting any task.

## Current Behavior

- Clicking completed checkbox toggles completion immediately (no confirmation)
- `toggleCompleted(task, completed = false)` clears `completionNote` in the else branch
- Both recurring and non-recurring tasks behave the same way

## Requirements

### Confirmation Dialog for Recurring Tasks

**When to show:**
- User clicks checkbox to unmark a recurring task (any repeatRule != None)
- Or user clicks completed task row to toggle off completion
- Only when transitioning from completed → incomplete

**Dialog content:**
- Title: "取消完成重复事项？"
- Message: "这是一个重复事项，取消完成后将重新进入待办状态。是否继续？"
- Actions: "取消" (dismiss), "确认取消完成" (proceed)

**Non-recurring tasks:**
- No confirmation, toggle immediately (existing behavior)

### Preserve Completion Notes

**Current issue:**
```kotlin
else {
    // No repeat or uncompleting: normal toggle
    existing.copy(
        completed = completed,
        completionNote = if (completed) existing.completionNote else null,  // ← clears note
        updatedAtMillis = now
    )
}
```

**Fix:**
- Keep `completionNote` when uncompleting (remove conditional clear)
- Note persists until user explicitly edits it or task is deleted

## Implementation Plan

### 1. Add State for Confirmation Dialog

In DianMaoApp:
```kotlin
var taskForUncompletion by remember { mutableStateOf<TaskUiModel?>(null) }
```

### 2. Update toggleComplete Logic

```kotlin
val toggleComplete: (TaskUiModel) -> Unit = { task ->
    scope.launch {
        if (!task.completed) {
            // Completing: direct toggle
            repository.toggleCompleted(task, true)
            scheduler.cancel(task.id)
        } else if (task.repeatRule != RepeatRule.None) {
            // Uncompleting recurring task: show confirmation
            taskForUncompletion = task
        } else {
            // Uncompleting non-recurring: direct toggle
            repository.toggleCompleted(task, false)
        }
    }
}
```

### 3. Add Confirmation Dialog Composable

```kotlin
@Composable
private fun UncompleteRecurringTaskDialog(
    task: TaskUiModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
)
```

### 4. Wire Up Dialog in DianMaoApp

After existing dialogs (CompletionNoteDialog, DeleteTaskDialog):
```kotlin
taskForUncompletion?.let { task ->
    UncompleteRecurringTaskDialog(
        task = task,
        onDismiss = { taskForUncompletion = null },
        onConfirm = {
            scope.launch {
                repository.toggleCompleted(task, false)
            }
            taskForUncompletion = null
        }
    )
}
```

### 5. Fix Note Preservation in TaskRepository

Update `toggleCompleted`:
```kotlin
else {
    // Uncompleting: preserve note
    existing.copy(
        completed = completed,
        completionNote = existing.completionNote,  // keep note
        updatedAtMillis = now
    )
}
```

## Acceptance Criteria

* [ ] Uncompleting recurring task shows confirmation dialog
* [ ] Uncompleting non-recurring task toggles immediately (no dialog)
* [ ] Completing any task works as before (no dialog)
* [ ] Dialog has clear messaging about recurring task behavior
* [ ] Completion notes preserved when uncompleting
* [ ] Dialog can be dismissed without action

## UI Copy

**Dialog title:** 取消完成重复事项？

**Dialog message:** 这是一个重复事项，取消完成后将重新进入待办状态。是否继续？

**Buttons:** 取消 | 确认取消完成

## Technical Notes

Files to modify:
- `MainActivity.kt`: Add dialog state, toggleComplete logic, dialog composable
- `TaskRepository.kt`: Fix note preservation in toggleCompleted

## Out of Scope

- Confirmation for non-recurring tasks (keep immediate toggle)
- Editing completion notes separately (use existing EditNote action)
- Undo functionality
