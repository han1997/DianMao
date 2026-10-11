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
    private val completionDao = db.taskCompletionDao()

    val tasks: Flow<List<TaskUiModel>> = dao.observeTasks().map { list ->
        list.map { it.toUiModel() }
    }

    val activeTasks: Flow<List<TaskUiModel>> = dao.observeActiveTasks().map { list ->
        list.map { it.toUiModel() }
    }

    val archivedTasks: Flow<List<TaskUiModel>> = dao.observeArchivedTasks().map { list ->
        list.map { it.toUiModel() }
    }

    val completionsWithTask: Flow<List<CompletionWithTask>> = db.taskCompletionDao().observeCompletionsWithTask()

    suspend fun seedIfEmpty(items: List<TaskEntity>) {
        if (dao.countTasks() == 0L) {
            dao.insertAll(items)
        }
    }

    suspend fun addTask(form: TaskFormState): TaskUiModel {
        val normalizedForm = form.normalizedTimeSettings()
        val now = System.currentTimeMillis()
        val dueAtMillis = dueAtMillis(normalizedForm)
        val (primaryLead, extraLeads) = reminderLeadSetToStorage(normalizedForm.reminderLeadMinutes)
        val entity = TaskEntity(
            title = normalizedForm.title.trim(),
            detail = normalizedForm.detail?.trim()?.takeIf { it.isNotEmpty() },
            quadrant = normalizedForm.quadrant,
            dueLabel = normalizedForm.dueDateMillis?.let { DateTimeFormatters.labelFor(it, normalizedForm.dueTimeMinutes) },
            dueAtMillis = dueAtMillis,
            useCalendar = normalizedForm.useCalendar && dueAtMillis != null,
            useAlarm = normalizedForm.useAlarm && dueAtMillis != null,
            useLocalReminder = normalizedForm.useLocalReminder && dueAtMillis != null,
            reminderLeadMinutes = primaryLead,
            reminderLeadExtraMinutes = extraLeads,
            repeatRule = normalizedForm.repeatRule,
            repeatInterval = normalizedForm.repeatInterval,
            repeatWeekdays = normalizedForm.repeatWeekdays,
            subTasks = normalizedSubTasks(normalizedForm.subTasks),
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
        val normalizedForm = form.normalizedTimeSettings()
        val dueAtMillis = dueAtMillis(normalizedForm)
        val (primaryLead, extraLeads) = reminderLeadSetToStorage(normalizedForm.reminderLeadMinutes)
        val updated = existing.copy(
            title = normalizedForm.title.trim(),
            detail = normalizedForm.detail?.trim()?.takeIf { it.isNotEmpty() },
            quadrant = normalizedForm.quadrant,
            dueLabel = normalizedForm.dueDateMillis?.let { DateTimeFormatters.labelFor(it, normalizedForm.dueTimeMinutes) },
            dueAtMillis = dueAtMillis,
            useCalendar = normalizedForm.useCalendar && dueAtMillis != null,
            useAlarm = normalizedForm.useAlarm && dueAtMillis != null,
            useLocalReminder = normalizedForm.useLocalReminder && dueAtMillis != null,
            reminderLeadMinutes = primaryLead,
            reminderLeadExtraMinutes = extraLeads,
            repeatRule = normalizedForm.repeatRule,
            repeatInterval = normalizedForm.repeatInterval,
            repeatWeekdays = normalizedForm.repeatWeekdays,
            subTasks = normalizedSubTasks(normalizedForm.subTasks),
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.update(updated)
        return updated.toUiModel()
    }

    suspend fun updateSubTaskCompleted(taskId: Long, subTaskId: Long, completed: Boolean): TaskUiModel? {
        val existing = dao.findById(taskId) ?: return null
        if (existing.subTasks.none { it.id == subTaskId }) return existing.toUiModel()
        if (existing.subTasks.firstOrNull { it.id == subTaskId }?.completed == completed) return existing.toUiModel()
        val updatedSubTasks = existing.subTasks.map { item ->
            if (item.id == subTaskId) item.copy(completed = completed) else item
        }
        val updated = existing.copy(
            subTasks = updatedSubTasks,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.update(updated)
        return updated.toUiModel()
    }

    suspend fun deleteTask(taskId: Long): TaskEntity? {
        val existing = dao.findById(taskId) ?: return null
        dao.delete(existing)
        return existing
    }

    /**
     * Targeted quadrant update for batch moves: only quadrant and
     * updatedAtMillis change, no full-entity rewrite, no migration.
     */
    suspend fun moveTasksToQuadrant(taskIds: List<Long>, quadrant: QuadrantType): Int {
        if (taskIds.isEmpty()) return 0
        dao.updateQuadrant(taskIds, quadrant, System.currentTimeMillis())
        return taskIds.size
    }

    suspend fun findTaskById(taskId: Long): TaskUiModel? {
        return dao.findById(taskId)?.toUiModel()
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
        db.withTransaction {
            val existing = dao.findById(task.id) ?: return@withTransaction
            val now = System.currentTimeMillis()

            val updated = if (completed && existing.repeatRule.isCountBased()) {
                // Count-based: increment count, no alarm rescheduling
                existing.copy(
                    completed = true,
                    completionCount = existing.completionCount + 1,
                    lastCompletedAtMillis = now,
                    completionNote = existing.completionNote,
                    updatedAtMillis = now
                )
            } else if (completed && existing.repeatRule.isCycleBased() && existing.useAlarm && existing.dueAtMillis != null) {
                // Cycle-based with alarm: advance to next cycle
                val nextDue = RepeatReminderCalculator.nextDueAt(
                    existing.dueAtMillis,
                    existing.repeatRule,
                    repeatInterval = existing.repeatInterval,
                    repeatWeekdays = existing.repeatWeekdays
                )
                existing.copy(
                    completed = false,
                    lastCompletedAtMillis = now,
                    dueLabel = DateTimeFormatters.labelFor(midnightMillis(nextDue), minutesOfDay(nextDue)),
                    dueAtMillis = nextDue,
                    completionNote = existing.completionNote,
                    updatedAtMillis = now
                )
            } else if (completed && existing.repeatRule.isCycleBased()) {
                // Cycle-based without alarm: record completion timestamp
                existing.copy(
                    completed = true,
                    lastCompletedAtMillis = now,
                    completionNote = existing.completionNote,
                    updatedAtMillis = now
                )
            } else if (completed && existing.repeatRule == RepeatRule.None) {
                // Non-recurring: auto-archive
                existing.copy(
                    completed = true,
                    archived = true,
                    completionNote = existing.completionNote,
                    updatedAtMillis = now
                )
            } else {
                // Uncompleting: preserve note
                existing.copy(
                    completed = completed,
                    completionNote = existing.completionNote,
                    updatedAtMillis = now
                )
            }

            dao.update(updated)
        }
    }

    /**
     * Silent completion used by the swipe gesture: completes without a note
     * (no history row for non-recurring tasks, no dialog interruption). The
     * top notice's 写心得 action can add a note afterwards via
     * [saveCompletionNote], which handles the already-completed path.
     */
    suspend fun completeSilently(task: TaskUiModel): Boolean {
        var completed = false
        db.withTransaction {
            val entity = dao.findById(task.id) ?: return@withTransaction
            val current = entity.toUiModel()
            if (!canApplySilentCompletion(task, current)) return@withTransaction
            val now = System.currentTimeMillis()
            dao.update(completedEntity(entity, now))
            completed = true
        }
        return completed
    }

    private fun completedEntity(existing: TaskEntity, now: Long): TaskEntity {
        return if (existing.repeatRule.isCountBased()) {
            existing.copy(
                completed = true,
                completionCount = existing.completionCount + 1,
                lastCompletedAtMillis = now,
                updatedAtMillis = now
            )
        } else if (existing.repeatRule.isCycleBased() && existing.useAlarm && existing.dueAtMillis != null) {
            val nextDue = RepeatReminderCalculator.nextDueAt(
                existing.dueAtMillis,
                existing.repeatRule,
                repeatInterval = existing.repeatInterval,
                repeatWeekdays = existing.repeatWeekdays
            )
            existing.copy(
                completed = false,
                lastCompletedAtMillis = now,
                dueLabel = DateTimeFormatters.labelFor(midnightMillis(nextDue), minutesOfDay(nextDue)),
                dueAtMillis = nextDue,
                updatedAtMillis = now
            )
        } else if (existing.repeatRule.isCycleBased()) {
            existing.copy(
                completed = true,
                lastCompletedAtMillis = now,
                updatedAtMillis = now
            )
        } else {
            existing.copy(
                completed = true,
                archived = true,
                updatedAtMillis = now
            )
        }
    }

    suspend fun saveCompletionNote(taskId: Long, note: String): TaskEntity? {
        var result: TaskEntity? = null
        db.withTransaction {
            val existing = dao.findById(taskId) ?: return@withTransaction
            val now = System.currentTimeMillis()
            val trimmedNote = note.trim().ifBlank { null }

            // An already completed task uses this dialog to edit its existing
            // note. Do not create another history row or increment its count.
            if (existing.completed) {
                val updated = existing.copy(
                    completionNote = trimmedNote,
                    updatedAtMillis = now
                )
                dao.update(updated)
                result = updated
                return@withTransaction
            }

            // Create completion record for all recurring tasks
            if (existing.repeatRule != RepeatRule.None) {
                completionDao.insert(
                    TaskCompletionEntity(
                        taskId = taskId,
                        completedAtMillis = now,
                        note = trimmedNote
                    )
                )
            }

            val updated = if (existing.repeatRule.isCountBased()) {
                // Count-based: increment count, no alarm rescheduling
                existing.copy(
                    completed = true,
                    completionCount = existing.completionCount + 1,
                    lastCompletedAtMillis = now,
                    completionNote = trimmedNote,
                    updatedAtMillis = now
                )
            } else if (existing.repeatRule.isCycleBased() && existing.useAlarm && existing.dueAtMillis != null) {
                // Cycle-based with alarm: advance to next cycle
                val nextDue = RepeatReminderCalculator.nextDueAt(
                    existing.dueAtMillis,
                    existing.repeatRule,
                    repeatInterval = existing.repeatInterval,
                    repeatWeekdays = existing.repeatWeekdays
                )
                existing.copy(
                    completed = false,
                    completionCount = existing.completionCount + 1,
                    lastCompletedAtMillis = now,
                    dueLabel = DateTimeFormatters.labelFor(midnightMillis(nextDue), minutesOfDay(nextDue)),
                    dueAtMillis = nextDue,
                    completionNote = trimmedNote,
                    updatedAtMillis = now
                )
            } else if (existing.repeatRule.isCycleBased()) {
                // Cycle-based without alarm: mark completed, increment count
                existing.copy(
                    completed = true,
                    completionCount = existing.completionCount + 1,
                    lastCompletedAtMillis = now,
                    completionNote = trimmedNote,
                    updatedAtMillis = now
                )
            } else if (existing.repeatRule == RepeatRule.None) {
                // Non-recurring: auto-archive
                existing.copy(
                    completed = true,
                    archived = true,
                    completionNote = trimmedNote,
                    updatedAtMillis = now
                )
            } else {
                // Fallback: normal completion
                existing.copy(
                    completed = true,
                    completionNote = trimmedNote,
                    updatedAtMillis = now
                )
            }

            dao.update(updated)
            result = updated
        }
        return result
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
        // DAO-level filter keeps the in-memory load to completed cycle tasks
        // instead of the whole table; the SQL rule list must stay in sync
        // with RepeatRule.isCycleBased(). The period check itself stays in
        // Kotlin because week/month boundaries need zone-aware date math.
        val expiredTasks = dao.findCompletedCycleTasks().filter { task ->
            val lastCompleted = task.lastCompletedAtMillis ?: return@filter false
            !isInSamePeriod(lastCompleted, nowMillis, task.repeatRule)
        }
        if (expiredTasks.isEmpty()) return

        // Targeted bulk update (no full-entity rewrite, consistent with
        // moveTasksToQuadrant) inside one transaction so a mid-reset crash
        // can never leave half the cycle reset.
        db.withTransaction {
            dao.resetCompleted(expiredTasks.map { it.id }, nowMillis)
        }
    }

    suspend fun exportBackupJson(): String {
        return TaskBackupCodec.encode(dao.findAllTasks(), completionDao.findAll())
    }

    /**
     * Recomputes reminders after a reboot or backup import: recurring tasks
     * whose due date has passed advance to the next future occurrence, and
     * every open task with a local reminder is returned for rescheduling.
     */
    suspend fun restoreScheduledReminders(nowMillis: Long = System.currentTimeMillis()): List<TaskUiModel> {
        return dao.findActiveReminderTasks().mapNotNull { task ->
            val dueAt = task.dueAtMillis ?: return@mapNotNull null
            when {
                task.repeatRule != RepeatRule.None && dueAt <= nowMillis -> {
                    val nextDue = RepeatReminderCalculator.nextFutureDueAfter(
                        dueAt,
                        task.repeatRule,
                        nowMillis,
                        repeatInterval = task.repeatInterval,
                        repeatWeekdays = task.repeatWeekdays
                    )
                    updateTaskDue(task, nextDue).toUiModel()
                }
                dueAt > nowMillis -> task.toUiModel()
                else -> null
            }
        }
    }

    /**
     * Advances a recurring task to its next cycle after its reminder fired;
     * returns the updated task so the caller can reschedule it.
     */
    suspend fun advanceRepeatAfterReminder(taskId: Long): TaskUiModel? {
        val existing = dao.findById(taskId) ?: return null
        val currentDue = existing.dueAtMillis ?: return null
        if (existing.repeatRule == RepeatRule.None || !existing.useLocalReminder || existing.completed) return null
        val nextDue = RepeatReminderCalculator.nextFutureDueAfter(
            currentMillis = currentDue,
            repeatRule = existing.repeatRule,
            nowMillis = System.currentTimeMillis(),
            repeatInterval = existing.repeatInterval,
            repeatWeekdays = existing.repeatWeekdays
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

    suspend fun importBackupJson(json: String): Int {
        val (tasks, completions) = TaskBackupCodec.decodeForImport(json)
        db.withTransaction {
            dao.deleteAll()
            completionDao.deleteAll()
            dao.insertAll(tasks)
            if (completions.isNotEmpty()) {
                completionDao.insertAll(completions)
            }
        }
        return tasks.size
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

    private fun normalizedSubTasks(items: List<TaskSubTask>): List<TaskSubTask> {
        val usedIds = mutableSetOf<Long>()
        var nextId = (items.maxOfOrNull { it.id } ?: 0L) + 1L
        return items.mapNotNull { item ->
            val title = item.title.trim()
            if (title.isBlank()) {
                null
            } else {
                val id = if (item.id > 0 && usedIds.add(item.id)) {
                    item.id
                } else {
                    while (!usedIds.add(nextId)) {
                        nextId += 1L
                    }
                    nextId++
                }
                item.copy(id = id, title = title)
            }
        }
    }

    suspend fun reorderTasks(tasks: List<TaskUiModel>) {
        db.withTransaction {
            tasks.forEachIndexed { index, task ->
                dao.updateDisplayOrder(task.id, index)
            }
        }
    }

    fun observeCompletions(taskId: Long): Flow<List<TaskCompletionEntity>> {
        return completionDao.observeCompletions(taskId)
    }

    /**
     * Deletes a completion record and returns the removed entity so the caller
     * can offer an undo window; [restoreCompletion] re-inserts it unchanged.
     */
    suspend fun deleteCompletionForUndo(completionId: Long): TaskCompletionEntity? {
        val removed = db.withTransaction {
            val completion = completionDao.findById(completionId) ?: return@withTransaction null
            completionDao.deleteById(completionId)
            val actualCount = completionDao.countForTask(completion.taskId)
            dao.updateCompletionCount(completion.taskId, actualCount)
            completion
        }
        return removed
    }

    suspend fun restoreCompletion(completion: TaskCompletionEntity) {
        db.withTransaction {
            completionDao.insert(completion)
            val actualCount = completionDao.countForTask(completion.taskId)
            dao.updateCompletionCount(completion.taskId, actualCount)
        }
    }

    /**
     * Re-inserts a previously deleted task with its original id, order, and
     * fields — the undo path for [deleteTask].
     */
    suspend fun restoreTask(task: TaskEntity) {
        dao.insert(task)
    }

    suspend fun updateCompletionNote(completionId: Long, note: String?) {
        completionDao.updateNote(completionId, note?.trim()?.takeIf { it.isNotBlank() })
    }

    suspend fun canCompleteInCurrentCycle(task: TaskUiModel): Boolean {
        if (task.repeatRule == RepeatRule.None) return !task.completed
        if (task.repeatRule.isCountBased()) return true

        // For cycle-based: check if last completion was in current cycle
        val lastCompleted = task.lastCompletedAtMillis ?: return true
        val now = System.currentTimeMillis()
        return !isInSamePeriod(lastCompleted, now, task.repeatRule)
    }

    /**
     * Whether two timestamps fall into the same repeat period (same day /
     * ISO week / month / year, in the system zone). Single implementation
     * behind both resetExpiredCycleTasks and canCompleteInCurrentCycle — the
     * two call sites previously carried byte-identical copies that could
     * drift apart.
     *
     * Weekly keys pair ISO weekOfWeekBasedYear with weekBasedYear (not the
     * calendar year): Dec 30 and Jan 3 can sit in the same ISO week whose
     * week-based year is the *next* calendar year, and the calendar-year key
     * would split that week in two.
     */
    private fun isInSamePeriod(millis1: Long, millis2: Long, repeatRule: RepeatRule): Boolean {
        val zone = ZoneId.systemDefault()
        val date1 = Instant.ofEpochMilli(millis1).atZone(zone).toLocalDate()
        val date2 = Instant.ofEpochMilli(millis2).atZone(zone).toLocalDate()

        return when (repeatRule) {
            RepeatRule.Daily -> date1.isEqual(date2)
            RepeatRule.Weekly -> {
                // Same formula as RepeatReminderCalculator: weekOfWeekBasedYear
                // pairs with weekBasedYear (not the calendar year), so the ISO
                // week spanning New Year's Eve stays one period instead of
                // being split across the year boundary.
                val weekFields = java.time.temporal.WeekFields.ISO
                date1.get(weekFields.weekBasedYear()) == date2.get(weekFields.weekBasedYear()) &&
                    date1.get(weekFields.weekOfWeekBasedYear()) == date2.get(weekFields.weekOfWeekBasedYear())
            }
            RepeatRule.Monthly -> date1.year == date2.year && date1.monthValue == date2.monthValue
            RepeatRule.Yearly -> date1.year == date2.year
            else -> false
        }
    }
}
