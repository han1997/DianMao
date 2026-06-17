# Simplify Task Display and Add Drag-to-Reorder in Quadrant Detail

## Goal

Simplify the main quadrant view to show more tasks with better readability, and add drag-to-reorder functionality in the quadrant detail view for better task management.

## User Requirements

1. **Quadrant pane (main view):** Show more tasks per quadrant by simplifying the display
2. **Task title:** Display task names more completely (less truncation)
3. **Quadrant detail view:** Support drag-and-drop reordering of tasks within a quadrant

## Current State

### QuadrantPane (Main View)
**Current behavior:**
- Shows only 2 tasks per quadrant (`tasks.take(2)`)
- Each task displays: priority color dot + title + multiple metadata badges (schedule, calendar, alarm, repeat, note indicators)
- Metadata takes significant vertical space

**Issues:**
- Only 2 tasks visible at a glance
- Metadata badges (CompactMeta) reduce density
- Users want to see more tasks without entering detail view

### QuadrantDetail View
**Current behavior:**
- Shows all tasks in a LazyColumn
- Static list, no reordering capability
- Full TaskRow component with all metadata

**Issues:**
- No way to manually prioritize/order tasks within a quadrant
- Users want to drag tasks to reorder by importance

## Design Requirements

### 1. Simplified Task Preview in QuadrantPane

**Show 3-4 tasks** instead of 2 (density improvement)

**Simplify task display:**
- Remove individual metadata badges (Schedule, Calendar, Alarm icons)
- Show only: priority dot + task title
- Optional: single icon indicator if task has ANY timing (alarm OR calendar OR due date)

**Title display:**
- Increase maxLines from 1 to 2 for better visibility
- Remove ellipsis where possible within 2 lines

**Visual changes:**
```kotlin
// Current: Full task preview with all metadata
TaskPreview(task) {
    Row { ColorDot + Title (maxLines=1) }
    FlowRow { CompactMeta × N }  // ← Remove this
}

// Proposed: Compact task preview
CompactTaskItem(task) {
    Row {
        ColorDot
        Title (maxLines=2, flex)
        [Optional: TimingIndicator icon if has timing]
    }
}
```

### 2. Drag-to-Reorder in Quadrant Detail

**Interaction:**
- Long-press on TaskRow to enter drag mode
- Drag task up/down to reorder within the quadrant
- Visual feedback: dragged item elevation + shadow
- Drop to commit new order

**Implementation approach:**
- Use `androidx.compose.foundation.lazy.LazyItemScope.animateItemPlacement()`
- Use `org.burnoutcrew.reorderable` library (or equivalent drag-drop solution)
- Store custom order in database (new field: `displayOrder` or `manualSortOrder`)

**Data model changes:**
- Add `displayOrder: Int` field to TaskEntity
- Default value: use `createdAtMillis` or `updatedAtMillis` for initial order
- Update `TaskDao` to query by `displayOrder` within each quadrant

**Database migration:**
```kotlin
// Migration N -> N+1
ALTER TABLE tasks ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0;
UPDATE tasks SET displayOrder = id; // initial value
```

**Repository method:**
```kotlin
suspend fun reorderTask(taskId: Long, newOrder: Int) {
    taskDao.updateDisplayOrder(taskId, newOrder)
}
```

## Acceptance Criteria

### QuadrantPane (Main View)
* [ ] Shows 3-4 tasks per quadrant (increased from 2)
* [ ] Task preview simplified: color dot + title only
* [ ] Task title wraps to 2 lines (no truncation if fits)
* [ ] Optional: single timing icon if task has any timing
* [ ] Metadata details only visible in detail view

### QuadrantDetail View
* [ ] Tasks can be reordered via drag-and-drop
* [ ] Long-press initiates drag mode
* [ ] Visual feedback during drag (elevation, shadow)
* [ ] Order persists across app restarts
* [ ] Smooth animations during reorder

### Data Persistence
* [ ] Database migration adds `displayOrder` field
* [ ] Tasks query respects custom order
* [ ] New tasks get appropriate default order

## Implementation Steps

### Phase 1: Simplify QuadrantPane Display

1. Create `CompactTaskPreview` composable
   - Color dot + title (2 lines max)
   - Optional timing indicator
   - Remove all CompactMeta badges

2. Update `QuadrantPane`
   - Change `tasks.take(2)` to `tasks.take(4)`
   - Replace full TaskPreview with CompactTaskPreview
   - Adjust vertical spacing for density

### Phase 2: Database Schema

1. Create migration to add `displayOrder` field
2. Update TaskEntity with `displayOrder: Int = 0`
3. Update TaskDao queries to ORDER BY `displayOrder`
4. Add `updateDisplayOrder(taskId, order)` method

### Phase 3: Drag-to-Reorder UI

1. Add reorderable library dependency (if not already present)
2. Wrap LazyColumn in quadrant detail with reorderable state
3. Add long-press detection to TaskRow
4. Implement drag gesture handling
5. Update repository on drop to save new order

### Phase 4: Order Calculation

When reordering between position A and B:
- Calculate new order value (e.g., average of surrounding items)
- Or: renumber all items in quadrant sequentially
- Ensure no order collisions

## Technical Notes

**Reorderable library options:**
- `org.burnoutcrew.reorderable:reorderable:0.9.6` (widely used)
- Custom implementation with `detectDragGestures` + `Modifier.offset()`

**Order field strategy:**
- Use integer with gaps (1000, 2000, 3000) for easy insertions
- Or use floating point for infinite precision
- Or renumber entire list on every change (simpler, less efficient)

**Files to modify:**
- `MainActivity.kt`: QuadrantPane, CompactTaskPreview, QuadrantDetailView drag logic
- `TaskEntity.kt`: Add displayOrder field
- `TaskDao.kt`: Update queries and add updateDisplayOrder
- `TaskRepository.kt`: Add reorderTask method
- `AppDatabase.kt`: Migration for schema change

## Out of Scope

- Reordering across quadrants (requires moving task's priority)
- Reordering in main view (only in detail view)
- Undo/redo for reordering
- Batch reorder operations

## Design Considerations

**Information hiding is good:**
- Main view: high-level overview, many tasks visible
- Detail view: full information + management actions

**Progressive disclosure:**
- Glanceable → Scannable → Actionable
- Main quadrant → Detail list → Individual task edit

**Touch targets:**
- Maintain 48dp minimum for tap targets
- Drag handle or entire row drag area
