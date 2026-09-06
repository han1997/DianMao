package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.calendar.CalendarWriter
import `fun`.han1997.dianmao.data.QuadrantType
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskEntity
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskSubTask
import `fun`.han1997.dianmao.data.TaskUiModel
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.lazy.items
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class TaskSaveOutcome(
    val saved: Boolean,
    val message: String
)

internal fun nextSubTaskId(items: List<TaskSubTask>): Long {
    return (items.maxOfOrNull { it.id } ?: 0L) + 1L
}

internal fun hasCalendarPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
}

internal fun saveOrOpenCalendarEvent(context: android.content.Context, task: TaskUiModel): Long? {
    if (hasCalendarPermission(context)) {
        CalendarWriter.saveOrUpdateEvent(context, task)?.let { return it }
    }

    if (task.useCalendar) {
        CalendarWriter.buildInsertIntent(task)?.let { context.startActivity(it) }
    }
    return null
}

internal fun openCalendarReminder(context: android.content.Context, task: TaskUiModel) {
    CalendarWriter.buildOpenReminderIntent(task)?.let { intent ->
        runCatching { context.startActivity(intent) }
    }
}

internal fun deleteCalendarEvent(context: android.content.Context, eventId: Long?): Boolean {
    if (eventId == null) return true
    if (!hasCalendarPermission(context)) return false
    return CalendarWriter.deleteEvent(context, eventId)
}

internal fun defaultBackupFileName(): String {
    val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
    return "dianmao-backup-$stamp.json"
}

internal suspend fun writeTextToUri(context: android.content.Context, uri: Uri, text: String) {
    withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write(text)
        } ?: error("无法打开备份文件")
    }
}

internal suspend fun readTextFromUri(context: android.content.Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
            reader.readText()
        } ?: error("无法读取备份文件")
    }
}

internal fun seedTaskEntities(): List<TaskEntity> {
    val now = System.currentTimeMillis()
    val today18 = LocalDate.now()
        .atTime(18, 0)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    val tomorrow930 = LocalDate.now()
        .plusDays(1)
        .atTime(9, 30)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    return listOf(
        TaskEntity(
            title = "梳理本周必须交付的工作",
            quadrant = QuadrantType.ImportantUrgent,
            dueLabel = "今天 18:00",
            dueAtMillis = today18,
            useCalendar = false,
            useAlarm = false,
            repeatRule = RepeatRule.None,
            subTasks = listOf(
                TaskSubTask(id = 1, title = "列出本周必须完成的事项"),
                TaskSubTask(id = 2, title = "标记今天要先推进的两项")
            ),
            completed = false,
            completionNote = null,
            archived = false,
            createdAtMillis = now - 3_000,
            updatedAtMillis = now - 3_000
        ),
        TaskEntity(
            title = "设计四象限任务模型",
            quadrant = QuadrantType.ImportantNotUrgent,
            dueLabel = "长期",
            dueAtMillis = null,
            useCalendar = false,
            useAlarm = false,
            repeatRule = RepeatRule.None,
            completed = false,
            completionNote = null,
            archived = false,
            createdAtMillis = now - 2_000,
            updatedAtMillis = now - 2_000
        ),
        TaskEntity(
            title = "确认明早会议提醒",
            quadrant = QuadrantType.NotImportantUrgent,
            dueLabel = "明天 09:30",
            dueAtMillis = tomorrow930,
            useCalendar = false,
            useAlarm = false,
            repeatRule = RepeatRule.None,
            completed = false,
            completionNote = null,
            archived = false,
            createdAtMillis = now - 1_000,
            updatedAtMillis = now - 1_000
        ),
        TaskEntity(
            title = "整理完成任务心得",
            quadrant = QuadrantType.NotImportantNotUrgent,
            dueLabel = null,
            dueAtMillis = null,
            useCalendar = false,
            useAlarm = false,
            repeatRule = RepeatRule.None,
            completed = true,
            completionNote = "完成后记录判断依据，方便以后复盘同类任务。",
            archived = false,
            createdAtMillis = now,
            updatedAtMillis = now
        )
    )
}

internal fun webDavHost(url: String): String? {
    val trimmed = url.trim()
    if (trimmed.isBlank()) return null
    return runCatching {
        val parsed = java.net.URI(trimmed)
        val scheme = parsed.scheme
        val host = parsed.host
        if (!scheme.isNullOrBlank() && !host.isNullOrBlank()) {
            if (parsed.port > 0) "$scheme://$host:${parsed.port}" else "$scheme://$host"
        } else {
            null
        }
    }.getOrNull()
}
