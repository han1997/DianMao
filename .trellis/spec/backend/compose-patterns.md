# Compose Patterns

> Executable contracts for Jetpack Compose UI implementation

## Scenario: BackHandler State Capture

### 1. Scope / Trigger

**Trigger**: Any `BackHandler` or callback that depends on mutable Compose state to determine behavior.

**Why code-spec depth**: State re-evaluation inside callbacks can see different values than composition time, causing inconsistent navigation or action execution. This crosses the composition/execution boundary.

### 2. Signatures

```kotlin
// State resolver (composition time)
// NOTE: the live signature now includes taskForDetail and returns null
// (not None) when no layer can consume back — see InAppBackTarget enum
// in MainActivity.kt. Update this spec when the signature changes again.
fun resolveInAppBackTarget(
    showBackupPanel: Boolean,
    showArchivedPanel: Boolean,
    isTaskPanelVisible: Boolean,
    hasActivePriority: Boolean,
    taskForDetail: Boolean
): InAppBackTarget?

// BackHandler callback (execution time)
BackHandler(enabled = backTarget != null) {
    // Callback body executes after composition
}
```

### 3. Contracts

**Composition-time state snapshot**:
- Must compute derived state (like back target) during composition
- Store result in a `val` that captures the snapshot
- Type: `InAppBackTarget?` (nullable enum representing priority order; null = fall through to system back)

**Callback-time execution**:
- Must reference the pre-computed snapshot variable
- Must NOT call state resolver functions inside the callback
- Must NOT read mutable state variables directly

**State mutation**:
- Dismiss operations must mutate the original state variables
- Type: `Boolean` flags or sealed class states that trigger recomposition

