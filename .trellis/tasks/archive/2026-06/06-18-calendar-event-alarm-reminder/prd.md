# calendar event alarm reminder

## Goal

When a task is written to the system calendar, set a reminder on the calendar event so the calendar app also triggers a notification/alarm at the event time.

## What I already know

* `CalendarWriter.eventValues()` creates the event but does not set `HAS_ALARM` or insert a row in `CalendarContract.Reminders`
* The Android Calendar API requires two steps: (1) insert the event with `HAS_ALARM=1`, (2) insert a reminder row via `CalendarContract.Reminders.CONTENT_URI`
* The app already has a separate `useAlarm` toggle that uses `AlarmManager` directly; this calendar reminder is independent and controlled by the `useCalendar` toggle

## Requirements

* When `saveOrUpdateEvent` creates or updates a calendar event, set `HAS_ALARM=1` on the event and insert a reminder row (default: 0 minutes before, `METHOD_ALARM`)
* When `useCalendar` is toggled off and the event is deleted, remove associated reminders automatically (handled by CASCADE on event delete)
* The reminder should only be added when useCalendar is true; the existing AlarmManager path (`useAlarm`) is unaffected

## Acceptance Criteria

* [ ] Calendar events created by DianMao show a reminder in the system calendar app
* [ ] The calendar notification fires at the event time
* [ ] Existing AlarmManager reminders still work independently
* [ ] App builds successfully

## Definition of Done

* Code changed, build passes, manual test on device

## Out of Scope

* Customizable reminder lead time (use 0 minutes = at event time)
* Multiple reminders per event
* Changes to AlarmManager-based reminders

## Technical Notes

* After inserting/updating the event via `CalendarContract.Events`, insert a reminder via `CalendarContract.Reminders.CONTENT_URI` with `EVENT_ID`, `MINUTES=0`, `METHOD=METHOD_ALARM`
* Must update existing reminders too (delete old reminders for the event, then insert new one)
