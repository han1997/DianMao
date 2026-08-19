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
* Manual reminder open: `CalendarWriter.buildOpenReminderIntent(task): Intent?`
* Fallback reminder extras: `CalendarWriter.reminderIntentExtras(): Map<String, Int>`
* Notification permission check: `ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)`

### 3. Contracts

* Local Room task saves are the source of truth and should not be blocked by a
  best-effort platform side effect.
* Calendar direct writes return the managed event id on success and `null` on
  failure; callers may open the system insert UI as a fallback.
* Calendar direct writes should prefer calendars that appear writable and whose
  `ALLOWED_REMINDERS` includes `METHOD_ALERT`, but provider metadata is only a
  hint. Candidate ranking must not block event creation when those fields are
  missing or inaccurate.
* Calendar insert-intent fallback must request calendar-app reminder support
  with `Events.HAS_ALARM = 1`, `Reminders.MINUTES = 0`, and
  `Reminders.METHOD = METHOD_ALERT`; this path is best effort because the
  receiving calendar app controls whether extras are honored.
* Manual "open system calendar reminder" actions should open the existing
  calendar event editor when `calendarEventId` is present, otherwise fall back
  to the insert intent with the same reminder extras.
* Calendar deletes return success/failure but task deletion still proceeds.
* Android 13+ notification posting must perform an inline permission check
  immediately before `NotificationManagerCompat.notify(...)` so Android lint
  can verify the call path.

### 4. Validation & Error Matrix

* Calendar permission missing -> do not call direct Calendar Provider write;
  use insert intent fallback when creating/updating a calendar-enabled task.
* Calendar permission missing + due date present -> fallback insert intent
  includes immediate alert reminder extras, not just event title/time fields.
* User taps manual calendar reminder button -> start the built intent
  best-effort; failure to resolve a calendar app must not affect the local task.
* Visible syncing calendar reports stronger write access -> prefer it over
  weaker candidates, but do not reject all candidates solely because access
  metadata is low or missing.
* Multiple calendar candidates exist -> prefer one that supports
  `METHOD_ALERT`; otherwise fall back to the primary/strongest candidate.
* Calendar row missing for stored id -> attempt insert and store the new id.
* Calendar delete fails -> continue local delete/disable flow; the side effect
  was best effort.
* `POST_NOTIFICATIONS` missing on Android 13+ -> skip notification posting but
  continue repeat reminder advancement.

### 5. Good/Base/Bad Cases

* Good: Check calendar permission in the app flow, call `saveOrUpdateEvent`,
  store the returned id only when non-null.
* Good: Rank calendar candidates so reminder-capable likely-writable calendars
  win when metadata is available, while still returning a fallback calendar if
  all metadata looks incomplete.
* Good: Share fallback reminder extras through a pure helper so JVM unit tests
  can assert the contract without constructing Android framework `Intent`
  objects.
* Good: Keep the manual open button on the task detail surface and only show it
  for calendar-enabled tasks with a due time, so the list view stays focused.
* Base: No due date means calendar/alarm side effects are disabled by repository
  normalization.
* Bad: Treat access-level metadata as a hard gate, return `null`, and block all
  calendar writes on devices whose provider reports incomplete calendar
  metadata.
* Bad: Hide notification permission checks in a helper that lint cannot trace,
  then call `notify(...)` inside the helper-guarded branch.

### 6. Tests Required

* Run `:app:lintDebug`; Android lint catches missing runtime permission checks.
* Add a regression test for calendar candidate ranking when reminder-capable
  and non-capable calendars coexist, plus a fallback case where access metadata
  is incomplete but a calendar id must still be returned.
* Assert fallback insert reminder extras through `reminderIntentExtras()`:
  `HAS_ALARM = 1`, `MINUTES = 0`, and `METHOD = METHOD_ALERT`.
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

#### Wrong

```kotlin
val projection = arrayOf(CalendarContract.Calendars._ID)
val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1"

resolver.query(..., projection, selection, null, null)?.use { cursor ->
    if (cursor.moveToFirst()) return cursor.getLong(0)
}
```

This can target a calendar whose reminders are unusable, because it ignores the
provider metadata entirely.

#### Correct

```kotlin
val candidates = readCalendarCandidates(cursor)
return chooseBestCalendarId(candidates)
```

Candidate selection uses provider metadata as ranking hints. It prefers
`METHOD_ALERT` support and stronger write access when present, but still falls
back to another visible syncing calendar instead of blocking event creation.

#### Wrong

```kotlin
Intent(Intent.ACTION_INSERT).apply {
    data = CalendarContract.Events.CONTENT_URI
    putExtra(CalendarContract.Events.TITLE, task.title)
    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dueAt)
    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dueAt + duration)
}
```

This creates a calendar event but leaves the calendar app no explicit reminder
request on the fallback path.

#### Correct

```kotlin
Intent(Intent.ACTION_INSERT).apply {
    data = CalendarContract.Events.CONTENT_URI
    putExtra(CalendarContract.Events.TITLE, task.title)
    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dueAt)
    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dueAt + duration)
    reminderIntentExtras().forEach { (key, value) -> putExtra(key, value) }
}
```

Plain JVM tests should assert `reminderIntentExtras()` instead of constructing
the full Android `Intent`, because framework `Intent.setData` is not mocked in
local unit tests.

#### Wrong

```kotlin
IconButton(onClick = { context.startActivity(CalendarWriter.buildInsertIntent(task)) }) {
    Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
}
```

This can create duplicate calendar events even when the app already stored a
managed event id.

#### Correct

```kotlin
FilledTonalButton(onClick = { openCalendarReminder(context, task) }) {
    Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
    Text("Open system calendar reminder")
}
```

