package `fun`.han1997.dianmao.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import `fun`.han1997.dianmao.data.TaskUiModel
import java.util.TimeZone

object CalendarWriter {
    fun saveEvent(context: Context, task: TaskUiModel): Long? {
        val dueAt = task.dueAtMillis ?: return null
        val resolver = context.contentResolver
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1"
        val sortOrder = "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars._ID} ASC"
        resolver.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, null, sortOrder)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val calendarId = cursor.getLong(0)
                val values = ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.TITLE, task.title)
                    put(CalendarContract.Events.DTSTART, dueAt)
                    put(CalendarContract.Events.DTEND, dueAt + 60 * 60 * 1000)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                    put(CalendarContract.Events.DESCRIPTION, "来自 DianMao")
                }
                val uri = resolver.insert(CalendarContract.Events.CONTENT_URI, values)
                return uri?.let(ContentUris::parseId)
            }
        }
        return null
    }

    fun buildInsertIntent(task: TaskUiModel): android.content.Intent? {
        val dueAt = task.dueAtMillis ?: return null
        return android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            type = "vnd.android.cursor.item/event"
            putExtra(CalendarContract.Events.TITLE, task.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dueAt)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
        }
    }
}
