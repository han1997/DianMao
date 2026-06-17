# Database Guidelines

> Database patterns and conventions for this project.

---

## Overview

DianMao stores task data with Room. `TaskDatabase` owns schema versioning,
`TaskEntity` is the persisted contract, and `TaskUiModel` is the UI-facing
projection. Any persisted field added to `TaskEntity` must be carried through
the Room migration chain, `toUiModel()`, the backup codec, and the exported
Room schema.

---

## Query Patterns

Use DAO methods for database reads/writes. Keep Android platform side effects
outside DAO methods; repositories may coordinate local entity updates, but
Calendar Provider, AlarmManager, and notification calls stay in their platform
modules or app flow.

---

## Migrations

## Scenario: Room Task Schema Field Addition

### 1. Scope / Trigger

Trigger this contract whenever a field is added, removed, renamed, or has its
nullability/default changed in `TaskEntity`.

### 2. Signatures

* Database annotation: `@Database(entities = [TaskEntity::class], version = <next>, exportSchema = true)`
* Migration object: `Migration(<from>, <to>) { override fun migrate(db: SupportSQLiteDatabase) }`
* Registration: `.addMigrations(MIGRATION_1_2, MIGRATION_2_3, ...)`
* Entity projection: `fun TaskEntity.toUiModel(): TaskUiModel`
* Schema output: `app/schemas/fun.han1997.dianmao.data.TaskDatabase/<version>.json`

### 3. Contracts

* New nullable fields use nullable Kotlin types and nullable SQLite columns.
* New non-null fields must define a SQLite `DEFAULT` in the migration.
* The migration chain must support every adjacent version from the previous
  release to the new release.
* The UI model must include persisted fields that later app flows need to
  update external systems.
* Room schema export stays enabled. Commit the generated schema JSON whenever
  the database schema changes.
* Backup import/export must be updated for every persisted task field.

### 4. Validation & Error Matrix

* Missing adjacent migration -> Room opens fail at runtime.
* Entity field missing from `toUiModel()` -> UI/app flow silently loses stored data.
* Non-null column without default -> migration fails on existing rows.
* Schema JSON not committed -> future migration diffs lose the baseline.
* Backup codec missing a new field -> backup/restore silently drops user data.
* Platform side effect stored only in memory -> later edits cannot target the
  same external record.

### 5. Good/Base/Bad Cases

* Good: Add `calendarEventId: Long?`, migrate with `ALTER TABLE tasks ADD COLUMN calendarEventId INTEGER`, and project it into `TaskUiModel`.
* Base: Add a UI-only field to `TaskFormState`; no Room migration is needed.
* Bad: Insert Calendar Provider events without storing the returned event id,
  then attempt future updates by matching title/time.

### 6. Tests Required

* Run `:app:compileDebugKotlin` or `:app:assembleDebug` so Room/KSP validates
  the entity shape and updates exported schema JSON.
* Run `:app:lintDebug` when migrations touch platform side effects or
  permission-gated APIs.
* If Room migration tests are added later, assert old rows survive and new
  columns contain the intended default/null value.

### 7. Wrong vs Correct

#### Wrong

```kotlin
@Database(entities = [TaskEntity::class], version = 3)
abstract class TaskDatabase : RoomDatabase()
```

This bumps the schema version but does not register a `Migration(2, 3)`.

#### Correct

```kotlin
@Database(entities = [TaskEntity::class], version = 3, exportSchema = true)
abstract class TaskDatabase : RoomDatabase() {
    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN calendarEventId INTEGER")
            }
        }
    }
}
```

---

## Naming Conventions

Use Kotlin property names as Room column names unless there is a strong reason
to annotate a column explicitly. Keep migration SQL aligned with the property
name, for example `calendarEventId`.

---

## Common Mistakes

* Updating `TaskEntity` without updating `TaskUiModel` and `toUiModel()`.
* Updating `TaskEntity` without updating `TaskBackupCodec`.
* Bumping the Room database version without adding the adjacent migration.
* Forgetting to commit `app/schemas/**/<version>.json` after schema changes.
* Managing external records without persisting their stable external id.
* Writing local JVM unit tests that use `org.json` — Android's `org.json` is a
  SDK stub that throws `RuntimeException("Stub!")` in local (JVM) tests. Either
  use Robolectric, instrumentation tests, or compile-check via
  `:app:compileDebugKotlin` / `:app:assembleDebug` instead.

## Scenario: Task Backup JSON

### 1. Scope / Trigger

Trigger this contract when adding backup/export/import behavior or changing any
field in `TaskEntity`.

### 2. Signatures

* Export: `TaskRepository.exportBackupJson(): String`
* Import: `TaskRepository.importBackupJson(json: String): Int`
* Codec: `TaskBackupCodec.encode(tasks): String`
* Codec: `TaskBackupCodec.decodeForImport(json): List<TaskEntity>`

### 3. Contracts

* Backup payloads are UTF-8 JSON with `format`, `backupVersion`,
  `databaseVersion`, `createdAtMillis`, and `tasks`.