`openCalendarReminder` uses `buildOpenReminderIntent`: existing managed events
open through `ACTION_EDIT`, while tasks without an event id use the same insert
fallback as the permission-missing save flow.

---

## API Error Responses

Not applicable. This app currently has no HTTP API layer.

---

## Common Mistakes

* Treating Calendar Provider writes as guaranteed after local task save.
* Clearing or deleting local task state only after an external provider call
  succeeds.
* Updating the direct Calendar Provider reminder path while forgetting the
  permission-missing fallback insert intent path.
* Opening a new calendar insert intent from detail when `calendarEventId` exists,
  creating a duplicate instead of opening the managed event.
* Moving runtime permission checks into helpers that Android lint cannot follow.

## Scenario: Coroutine Cancellation in UI Save Flows

### 1. Scope / Trigger

Apply this contract when a Compose-launched save or platform-side-effect flow
catches `Exception` to convert failures into visible UI status.

### 2. Signatures

* Save callback: `suspend (TaskFormState) -> TaskSaveOutcome`
* Cancellation type: `kotlinx.coroutines.CancellationException`

### 3. Contracts

* Ordinary persistence and platform-side-effect exceptions may be converted to
  retryable form feedback or snackbar status.
* `CancellationException` must be rethrown immediately and must not be converted
  into a save failure, snackbar, or other business result.
* A successful Room save remains successful when a later reminder or calendar
  side effect fails; report the side-effect failure separately.

### 4. Validation & Error Matrix

* Room save throws ordinary exception -> preserve draft and show retryable error.
* Reminder/calendar side effect throws ordinary exception after Room save ->
  report partial synchronization failure without claiming local data was lost.
* Coroutine is cancelled -> rethrow cancellation and do not update UI status.

### 5. Good/Base/Bad Cases

* Good: Catch `Exception`, rethrow cancellation, then map remaining failures to
  a user-visible outcome.
* Base: Let the exception propagate when no local recovery or status is needed.
* Bad: Use a broad `runCatching` or `catch (Exception)` that converts coroutine
  cancellation into "save failed".

### 6. Tests Required

* Run `:app:testDebugUnitTest`, `:app:lintDebug`, and `:app:assembleDebug` after
  changing the Compose save flow.
* When extracting the save coordinator into a pure/testable unit, assert that
  ordinary failures produce an error outcome and cancellation is rethrown.

### 7. Wrong vs Correct

#### Wrong

```kotlin
val outcome = runCatching { repository.addTask(form) }
    .fold(
        onSuccess = { TaskSaveOutcome(saved = true, message = "Saved") },
        onFailure = { TaskSaveOutcome(saved = false, message = "Save failed") }
    )
```

#### Correct

```kotlin
val outcome = try {
    repository.addTask(form)
    TaskSaveOutcome(saved = true, message = "Saved")
} catch (error: Exception) {
    if (error is CancellationException) throw error
    TaskSaveOutcome(saved = false, message = "Save failed")
}
```

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

## Scenario: WebDAV Backup Transport

### 1. Scope / Trigger

Trigger this contract when uploading or downloading backup payloads through
WebDAV or another user-configured HTTP endpoint.

### 2. Signatures

* Settings: `WebDavSettings(url: String, username: String, password: String)`
* Store: `BackupSettingsStore.loadWebDavSettings(): WebDavSettings`
* Store: `BackupSettingsStore.saveWebDavSettings(settings)`
* Upload: `WebDavBackupClient.upload(settings, payload)`
* Download: `WebDavBackupClient.download(settings): String`

### 3. Contracts

* WebDAV backup uses manual `PUT` to the configured file URL.
* WebDAV restore uses manual `GET` from the configured file URL.
* Optional Basic auth is sent only when username or password is present.
* Local WebDAV servers may use `http://` LAN URLs; public or remote backup
  endpoints should prefer `https://`.
* The Android manifest must permit cleartext traffic when LAN HTTP WebDAV is
  supported, otherwise Android 9+ blocks uploads before the client receives an
  HTTP response.
* All network work runs on `Dispatchers.IO`.
* Failed upload/download must surface status text and leave Room data unchanged.
* Downloaded backup JSON is decoded before local rows are deleted.

### 4. Validation & Error Matrix

* Blank URL -> block upload/restore in UI and reject in client.
* LAN `http://` URL blocked by platform cleartext policy -> upload/download
  fails before reaching WebDAV; allow cleartext traffic in the manifest for
  this personal LAN backup use case.
* Non-2xx HTTP status -> throw an `IOException` that includes status code.
* Connection/read timeout -> surface the exception message in the backup panel.
* Invalid downloaded JSON -> restore fails before deleting local rows.

### 5. Good/Base/Bad Cases

* Good: Save settings, upload current JSON, then show a compact success message.
* Base: Anonymous WebDAV URL with no username/password.
* Bad: Perform network calls on the Compose main thread.

### 6. Tests Required

* Run `:app:lintDebug` for network permission and UI call-path checks.
* Run `:app:assembleDebug` after changing the backup panel or WebDAV client.
* Manual verification should include a known WebDAV endpoint before relying on
  the feature for real backups.

### 7. Wrong vs Correct

#### Wrong

```kotlin
val json = URL(url).readText()
repository.importBackupJson(json)
```

This performs network I/O on the caller thread and has poor HTTP error
reporting.

#### Correct

```kotlin
val json = webDavClient.download(settings)
val count = repository.importBackupJson(json)
```

The client owns IO dispatch and HTTP status handling; the repository validates
JSON before replacing local rows.
