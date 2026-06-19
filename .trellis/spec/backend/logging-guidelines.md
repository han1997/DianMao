# Logging Guidelines

DianMao is a local-first personal task app. Logging should be rare, scoped to
diagnosing platform integration failures, and safe for user privacy.

## Current Logging Stack

The app uses Android's platform `android.util.Log` directly. There is no Timber,
structured logging framework, analytics SDK, or remote log collector.

Current reference:

- `app/src/main/java/fun/han1997/dianmao/calendar/CalendarWriter.kt` logs a
  warning when an existing Calendar Provider event cannot be updated and the app
  falls back to inserting a replacement.

Do not add a logging framework unless there is a product decision to collect or
route logs consistently.

## When To Log

Use logs only for developer-diagnosable platform failures where the local Room
state can still proceed:

- Calendar Provider update/delete/insert fallback behavior.
- AlarmManager or boot restoration failures that cannot be surfaced directly to
  the user.
- WebDAV HTTP failures only when the message is already sanitized and useful for
  debugging.

Prefer returning a nullable result, boolean, or exception to the caller over
logging and continuing silently. UI-facing operations should still surface a
compact status message when the user initiated the action.

## Levels

- `Log.w`: recoverable platform side-effect failure with fallback or local-state
  continuation.
- `Log.e`: unexpected failure that prevents the requested platform side effect
  and cannot be represented cleanly to the caller. Use sparingly.
- `Log.d` / `Log.i`: avoid in committed code unless temporarily diagnosing a
  local issue; remove before finishing.

## Tags

Use the owning class or object name as the tag, for example `CalendarWriter` or
`ReminderRestoreReceiver`. Keep tags stable so Android Studio Logcat filters
remain useful.

## Privacy Rules

Never log:

- Task titles, completion notes, subtasks, or backup JSON.
- WebDAV URLs that may contain private hostnames or paths.
- WebDAV usernames, passwords, Basic auth headers, or encoded credentials.
- Calendar event descriptions copied from task content.
- Raw exception messages if they may include sensitive payload text.

When context is needed, log stable technical identifiers such as a local task id
or calendar event id. Even then, avoid logging large batches of ids.

## Common Mistakes

- Adding `println`, `printStackTrace`, or broad debug logs while investigating a
  Compose state issue and leaving them in the final diff.
- Logging full WebDAV error bodies. `WebDavBackupClient` already truncates HTTP
  error text in thrown exceptions; do not duplicate raw bodies in logs.
- Treating logs as user feedback. User-initiated backup, calendar, or reminder
  failures need UI status or best-effort fallback behavior.
- Logging inside tight Compose recomposition paths, which can flood Logcat and
  hide real platform warnings.

## Verification

Before finishing, search for accidental logs:

```powershell
rg -n "Log\.|println\(|printStackTrace|Timber|Logger" app\src
```

Any remaining log call should be intentional, privacy-safe, and tied to a
recoverable platform integration issue.
