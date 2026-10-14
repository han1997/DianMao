package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.calendar.CalendarWriter
import `fun`.han1997.dianmao.data.TaskSubTask
import `fun`.han1997.dianmao.data.TaskUiModel
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.lazy.items
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
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

/**
 * Writes the pre-restore safety snapshot into the public Downloads
 * collection, deleting any previous snapshot of the same name first so the
 * picker never shows two candidates. Local restores have no remote share to
 * mirror the WebDAV snapshot, so Downloads is where the existing 本地恢复
 * picker can select it back for a manual undo. Throws on failure so the
 * caller can abort the destructive import.
 */
internal suspend fun writeLocalRestoreSnapshot(context: android.content.Context, fileName: String, text: String) {
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val collection = android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
        resolver.delete(
            collection,
            "${android.provider.MediaStore.MediaColumns.DISPLAY_NAME} = ?",
            arrayOf(fileName)
        )
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(collection, values) ?: error("无法创建恢复前快照")
        resolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write(text)
        } ?: error("无法写入恢复前快照")
    }
}

internal fun openAppNotificationSettings(context: android.content.Context) {
    val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
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
