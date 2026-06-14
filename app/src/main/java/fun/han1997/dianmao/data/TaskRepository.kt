package `fun`.han1997.dianmao.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        val dueAtMillis = form.dueDateMillis?.let { dateMillis ->
            val minutes = form.dueTimeMinutes
            if (minutes != null) dateMillis + minutes * 60_000L else dateMillis
        }
        val entity = TaskEntity(
            title = form.title.trim(),
            quadrant = form.quadrant,
            dueLabel = form.dueDateMillis?.let { DateTimeFormatters.labelFor(it, form.dueTimeMinutes) },
            dueAtMillis = dueAtMillis,
            useCalendar = form.useCalendar && dueAtMillis != null,
            useAlarm = form.useAlarm && dueAtMillis != null,
            completed = false,
            completionNote = null,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        val id = dao.insert(entity)
        return entity.copy(id = id).toUiModel()
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
}
