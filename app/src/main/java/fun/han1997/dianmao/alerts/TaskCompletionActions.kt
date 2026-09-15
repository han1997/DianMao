package `fun`.han1997.dianmao.alerts

import android.content.Context
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskUiModel

/**
 * Completes [task] silently and settles its alarm side effects: cancels the
 * task's current alarms, then re-schedules the next cycle for recurring
 * tasks from the persisted post-completion state so the following reminders
 * track the advanced due date. Callers own the widget refresh.
 */
suspend fun completeTaskSilently(context: Context, task: TaskUiModel) {
    val appContext = context.applicationContext
    val repository = TaskRepository(appContext)
    val scheduler = ReminderScheduler(appContext)
    repository.completeSilently(task)
    if (task.useLocalReminder && task.dueAtMillis != null) {
        scheduler.cancel(task.id, task.reminderLeadMinutes)
        val updated = repository.findTaskById(task.id)
        if (updated != null && updated.repeatRule != RepeatRule.None) {
            scheduler.schedule(updated)
        }
    }
}
