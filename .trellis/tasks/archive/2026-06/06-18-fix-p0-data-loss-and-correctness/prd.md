# fix P0 data loss and correctness bugs

## Goal

Fix five data-loss or correctness bugs that can cause silent data corruption in normal usage: backup losing fields, concurrent write races, alarm not rescheduled for recurring tasks, notification ID truncation, and completionCount divergence.

## What I already know

* `TaskBackupCodec` serializes 12 fields but `TaskEntity` has 15 columns; `displayOrder`, `completionCount`, `lastCompletedAtMillis` are dropped; `task_completions` table is not backed up at all
* `toggleCompleted` and `saveCompletionNote` use read-modify-write without `@Transaction`/`withTransaction`
* `saveCompletionNote` for cycle-based+alarm tasks advances `dueAtMillis` to next cycle, but the caller in `MainActivity.kt:877` only calls `scheduler.cancel()` and never `scheduler.schedule()` for the new due time
* `ReminderScheduler` and `TaskReminderReceiver` use `taskId.toInt()` for PendingIntent request codes and notification IDs
* `deleteCompletion` removes a `task_completions` row but does not decrement `TaskEntity.completionCount`
* `TaskDatabase` has no `fallbackToDestructiveMigration()`

## Requirements

* Backup codec must serialize ALL `TaskEntity` fields (including `displayOrder`, `completionCount`, `lastCompletedAtMillis`) and the entire `task_completions` table
* Backup restore must correctly reconstruct both tables
* `toggleCompleted` and `saveCompletionNote` must be transactional (use `@Transaction` or `db.withTransaction`)
* After `saveCompletionNote` advances a cycle-based+alarm task's due date, the new alarm must be scheduled
* PendingIntent and notification IDs must not truncate Long task IDs
* Deleting a completion record must decrement `completionCount` on the parent task (or recompute it from the table)
* Add `fallbackToDestructiveMigration()` to Room builder as safety net

## Acceptance Criteria

* [ ] Backup JSON includes `displayOrder`, `completionCount`, `lastCompletedAtMillis` per task
* [ ] Backup JSON includes a `completions` array with all `task_completions` rows
* [ ] Restoring a backup recreates both `tasks` and `task_completions` tables with correct data
* [ ] `toggleCompleted` and `saveCompletionNote` are annotated `@Transaction` or wrapped in `withTransaction`
* [ ] Completing a cycle-based+alarm recurring task through the note dialog re-schedules the alarm for the next cycle
* [ ] No `taskId.toInt()` calls remain in `ReminderScheduler` or `TaskReminderReceiver`; use stable hash or UID-based approach instead
* [ ] `deleteCompletion` keeps `completionCount` consistent (decrement or recompute)
* [ ] `TaskDatabase` builder includes `fallbackToDestructiveMigration()`
* [ ] App builds successfully

## Definition of Done

* All acceptance criteria met
* Build passes
* Manual smoke test: backup, clear data, restore, verify counts and order preserved

## Out of Scope

* Dark theme support (P2-14)
* File splitting / monolith refactor (P2-10)
* `rememberSaveable` fixes (P2-12)
* WebDAV encryption / network security (P1-8, P1-9)
* `SCHEDULE_EXACT_ALARM` permission UX (P1-7)
* Database indices (P3-17)
* Dependency upgrades (P4-23)
* R8/minify (P4-22)

## Technical Notes

### Backup codec fix
* `taskToJson`: add `.put("displayOrder", task.displayOrder)`, `.put("completionCount", task.completionCount)`, `.putNullable("lastCompletedAtMillis", task.lastCompletedAtMillis)`
* `taskFromJson`: read the 3 missing fields, with backward-compatible defaults
* Encoder: add `completions` array at root level
* Decoder: read and insert `task_completions` rows, mapping task IDs
* Version bump: BACKUP_VERSION = 2, with backward-compatible reading of v1

### Transactional writes
* Add `@Transaction` annotation to `toggleCompleted` and `saveCompletionNote`
* Or wrap body in `db.withTransaction { ... }` since they use both `dao` and `completionDao`

### Alarm reschedule after completion note
* `saveCompletionNote` returns the updated `TaskEntity`
* Caller in `CompletionNoteDialog.onSave` must call `scheduler.schedule(updated)` after cancel, when `updated.useAlarm && updated.dueAtMillis != null`
* Check `TaskDetailScreen.onToggleComplete` path too

### Long ID → Int truncation
* Use `taskId.xor(taskId >>> 32).toInt()` for a non-collapsing hash, or use `taskId.hashCode()`
* Better: use a unique request code derived from a small sequential counter + stored mapping. Simplest fix: `(taskId % Int.MAX_VALUE).toInt()` if we accept near-zero collision risk, or `taskId.hashCode()` which is the Kotlin `Long.hashCode()` impl
* Actually safest: use `taskId.xor(taskId.ushr(32)).toInt()` which is exactly `Long.hashCode()`

### completionCount fix
* In `deleteCompletion`: after `completionDao.deleteById(completionId)`, recompute count: `val actual = completionDao.countForTask(taskId)` + `dao.updateCompletionCount(taskId, actual)`
* Add `@Query("SELECT COUNT(*) FROM task_completions WHERE taskId = :taskId")` and `@Query("UPDATE tasks SET completionCount = :count WHERE id = :taskId")` to DAOs
