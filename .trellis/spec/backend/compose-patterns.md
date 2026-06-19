# Compose Patterns

> Executable contracts for Jetpack Compose UI implementation

## Scenario: BackHandler State Capture

### 1. Scope / Trigger

**Trigger**: Any `BackHandler` or callback that depends on mutable Compose state to determine behavior.

**Why code-spec depth**: State re-evaluation inside callbacks can see different values than composition time, causing inconsistent navigation or action execution. This crosses the composition/execution boundary.

### 2. Signatures

```kotlin
// State resolver (composition time)
fun resolveInAppBackTarget(
    backupPanelShown: Boolean,
    archivedPanelShown: Boolean,
    taskPanelState: TaskPanelState,
    activePriority: Priority?
): InAppBackTarget

// BackHandler callback (execution time)
BackHandler(enabled = backTarget != InAppBackTarget.None) {
    // Callback body executes after composition
}
```

### 3. Contracts

**Composition-time state snapshot**:
- Must compute derived state (like back target) during composition
- Store result in a `val` that captures the snapshot
- Type: `InAppBackTarget` (enum representing priority order)

**Callback-time execution**:
- Must reference the pre-computed snapshot variable
- Must NOT call state resolver functions inside the callback
- Must NOT read mutable state variables directly

**State mutation**:
- Dismiss operations must mutate the original state variables
- Type: `Boolean` flags or sealed class states that trigger recomposition

### 4. Validation & Error Matrix

| Condition | Correct Behavior | Wrong Behavior |
|-----------|-----------------|----------------|
| State changes during dismiss | Uses snapshot from composition | Re-evaluates and sees new state |
| Multiple layers enabled | Closes topmost per snapshot | Closes wrong layer due to timing |
| Rapid state changes | Deterministic from snapshot | Race condition between states |

### 5. Good/Base/Bad Cases

**Good**: Pre-compute at composition time, use snapshot in callback
```kotlin
val backTarget = resolveInAppBackTarget(
    backupPanelShown, archivedPanelShown, 
    taskPanelState, activePriority
)
BackHandler(enabled = backTarget != InAppBackTarget.None) {
    when (backTarget) {  // Uses snapshot
        InAppBackTarget.BackupPanel -> { backupPanelShown = false }
        // ...
    }
}
```

**Base**: Direct state read in callback (works if state is stable)
```kotlin
BackHandler(enabled = someFlag) {
    if (someFlag) {  // OK if someFlag never changes during callback
        someFlag = false
    }
}
```

**Bad**: Re-compute inside callback
```kotlin
val backTarget = resolveInAppBackTarget(...)
BackHandler(enabled = backTarget != InAppBackTarget.None) {
    when (resolveInAppBackTarget(...)) {  // WRONG: re-evaluates
        InAppBackTarget.BackupPanel -> { backupPanelShown = false }
        // State seen here may differ from composition time
    }
}
```

### 6. Tests Required

**Unit tests** for state resolver (pure function):
- Assert priority order: backup > archived > task > quadrant
- Assert None when all layers closed
- Assert correct target for each combination

Example assertions:
```kotlin
@Test
fun `edit panel over quadrant detail resolves to TaskPanel`() {
    val target = resolveInAppBackTarget(
        backupPanelShown = false,
        archivedPanelShown = false,
        taskPanelState = TaskPanelState.Editing(taskId = 1),
        activePriority = Priority.P1
    )
    assertEquals(InAppBackTarget.TaskPanel, target)
}
```

**Manual test** for callback execution:
- Open multiple layers
- Trigger back via system gesture and app bar button
- Verify layers close in priority order (topmost first)

### 7. Wrong vs Correct

