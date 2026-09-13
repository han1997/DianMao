package `fun`.han1997.dianmao.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import `fun`.han1997.dianmao.data.TaskUiModel

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /**
     * Schedules the local reminders for [task]: one exact alarm per selected
     * lead time (due moment pulled back by the lead). Tasks without a due
     * date or with the local reminder off are ignored.
     */
    fun schedule(task: TaskUiModel) {
        val dueAt = task.dueAtMillis ?: return
        if (!task.useLocalReminder) return
        task.reminderLeadMinutes.forEach { lead ->
            val triggerAt = dueAt - lead * 60_000L
            if (triggerAt <= System.currentTimeMillis()) return@forEach
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                putExtra(TaskReminderReceiver.EXTRA_TITLE, task.title)
                putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
                putExtra(TaskReminderReceiver.EXTRA_LEAD_MINUTES, lead)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderRequestCode(task.id, lead),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompatFlags.immutable()
            )
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
    }

    /**
     * Cancels every alarm scheduled for [taskId] across [leadMinutes]. The
     * caller passes the task's current effective lead set — the same set the
     * schedule path used.
     */
    fun cancel(taskId: Long, leadMinutes: Set<Int>) {
        leadMinutes.forEach { lead ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderRequestCode(taskId, lead),
                Intent(context, TaskReminderReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntentCompatFlags.immutable()
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}

/**
 * Stable request code distinct per (taskId, lead time). Long/Int hashCode and
 * Arrays.hashCode are value-based, so the code is deterministic across
 * process restarts — required for FLAG_UPDATE_CURRENT/NO_CREATE matching.
 */
internal fun reminderRequestCode(taskId: Long, leadMinutes: Int): Int {
    var result = taskId.hashCode()
    result = 31 * result + leadMinutes
    return result
}

internal object PendingIntentCompatFlags {
    fun immutable(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
}
