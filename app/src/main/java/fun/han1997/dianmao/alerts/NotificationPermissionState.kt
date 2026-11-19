package `fun`.han1997.dianmao.alerts

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

internal data class NotificationPermissionState(
    val notificationsEnabled: Boolean,
    val exactAlarmsEnabled: Boolean
)

internal fun readNotificationPermissionState(context: Context): NotificationPermissionState {
    val notificationManager = NotificationManagerCompat.from(context)
    val channelEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.getNotificationChannel(TaskNotificationChannels.REMINDERS)
            ?.importance?.let { it != NotificationManager.IMPORTANCE_NONE } ?: false
    } else {
        true
    }
    val notificationsEnabled = notificationManager.areNotificationsEnabled() && channelEnabled
    val exactAlarmsEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    return NotificationPermissionState(notificationsEnabled, exactAlarmsEnabled)
}
