# Fix Summary: Edit Item Back Behavior

## Problem

When editing an item from a secondary page (quadrant detail), pressing back would bypass the secondary page and return directly to the main page, instead of first closing the edit panel and returning to the secondary page.

## Root Cause

The `BackHandler` callback was re-computing the back target inside the lambda instead of using the pre-computed value. This created a subtle timing issue:

**Before (lines 484-492):**
```kotlin
val backTarget = resolveInAppBackTarget(...)

BackHandler(enabled = backTarget != null) {
    when (resolveInAppBackTarget(...)) {  // ❌ Re-computing!
        InAppBackTarget.BackupPanel -> showBackupPanel = false
        InAppBackTarget.ArchivedPanel -> showArchivedPanel = false
        InAppBackTarget.TaskPanel -> dismissTaskPanel()
        InAppBackTarget.QuadrantDetail -> activePriority = null
        null -> Unit
    }
}
```

The issue: when the `BackHandler` callback executes, it re-evaluates `resolveInAppBackTarget` with the current state. If state changes happen rapidly (e.g., closing the edit panel triggers a recomposition that also affects the secondary page state), the second evaluation might see different state than the first, causing incorrect back behavior.

## Solution

Use the pre-computed `backTarget` variable consistently:

**After (lines 484-492):**
```kotlin
val backTarget = resolveInAppBackTarget(...)

BackHandler(enabled = backTarget != null) {
    when (backTarget) {  // ✓ Using pre-computed value
        InAppBackTarget.BackupPanel -> showBackupPanel = false
        InAppBackTarget.ArchivedPanel -> showArchivedPanel = false
        InAppBackTarget.TaskPanel -> dismissTaskPanel()
        InAppBackTarget.QuadrantDetail -> activePriority = null
        null -> Unit
    }
}
```

## Why This Fix Works

1. **Consistent state snapshot**: The `backTarget` is computed once during composition with a consistent snapshot of all state variables.

2. **Deterministic priority**: The back target is determined at composition time using explicit priority rules, not at callback execution time.

3. **No state race conditions**: The callback executes the action corresponding to the pre-determined target, avoiding any timing-dependent behavior.

## Prevention Guidelines

### 1. Always Use Pre-computed Values in Callbacks

❌ **Bad**: Re-computing inside callbacks
```kotlin
val target = computeTarget(state)
Button(onClick = {
    when (computeTarget(state)) { ... }  // Wrong!
})
```

✓ **Good**: Using pre-computed values
```kotlin
val target = computeTarget(state)
Button(onClick = {
    when (target) { ... }  // Correct!
})
```

### 2. Follow the Overlay Navigation Thinking Guide

As documented in `.trellis/spec/guides/overlay-navigation-thinking-guide.md`:

- **Model back priority explicitly** as ordered state, not registration order
- **Prefer one source of truth** for dismissal operations
- **Write regression tests** for priority order (already exists in `InAppBackTargetTest.kt`)
- **Propagate new layers through the model** when adding new overlays

### 3. Verify Priority Order

The existing test suite (`InAppBackTargetTest.kt`) ensures:
- Backup panel > Archived panel > Task panel > Quadrant detail
- System back is used when no overlay is visible

Always run tests after modifying back navigation logic.

## Files Modified

- `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` (line 485)
  - Changed: Use pre-computed `backTarget` instead of re-computing
- `CHANGELOG.md`
  - Added entry documenting the fix

## Verification

- ✅ Lint: Passed
- ✅ Unit tests: All 5 tests in `InAppBackTargetTest` pass
- ✅ Type check: No compilation errors
- ✅ Build: Successful

## Testing Checklist

To verify this fix manually:

1. [ ] Open any quadrant detail page (secondary page)
2. [ ] Tap edit on any task item
3. [ ] Press system back button or gesture → Should close edit panel and return to quadrant detail
4. [ ] Repeat steps 1-2
5. [ ] Tap app bar back button in edit panel → Should close edit panel and return to quadrant detail
6. [ ] From main page, add new task
7. [ ] Press system back → Should close add panel and return to main page
8. [ ] Open backup panel
9. [ ] Press system back → Should close backup panel and return to previous page

## Lessons Learned

### Why This Bug Persisted

The issue was subtle because:
1. The code structure appeared correct (single BackHandler, explicit priority function)
2. The bug only manifested in specific state transition scenarios
3. Re-computing inside the callback seemed harmless but violated composition principles

### Core Principle

**In Compose, always capture computed values at composition time and use those captures in callbacks.** Don't re-compute inside callbacks unless you explicitly need current runtime state (rare).

This is similar to React's dependency array principle: what you use should match what you capture.
