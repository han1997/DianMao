# Restore reminders after reboot

## Goal

Implement the README unfinished item "device reboot reminder restoration and repeat reminder batch compensation" so DianMao reminders remain reliable after the phone restarts or the app is opened after missed repeat intervals.

## What I already know

* README lists reboot reminder restoration and repeat reminder batch compensation as unfinished.
* `ReminderScheduler.schedule` writes a one-shot `AlarmManager` pending intent per task id.
* `TaskReminderReceiver` posts the reminder notification, advances repeating tasks once, and schedules the next reminder.
* `TaskRepository.advanceRepeatAfterReminder` only advances one interval. It does not catch up if a daily/weekly/monthly reminder is multiple intervals behind.
* `AndroidManifest.xml` has `SCHEDULE_EXACT_ALARM` and notification/calendar permissions, but no `RECEIVE_BOOT_COMPLETED` permission or reboot receiver.
* `TaskDao` has no query for all active alarm tasks.
* The product register is `product`; this is reliability plumbing and should not add new UI.

## Assumptions

* Local Room task state is the source of truth for which reminders should be active.
* One-shot reminders whose due time is already in the past should not fire stale notifications during restoration.
* Repeating reminders whose due time is in the past should be advanced repeatedly until their next due time is in the future, then scheduled.
* Restoration should run both after device boot and on app launch, because app launch can repair missed repeat intervals even when the boot broadcast was unavailable.
* Scheduling restoration is idempotent because `ReminderScheduler` uses each task id as the pending intent request code with `FLAG_UPDATE_CURRENT`.

## Requirements

* Add an Android boot-completed receiver and manifest permission.
* On boot, load active incomplete tasks with `useAlarm = true` and a non-null due time, then schedule future reminders.
* On app launch, run the same restoration path after seeding local data.
* Add repository logic that:
  * returns future one-shot reminders for scheduling;
  * advances repeating reminders whose due time is at or before the restore time until the next due time is in the future;
  * updates `dueAtMillis`, `dueLabel`, and `updatedAtMillis` when a repeating reminder is compensated.
* Make normal reminder delivery use the same "next future due" logic so delayed repeat alarms do not remain behind.
* Keep the UI unchanged.
* Update `CHANGELOG.md`; update README current status and remaining unfinished items.

## Acceptance Criteria

* [ ] `AndroidManifest.xml` declares `RECEIVE_BOOT_COMPLETED` and a receiver for `BOOT_COMPLETED`.
* [ ] Reboot restoration schedules all incomplete future alarm tasks.
* [ ] Reboot restoration advances overdue repeating tasks to the next future due time and schedules them.
* [ ] App launch performs the same restoration and compensation, making the behavior testable without rebooting a device.
* [ ] Past one-shot reminders are not rescheduled as stale notifications.
* [ ] Existing notification permission behavior remains lint-clean.
* [ ] Build/lint/test verification passes.

## Definition of Done

* Code follows existing Kotlin, Room, AlarmManager, and BroadcastReceiver patterns.
* `CHANGELOG.md` is updated.
* README is updated if the unfinished task list changes.
* Lint/type-check/build/test checks are run where practical.
* Local git commit is created after user confirmation, per README convention.

## Technical Approach

Add a DAO query for active alarm tasks and a repository method that computes the tasks to schedule during restoration. Add a boot receiver under `alerts/` that calls the repository method and schedules the returned tasks using `ReminderScheduler`. Reuse the repository catch-up logic from both boot restoration and normal repeat advancement. Invoke restoration from `DianMaoApp` startup after seeding.

## Decision (ADR-lite)

**Context**: AlarmManager alarms are not durable across device reboot, and repeat reminders can fall behind when the device or app misses several intervals.

**Decision**: Restore from Room, not from AlarmManager state. Treat Room as the durable source of truth, skip stale one-shot reminders, and advance repeat reminders to the next future due time before scheduling.

**Consequences**: Restoration is deterministic and idempotent. Missed one-shot reminders are not surfaced retroactively in this MVP; overdue repeat reminders continue from the next valid future occurrence.

## Out of Scope

* Showing a UI banner or notification for missed one-shot reminders.
* Rescheduling after time-zone or manual time changes.
* WorkManager integration.
* Formal backup strategy.

## Technical Notes

* Relevant files inspected:
  * `README.md`
  * `app/src/main/AndroidManifest.xml`
  * `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
  * `app/src/main/java/fun/han1997/dianmao/alerts/ReminderScheduler.kt`
  * `app/src/main/java/fun/han1997/dianmao/alerts/TaskReminderReceiver.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskDao.kt`
  * `app/src/main/java/fun/han1997/dianmao/data/TaskRepository.kt`
* Design scene: the owner restarts the phone overnight and expects important reminders to continue without seeing a new setup surface.
* Color strategy remains restrained; no new UI or visual token is needed.

## Verification

* Initial parallel `compileDebugKotlin` and `testDebugUnitTest` run hit KSP generated-file contention; rerunning sequentially resolved it.
* `.\gradlew.bat :app:compileDebugKotlin --no-daemon` passed.
* `.\gradlew.bat :app:testDebugUnitTest --no-daemon` passed.
* `.\gradlew.bat :app:lintDebug --no-daemon` passed.
* `.\gradlew.bat :app:assembleDebug --no-daemon` passed.
* `git diff --check` passed before spec update and will be rerun before commit.
