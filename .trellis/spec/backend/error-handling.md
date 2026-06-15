# Error Handling

> How errors are handled in this project.

---

## Overview

DianMao is a local Android app, so most backend-adjacent errors come from
platform integrations rather than network APIs. Calendar Provider,
AlarmManager, and notifications must be treated as best-effort side effects
around the Room source of truth.

---

## Error Types

No custom error hierarchy is currently used. Platform integrations return
nullable ids or booleans for recoverable failures and let the app continue
where the local task state can still be saved.

---

## Error Handling Patterns

## Scenario: Permission-Gated Android Platform Calls

### 1. Scope / Trigger

Trigger this contract when code calls Calendar Provider, notification APIs,
AlarmManager, or another Android service that can fail because of runtime
permission, provider availability, or user-controlled settings.

### 2. Signatures

* Calendar write: `CalendarWriter.saveOrUpdateEvent(context, task): Long?`
* Calendar delete: `CalendarWriter.deleteEvent(context, eventId): Boolean`
* Fallback insert: `CalendarWriter.buildInsertIntent(task): Intent?`
* Notification permission check: `ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)`

### 3. Contracts

* Local Room task saves are the source of truth and should not be blocked by a
  best-effort platform side effect.
* Calendar direct writes return the managed event id on success and `null` on
  failure; callers may open the system insert UI as a fallback.
* Calendar deletes return success/failure but task deletion still proceeds.
* Android 13+ notification posting must perform an inline permission check
  immediately before `NotificationManagerCompat.notify(...)` so Android lint
  can verify the call path.

### 4. Validation & Error Matrix

* Calendar permission missing -> do not call direct Calendar Provider write;
  use insert intent fallback when creating/updating a calendar-enabled task.
* Calendar row missing for stored id -> attempt insert and store the new id.
* Calendar delete fails -> continue local delete/disable flow; the side effect
  was best effort.
* `POST_NOTIFICATIONS` missing on Android 13+ -> skip notification posting but
  continue repeat reminder advancement.

### 5. Good/Base/Bad Cases

* Good: Check calendar permission in the app flow, call `saveOrUpdateEvent`,
  store the returned id only when non-null.
* Base: No due date means calendar/alarm side effects are disabled by repository
  normalization.
* Bad: Hide notification permission checks in a helper that lint cannot trace,
  then call `notify(...)` inside the helper-guarded branch.

### 6. Tests Required

* Run `:app:lintDebug`; Android lint catches missing runtime permission checks.
* Run `:app:assembleDebug`; platform API signatures and Room/KSP projection must
  compile together.
* For future instrumentation tests, assert that missing permissions do not crash
  save/delete/reminder flows.

### 7. Wrong vs Correct

#### Wrong

```kotlin
if (canPostNotifications(context)) {
    NotificationManagerCompat.from(context).notify(id, notification)
}
```

Lint may not infer that `canPostNotifications` protects the permission-gated
call.

#### Correct

```kotlin
if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
) {
    NotificationManagerCompat.from(context).notify(id, notification)
}
```

---

## API Error Responses

Not applicable. This app currently has no HTTP API layer.

---

## Common Mistakes

* Treating Calendar Provider writes as guaranteed after local task save.
* Clearing or deleting local task state only after an external provider call
  succeeds.
* Moving runtime permission checks into helpers that Android lint cannot follow.

## Scenario: AlarmManager Reminder Restoration

### 1. Scope / Trigger

Trigger this contract when restoring, rescheduling, or compensating local
reminders after boot, app launch, or delayed repeat delivery.

### 2. Signatures

* Active reminder query: `TaskDao.findActiveAlarmTasks(): List<TaskEntity>`
* Restoration: `TaskRepository.restoreScheduledReminders(nowMillis: Long = System.currentTimeMillis()): List<TaskUiModel>`
* Repeat catch-up: `RepeatReminderCalculator.nextFutureDueAfter(currentMillis, repeatRule, nowMillis, zoneId)`
* Platform receiver: `ReminderRestoreReceiver : BroadcastReceiver`

### 3. Contracts

* Room is the durable source of truth. AlarmManager is rebuilt from active task
  rows, not queried for state.
* Restore only tasks where `completed = false`, `useAlarm = true`, and
  `dueAtMillis IS NOT NULL`.
* Future one-shot reminders are scheduled as-is.
* Past one-shot reminders are skipped during restoration; do not show stale
  notifications without an explicit product decision.
* Past repeating reminders are advanced until their next due time is greater
  than the restoration time, then persisted and scheduled.
* Restoration may run from both boot receivers and app launch. It must be
  idempotent because pending intents use task ids with `FLAG_UPDATE_CURRENT`.

### 4. Validation & Error Matrix

* Boot broadcast received -> run restoration asynchronously with `goAsync()`.
* Repeating task overdue by multiple intervals -> advance across every missed
  interval before scheduling.
* One-shot task overdue -> return no task to schedule.
* Task completed or alarm disabled -> do not restore.
* Receiver coroutine exits normally or with an exception -> call
  `pendingResult.finish()` in `finally`.

### 5. Good/Base/Bad Cases

* Good: App launch calls repository restoration, then schedules each returned
  `TaskUiModel` with `ReminderScheduler`.
* Base: Future one-shot task survives reboot and is scheduled once.
* Bad: On boot, schedule all alarm tasks directly without compensating repeat
  due times, causing stale repeat notifications or immediate loops.

### 6. Tests Required

* Unit-test repeat catch-up with multiple missed daily/weekly/monthly intervals.
* Run `:app:lintDebug` to validate manifest receiver and permission behavior.
* Run `:app:testDebugUnitTest` and `:app:assembleDebug` before commit.

### 7. Wrong vs Correct

#### Wrong

```kotlin
dao.findActiveAlarmTasks().forEach { task ->
    scheduler.schedule(task.toUiModel())
}
```

This restores stale one-shot alarms and leaves overdue repeats behind.

#### Correct

```kotlin
repository.restoreScheduledReminders().forEach { task ->
    scheduler.schedule(task)
}
```

The repository filters one-shot reminders and persists repeat catch-up before
the platform scheduler receives tasks.
