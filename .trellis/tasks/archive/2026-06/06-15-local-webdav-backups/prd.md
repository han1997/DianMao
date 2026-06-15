# Local and WebDAV backups

## Goal

Implement the README unfinished item "Room migration and formal data backup strategy" with local file backup and WebDAV backup/restore. The feature should make DianMao's Room task data portable without adding a heavy settings system or non-native controls.

## What I already know

* README lists Room migration and formal data backup strategy as the last unfinished item.
* The app uses Room with explicit migrations and is currently at database version 3.
* `TaskEntity` is the durable task record and includes task content, quadrant, due time, calendar/alarm flags, repeat rule, completion note, and stored Calendar Provider event id.
* There is no settings screen, account screen, backup format, import/export code, WebDAV client, or Room schema export.
* Existing Compose UI is a single main screen with a header, quadrant board, task sheet, and dialogs.
* Product register is `product`; backup controls should be quiet, native, and task-focused, not a marketing or dashboard surface.
* Current dependencies do not include a JSON or HTTP/WebDAV library. Android/JDK platform APIs are available.

## Assumptions

* MVP supports manual backups, not automatic scheduled background backups.
* Local backup uses Android Storage Access Framework:
  * export through `ACTION_CREATE_DOCUMENT`;
  * import through `ACTION_OPEN_DOCUMENT`.
* Backup format is UTF-8 JSON with a small metadata header and a `tasks` array.
* Restore replaces local task rows with the imported backup contents after the user explicitly chooses an import action.
* Calendar Provider event ids are not portable across devices. Imported tasks should clear `calendarEventId` to avoid mutating unrelated calendar events.
* Alarm scheduling can be restored by existing app-start reminder restoration after imported tasks are saved.
* WebDAV MVP stores one configured backup file URL plus optional username/password in app private preferences. It supports manual upload and download/restore.
* WebDAV transport can use `HttpURLConnection` with `PUT` and `GET`; advanced WebDAV discovery, directory creation, conflict resolution, encryption, and background sync are out of scope.

## Requirements

* Export all Room task rows to a versioned JSON backup payload.
* Import a backup JSON payload and replace local task rows.
* Clear `calendarEventId` during import.
* Keep Room migrations explicit and enable Room schema export for the current database version.
* Add a low-noise settings/backup panel from the app header.
* Support local export and local import through system file pickers.
* Support WebDAV configuration:
  * backup file URL;
  * optional username;
  * optional password.
* Support manual WebDAV upload of the current backup JSON.
* Support manual WebDAV download and restore.
* Show compact backup status/error text in the backup panel.
* Update `README.md` and `CHANGELOG.md`.

## Acceptance Criteria

* [ ] Room schema export is enabled and the current schema is generated.
* [ ] Local export creates a JSON document containing all tasks.
* [ ] Local import restores tasks from that JSON and clears stored calendar event ids.
* [ ] Invalid backup JSON shows an error instead of crashing.
* [ ] WebDAV upload sends the current JSON payload with `PUT`.
* [ ] WebDAV restore downloads JSON with `GET` and imports it.
* [ ] WebDAV configuration persists across app restarts.
* [ ] Backup controls are reachable from the home header and do not disturb task capture.
* [ ] Lint, tests, and debug build pass.

## Definition of Done

* Code follows existing Kotlin, Compose, Room, and Android platform patterns.
* No new network/dependency download is required unless absolutely necessary.
* `CHANGELOG.md` is updated.
* README current status and unfinished list are updated.
* Local git commit is created after user confirmation, per README convention.

## Technical Approach

Add DAO methods for full export/import, a JSON codec for `TaskEntity`, and repository methods for backup export/import. Add a small backup settings store backed by `SharedPreferences`, plus a minimal WebDAV client using `HttpURLConnection`. Add a backup sheet from the app header with local export/import buttons, WebDAV fields, save config, upload, and restore actions. Enable Room schema export in Gradle and the database annotation.

## Decision (ADR-lite)

**Context**: The app is a local-first personal Android tool, so backup must be reliable without making the main task surface feel like an admin console.

**Decision**: Use explicit JSON backups and manual local/WebDAV actions. Keep network code dependency-free in the MVP and use Storage Access Framework for local files.

**Consequences**: The first implementation is inspectable and easy to restore manually. It does not yet provide encryption, automatic sync, or WebDAV directory management.

## Out of Scope

* Automatic scheduled backups.
* Conflict resolution or merge restore.
* Backup encryption.
* WebDAV directory discovery or `MKCOL`.
* OAuth/cloud-provider-specific integrations.
* Restoring Calendar Provider event ids.

## Technical Notes

* Relevant files inspected:
  * `README.md`
  * `gradle/libs.versions.toml`
  * `app/build.gradle.kts`
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskModels.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskDatabase.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskDao.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskRepository.kt`
* Design scene: the owner is on the phone before changing devices or after planning work, and needs a clear backup action without leaving the app's calm task context.
* Theme remains light and restrained. Use system file picker and a compact settings sheet instead of new navigation.

## Verification

* `.\gradlew.bat :app:compileDebugKotlin --no-daemon` passed and generated Room schema `app/schemas/fun.han1997.dianmao.data.TaskDatabase/3.json`.
* `.\gradlew.bat :app:testDebugUnitTest --no-daemon` passed.
* `.\gradlew.bat :app:lintDebug --no-daemon` passed.
* `.\gradlew.bat :app:assembleDebug --no-daemon` passed.
* `git diff --check` passed.
* A local JVM unit test for `TaskBackupCodec` was attempted, but Android's `org.json` implementation is a not-mocked SDK stub in local unit tests. It was removed to avoid adding a new network-fetched test dependency.
