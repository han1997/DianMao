package `fun`.han1997.dianmao.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskUiModel
import java.util.TimeZone

object CalendarWriter {
    private const val EVENT_DURATION_MILLIS = 60 * 60 * 1000L

    fun saveOrUpdateEvent(context: Context, task: TaskUiModel): Long? {
        val dueAt = task.dueAtMillis ?: return null
        val resolver = context.contentResolver
        val calendarId = findWritableCalendarId(resolver) ?: return null
        val values = eventValues(task, dueAt, calendarId)
        val existingEventId = task.calendarEventId

        val eventId: Long = if (existingEventId != null) {
            val updatedRows = runCatching {
                resolver.update(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, existingEventId),
                    values,
                    null,
                    null
                )
            }.getOrDefault(0)

            if (updatedRows > 0) existingEventId else {
                insertEvent(resolver, values) ?: return null
            }
        } else {
            insertEvent(resolver, values) ?: return null
        }

        setReminder(resolver, eventId)
        return eventId
    }

    private fun insertEvent(resolver: ContentResolver, values: ContentValues): Long? {
        val uri = runCatching {
            resolver.insert(CalendarContract.Events.CONTENT_URI, values)
        }.getOrNull()
        return uri?.let(ContentUris::parseId)
    }

    private fun setReminder(resolver: ContentResolver, eventId: Long) {
        // Remove existing reminders for this event, then add one
        runCatching {
            resolver.delete(
                CalendarContract.Reminders.CONTENT_URI,
                "${CalendarContract.Reminders.EVENT_ID} = ?",
                arrayOf(eventId.toString())
            )
        }
        val reminderValues = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, 0)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        val result = runCatching {
            resolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        }
        if (result.isFailure) {
            Log.w("CalendarWriter", "Failed to insert reminder for event $eventId", result.exceptionOrNull())
        }
    }

    fun deleteEvent(context: Context, eventId: Long): Boolean {
        val resolver = context.contentResolver
        val deletedRows = runCatching {
            resolver.delete(
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
                null,
                null
            )
        }.getOrDefault(0)
        return deletedRows > 0
    }

    fun buildInsertIntent(task: TaskUiModel): android.content.Intent? {
        val dueAt = task.dueAtMillis ?: return null
        return android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            type = "vnd.android.cursor.item/event"
            putExtra(CalendarContract.Events.TITLE, task.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dueAt)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dueAt + EVENT_DURATION_MILLIS)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
            repeatRuleToRRule(task.repeatRule)?.let { putExtra(CalendarContract.Events.RRULE, it) }
        }
    }

    private fun findWritableCalendarId(resolver: ContentResolver): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1"
        val sortOrder = "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars._ID} ASC"
        return runCatching {
            resolver.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, null, sortOrder)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else null
            }
        }.getOrNull()
    }

    private fun eventValues(task: TaskUiModel, dueAt: Long, calendarId: Long): ContentValues {
        return ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, task.title)
            put(CalendarContract.Events.DTSTART, dueAt)
            put(CalendarContract.Events.DTEND, dueAt + EVENT_DURATION_MILLIS)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.DESCRIPTION, "DianMao")
            put(CalendarContract.Events.HAS_ALARM, 1)
            val rRule = repeatRuleToRRule(task.repeatRule)
            if (rRule == null) {
                putNull(CalendarContract.Events.RRULE)
            } else {
                put(CalendarContract.Events.RRULE, rRule)
            }
        }
    }

    private fun repeatRuleToRRule(repeatRule: RepeatRule): String? {
        return when (repeatRule) {
            RepeatRule.None, RepeatRule.Count -> null
            RepeatRule.Daily -> "FREQ=DAILY"
            RepeatRule.Weekly -> "FREQ=WEEKLY"
            RepeatRule.Monthly -> "FREQ=MONTHLY"
        }
    }
}
