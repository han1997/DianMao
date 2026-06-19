# Calendar event dedupe and update

## Goal

Implement the README unfinished item "calendar event dedupe/update" so DianMao manages a single system calendar event for each task that has calendar sync enabled. Editing a synced task should update the existing event instead of inserting duplicates, and disabling sync or deleting the task should remove the event created by DianMao.

## What I already know

* README lists calendar event dedupe/update as unfinished.
* Current `CalendarWriter.saveEvent` always inserts a new Calendar Provider event and returns the generated event id.
* `TaskEntity` and `TaskUiModel` do not persist a calendar event id, so later edits cannot target the original event.
* `MainActivity` only writes a calendar event when a newly saved task has `useCalendar = true` and the previous task did not use calendar sync.
* Deleting a task cancels the local alarm but does not remove a system calendar event.
* Project README requires updating `CHANGELOG.md` after code changes and updating README when behavior/status changes.

## Assumptions

* The MVP should only manage events that DianMao successfully inserted and stored locally. It should not scan or mutate unrelated user calendar events.
* If direct Calendar Provider writes are unavailable and the app falls back to the system insert UI, DianMao cannot know the created event id; that fallback remains best-effort and is out of managed upsert/delete scope.
* Existing tasks created before this migration have no stored event id. On the next save with calendar sync enabled, DianMao may insert once and then manage that event going forward.

## Requirements

* Persist the Calendar Provider event id on each task.
* Add a Room migration for the new nullable calendar event id field.
* When saving a task with calendar sync enabled and calendar permission available:
  * update the stored calendar event if an event id exists;
  * insert a new event if no event id exists or the stored event can no longer be updated;
  * store the resulting event id back on the task.
* When saving a task with calendar sync disabled or no due date, delete any stored calendar event and clear the stored event id.
* When deleting a task, delete any stored calendar event before removing the local task.
* Preserve the current system insert fallback when direct write is not possible.
* Keep UI behavior native and quiet; do not add new visible controls for this internal sync fix.
* Update `CHANGELOG.md`; update README current status if the unfinished item changes.

## Acceptance Criteria

* [ ] Editing the title, due time, or repeat rule of a task with calendar sync enabled updates the same system event instead of creating another one.
* [ ] Disabling calendar sync for a task removes the previously managed system event.
* [ ] Deleting a task removes the previously managed system event.
* [ ] Tasks with no stored event id still work: saving with calendar sync inserts once, records the id, and future saves update it.
* [ ] The app still falls back to the system calendar insert page when direct Calendar Provider write is unavailable.
* [ ] Room migrates from the previous schema without destructive migration.
* [ ] Build or compile verification passes.

## Definition of Done

* Code follows existing Kotlin, Compose, Room, and Android platform patterns.
* Lint/type-check/build verification is run where practical.
* `CHANGELOG.md` is updated.
* README is updated if the unfinished task list changes.
* Commit plan is prepared for user confirmation.

## Technical Approach

Add a nullable `calendarEventId: Long?` to the task entity and UI model, then migrate Room from version 2 to 3 with `ALTER TABLE tasks ADD COLUMN calendarEventId INTEGER`. Extend `CalendarWriter` with update/delete behavior around the stored id. Move the calendar sync decision into the save/delete flows so repository state and Calendar Provider state stay aligned after each user action.

## Decision (ADR-lite)

**Context**: Calendar Provider event identity is required to update or delete a specific event. Guessing by title/time is unsafe because the user's calendar can contain unrelated events with the same data.

**Decision**: Persist the exact event id returned by Calendar Provider and manage only those events. If the app cannot write directly and falls back to an external insert intent, leave that event unmanaged because Android does not return a reliable id to this flow.

**Consequences**: The behavior is safe and deterministic for app-created events after this release. Older or fallback-created events may need one more insertion before DianMao can manage them.

## Out of Scope

* Scanning the user's calendar to deduplicate historical events.
* Syncing completion state into calendar events.
* Recurrence exception handling beyond existing RRULE support.
* Device reboot reminder restoration and repeat reminder batch repair.
* Formal backup strategy.

## Technical Notes

* Relevant files inspected:
  * `README.md`
  * `CHANGELOG.md`
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
  * `app/src/main/java/fun/han1997/dianmao/calendar/CalendarWriter.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskModels.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskRepository.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskDao.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskDatabase.kt`
* Product design register is `product`; no new UI should be added unless necessary.
* Design scene: the owner edits a scheduled task on a phone during normal daily planning, expecting the system calendar state to quietly follow the app without extra decisions.
* Color strategy remains restrained; no new color or component surface is needed.

## Verification

* `.\gradlew.bat :app:compileDebugKotlin --no-daemon` passed.
* `.\gradlew.bat :app:lintDebug --no-daemon` passed after adding an inline Android 13+ notification permission guard.
* `.\gradlew.bat :app:testDebugUnitTest --no-daemon` passed after rerunning with approved network/cache access for the Gradle wrapper.
* `.\gradlew.bat :app:assembleDebug --no-daemon` passed.
* `git diff --check` passed.
