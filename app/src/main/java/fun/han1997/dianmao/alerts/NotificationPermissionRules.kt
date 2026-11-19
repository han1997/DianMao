package `fun`.han1997.dianmao.alerts

internal fun canPostTestNotification(
    runtimePermissionGranted: Boolean,
    appNotificationsEnabled: Boolean,
    reminderChannelEnabled: Boolean
): Boolean = runtimePermissionGranted && appNotificationsEnabled && reminderChannelEnabled