* Import replaces all local task rows inside a Room transaction.
* Import clears `calendarEventId`; Calendar Provider ids are device-local and
  must not be restored across devices.
* Import preserves task ids so app-owned alarms can be rescheduled consistently
  after restore.

### 4. Validation & Error Matrix

* Unsupported `format` -> reject import.
* Future `backupVersion` -> reject import.
* Invalid enum value -> reject import.
* Local file/WebDAV read failure -> surface an error, keep existing Room data.

### 5. Good/Base/Bad Cases

* Good: Decode the whole payload first, then replace rows in a transaction.
* Base: Empty backup imports as an empty task list.
* Bad: Delete local rows before validating the backup JSON.

### 6. Tests Required

* Run `:app:compileDebugKotlin` because the codec uses Android JSON APIs.
* Run `:app:assembleDebug` to validate the backup UI, repository, and schema
  generation together.

### 7. Wrong vs Correct

#### Wrong

```kotlin
dao.deleteAll()
val tasks = TaskBackupCodec.decodeForImport(json)
dao.insertAll(tasks)
```

Invalid JSON would erase local data.

#### Correct

```kotlin
val tasks = TaskBackupCodec.decodeForImport(json)
db.withTransaction {
    dao.deleteAll()
    dao.insertAll(tasks)
}
```

---

## Scenario: Recurring Task Completion with History Tracking

### 1. Scope / Trigger

Trigger this contract when implementing recurring task completion behavior with cycle boundaries and historical completion records.

### 2. Signatures

**Database Schema:**
```kotlin
@Entity(
    tableName = "task_completions",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["taskId"])]
)
data class TaskCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val taskId: Long,
    val completedAtMillis: Long,
    val note: String?
)
```

**DAO Methods:**
```kotlin
@Dao
interface TaskCompletionDao {
    fun observeCompletions(taskId: Long): Flow<List<TaskCompletionEntity>>
    suspend fun insert(completion: TaskCompletionEntity): Long
    suspend fun deleteById(completionId: Long)
}
```

**Repository Methods:**
```kotlin
suspend fun canCompleteInCurrentCycle(task: TaskUiModel): Boolean
suspend fun saveCompletionNote(taskId: Long, note: String)
suspend fun deleteCompletion(completionId: Long)
fun observeCompletions(taskId: Long): Flow<List<TaskCompletionEntity>>
```

### 3. Contracts

**Cycle Boundaries:**
- Daily: Same calendar date (LocalDate comparison)
- Weekly: Same ISO week (Monday-Sunday, uses ISO week-of-year)
- Monthly: Same year and month
- Count-based: No cycle restriction (unlimited completions)

**Completion Recording:**
- Create `TaskCompletionEntity` for every recurring task completion
- Store `completedAtMillis` and optional `note`
- Increment `completionCount` on parent task
- Update `lastCompletedAtMillis` on parent task

**Cycle Enforcement:**
- Check `canCompleteInCurrentCycle()` before allowing completion
- Compare `lastCompletedAtMillis` against current time using cycle rules
- Return `false` if already completed in current cycle

**Foreign Key Behavior:**
- `ON DELETE CASCADE`: Deleting a task automatically deletes all its completion records

### 4. Validation & Error Matrix

**Cycle Calculation Bugs:**
- Using `with(DayOfWeek.MONDAY)` for weekly cycle → incorrect week boundaries (Sunday/Monday mismatch)
- Comparing `Instant` instead of `LocalDate` for daily → timezone issues
- Not using ISO week numbers → incorrect cross-year week comparison

**State Inconsistencies:**
- Not incrementing `completionCount` → stats drift from actual history
- Not updating `lastCompletedAtMillis` → cycle check always passes
- Creating completion record without updating parent task → orphaned data

**UI Sync Issues:**
- Not checking `canCompleteInCurrentCycle()` in UI → duplicate completions possible
- Not observing `completions` Flow → history list doesn't update
- Not using `LaunchedEffect` on task state → stale cycle check

### 5. Good/Base/Bad Cases

**Good:**
```kotlin
// Repository: Create completion record + update parent atomically
suspend fun saveCompletionNote(taskId: Long, note: String) {
    val existing = dao.findById(taskId) ?: return
    val now = System.currentTimeMillis()
    
    if (existing.repeatRule != RepeatRule.None) {
        completionDao.insert(
            TaskCompletionEntity(taskId, now, note)
        )
    }
    
    val updated = existing.copy(
        completed = true,
        completionCount = existing.completionCount + 1,
        lastCompletedAtMillis = now,
        completionNote = note
    )
    dao.update(updated)
}

// UI: Check cycle before enabling button
LaunchedEffect(task.id, task.lastCompletedAtMillis, task.completed) {
    canComplete = repository.canCompleteInCurrentCycle(task)
}
```

**Base:**
```kotlin
// Non-recurring task: just mark completed, no history
existing.copy(completed = true, completionNote = note)
```

