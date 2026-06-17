# Implement Drag-to-Reorder UI in Quadrant Detail View

## Goal

Add drag-and-drop reordering functionality to the quadrant detail view, allowing users to manually prioritize tasks within each quadrant.

## Prerequisites

✅ Database schema with `displayOrder` field (completed)
✅ Repository method `reorderTasks()` (completed)
✅ DAO queries ordering by `displayOrder` (completed)

## Requirements

### User Interaction

**Gesture:**
- Long-press on a task row to initiate drag mode
- Drag vertically to reorder
- Release to commit new position

**Visual Feedback:**
- Dragged item: elevated with shadow
- Drop target: visual indicator where item will land
- Smooth animation when items shift

**Scope:**
- Only in quadrant detail view (not main quadrant grid)
- Only within same quadrant (no cross-quadrant drag)
- Works for both completed and incomplete tasks

## Implementation Approach

### Option 1: LazyColumn with Custom Gestures (Native Compose)

Use `Modifier.pointerInput` with `detectDragGestures` to implement custom drag-drop.

**Pros:**
- No external dependencies
- Full control over behavior
- Lighter weight

**Cons:**
- More code to write
- Need to handle animation manually
- Edge cases (scroll + drag)

### Option 2: org.burnoutcrew.reorderable Library

Use battle-tested library: `org.burnoutcrew.reorderable:reorderable:0.9.6`

**Pros:**
- Production-ready, handles edge cases
- Smooth animations built-in
- Less code, faster implementation

**Cons:**
- External dependency (~100KB)
- Less flexibility for customization

**Decision:** Use Option 2 (reorderable library) for reliability and speed.

## Implementation Steps

### Step 1: Add Dependency

`app/build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.burnoutcrew.reorderable:reorderable:0.9.6")
}
```

### Step 2: Modify QuadrantDetailView

Current structure:
```kotlin
LazyColumn {
    items(tasks, key = { it.id }) { task ->
        TaskRow(...)
    }
}
```

New structure with reorderable:
```kotlin
val reorderableState = rememberReorderableLazyListState(
    onMove = { from, to -> /* reorder list locally */ }
)

LazyColumn(state = reorderableState.listState) {
    items(tasks, key = { it.id }) { task ->
        ReorderableItem(reorderableState, key = task.id) { isDragging ->
            val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
            
            TaskRow(
                task = task,
                modifier = Modifier.shadow(elevation.value),
                ...
            )
        }
    }
}

LaunchedEffect(reorderedList) {
    repository.reorderTasks(reorderedList)
}
```

### Step 3: State Management

**Local reordering state:**
- Keep a mutable list of tasks in composable
- Update immediately on drag (optimistic UI)
- Persist to repository on drop

**State flow:**
1. Load tasks from repository → local state
2. User drags → update local state immediately
3. User drops → persist via `reorderTasks()`
4. Repository updates → flow emits → UI reflects (already in correct order)

### Step 4: Visual Feedback

**Dragging state:**
- Elevation: 0dp → 8dp
- Scale: 1.0 → 1.02 (subtle)
- Border: add accent color border

**Drop target indicator:**
- Horizontal line between items
- Accent color
- Appears at drop position

### Step 5: Handle Edge Cases

**Completed vs incomplete tasks:**
- Allow reordering within each group
- Don't separate groups visually (keep current behavior)

**Empty state:**
- No drag if no tasks (handled naturally)

**Scroll during drag:**
- Library handles auto-scroll near edges

## Acceptance Criteria

* [ ] Long-press initiates drag mode
* [ ] Dragged item has visual elevation
* [ ] Items smoothly animate when reordering
* [ ] Drop commits new order
* [ ] Order persists across app restarts
* [ ] Works for both completed and incomplete tasks
* [ ] No jank or lag during drag
* [ ] Accessibility: drag handle or full row drag

## Technical Details

### Files to Modify

- `app/build.gradle.kts`: Add reorderable dependency
- `MainActivity.kt`: Update QuadrantDetail composable with reorderable state

### QuadrantDetail Location

Current implementation uses `modalBottomSheet` to show detail view.

### Reorderable API

```kotlin
rememberReorderableLazyListState(
    onMove = { from, to ->
        // Swap items in local list
        val list = tasks.toMutableList()
        list.add(to.index, list.removeAt(from.index))
        tasks = list
    }
)
```

### Persist on Drop

```kotlin
LaunchedEffect(tasks) {
    snapshotFlow { tasks }
        .debounce(300) // Wait for user to finish dragging
        .collect { reorderedTasks ->
            repository.reorderTasks(reorderedTasks)
        }
}
```

Or simpler: persist immediately on `onMove` (no debounce needed).

## Out of Scope

- Drag between quadrants (requires changing task.quadrant)
- Undo/redo
- Batch operations
- Keyboard navigation for reordering

## Design Notes

**Elevation during drag:**
- 8dp elevation creates clear "lifted" effect
- Subtle scale (1.02) enhances perception

**Animation timing:**
- Use library defaults (already tuned)
- Smooth spring animations for item shifts

**Touch target:**
- Entire TaskRow is draggable (no separate handle)
- Long-press threshold: 500ms (library default)

## Fallback Plan

If reorderable library has issues:
- Fall back to Option 1 (custom gestures)
- Or simplify to up/down arrow buttons
- Core data layer already works
