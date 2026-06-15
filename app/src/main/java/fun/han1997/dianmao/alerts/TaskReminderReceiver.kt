package `fun`.han1997.dianmao.alerts

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import `fun`.han1997.dianmao.R
import `fun`.han1997.dianmao.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val notification = NotificationCompat.Builder(context, TaskNotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("DianMao 提醒")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        }

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
    }
}
