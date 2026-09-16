# Quality Guidelines

Quality checks for DianMao must protect local user data, Android platform
integration behavior, and compact Compose workflows.

## Required Verification

Run the narrowest command that proves the touched area, and run the broader
commands before finishing a multi-file feature.

| Change area | Required checks |
|---|---|
| Pure Kotlin helper or resolver | `.\gradlew.bat :app:testDebugUnitTest --no-daemon` |
| Compose or Android platform call path | `.\gradlew.bat :app:lintDebug --no-daemon` |
| Room schema, KSP, manifest, resources, or broad feature work | `.\gradlew.bat :app:assembleDebug --no-daemon` |
| Any committed diff | `git diff --check` |

Run Gradle tasks sequentially when they touch KSP output. Parallel
`compileDebugKotlin` and test runs have previously contended on generated
files.

## Testing Pattern

Prefer pure helper tests for deterministic behavior:

- `RepeatReminderCalculatorTest.kt` verifies missed repeat interval catch-up.
- `InAppBackTargetTest.kt` verifies explicit in-app back priority.
- `CalendarWriterSelectionTest.kt` verifies calendar candidate ranking without
  constructing framework intents.

When changing date/cycle logic, add tests around boundary behavior such as day,
week, month, and year transitions. When changing UI navigation, extract or
reuse pure resolver functions so the priority rules can be unit-tested.

Do not add local JVM tests that rely on Android SDK stubs such as `org.json` or
framework `Intent` behavior. Use compile/build verification, Robolectric, or
instrumentation coverage for framework-dependent behavior.

## Data Safety

Room data is user-owned. Schema and backup changes must follow the database
guidelines:

- Add an adjacent migration for every Room version bump.
- Keep `exportSchema = true` and commit the generated schema JSON.
- Never use `fallbackToDestructiveMigration()` in the production database
  builder.
- Update `TaskBackupCodec` for every persisted `TaskEntity` field.
- Wrap multi-row or multi-table mutations in `db.withTransaction`.

Reference files:

- `app/src/main/java/fun/han1997/dianmao/data/TaskDatabase.kt`
- `app/src/main/java/fun/han1997/dianmao/data/TaskBackupCodec.kt`
- `app/src/main/java/fun/han1997/dianmao/data/TaskRepository.kt`

## Platform Safety

Platform integrations are best-effort side effects around Room state:

- Calendar writes return nullable event ids and must not block local task
  saves.
- Calendar deletes may fail; local task deletion or sync disabling still
  proceeds.
- Android 13+ notification posting needs an inline
  `POST_NOTIFICATIONS` permission guard close to `notify(...)` so lint can see
  it.
- Boot restoration receivers must call `goAsync()` and finish the pending
  result in `finally`.
- WebDAV network calls run on `Dispatchers.IO` and validate downloaded JSON
  before replacing Room data.

## Scenario: Idempotent Silent Task Completion

### 1. Scope / Trigger

Use this contract whenever a task can be completed from more than one entry
point, including the home swipe, a notification action, a Glance callback, or
batch operations.

### 2. Signatures

* `TaskRepository.completeSilently(task: TaskUiModel): Boolean`
* `canApplySilentCompletion(expected: TaskUiModel, current: TaskUiModel): Boolean`
* `completeTaskSilently(context: Context, task: TaskUiModel): Boolean`

### 3. Contracts

* The repository must read the latest `TaskEntity` inside the same Room
  transaction that applies completion.
* Completion is allowed only when the latest task is open, has the same
  `dueAtMillis`, `completionCount`, reminder settings, and repeat settings as
  the caller snapshot.
* A stale, completed, archived, or missing task returns `false` and must not
  change task data or schedule a new reminder.
* Alarm cancellation and recurring-cycle rescheduling happen only after the
  repository returns `true`; widget refresh is also conditional on `true`.

### 4. Validation & Error Matrix

| Condition | Correct behavior |
|---|---|
| Another entry point completed the task first | Return `false`; do not advance or reschedule again |
| Task was deleted before the action ran | Return `false`; do not cancel another task's calendar/reminder state |
| Open task snapshot still matches current entity | Apply one completion and return `true` |
| Platform refresh fails after local completion | Keep the Room result; platform refresh remains best effort |

### 5. Good/Base/Bad Cases

* Good: Read `dao.findById(task.id)` inside `db.withTransaction`, validate the
  snapshot, then update that entity.
