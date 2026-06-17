# Implement Native Drag-and-Drop Reordering

## Goal

Replace the move up/down buttons with true drag-and-drop functionality using native Compose gestures. Users can long-press and drag tasks to reorder them smoothly.

## User Requirements

- Long-press on a task to start dragging
- Drag vertically to reorder
- Visual feedback during drag (elevation, other items shift)
- Smooth animations
- One gesture to move multiple positions

## Implementation Approach

Use native Compose gestures with:
- `Modifier.pointerInput` for detecting drag gestures
- `LazyColumn` with `animateItemPlacement()` for smooth reordering animations
- Offset tracking for drag position
- State management for reordering

## Technical Plan

### 1. Drag State Management

```kotlin
@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragDropState {
    // Track dragging task, current offset, target position
}
```

### 2. Gesture Detection

- Use `detectDragGesturesAfterLongPress` for long-press + drag
- Calculate drop position from drag offset
- Update item positions during drag

### 3. Visual Feedback

- Dragged item: increased elevation, slight scale
- Other items: animate to new positions
- Use `Modifier.graphicsLayer` for smooth transforms

### 4. LazyColumn Setup

```kotlin
LazyColumn(state = lazyListState) {
    items(tasks, key = { it.id }) { task ->
        Box(
            modifier = Modifier
                .animateItemPlacement()
                .dragGesture(...)
        ) {
            TaskRow(task)
        }
    }
}
```

## Acceptance Criteria

* [ ] Long-press initiates drag
* [ ] Task follows finger during drag
* [ ] Other items animate smoothly out of the way
* [ ] Drop commits new order
* [ ] Order persists to database
* [ ] Works smoothly with scrolling
* [ ] Visual elevation during drag

## Files to Modify

- `MainActivity.kt`: Add drag state and gesture detection
