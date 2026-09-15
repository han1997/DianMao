package `fun`.han1997.dianmao.alerts

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import `fun`.han1997.dianmao.MainActivity
import `fun`.han1997.dianmao.R
import `fun`.han1997.dianmao.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val leadMinutes = intent.getIntExtra(EXTRA_LEAD_MINUTES, 0)
        val isRecurring = intent.getBooleanExtra(EXTRA_IS_RECURRING, false)
        val contentIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompatFlags.immutable()
        )
        val completeLabel = if (isRecurring) "记一次完成" else "完成"
        val notification = NotificationCompat.Builder(context, TaskNotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("DianMao 提醒")
            .setContentText(title)
            .setContentIntent(contentIntent)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_action_complete,
                    completeLabel,
                    reminderActionPendingIntent(
                        context,
                        taskId,
                        title,
                        leadMinutes,
                        isRecurring,
                        ReminderActionKind.Complete
                    )
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_action_snooze,
                    "稍后提醒",
                    reminderActionPendingIntent(
                        context,
                        taskId,
                        title,
                        leadMinutes,
                        isRecurring,
                        ReminderActionKind.Snooze
                    )
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(reminderRequestCode(taskId, leadMinutes), notification)
        }

        // Recurring tasks advance to their next cycle after the reminder fires.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val repository = TaskRepository(context.applicationContext)
            val nextTask = repository.advanceRepeatAfterReminder(taskId)
            if (nextTask != null) {
                ReminderScheduler(context.applicationContext).schedule(nextTask)
            }
            pendingResult.finish()
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_LEAD_MINUTES = "extra_lead_minutes"
        const val EXTRA_IS_RECURRING = "extra_is_recurring"
    }
}

private fun reminderActionPendingIntent(
    context: Context,
    taskId: Long,
    title: String,
    leadMinutes: Int,
    isRecurring: Boolean,
    kind: ReminderActionKind
): PendingIntent {
    val intent = Intent(context, TaskActionReceiver::class.java).apply {
        action = when (kind) {
            ReminderActionKind.Complete -> TaskActionReceiver.ACTION_COMPLETE
            ReminderActionKind.Snooze -> TaskActionReceiver.ACTION_SNOOZE
        }
        putExtra(TaskReminderReceiver.EXTRA_TITLE, title)
        putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
        putExtra(TaskReminderReceiver.EXTRA_LEAD_MINUTES, leadMinutes)
        putExtra(TaskReminderReceiver.EXTRA_IS_RECURRING, isRecurring)
    }
    return PendingIntent.getBroadcast(
        context,
        reminderActionRequestCode(taskId, leadMinutes, kind),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompatFlags.immutable()
    )
}