#### Wrong
```kotlin
// Line 477: Compute back target
val backTarget = resolveInAppBackTarget(
    backupPanelShown, archivedPanelShown,
    taskPanelState, activePriority
)

// Line 484: Re-compute inside callback
BackHandler(enabled = backTarget != InAppBackTarget.None) {
    when (resolveInAppBackTarget(  // BUG: re-evaluates state
        backupPanelShown, archivedPanelShown,
        taskPanelState, activePriority
    )) {
        InAppBackTarget.BackupPanel -> { 
            backupPanelShown = false  // Changes state
        }
        InAppBackTarget.TaskPanel -> {
            taskPanelState = TaskPanelState.Closed
        }
        InAppBackTarget.QuadrantDetail -> {
            activePriority = null  // Changes state
        }
        // When dismiss changes state, re-evaluation sees
        // different values than the composition-time snapshot
    }
}
```

**Problem**: After `backupPanelShown = false` executes, the next line re-evaluating `resolveInAppBackTarget()` sees `backupPanelShown=false` and might jump to `TaskPanel` or `QuadrantDetail` instead of just returning. This causes layers to be skipped.

#### Correct
```kotlin
// Line 477: Compute back target ONCE at composition time
val backTarget = resolveInAppBackTarget(
    backupPanelShown, archivedPanelShown,
    taskPanelState, activePriority
)

// Line 484: Use the snapshot in callback
BackHandler(enabled = backTarget != InAppBackTarget.None) {
    when (backTarget) {  // Uses pre-computed snapshot
        InAppBackTarget.BackupPanel -> { 
            backupPanelShown = false
        }
        InAppBackTarget.ArchivedPanel -> {
            archivedPanelShown = false
        }
        InAppBackTarget.TaskPanel -> {
            taskPanelState = TaskPanelState.Closed
        }
        InAppBackTarget.QuadrantDetail -> {
            activePriority = null
        }
        InAppBackTarget.None -> {}
    }
}
```

**Why it works**: The `when` expression reads `backTarget` once. State mutations inside the branches don't affect the execution path. On the next recomposition, `resolveInAppBackTarget()` runs again with the new state and produces the next priority target.

---

## Design Decision: Explicit Back Priority Model

**Context**: Multiple overlays (backup panel, archived panel, task edit/add panel) can overlap with underlying detail views (quadrant detail). Back gesture must close the topmost visible layer first.

**Options Considered**:
1. Multiple conditional `BackHandler` calls (one per layer)
2. Single `BackHandler` with explicit priority enum

**Decision**: We chose option 2 (explicit priority enum) because:
- Registration order of multiple `BackHandler` calls is composition-dependent
- Priority becomes implicit and fragile
- Hard to test and reason about

**Implementation**:
```kotlin
enum class InAppBackTarget {
    None,
    QuadrantDetail,
    TaskPanel,
    ArchivedPanel,
    BackupPanel  // Highest priority
}

fun resolveInAppBackTarget(...): InAppBackTarget {
    return when {
        backupPanelShown -> InAppBackTarget.BackupPanel
        archivedPanelShown -> InAppBackTarget.ArchivedPanel
        taskPanelState != TaskPanelState.Closed -> InAppBackTarget.TaskPanel
        activePriority != null -> InAppBackTarget.QuadrantDetail
        else -> InAppBackTarget.None
    }
}
```

**Extensibility**: When adding a new back-capable layer:
1. Add enum value in priority position
2. Add condition to resolver
3. Add branch to dispatch when
4. Add test case for the new priority

---

## Common Mistake: Callback State Capture

### Symptom
Back navigation skips layers or closes the wrong panel. For example, closing edit panel also closes the underlying detail view, jumping directly to the main screen.

### Cause
The `BackHandler` callback re-evaluates state resolver functions at execution time. State mutations during dismiss change what the resolver sees mid-execution, causing inconsistent behavior.

### Root Cause
Compose callbacks capture variables at composition time, but calling functions inside the callback executes those functions at callback time. If the function reads mutable state, it sees the current values, not the composition-time values.

### Fix
```kotlin
// BEFORE (wrong)
val backTarget = resolveInAppBackTarget(...)
BackHandler(...) {
    when (resolveInAppBackTarget(...)) { /* re-evaluates */ }
}

// AFTER (correct)
val backTarget = resolveInAppBackTarget(...)
BackHandler(...) {
    when (backTarget) { /* uses snapshot */ }
}
```

