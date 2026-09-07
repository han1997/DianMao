package `fun`.han1997.dianmao.alerts

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

object TaskNotificationChannels {
    const val REMINDERS = "task_reminders"

    fun ensure(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        val channel = NotificationChannelCompat.Builder(
            REMINDERS,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName("事项提醒")
            .setDescription("事项到点提醒")
            .build()
        manager.createNotificationChannel(channel)
    }
}
