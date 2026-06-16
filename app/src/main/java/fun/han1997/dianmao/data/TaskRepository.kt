package `fun`.han1997.dianmao.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId

class TaskRepository(context: Context) {
    private val db = TaskDatabase.getInstance(context)
    private val dao = db.taskDao()

    val tasks: Flow<List<TaskUiModel>> = dao.observeTasks().map { list ->
        list.map { it.toUiModel() }
    }

    val activeTasks: Flow<List<TaskUiModel>> = dao.observeActiveTasks().map { list ->
        list.map { it.toUiModel() }
    }

    val archivedTasks: Flow<List<TaskUiModel>> = dao.observeArchivedTasks().map { list ->
        list.map { it.toUiModel() }
    }

    suspend fun seedIfEmpty(items: List<TaskEntity>) {
        if (dao.countTasks() == 0L) {
            dao.insertAll(items)
        }
    }

    suspend fun addTask(form: TaskFormState): TaskUiModel {
        val now = System.currentTimeMillis()
        val dueAtMillis = dueAtMillis(form)
        val entity = TaskEntity(
            title = form.title.trim(),
            quadrant = form.quadrant,
            dueLabel = form.dueDateMillis?.let { DateTimeFormatters.labelFor(it, form.dueTimeMinutes) },
            dueAtMillis = dueAtMillis,
            useCalendar = form.useCalendar && dueAtMillis != null,
            useAlarm = form.useAlarm && dueAtMillis != null,
            repeatRule = form.repeatRule,
            completed = false,
            completionNote = null,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        val id = dao.insert(entity)
        return entity.copy(id = id).toUiModel()
    }

    suspend fun updateTask(taskId: Long, form: TaskFormState): TaskUiModel? {
        val existing = dao.findById(taskId) ?: return null
        val dueAtMillis = dueAtMillis(form)
        val updated = existing.copy(
            title = form.title.trim(),
            quadrant = form.quadrant,
            dueLabel = form.dueDateMillis?.let { DateTimeFormatters.labelFor(it, form.dueTimeMinutes) },
            dueAtMillis = dueAtMillis,
            useCalendar = form.useCalendar && dueAtMillis != null,
            useAlarm = form.useAlarm && dueAtMillis != null,
            repeatRule = form.repeatRule,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.update(updated)
        return updated.toUiModel()
    }

    suspend fun deleteTask(taskId: Long) {
        val existing = dao.findById(taskId) ?: return
        dao.delete(existing)
    }

    suspend fun updateCalendarEventId(taskId: Long, calendarEventId: Long?): TaskUiModel? {
        val existing = dao.findById(taskId) ?: return null
        val updated = existing.copy(
            calendarEventId = calendarEventId,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.update(updated)
        return updated.toUiModel()
    }

    suspend fun toggleCompleted(task: TaskUiModel, completed: Boolean) {
        val existing = dao.findById(task.id) ?: return
        val now = System.currentTimeMillis()

        val updated = if (completed && existing.repeatRule.isCountBased()) {
            // Count-based: increment count
            existing.copy(
                completed = true,
                completionCount = existing.completionCount + 1,
                lastCompletedAtMillis = now,
                completionNote = existing.completionNote,
                updatedAtMillis = now
            )
        } else if (completed && existing.repeatRule.isCycleBased()) {
            // Cycle-based: record completion timestamp
            existing.copy(
                completed = true,
                lastCompletedAtMillis = now,
                completionNote = existing.completionNote,
                updatedAtMillis = now
            )
        } else {
            // No repeat or uncompleting: normal toggle
            existing.copy(
                completed = completed,
                completionNote = if (completed) existing.completionNote else null,
                updatedAtMillis = now
            )
        }

        dao.update(updated)
    }

    suspend fun saveCompletionNote(taskId: Long, note: String) {
        val existing = dao.findById(taskId) ?: return
        val now = System.currentTimeMillis()
        val trimmedNote = note.trim().ifBlank { null }

        val updated = if (existing.repeatRule.isCountBased()) {
            // Count-based: increment count
            existing.copy(
                completed = true,
                completionCount = existing.completionCount + 1,
                lastCompletedAtMillis = now,
                completionNote = trimmedNote,
                updatedAtMillis = now
            )
        } else if (existing.repeatRule.isCycleBased()) {
            // Cycle-based: record completion timestamp
            existing.copy(
                completed = true,
                lastCompletedAtMillis = now,
                completionNote = trimmedNote,
                updatedAtMillis = now
            )
        } else {
            // No repeat: normal completion
            existing.copy(
                completed = true,
                completionNote = trimmedNote,
                updatedAtMillis = now
            )
        }

        dao.update(updated)
    }

    suspend fun archiveTask(taskId: Long) {
        val existing = dao.findById(taskId) ?: return
        dao.update(
            existing.copy(
                archived = true,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun unarchiveTask(taskId: Long) {
        val existing = dao.findById(taskId) ?: return
        dao.update(
            existing.copy(
                archived = false,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetExpiredCycleTasks(nowMillis: Long = System.currentTimeMillis()) {
        val completedTasks = dao.findAllTasks().filter { it.completed && it.repeatRule.isCycleBased() }
        val tasksToReset = completedTasks.filter { task ->
            val lastCompleted = task.lastCompletedAtMillis ?: return@filter false
            !isInSamePeriod(lastCompleted, nowMillis, task.repeatRule)
        }

        tasksToReset.forEach { task ->
            dao.update(
                task.copy(
                    completed = false,
                    updatedAtMillis = nowMillis
                )
            )
        }
    }

    private fun isInSamePeriod(time1: Long, time2: Long, repeatRule: RepeatRule): Boolean {
        val zone = ZoneId.systemDefault()
        val date1 = Instant.ofEpochMilli(time1).atZone(zone).toLocalDate()
        val date2 = Instant.ofEpochMilli(time2).atZone(zone).toLocalDate()

        return when (repeatRule) {
            RepeatRule.Daily -> date1.isEqual(date2)
            RepeatRule.Weekly -> {
                val week1 = date1.year * 53 + date1.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
                val week2 = date2.year * 53 + date2.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
                week1 == week2
            }
            RepeatRule.Monthly -> date1.year == date2.year && date1.monthValue == date2.monthValue
            else -> false
        }
    }

    suspend fun exportBackupJson(): String {
        return TaskBackupCodec.encode(dao.findAllTasks())
    }

    suspend fun importBackupJson(json: String): Int {
        val tasks = TaskBackupCodec.decodeForImport(json)
        db.withTransaction {
            dao.deleteAll()
            dao.insertAll(tasks)
        }
        return tasks.size
    }

    suspend fun restoreScheduledReminders(nowMillis: Long = System.currentTimeMillis()): List<TaskUiModel> {
        return dao.findActiveAlarmTasks().mapNotNull { task ->
            val dueAt = task.dueAtMillis ?: return@mapNotNull null
            when {
                task.repeatRule != RepeatRule.None && dueAt <= nowMillis -> {
                    val nextDue = RepeatReminderCalculator.nextFutureDueAfter(dueAt, task.repeatRule, nowMillis)
                    updateTaskDue(task, nextDue).toUiModel()
                }
                dueAt > nowMillis -> task.toUiModel()
                else -> null
            }
        }
    }

    suspend fun advanceRepeatAfterReminder(taskId: Long): TaskUiModel? {
        val existing = dao.findById(taskId) ?: return null
        val currentDue = existing.dueAtMillis ?: return null
        if (existing.repeatRule == RepeatRule.None || !existing.useAlarm || existing.completed) return null
        val nextDue = RepeatReminderCalculator.nextFutureDueAfter(
            currentMillis = currentDue,
            repeatRule = existing.repeatRule,
            nowMillis = System.currentTimeMillis()
        )
        return updateTaskDue(existing, nextDue).toUiModel()
    }

    private suspend fun updateTaskDue(task: TaskEntity, nextDue: Long): TaskEntity {
        val updated = task.copy(
            dueLabel = DateTimeFormatters.labelFor(
                dateMillis = midnightMillis(nextDue),
                minutesOfDay = minutesOfDay(nextDue)
            ),
            dueAtMillis = nextDue,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.update(updated)
        return updated
    }

    private fun midnightMillis(timestamp: Long): Long {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun minutesOfDay(timestamp: Long): Int {
        val time = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return time.hour * 60 + time.minute
    }

    private fun dueAtMillis(form: TaskFormState): Long? {
        return form.dueDateMillis?.let { dateMillis ->
            val minutes = form.dueTimeMinutes
            if (minutes != null) dateMillis + minutes * 60_000L else dateMillis
        }
    }
}
