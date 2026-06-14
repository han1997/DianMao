package `fun`.han1997.dianmao.data

import android.content.Context
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
            repeatRule = if (dueAtMillis != null && form.useAlarm) form.repeatRule else RepeatRule.None,
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
            repeatRule = if (dueAtMillis != null && form.useAlarm) form.repeatRule else RepeatRule.None,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.update(updated)
        return updated.toUiModel()
    }

    suspend fun deleteTask(taskId: Long) {
        val existing = dao.findById(taskId) ?: return
        dao.delete(existing)
    }

    suspend fun toggleCompleted(task: TaskUiModel, completed: Boolean) {
        val existing = dao.findById(task.id) ?: return
        dao.update(
            existing.copy(
                completed = completed,
                completionNote = if (completed) existing.completionNote else null,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveCompletionNote(taskId: Long, note: String) {
        val existing = dao.findById(taskId) ?: return
        dao.update(
            existing.copy(
                completed = true,
                completionNote = note.trim().ifBlank { null },
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun advanceRepeatAfterReminder(taskId: Long): TaskUiModel? {
        val existing = dao.findById(taskId) ?: return null
        val currentDue = existing.dueAtMillis ?: return null
        if (existing.repeatRule == RepeatRule.None || !existing.useAlarm || existing.completed) return null
        val nextDue = nextDueAt(currentDue, existing.repeatRule)
        val nextLabel = DateTimeFormatters.labelFor(
            dateMillis = midnightMillis(nextDue),
            minutesOfDay = minutesOfDay(nextDue)
        )
        val updated = existing.copy(
            dueLabel = nextLabel,
            dueAtMillis = nextDue,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.update(updated)
        return updated.toUiModel()
    }

    private fun nextDueAt(currentMillis: Long, repeatRule: RepeatRule): Long {
        val dateTime = Instant.ofEpochMilli(currentMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val next = when (repeatRule) {
            RepeatRule.None -> dateTime
            RepeatRule.Daily -> dateTime.plusDays(1)
            RepeatRule.Weekly -> dateTime.plusWeeks(1)
            RepeatRule.Monthly -> dateTime.plusMonths(1)
        }
        return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
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