**Bad:**
```kotlin
// Wrong: Uses temporal adjustment for weekly cycle
val week1 = time1.atStartOfDay().with(DayOfWeek.MONDAY)
val week2 = time2.atStartOfDay().with(DayOfWeek.MONDAY)
week1.isEqual(week2)  // ❌ Sunday and Monday can incorrectly match

// Wrong: No cycle check in UI
IconButton(
    onClick = { onComplete(task) },
    enabled = !task.completed  // ❌ Allows duplicate completions
)
```

### 6. Tests Required

**Unit Tests (if added):**
- `isInSameCycle()` with dates at cycle boundaries (23:59 → 00:01)
- `isInSameCycle()` across year boundaries (Dec 31 → Jan 1)
- `isInSameCycle()` for ISO week calculation (Sunday → Monday)
- `canCompleteInCurrentCycle()` for all RepeatRule types

**Integration Tests:**
- Complete daily task → button disabled until midnight
- Complete weekly task → button disabled until next Monday
- Complete monthly task → button disabled until 1st of next month
- Count-based task → button always enabled

**Assertions:**
- `completionCount` increments on each completion
- `TaskCompletionEntity` created with correct timestamp
- `lastCompletedAtMillis` updated to completion time
- Foreign key cascade deletes completion records when task deleted

### 7. Wrong vs Correct

#### Wrong: Buggy Weekly Cycle Calculation

```kotlin
private fun isInSameCycle(millis1: Long, millis2: Long, repeatRule: RepeatRule): Boolean {
    val time1 = Instant.ofEpochMilli(millis1).atZone(ZoneId.systemDefault()).toLocalDate()
    val time2 = Instant.ofEpochMilli(millis2).atZone(ZoneId.systemDefault()).toLocalDate()
    
    return when (repeatRule) {
        RepeatRule.Weekly -> {
            val week1 = time1.atStartOfDay().with(DayOfWeek.MONDAY)
            val week2 = time2.atStartOfDay().with(DayOfWeek.MONDAY)
            week1.isEqual(week2)  // ❌ Incorrect for cross-week dates
        }
        // ...
    }
}
```

**Problem:** `with(DayOfWeek.MONDAY)` adjusts to the nearest Monday, which can be in the previous or next week. A Sunday completion and Monday completion can incorrectly be treated as the same week.

#### Correct: ISO Week Number Calculation

```kotlin
private fun isInSameCycle(millis1: Long, millis2: Long, repeatRule: RepeatRule): Boolean {
    val zone = ZoneId.systemDefault()
    val time1 = Instant.ofEpochMilli(millis1).atZone(zone).toLocalDate()
    val time2 = Instant.ofEpochMilli(millis2).atZone(zone).toLocalDate()
    
    return when (repeatRule) {
        RepeatRule.Daily -> time1.isEqual(time2)
        RepeatRule.Weekly -> {
            val week1 = time1.year * 53 + time1.get(WeekFields.ISO.weekOfWeekBasedYear())
            val week2 = time2.year * 53 + time2.get(WeekFields.ISO.weekOfWeekBasedYear())
            week1 == week2  // ✅ Accurate across year boundaries
        }
        RepeatRule.Monthly -> {
            time1.year == time2.year && time1.monthValue == time2.monthValue
        }
        else -> false
    }
}
```

**Why:** ISO week numbers (Monday-Sunday) are stable across year boundaries. Multiplying by 53 (max weeks per year) ensures year rollover doesn't cause false positives.

#### Wrong: No Cycle Check in UI

```kotlin
@Composable
private fun TaskRow(task: TaskUiModel, onComplete: (TaskUiModel) -> Unit) {
    IconButton(
        onClick = { onComplete(task) },
        enabled = !task.completed  // ❌ No cycle boundary check
    )
}
```

**Problem:** A daily task completed at 11pm can be completed again at 11:01pm, violating the "once per day" requirement.

#### Correct: Reactive Cycle Check

```kotlin
@Composable
private fun TaskRow(
    task: TaskUiModel,
    repository: TaskRepository,
    onComplete: (TaskUiModel) -> Unit
) {
    var canComplete by remember { mutableStateOf(true) }
    
    LaunchedEffect(task.id, task.lastCompletedAtMillis, task.completed) {
        canComplete = repository.canCompleteInCurrentCycle(task)
    }
    
    IconButton(
        onClick = { onComplete(task) },
        enabled = canComplete || task.completed  // ✅ Respects cycle boundaries
    )
}
```

**Why:** `LaunchedEffect` recomputes `canComplete` whenever the task's completion state changes, ensuring the button accurately reflects whether the current cycle allows completion.

---

## Common Mistakes (Recurring Tasks)

* Using `with(DayOfWeek.MONDAY)` for weekly cycle calculation instead of ISO week numbers
* Not passing `repository` parameter to UI components that need cycle checks
* Checking `!task.completed` instead of `canCompleteInCurrentCycle()` in UI
* Creating completion records without incrementing parent task's `completionCount`
* Not updating `lastCompletedAtMillis` when recording completion
* Comparing `Instant` directly instead of converting to `LocalDate` for daily cycles
* Missing `LaunchedEffect` dependencies: UI shows stale cycle state after completion