### Prevention
1. Always compute derived state outside the callback (during composition)
2. Reference the computed variable inside the callback
3. Never call state-reading functions inside callbacks unless you explicitly want current values
4. Write unit tests for the pure state resolver function
5. Write manual tests that verify layer-by-layer dismissal

---

## Pattern: Single Dispatch Point for Multi-Path Dismissal

**Problem**: An overlay can be dismissed via multiple UI paths:
- App bar back button
- System back gesture
- Predictive back gesture
- Scrim/outside tap
- Save/Cancel button
- Completion/timeout

If each path has its own dismiss logic, they can diverge and cause inconsistent state.

**Solution**: Centralize dismissal in one state mutation, called from all paths.

**Example**:
```kotlin
// Centralized dismiss
fun dismissTaskPanel() {
    taskPanelState = TaskPanelState.Closed
}

// All paths use the same mutation
BackHandler(enabled = ...) { dismissTaskPanel() }
TopAppBar(navigationIcon = { IconButton(onClick = { dismissTaskPanel() }) })
ModalBottomSheet(onDismissRequest = { dismissTaskPanel() })
```

**Why**: State consistency. All dismiss paths mutate the same state variable, so side effects (like navigation, analytics, cleanup) can be centralized in state observers or `LaunchedEffect` blocks watching that state.

---

## Pattern: Accessible Header Actions and Wrap-Safe Summary Rows

**Problem**: Compact UI refreshes often replace `IconButton` with a generic `clickable` container and flatten summary chips into a single `Row`. This preserves the look but regresses accessibility and small-screen resilience.

**Contracts**:
- Header navigation or dismiss actions should use `IconButton` or another control that preserves button semantics.
- Actionable icon targets should be at least `48.dp` even when the glyph itself is smaller.
- Mixed-width summary items must use `FlowRow` or weighted cells when text length or font scale can vary.
- A plain `Row` is only safe when each child has a bounded width strategy.

**Good**:
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
) {
    IconButton(
        onClick = onBack,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
    }
    Text(
        text = title,
        modifier = Modifier.weight(1f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
) {
    InlineStatDot(count = openCount, label = "Open", color = primary)
    InlineStatDot(count = reminderCount, label = "Reminder", color = secondary)
}
```

**Bad**:
```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clickable(onClick = onBack)
    ) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
    }
    Text(text = title)
}

Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    InlineStatDot(count = openCount, label = "Open", color = primary)
    InlineStatDot(count = reminderCount, label = "Reminder", color = secondary)
    InlineStatDot(count = doneCount, label = "Done", color = tertiary)
}
```

**Why**:
- `IconButton` keeps accessibility semantics and minimum touch affordance without reimplementing them manually.
- `weight(1f)` ensures the title absorbs remaining width instead of forcing sibling controls to compete unpredictably.
- `FlowRow` degrades gracefully on narrow devices and with larger system font scales.

**Manual tests**:
- Open the detail header on a narrow device or emulator and verify the stats wrap cleanly instead of clipping.
- Increase system font scale and verify the back action remains easy to tap and the title still ellipsizes predictably.

---

## Pattern: Capture-First Task Entry

**Problem**: Task creation and task editing share the same sheet, but they serve
different workflows. Creation should support rapid consecutive capture. Editing
should finish the current correction and return the user to the previous
context. Treating both paths the same makes quick capture slower or makes edits
feel unfinished.

**Contracts**:
- The floating add action should be available from the home quadrant board and
  from a quadrant detail page, unless a task detail overlay is already open.
- When opened from a quadrant detail page, a new task should default to that
  quadrant.
- Saving a new task should keep the add sheet open, clear title/subtasks/time
  fields, clear calendar/alarm/repeat toggles, and preserve the current
  quadrant selection for the next capture.
- Saving an edited task should close the sheet.
- New-task save should show local confirmation feedback so the user knows the
  capture succeeded without leaving the flow.
- Back handling must still close the task panel before the underlying quadrant
  detail page.

**Good**:
```kotlin
FloatingActionButton(
    onClick = { showAddSheet = true }
) { /* available on board and quadrant detail */ }