* Base: A caller may ignore a `false` result when no user-facing message is
  needed, but must not show a success message or refresh as if it completed.
* Bad: Call `toggleCompleted(task, true)` using a stale `TaskUiModel`, then
  unconditionally cancel alarms and schedule the next recurring cycle.

### 6. Tests Required

* Assert a changed `completionCount` rejects a stale completion snapshot.
* Assert completed and archived current entities are rejected.
* Assert an unchanged open snapshot is accepted.
* For each platform entry point, verify the success side effects are gated by
  the Boolean completion result when runtime tests are available.

### 7. Wrong vs Correct

#### Wrong

```kotlin
repository.completeSilently(task)
scheduler.schedule(repository.findTaskById(task.id)!!)
```

The action can schedule a second cycle after another entry point already
advanced the same recurring task.

#### Correct

```kotlin
if (repository.completeSilently(task)) {
    scheduler.cancel(task.id, task.reminderLeadMinutes)
    repository.findTaskById(task.id)?.let(scheduler::schedule)
}
```

The persisted transaction decides whether the caller still owns the current
completion, and all dependent side effects are conditional on that result.

Reference files:

- `app/src/main/java/fun/han1997/dianmao/calendar/CalendarWriter.kt`
- `app/src/main/java/fun/han1997/dianmao/alerts/TaskReminderReceiver.kt`
- `app/src/main/java/fun/han1997/dianmao/alerts/ReminderRestoreReceiver.kt`
- `app/src/main/java/fun/han1997/dianmao/backup/WebDavBackupClient.kt`

## Compose Quality

Keep workflow state explicit and testable:

- Use a precomputed back target for BackHandler dispatch.
- Use stable IDs for task/subtask state instead of deriving identity from list
  position.
- Keep repeated visual rules in small composables when they appear in multiple
  surfaces.
- Preserve accessibility semantics for actions with `IconButton`,
  `FilledTonalButton`, or another proper button component.
- Use `FlowRow`, `weight`, `maxLines`, and ellipsis for compact text that may
  wrap on narrow devices.

Reference files:

- `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
- `app/src/test/java/fun/han1997/dianmao/InAppBackTargetTest.kt`

## Forbidden Patterns

- Destructive Room migration fallback in production.
- Direct platform side effects inside DAO methods.
- Calendar event update/delete by title or time matching instead of stored
  `calendarEventId`.
- Scheduling stale one-shot reminders during restore.
- Recomputing state-reading back targets inside BackHandler callbacks.
- Swallowing WebDAV or backup import failures after deleting local data.
- Logging task titles, notes, WebDAV credentials, or backup payloads.

## Gotcha: PowerShell Text Cmdlets Corrupt UTF-8 Source

> **Warning**: Never edit Kotlin/Markdown files containing Chinese text via
> PowerShell `Get-Content` + `Set-Content` on this machine. The console decodes
> UTF-8 as GBK, so a read-modify-write round trip silently mojibakes every
> multi-byte character (source became `梳理…` → `姊崇悕...`-style corruption,
> plus a BOM was prepended). Verified 2026-09 during the UI-comfort task.

**Why it happens**: Windows PowerShell 5.1 uses the system ANSI code page
(GBK on zh-CN) for `Get-Content` without `-Encoding`, and writes BOM +
re-encoded bytes on `Set-Content -Encoding UTF8`.

**Correct approach**:
- Use the editor tools (Read/Edit) for small edits — they are UTF-8 safe.
- For bulk/mechanical edits, use Python with explicit encoding:

```python
import io
with io.open(path, 'r', encoding='utf-8', newline='') as f:
    src = f.read()
# ... string replaces ...
with io.open(path, 'w', encoding='utf-8', newline='') as f:
    f.write(src)
```

**Prevention**: after any scripted bulk edit, verify a known Chinese string
still exists (`grep "梳理本周"`) and `git diff` shows only the intended lines.

## Review Checklist

Before finishing:

- The change has a source-backed reason and fits an existing package owner.
- The persisted model, UI model, backup codec, migrations, and schema exports
  are consistent when data fields change.
- Permission-gated Android calls are lint-clean.
- Relevant tests or build commands have passed.
- `CHANGELOG.md` is updated for product-visible code changes, per README.
- README is updated when the current status or unfinished work list changes.
