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

private const val IMMEDIATE_REMINDER_MINUTES = 0
private val IMMEDIATE_ALERT_REMINDER = ReminderConfig(
    method = CalendarContract.Reminders.METHOD_ALERT,
    minutes = IMMEDIATE_REMINDER_MINUTES
)

object CalendarWriter {
    private const val EVENT_DURATION_MILLIS = 60 * 60 * 1000L

    fun saveOrUpdateEvent(context: Context, task: TaskUiModel): Long? {
        val dueAt = task.dueAtMillis ?: return null
        val resolver = context.contentResolver
        val calendar = findWritableCalendar(resolver) ?: return null
        val values = eventValues(task, dueAt, calendar.id)
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

        // A system-calendar event only carries an alert reminder when the
        // task's own alarm is enabled. Syncing to the calendar without an
        // alarm must produce a quiet event (HAS_ALARM=0 and no Reminders row).
        if (task.useAlarm) {
            setReminder(resolver, eventId, calendar.allowedReminderMethods)
        } else {
            clearReminder(resolver, eventId)
        }
        return eventId
    }

    private fun insertEvent(resolver: ContentResolver, values: ContentValues): Long? {
        val uri = runCatching {
            resolver.insert(CalendarContract.Events.CONTENT_URI, values)
        }.getOrNull()
        return uri?.let(ContentUris::parseId)
    }

    private fun clearReminder(resolver: ContentResolver, eventId: Long) {
        runCatching {
            resolver.delete(
                CalendarContract.Reminders.CONTENT_URI,
                "${CalendarContract.Reminders.EVENT_ID} = ?",
                arrayOf(eventId.toString())
            )
        }
    }

    private fun setReminder(resolver: ContentResolver, eventId: Long, allowedReminderMethods: Set<Int>?) {
        // Remove existing reminders for this event, then add one
        clearReminder(resolver, eventId)

        val attempts = reminderConfigsToTry(allowedReminderMethods)
        for (attempt in attempts) {
            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, attempt.minutes)
                put(CalendarContract.Reminders.METHOD, attempt.method)
            }
            val inserted = runCatching {
                resolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
            }.getOrNull()

            if (inserted != null && hasReminderRow(resolver, eventId)) {
                return
            }
        }

        Log.w(
            "CalendarWriter",
            "Failed to insert reminder for event $eventId with methods ${attempts.map { it.method }}"
        )
    }

    private fun hasReminderRow(resolver: ContentResolver, eventId: Long): Boolean {
        return runCatching {
            CalendarContract.Reminders.query(
                resolver,
                eventId,
                arrayOf(CalendarContract.Reminders.EVENT_ID)
            )?.use { cursor ->
                cursor.moveToFirst()
            } == true
        }.getOrDefault(false)
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
            setDataAndType(CalendarContract.Events.CONTENT_URI, "vnd.android.cursor.item/event")
            putExtra(CalendarContract.Events.TITLE, task.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dueAt)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dueAt + EVENT_DURATION_MILLIS)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
            if (task.useAlarm) {
                reminderIntentExtras().forEach { (key, value) -> putExtra(key, value) }
            }
            repeatRuleToRRule(task.repeatRule)?.let { putExtra(CalendarContract.Events.RRULE, it) }
        }
    }

    fun buildOpenReminderIntent(task: TaskUiModel): android.content.Intent? {
        val eventId = task.calendarEventId
        if (eventId != null) {
            return android.content.Intent(android.content.Intent.ACTION_EDIT).apply {
                setDataAndType(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
                    "vnd.android.cursor.item/event"
                )
                if (task.useAlarm) {
                    reminderIntentExtras().forEach { (key, value) -> putExtra(key, value) }
                }
            }
        }
        return buildInsertIntent(task)
    }

    private fun findWritableCalendar(resolver: ContentResolver): CalendarCandidate? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.ALLOWED_REMINDERS
        )
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1"
        val sortOrder = "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars._ID} ASC"
        return runCatching {
            resolver.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, null, sortOrder)?.use { cursor ->
                val candidates = buildList {
                    while (cursor.moveToNext()) {
                        add(
                            CalendarCandidate(
                                id = cursor.getLong(0),
                                isPrimary = cursor.getInt(1) != 0,
                                accessLevel = cursor.getInt(2),
                                allowedReminderMethods = parseAllowedReminderMethods(cursor.getString(3))
                            )
                        )
                    }
                }
                chooseBestCalendar(candidates)
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
            put(CalendarContract.Events.HAS_ALARM, if (task.useAlarm) 1 else 0)
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

internal data class CalendarCandidate(
    val id: Long,
    val isPrimary: Boolean,
    val accessLevel: Int,
    val allowedReminderMethods: Set<Int>?
)

internal fun chooseBestCalendar(candidates: List<CalendarCandidate>): CalendarCandidate? {
    return candidates
        .asSequence()
        .sortedWith(
            compareByDescending<CalendarCandidate> { it.looksWritable() }
                .thenByDescending { it.supportsAlertReminder() }
                .thenByDescending { it.isPrimary }
                .thenByDescending { it.accessLevel }
                .thenBy { it.id }
        )
        .firstOrNull()
}

internal fun chooseBestCalendarId(candidates: List<CalendarCandidate>): Long? =
    chooseBestCalendar(candidates)?.id

internal fun reminderIntentExtras(): Map<String, Int> {
    return mapOf(
        CalendarContract.Events.HAS_ALARM to 1,
        CalendarContract.Reminders.MINUTES to IMMEDIATE_ALERT_REMINDER.minutes,
        CalendarContract.Reminders.METHOD to IMMEDIATE_ALERT_REMINDER.method
    )
}

internal data class ReminderConfig(
    val method: Int,
    val minutes: Int
)

internal fun reminderConfigsToTry(allowedReminderMethods: Set<Int>?): List<ReminderConfig> {
    val preferredOrder = listOf(
        IMMEDIATE_ALERT_REMINDER,
        ReminderConfig(
            method = CalendarContract.Reminders.METHOD_DEFAULT,
            minutes = CalendarContract.Reminders.MINUTES_DEFAULT
        ),
        ReminderConfig(
            method = CalendarContract.Reminders.METHOD_ALARM,
            minutes = IMMEDIATE_REMINDER_MINUTES
        )
    )
    val supported = allowedReminderMethods.orEmpty()
    if (supported.isEmpty()) {
        return preferredOrder
    }
    val supportedFirst = preferredOrder.filter { supported.contains(it.method) }
    return (supportedFirst + preferredOrder).distinctBy { it.method }
}

private fun CalendarCandidate.looksWritable(): Boolean {
    return accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR
}

private fun CalendarCandidate.supportsAlertReminder(): Boolean {
    val methods = allowedReminderMethods ?: return false
    return methods.contains(CalendarContract.Reminders.METHOD_ALERT)
}

private fun parseAllowedReminderMethods(raw: String?): Set<Int>? {
    if (raw == null) return null
    if (raw.isBlank()) return emptySet()
    return raw.split(',')
        .mapNotNull { value -> value.trim().toIntOrNull() }
        .toSet()
}