AddTaskPanel(
    initialPriority = activePriority ?: TaskPriority.ImportantUrgent,
    onSave = { form ->
        val previousTask = taskForEdit
        scope.launch { saveTask(form, previousTask) }
        if (previousTask != null) {
            dismissTaskPanel()
        }
    }
)
```

**Bad**:
```kotlin
// Hides add while browsing a quadrant detail page.
if (activePriority == null) {
    FloatingActionButton(onClick = { showAddSheet = true }) { ... }
}

// Closes after every save, including fresh capture.
onSave(form)
dismissTaskPanel()
```

**Manual tests**:
- Open a quadrant detail page and verify the add action is visible.
- Create a task from that page and verify the sheet stays open, the saved item
  appears in the quadrant, and the next draft keeps the same quadrant selected.
- Edit an existing task and verify saving closes the sheet.
- Open a quadrant detail page, open add, then press system back; verify the
  sheet closes first and the detail page remains.

---

## Pattern: Quiet Daily Task UI

**Problem**: DianMao needs TickTick-like scanning polish without losing its
four-quadrant model. Overly heavy cards, saturated inactive states, dashboard
metrics, and cramped custom controls make the app feel less native and slower to
use.

**Contracts**:
- Preserve DianMao's warm primary accent for primary actions and current
  selection; do not switch the product identity to a reference app's palette.
- Use balanced compact density: reduce excess padding and border strength, but
  keep primary action targets at about `48.dp`.
- Use tinted neutrals for app background, surfaces, and scrims; avoid
  `Color.White` and `Color.Black` in refreshed UI code.
- Metadata should be compact and semantic. Use quiet chips or inline icon/text
  groups for due time, calendar, alarm, repeat, quadrant, subtasks, and notes.
- Task rows should prioritize completion state and title first; secondary
  classification belongs in lighter metadata.
- Task form text fields should reuse a shared quiet text-field color helper
  for task capture, subtasks, and completion notes instead of scattering local
  `TextFieldDefaults.colors` blocks. Independent tool panels can keep their own
  field styling when they are outside the daily task workflow.
- Letter spacing should stay `0.sp` in refreshed UI. Do not use negative
  tracking to fake density.

**Good**:
```kotlin
private val RowShape = RoundedCornerShape(12.dp)
private val ChipShape = RoundedCornerShape(8.dp)

@Composable
private fun quietBorder(alpha: Float = 0.22f): BorderStroke {
    return BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = alpha))
}

@Composable
private fun quietTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
)

Surface(
    color = MaterialTheme.colorScheme.surface,
    shape = RowShape,
    border = quietBorder(0.18f)
) {
    Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        CompletionMark(...)
        Text(task.title, style = MaterialTheme.typography.titleMedium)
        CompactMeta(icon = Icons.Rounded.Schedule, text = task.dueLabel)
    }
}
```

**Bad**:
```kotlin
Card(
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
    shape = RoundedCornerShape(16.dp)
) {
    Column(Modifier.padding(16.dp)) {
        Text(task.quadrant.title)
        Text(task.title, letterSpacing = (-0.1).sp)
    }
}
```

**Why**: The good pattern keeps rows scannable and native while preserving
touch targets and accessibility. The bad pattern spends too much vertical space
on classification, uses heavy borders, and relies on negative letter spacing for
density.

**Reference implementation**:
- `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
- `DianMaoTheme`, `AppHeader`, `QuadrantBoard`, `TaskRow`, `CompactMeta`,
  `AddTaskPanel`, `TaskDetailScreen`

**Verification**:
- Run `:app:compileDebugKotlin`, `:app:lintDebug`, and `:app:assembleDebug`
  after broad Compose polish.
- Search touched UI files for `letterSpacing = (-`, `Color.White`, and
  `Color.Black`.

---

## Related Specs

- [Overlay Navigation Thinking Guide](../guides/overlay-navigation-thinking-guide.md) - Checklist for back navigation design
- [Error Handling](error-handling.md) - Error boundary patterns in Compose
- [Database Guidelines](database-guidelines.md) - Room schema and migration patterns