**Priority order (current, 2026-09)**: BackupPanel > ArchivedPanel > TaskPanel > TaskDetail > QuadrantDetail > null. `TaskDetail` was added when the task detail surface became a back-consuming layer; any new back-capable layer must be inserted into the enum, the resolver `when`, the dispatch `when`, and `InAppBackTargetTest` in the same change.

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
    showBackupPanel, showArchivedPanel,
    isTaskPanelVisible, hasActivePriority, taskForDetail
)
BackHandler(enabled = backTarget != null) {
    when (backTarget) {  // Uses snapshot
        InAppBackTarget.BackupPanel -> { showBackupPanel = false }
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
BackHandler(enabled = backTarget != null) {
    when (resolveInAppBackTarget(...)) {  // WRONG: re-evaluates
        InAppBackTarget.BackupPanel -> { showBackupPanel = false }
        // State seen here may differ from composition time
    }
}
```

### 6. Tests Required

**Unit tests** for state resolver (pure function):
- Assert priority order: backup > archived > task panel > task detail > quadrant
- Assert null when all layers closed (system-back fallback)
- Assert correct target for each combination

Example assertions:
```kotlin
@Test
fun taskDetailTakesPriorityOverQuadrantDetail() {
    val target = resolveInAppBackTarget(
        showBackupPanel = false,
        showArchivedPanel = false,
        isTaskPanelVisible = false,
        hasActivePriority = true,
        taskForDetail = true
    )

    assertEquals(InAppBackTarget.TaskDetail, target)
}
```

**Manual test** for callback execution:
- Open multiple layers
- Trigger back via system gesture and app bar button
- Verify layers close in priority order (topmost first)

### 7. Wrong vs Correct

#### Wrong
```kotlin
// Compute back target
val backTarget = resolveInAppBackTarget(
    showBackupPanel, showArchivedPanel,
    isTaskPanelVisible, hasActivePriority, taskForDetail
)

// Re-compute inside callback
BackHandler(enabled = backTarget != null) {
    when (resolveInAppBackTarget(  // BUG: re-evaluates state
        showBackupPanel, showArchivedPanel,
        isTaskPanelVisible, hasActivePriority, taskForDetail
    )) {
        InAppBackTarget.BackupPanel -> { 
            showBackupPanel = false  // Changes state
        }
        // When dismiss changes state, re-evaluation sees
        // different values than the composition-time snapshot
    }
}
```

**Problem**: After `showBackupPanel = false` executes, the next line re-evaluating `resolveInAppBackTarget()` sees `showBackupPanel=false` and might jump to `TaskPanel` or `QuadrantDetail` instead of just returning. This causes layers to be skipped.

#### Correct
```kotlin
// Compute back target ONCE at composition time
val backTarget = resolveInAppBackTarget(
    showBackupPanel, showArchivedPanel,
    isTaskPanelVisible, hasActivePriority, taskForDetail
)

// Use the snapshot in callback
BackHandler(enabled = backTarget != null) {
    when (backTarget) {  // Uses pre-computed snapshot
        InAppBackTarget.BackupPanel -> { 
            showBackupPanel = false
        }
        InAppBackTarget.ArchivedPanel -> {
            showArchivedPanel = false
        }
        InAppBackTarget.TaskPanel -> {
            // close the add/edit sheet (live code: showAddSheet = false;
            // taskForEdit = null)
            dismissTaskPanel()
        }
        InAppBackTarget.TaskDetail -> {
            taskForDetail = null
        }
        InAppBackTarget.QuadrantDetail -> {
            activePriority = null
        }
        null -> {}
    }
}
```

**Why it works**: The `when` expression reads `backTarget` once. State mutations inside the branches don't affect the execution path. On the next recomposition, `resolveInAppBackTarget()` runs again with the new state and produces the next priority target.

---

## Design Decision: Explicit Back Priority Model

**Context**: Multiple overlays (backup panel, archived panel, task edit/add panel) can overlap with underlying detail views (quadrant detail, task detail). Back gesture must close the topmost visible layer first.

**Options Considered**:
1. Multiple conditional `BackHandler` calls (one per layer)
2. Single `BackHandler` with explicit priority enum

**Decision**: We chose option 2 (explicit priority enum) because:
- Registration order of multiple `BackHandler` calls is composition-dependent
- Priority becomes implicit and fragile
- Hard to test and reason about

**Implementation** (live signature, 2026-09):
```kotlin
enum class InAppBackTarget {
    TaskDetail,
    BackupPanel,
    ArchivedPanel,
    TaskPanel,
    QuadrantDetail
}

fun resolveInAppBackTarget(
    showBackupPanel: Boolean,
    showArchivedPanel: Boolean,
    isTaskPanelVisible: Boolean,
    hasActivePriority: Boolean,
    taskForDetail: Boolean
): InAppBackTarget? {
    return when {
        showBackupPanel -> InAppBackTarget.BackupPanel
        showArchivedPanel -> InAppBackTarget.ArchivedPanel
        isTaskPanelVisible -> InAppBackTarget.TaskPanel
        taskForDetail -> InAppBackTarget.TaskDetail
        hasActivePriority -> InAppBackTarget.QuadrantDetail
        else -> null
    }
}
}
```

**Extensibility**: When adding a new back-capable layer:
1. Add enum value in priority position
2. Add condition to resolver
3. Add branch to dispatch when
4. Add test case for the new priority

(The 2026-09 UI-polish task added `TaskDetail` this way — resolver, enum,
dispatch, and `InAppBackTargetTest` all changed together.)

---

## Pattern: Drag Reorder With Sectioned LazyColumn

**Problem**: When a drag-to-reorder LazyColumn gains non-task items (sticky
header, intro block, section labels, collapsed groups), `onMove` receives
**absolute LazyColumn item indices**, while the reorder state operates on a
**task-only list**. Naive index math corrupts reorder state or lets drags
cross section boundaries.

**Contracts**:
- Compute the absolute item offset: count every non-task item rendered before
  the first task row (header + intro + section label = constant prefix).
- Only enable drag on rows whose absolute index falls inside the active
  (未完成) section's absolute range.
- Guard `onMove` with the open-task count so a drag cannot move an item past
  the boundary into collapsed-group items.
- If a collapsed group renders expanded task rows inside the same LazyColumn,
  do not include them in the reorderable list indices.

**Good**:
```kotlin
val sectionPrefix = 3 // header item + intro item + section label item
val openCount = openDetailTaskCount

dragEnabled = index >= sectionPrefix && index < sectionPrefix + openCount

onMove = { from, to ->
    val fromTask = from - sectionPrefix
    val toTask = to - sectionPrefix
    if (fromTask in 0 until openCount && toTask in 0 until openCount) {
        onReorder(fromTask, toTask)
    }
}
```

**Bad**:
```kotlin
// Passes absolute LazyColumn indices straight into task-list reorder.
onMove = { from, to -> onReorder(from, to) }
```

**Why**: Absolute vs relative index mismatch silently swaps the wrong tasks,
and without the range guard a drag can cross into the completed section whose
items are not part of the reorderable list.

**Reference implementation**:
- `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` — quadrant detail
  LazyColumn assembly (header + `QuadrantDetailIntro` + "未完成" label + open
  tasks + collapsed completed group); drag state helper lives in
  `app/src/main/java/fun/han1997/dianmao/ui/components/TaskRow.kt`
  (`DragDropListState`)

---

## Pattern: Capture Auto-Focus and IME Save in Add Panels

**Problem**: An add-task sheet that opens without focus forces a tap before
typing, and an IME Done that only clears focus wastes the primary capture
gesture. Mainstream capture flows (Todoist/TickTick/Things) focus the title on
open and commit via the keyboard action.

**Contracts**:
- In add mode, request focus on the title field via `FocusRequester` inside
  `LaunchedEffect(visible)` with a short delay (compositional settle), gated to
  `!isEditing`. Edit mode must NOT auto-focus.
- IME `Done` on the title field: in add mode, run the same save path as the
  save button (save-and-continue capture); in edit mode, only clear focus.
- Guard the save action against blank titles — the IME path must behave
  identically to the button path (no double-save, same validation).
- Keep the save-feedback pill behavior unchanged: panel stays open, fields
  cleared, pill auto-dismisses after 2200ms.

**Good**:
```kotlin
val titleFocusRequester = remember { FocusRequester() }

LaunchedEffect(visible, editingTask?.id) {
    if (visible && !isEditing) {
        kotlinx.coroutines.delay(220)
        titleFocusRequester.requestFocus()
    }
}

OutlinedTextField(
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(
        onDone = {
            if (!isEditing) saveTask() else focusManager.clearFocus()
        }
    ),
    modifier = Modifier.focusRequester(titleFocusRequester)
)
```

**Bad**:
```kotlin
// Auto-focuses in edit mode too — surprising and hides existing content intent.
LaunchedEffect(visible) { titleFocusRequester.requestFocus() }

// IME Done silently saves in edit mode, doubling the button's role.
onDone = { saveTask() }
```

**Why**: Capture speed is a product principle ("capture before classification").
The keyboard should be up on the first frame in add mode, and the keyboard's
primary action should commit the capture. Edit mode is a "refine" flow —
focus and Done behave conservatively there.

**Reference implementation**:
- `app/src/main/java/fun/han1997/dianmao/ui/panels/AddTaskPanel.kt` —
  `AddTaskPanel` title field + `LaunchedEffect(visible, editingTask?.id)`
  auto-focus

**Verification**:
- Open add sheet from FAB: keyboard is up, cursor in title field.
- Open edit sheet from a task: no auto-focus, no keyboard.
- Type title, tap IME Done in add mode: task saves, sheet stays open, pill
  shows, next draft keeps the quadrant.
- Edit mode IME Done: keyboard hides, no save.

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
// Centralized dismiss — one mutation covering all task-panel state
fun dismissTaskPanel() {
    showAddSheet = false
    taskForEdit = null
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
- Functional icons should use a consistent low-noise treatment instead of
  scattering default Material glyphs at unrelated sizes. Keep the accessible
  action target at `48.dp` with `IconButton`, and place the visible glyph inside
  a small tonal container such as `IconTile` when it appears in headers,
  metadata chips, or setting rows.

**Good**:
```kotlin
private val RowShape = RoundedCornerShape(12.dp)
private val ChipShape = RoundedCornerShape(8.dp)
private val IconTileShape = RoundedCornerShape(14.dp)

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

@Composable
private fun HeaderActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        IconTile(
            icon = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

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

Icon(
    imageVector = Icons.Rounded.Settings,
    contentDescription = "Settings",
    modifier = Modifier.size(20.dp)
)
```

**Why**: The good pattern keeps rows scannable and native while preserving
touch targets and accessibility. The bad pattern spends too much vertical space
on classification, uses heavy borders, relies on negative letter spacing for
density, and leaves unrelated default icons without a shared visual system.

**Reference implementation** (2026-09 file split):
- `app/src/main/java/fun/han1997/dianmao/ui/theme/DianMaoTheme.kt` —
  `DianMaoTheme`, shapes, `quietBorder`, `quietTextFieldColors`
- `app/src/main/java/fun/han1997/dianmao/ui/components/` — shared components
  (`IconTile`, `HeaderActionButton`, `CompactMeta`, `TaskRow`, ...)
- `app/src/main/java/fun/han1997/dianmao/ui/screens/` — `HomeScreen.kt`
  (`AppHeader`, `QuadrantBoard`), `QuadrantDetailScreen.kt`,
  `TaskDetailScreen.kt`
- `app/src/main/java/fun/han1997/dianmao/ui/panels/` — `AddTaskPanel.kt`,
  `BackupPanel.kt`, `Dialogs.kt`
- `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` — Activity entry +
  `DianMaoApp` wiring only

**Verification**:
- Run `:app:compileDebugKotlin`, `:app:lintDebug`, and `:app:assembleDebug`
  after broad Compose polish.
- Search touched UI files for `letterSpacing = (-`, `Color.White`, and
  `Color.Black`.

---

## Pattern: Home Smart Views

**Problem**: TickTick-style daily browsing needs Today, Upcoming, and Inbox
surfaces, but DianMao's persisted model should remain the four-quadrant task
model unless tags/lists or another durable organization feature is explicitly
scoped.

**Solution**: Build home smart views as a pure projection over active
`TaskUiModel` data, then reuse existing task row/detail workflows for display
and interaction.

**Signatures**:
```kotlin
internal enum class HomeSmartView {
    Quadrants,
    Today,
    Upcoming,
    Inbox
}

internal fun buildHomeSmartTaskItems(
    tasks: List<TaskUiModel>,
    view: HomeSmartView,
    query: String,
    nowMillis: Long,
    zoneId: ZoneId
): List<HomeSmartTaskItem>

internal data class HomeSmartTaskGroup(
    val label: String,
    val items: List<HomeSmartTaskItem>
)

internal fun groupHomeSmartItems(
    items: List<HomeSmartTaskItem>,
    view: HomeSmartView,
    nowMillis: Long,
    zoneId: ZoneId
): List<HomeSmartTaskGroup>
```

**Contracts**:
- Exclude completed and archived tasks from every smart view.
- `Quadrants` returns open tasks in repository order and feeds the existing
  `QuadrantBoard`; it must not replace the four-quadrant model.
- `Today` includes overdue tasks and timed tasks before the next local midnight.
- `Upcoming` includes timed tasks from the next local day through the next 7
  local days, excluding today and excluding the day-7 boundary.
- `Inbox` includes open tasks with no `dueAtMillis`; do not infer inbox state
  from quadrant or title.
- Search matches task title and subtask title with case-insensitive containment.
- Timed views must use `RepeatReminderCalculator.currentCycleDueAt(...)` so
  repeating tasks are classified by the current cycle, not stale stored dates.
- Smart-list rows that display due metadata should use the computed smart-item
  due label, not `task.dueLabel`, so repeating tasks do not show stale labels.
- Grouping is a second pure projection (`groupHomeSmartItems`), never a UI
  concern: Today splits into 过期/今天 groups at local midnight; Upcoming
  groups per local day with relative labels (明天 · 周X); other views render
  one ungrouped section with an empty label. Empty groups are omitted.
- Group keys in the LazyColumn must be namespaced (`group-<label>`) so they
  never collide with task-id row keys.
- Rows inside grouped smart lists use `Modifier.animateItemPlacement()`
  (experimental foundation API) so completion/removal settles with the default
  Material tween — no springs, per the State-Change Only motion rule.

**Good**:
```kotlin
val homeSmartItems = buildHomeSmartTaskItems(
    tasks = activeTasks,
    view = homeSmartView,
    query = homeSearchQuery,
    nowMillis = nowMillis,
    zoneId = zoneId
)

TaskRow(
    task = smartItem.task,
    dueLabelOverride = smartItem.dueLabel
)
```

**Bad**:
```kotlin
// Adds a second durable planner model for what is only a view concern.
data class TaskEntity(
    val smartView: String
)

// Uses stale stored labels after classifying recurring tasks by current cycle.
TaskRow(task = smartItem.task)
```

**Tests Required**:
- Unit tests for `buildHomeSmartTaskItems` must cover Today, Upcoming, Inbox,
  completed/archived exclusion, and search over task plus subtask title.
- Unit tests for `groupHomeSmartItems` must cover the Today overdue/today
  split at the local-midnight boundary, per-day Upcoming grouping with
  relative labels, the ungrouped single section for Inbox/Quadrants, and
  empty-input behavior (`GroupHomeSmartItemsTest`).
- Recurring-date tests must assert smart views classify and label the current
  repeat cycle instead of using stale stored due metadata.
- Compose or build verification must pass after wiring the views into the home
  screen.

---

## Pattern: Home Time Focus Lane

**Problem**: Time-sensitive tasks can be buried inside quadrants, but a full
Today page or dashboard-heavy summary would add navigation weight and weaken the
four-quadrant model.

**Contracts**:
- Build the lane from a pure helper with this shape:
  `buildTimeFocusItems(tasks, nowMillis, zoneId, limit)`.
- Exclude tasks that are completed, archived, or missing `dueAtMillis`.
- For cycle-based recurring tasks, compute an effective due time for the current
  repeat cycle before classification. Do not mutate the persisted task from this
  helper.
- Classify timed tasks as `Overdue` when the effective due time is before
  `nowMillis`, `Today` when due before the next local midnight, and `Upcoming`
  otherwise.
- Sort by focus kind rank (`Overdue`, `Today`, `Upcoming`) and then by due time.
- Render recurring task due metadata from the effective due time so stale stored
  creation dates do not appear in the lane. Non-recurring tasks may keep their
  stored `dueLabel`.
- Limit the lane to a small number of rows; the home default is three.
- Hide the lane entirely when no focus items exist.
- Place the lane between `AppHeader` and `QuadrantBoard`; keep the quadrant
  board visible and primary below it.
- Tapping a focus row should open task detail, not mutate completion, reminders,
  calendar events, repeat state, or persistence.
- Rows should use quiet task UI styling: restrained semantic tint, `FlowRow`
  metadata, minimum `48.dp` touch height, and button semantics for row taps.

**Good**:
```kotlin
TimeFocusLane(
    tasks = activeTasks,
    onTaskSelected = { taskForDetail = it }
)

internal fun buildTimeFocusItems(
    tasks: List<TaskUiModel>,
    nowMillis: Long,
    zoneId: ZoneId,
    limit: Int = 3
): List<TimeFocusItem> {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val tomorrowStartMillis = today.plusDays(1).atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()

    return tasks.mapNotNull { task ->
        val storedDueAt = task.dueAtMillis ?: return@mapNotNull null
        if (task.completed || task.archived) return@mapNotNull null
        val dueAt = RepeatReminderCalculator.currentCycleDueAt(
            currentMillis = storedDueAt,
            repeatRule = task.repeatRule,
            nowMillis = nowMillis,
            zoneId = zoneId
        )
        val kind = when {
            dueAt < nowMillis -> TimeFocusKind.Overdue
            dueAt < tomorrowStartMillis -> TimeFocusKind.Today
            else -> TimeFocusKind.Upcoming
        }
        TimeFocusItem(task = task, kind = kind, dueAtMillis = dueAt)
    }.sortedWith(compareBy({ it.kind.rank }, { it.dueAtMillis }))
        .take(limit)
}
```

**Bad**:
```kotlin
// Turns the home screen into a second planner model and changes task behavior.
TodayDashboard(
    tasks = allTasks,
    onClick = { completeTaskAndRescheduleReminder(it) }
)
```

**Tests Required**:
- Unit tests for the pure helper must cover overdue/today/upcoming ordering,
  completed/archived/unscheduled exclusion, and the row limit after sorting.
- Recurring-date regression tests must cover both later-today and already-past
  today occurrences so stale stored dates cannot reappear.
- Compose or build verification must still prove the home screen and task
  detail click path compile after the lane is inserted.

---

## Pattern: Full-Screen Safe-Area Ownership

**Scope**: Full-screen sibling surfaces in the root `Box` of `MainActivity.kt`,
including `TaskDetailScreen` in `ui/screens/TaskDetailScreen.kt`.

**Contracts**:
- `MainActivity` opts into edge-to-edge rendering and sets Scaffold
  `contentWindowInsets = WindowInsets(0, 0, 0, 0)`. Each full-screen content
  branch owns its safe area; home/quadrant inset modifiers do not protect a
  sibling detail surface.
- Keep the outer background `Surface` full-screen. Apply
  `windowInsetsPadding(WindowInsets.safeDrawing)` once to its content container,
  enclosing the header, scrolling body, and bottom actions.
- Use Compose inset consumption rather than fixed status-bar heights or manual
  repeated padding. Do not change the shared Scaffold insets to fix one screen.

```kotlin
Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Header, weighted scroll content, bottom actions.
    }
}
```

**Verification**: Open details from home and quadrant views. Check that the back,
edit, and title elements clear the status bar/cutout, bottom actions clear both
navigation modes, long content still scrolls, and returning from edit adds no
extra inset. Include a landscape/cutout check for horizontal safe areas. JVM
logic tests cannot verify these Android layout bounds; use device/Compose UI
checks and explicitly record when runtime verification is unavailable.

---

## Pattern: Overlay Panel Visual Consistency

**Problem**: DianMao has three overlay panels (`AddTaskPanel`, `BackupPanel`,
`ArchivedPanel`) that appear as full-height or near-full-height sheets over the
main content. If each panel uses a different shape, elevation, and drag-handle
treatment, the app feels inconsistent and some panels look heavier or lighter
than others for no semantic reason.

**Contracts**:
- All three overlay panels must use `Card` with `SheetShape` (22dp top corners)
  and `cardElevation(6.dp)` — not `Surface` with `PanelShape` or
  `tonalElevation`.
- Bottom-docked panels (`AddTaskPanel`, `BackupPanel`) must include a 38×4dp
  drag handle in `outline` at 0.34 alpha, centered at the top of the card
  content. `ArchivedPanel` is a centered card that cannot be dragged, so it
  omits the handle (updated 2026-09; DESIGN.md matches).
- All three must use `windowInsetsPadding(WindowInsets.safeDrawing)` inside the
  card so content is not clipped by status bar or navigation bar.
- Panel heights may differ by purpose: `AddTaskPanel` 90%, `BackupPanel` 88%,
  `ArchivedPanel` 75% (centered rather than bottom-docked).
- Scrim background must be `Color(0xFF211815).copy(alpha = 0.16f)` — the warm
  scrim color from DESIGN.md, not pure black.
- Enter/exit animation must use the shared spec: `fadeIn(150ms) +
  slideInVertically(220ms, 1/4 travel)` for enter; `fadeOut(120ms) +
  slideOutVertically(180ms, 1/4 travel)` for exit.

**Good**:
```kotlin
Card(
    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.88f),
    shape = SheetShape,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(38.dp)
                .height(4.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.34f), CircleShape)
        )
        // ... panel content
    }
}
```

**Bad**:
```kotlin
// Uses Surface + PanelShape + tonalElevation — inconsistent with other panels
Surface(
    modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.75f),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    shape = PanelShape
) {
    // No drag handle, no safeDrawing padding
    Column(modifier = Modifier.padding(20.dp)) { ... }
}
```

**Why**: Visual consistency across overlay panels. When all three panels share
the same shape, elevation, drag handle, and scrim, the user perceives them as
the same kind of surface — a sheet that slides over the main content — rather
than three unrelated dialogs. The `Card` + `cardElevation` also produces the
correct shadow per DESIGN.md's Sheet (6dp) elevation vocabulary.

**Reference implementation**:
- `app/src/main/java/fun/han1997/dianmao/ui/panels/AddTaskPanel.kt` —
  `AddTaskPanel`
- `app/src/main/java/fun/han1997/dianmao/ui/panels/BackupPanel.kt` —
  `BackupPanel`, `ArchivedPanel`

**Verification**:
- Open each panel and verify they share the same top-corner radius, shadow
  depth, and drag handle.
- Verify scrim color is warm (not pure black) on all three.
- Verify content is not clipped under the status bar or navigation bar.

---

## Pattern: Button-Triggered Floating Search

**Problem**: An always-visible search text field in a compact home screen
consumes significant vertical space (~56dp) even when the user is not
searching. On screens where vertical real estate is critical (e.g., the home
quadrant board), this reduces the primary content area unnecessarily.

**Contracts**:
- The search input should be hidden by default, replaced by a search toggle
  button in the controls header row.
- The toggle button must use `HeaderActionButton` (or equivalent 48dp
  `IconButton` + `IconTile`) to preserve accessibility and touch target size.
- When toggled open, a floating search panel appears using `AnimatedVisibility`
  with the standard sheet motion vocabulary: `fadeIn(150ms) +
  slideInVertically(220ms, 1/4 travel)` for enter; `fadeOut(120ms) +
  slideOutVertically(180ms, 1/4 travel)` for exit.
- The search panel must not occupy fixed space in the column flow when closed —
  `AnimatedVisibility` collapses it entirely.
- When opened, the search text field must auto-focus via `FocusRequester` +
  `LaunchedEffect`.
- When closed, the search query must be cleared so stale filters don't
  persist invisibly.
- The search panel should use `Surface` with `RowShape`, `quietBorder`, and
  `quietTextFieldColors()` to match the quiet daily UI pattern.
- A trailing clear button may appear inside the search field when the query is
  non-empty.

**Good**:
```kotlin
var searchVisible by rememberSaveable { mutableStateOf(false) }
val focusRequester = remember { FocusRequester() }

