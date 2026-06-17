# Optimize Drag-Drop Animation to Prevent Duplicate Swaps

## Problem

When dragging a task to a new position, there's a duplicate swap animation. The items swap during drag, then swap again after release.

## Root Cause

Two separate list updates trigger animations:

1. **During drag** (`onDrag`): Items swap in local state → triggers animateItemPlacement()
2. **After drag end** (`LaunchedEffect`): Repository persists → flow emits → state updates again → duplicate animation

The second update is redundant because the list is already in the correct order.

## Solution

Only persist to repository on drag end, don't trigger UI update from flow while dragging.

### Approach 1: Debounce Flow Updates

Ignore flow updates for a short period after drag end to prevent duplicate animation.

### Approach 2: Reset draggedOffset Gradually

Instead of instant reset, animate draggedOffset back to 0, making the transition smoother.

### Approach 3: Single Source of Truth

Keep list in local state during drag, persist on drag end without re-reading from flow immediately.

**Recommended: Approach 3** - simplest and most reliable.

## Implementation

**Current flow:**
```
Drag → onMove → update local list → animate
Drag end → persist → flow emits → update local list → animate again ❌
```

**Fixed flow:**
```
Drag → onMove → update local list → animate
Drag end → persist (local list already correct, no re-render) ✓
```

Key: Don't reset `reorderableDetailTasks` from `quadrantDetailTasks` immediately after persisting.

Use a flag to track if we're currently dragging or just finished dragging, and skip the LaunchedEffect update during that window.

## Testing

- Drag task up → smooth single animation
- Drag task down → smooth single animation  
- No duplicate swap after release
- Order still persists correctly
