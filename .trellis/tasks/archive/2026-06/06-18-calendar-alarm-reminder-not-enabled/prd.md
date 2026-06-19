# calendar alarm reminder not enabled

## Goal

Fix the system calendar integration so tasks written from DianMao can reliably
use the calendar app's own alarm/reminder capability.

## What I already know

* Calendar writes go through `CalendarWriter.saveOrUpdateEvent(...)` when
  `READ_CALENDAR` and `WRITE_CALENDAR` are granted.
* The direct provider path already sets `Events.HAS_ALARM = 1` and recreates a
  `CalendarContract.Reminders` row with `MINUTES = 0` and `METHOD_ALERT`.
* If calendar permission is missing, the app falls back to
  `CalendarWriter.buildInsertIntent(...)`, which opens the system calendar's
  insert UI instead of writing via `CalendarProvider`.
* The fallback insert-intent path currently sets title, start, end, all-day,
  and optional RRULE, but does not explicitly configure a calendar reminder.
* The writable-calendar lookup currently picks the first visible syncing
  calendar and does not check whether that calendar supports reminders.
* A same-day fix already changed reminder method from `METHOD_ALARM` to
  `METHOD_ALERT`, so this bug is likely in another part of the flow.

## Assumptions (temporary)

* The local Room task save should remain the source of truth even if calendar
  reminder setup is only best-effort.
* The bug is in the calendar integration layer rather than AlarmManager,
  because the complaint is specifically about system calendar reminders.

## Open Questions

* Does the failure happen on the direct provider-write path, or only on the
  fallback system-calendar insert UI path?

## Requirements (evolving)

* Tasks synced to the system calendar should produce a calendar event whose
  reminder/alarm can be enabled and used in the calendar app.
* Calendar-enabled tasks should expose a detail-page button that opens the
  system calendar reminder UI for manual verification or adjustment.
* Existing local AlarmManager reminders must remain unaffected.
* The app should keep current best-effort behavior for external calendar side
  effects and should not block local task saves.

## Acceptance Criteria (evolving)

* [x] A calendar-enabled task results in a system calendar event with usable
  reminder/alarm support.
* [x] A calendar-enabled task detail page can open the existing system calendar
  event reminder editor, or the insert UI when no managed event id is stored.
* [x] Existing AlarmManager-based task reminders still behave as before.
* [x] The app still builds successfully after the fix.

## Definition of Done

* Code updated
* Relevant verification run
* Behavior explained clearly enough for manual device validation

## Out of Scope (explicit)

* Custom reminder lead times
* Multiple reminders per event
* Reworking the separate AlarmManager reminder feature

## Technical Notes

* Relevant files:
  `app/src/main/java/fun/han1997/dianmao/calendar/CalendarWriter.kt`
  `app/src/main/java/fun/han1997/dianmao/MainActivity.kt`
* Related history:
  `fcd0dd8` added calendar reminder rows;
  `4a52079` changed reminder method to `METHOD_ALERT`.
