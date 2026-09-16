package `fun`.han1997.dianmao.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.widget.refreshHomeWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, 0L)
        if (taskId <= 0L) return
        val title = intent.getStringExtra(TaskReminderReceiver.EXTRA_TITLE).orEmpty()
        val leadMinutes = intent.getIntExtra(TaskReminderReceiver.EXTRA_LEAD_MINUTES, 0)
        val isRecurring = intent.getBooleanExtra(TaskReminderReceiver.EXTRA_IS_RECURRING, false)
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_COMPLETE -> {
                        val task = TaskRepository(appContext).findTaskById(taskId)
                        if (task != null) {
                            if (completeTaskSilently(appContext, task)) {
                                refreshHomeWidget(appContext)
                            }
                        }
                        cancelReminderNotifications(
                            appContext,
                            taskId,
                            task?.reminderLeadMinutes ?: setOf(leadMinutes)
                        )
                    }
                    ACTION_SNOOZE -> {
                        ReminderScheduler(appContext).snooze(taskId, title, leadMinutes, isRecurring)
                        NotificationManagerCompat.from(appContext)
                            .cancel(reminderRequestCode(taskId, leadMinutes))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "fun.han1997.dianmao.action.COMPLETE_REMINDER"
        const val ACTION_SNOOZE = "fun.han1997.dianmao.action.SNOOZE_REMINDER"
    }
}

internal fun cancelReminderNotifications(context: Context, taskId: Long, leadMinutes: Set<Int>) {
    val manager = NotificationManagerCompat.from(context)
    leadMinutes.forEach { lead -> manager.cancel(reminderRequestCode(taskId, lead)) }
}
