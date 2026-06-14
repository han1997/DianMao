package `fun`.han1997.dianmao.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object TaskNotificationChannels {
    const val REMINDERS = "task_reminders"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            REMINDERS,
            "任务提醒",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }
}

