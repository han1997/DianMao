package `fun`.han1997.dianmao.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import `fun`.han1997.dianmao.R

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val notification = NotificationCompat.Builder(context, TaskNotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("DianMao 提醒")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(intent.getIntExtra(EXTRA_TASK_ID, 0), notification)
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}

