# Fix Drag-Drop Index Offset Bug with Header Item

## Problem

When long-pressing the first task, the second task gets selected. The index calculation is off by one because the LazyColumn has a header item (QuadrantDetailHeader) that shifts all task indices.

## Root Cause

The `onDragStart` callback receives the layout index from `layoutInfo.visibleItemsInfo`, which includes:
- Index 0: QuadrantDetailHeader
- Index 1: First task
- Index 2: Second task
- etc.

But `itemsIndexed` for tasks starts from index 0, so there's a mismatch.

## Solution

Subtract 1 from the detected index to account for the header item before passing to `onDragStart`.

## Implementation

In the `pointerInput` block where we detect drag start:
```kotlin
onDragStart = { offset ->
    listState.layoutInfo.visibleItemsInfo
        .firstOrNull { item ->
            offset.y.toInt() in item.offset..(item.offset + item.size)
        }
        ?.let { 
            // Subtract 1 to account for header item
            val taskIndex = it.index - 1
            if (taskIndex >= 0) {
                dragDropState.onDragStart(taskIndex)
            }
        }
}
```

## Testing

- Long-press first task → first task should be selected
- Long-press second task → second task should be selected
- Drag should work correctly for all positions