HeaderActionButton(
    icon = if (searchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
    contentDescription = if (searchVisible) "关闭搜索" else "搜索",
    onClick = {
        searchVisible = !searchVisible
        if (!searchVisible) onQueryChanged("")
    }
)

AnimatedVisibility(
    visible = searchVisible,
    enter = fadeIn(tween(150)) + slideInVertically(tween(220)) { it / 4 },
    exit = fadeOut(tween(120)) + slideOutVertically(tween(180)) { it / 4 }
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RowShape,
        color = MaterialTheme.colorScheme.surface,
        border = quietBorder(0.10f)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            // ...
            colors = quietTextFieldColors(),
            shape = RowShape
        )
    }
}

LaunchedEffect(searchVisible) {
    if (searchVisible) focusRequester.requestFocus()
}
```

**Bad**:
```kotlin
// Always-visible search field wastes ~56dp even when not searching
OutlinedTextField(
    value = query,
    onValueChange = onQueryChanged,
    modifier = Modifier.fillMaxWidth(),
    // ...
)

// Toggle button too small, no IconTile, no accessibility semantics
Box(
    modifier = Modifier.size(36.dp).clickable(onClick = { searchVisible = !searchVisible })
) {
    Icon(Icons.Rounded.Search, contentDescription = "Search")
}
```

**Why**: The floating search pattern frees vertical space for primary content
(the quadrant board) while keeping search accessible. The motion vocabulary
ensures the panel feels like a state change (appearing/disappearing), not
decorative animation. Clearing the query on close prevents invisible stale
filters.

**Reference implementation**:
- `app/src/main/java/fun/han1997/dianmao/ui/screens/HomeScreen.kt` —
  `AppHeader` search toggle (the standalone `HomeSmartControls` variant was
  removed as dead code in the 2026-09 split)
- `app/src/main/java/fun/han1997/dianmao/MainActivity.kt` — `DianMaoApp`

**Verification**:
- Verify the quadrant board has more vertical space than before.
- Verify search toggle opens a focused text field, typing filters tasks.
- Verify closing search clears the query and collapses the panel.
- Verify the toggle button is 48dp with an `IconTile` container.

---

## Related Specs

- [Overlay Navigation Thinking Guide](../guides/overlay-navigation-thinking-guide.md) - Checklist for back navigation design
- [Error Handling](error-handling.md) - Error boundary patterns in Compose
- [Database Guidelines](database-guidelines.md) - Room schema and migration patterns
