package `fun`.han1997.dianmao.alerts

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import `fun`.han1997.dianmao.R

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

internal fun postTestNotification(context: Context): Boolean {
    val manager = NotificationManagerCompat.from(context)
    val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    val channelEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
        manager.getNotificationChannel(TaskNotificationChannels.REMINDERS)
            ?.importance?.let { it != NotificationManager.IMPORTANCE_NONE } == true
    if (!canPostTestNotification(runtimePermissionGranted, manager.areNotificationsEnabled(), channelEnabled)) return false

    val message = "通知已正常显示。到点提醒会通过此通知渠道送达。"
    val notification = NotificationCompat.Builder(context, TaskNotificationChannels.REMINDERS)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("DianMao 测试通知")
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
    return try {
        manager.notify(TEST_NOTIFICATION_TAG, TEST_NOTIFICATION_ID, notification)
        true
    } catch (_: SecurityException) {
        false
    }
}
private const val TEST_NOTIFICATION_TAG = "fun.han1997.dianmao.TEST_NOTIFICATION"
private const val TEST_NOTIFICATION_ID = Int.MIN_VALUE
