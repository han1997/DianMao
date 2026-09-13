package `fun`.han1997.dianmao.backup

import `fun`.han1997.dianmao.data.DateTimeFormatters

/** Auto backup runs at most once per day, on app launch. */
const val AUTO_BACKUP_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L

/**
 * Whether the app-start auto backup should run now: the switch must be on,
 * WebDAV must be configured, a failed attempt retries on the next start, and
 * a successful one waits for the interval to elapse.
 */
internal fun shouldAutoBackupNow(
    enabled: Boolean,
    webDavConfigured: Boolean,
    lastAutoBackupAtMillis: Long,
    lastAutoBackupSucceeded: Boolean,
    nowMillis: Long = System.currentTimeMillis(),
    intervalMillis: Long = AUTO_BACKUP_INTERVAL_MILLIS
): Boolean {
    if (!enabled || !webDavConfigured) return false
    if (!lastAutoBackupSucceeded) return true
    return nowMillis - lastAutoBackupAtMillis >= intervalMillis
}

/**
 * One-line status shown in the backup panel under the auto-backup switch.
 */
internal fun autoBackupStatusText(
    settings: AutoBackupSettings,
    webDavConfigured: Boolean
): String {
    if (!webDavConfigured) return "需先配置 WebDAV 地址"
    return when {
        !settings.enabled -> "开启后每次启动自动上传，最多每天一次"
        settings.lastAutoBackupAtMillis == 0L -> "等待下次启动时首次上传"
        settings.lastAutoBackupSucceeded -> "上次自动备份：${DateTimeFormatters.formatTimestamp(settings.lastAutoBackupAtMillis)}"
        else -> "上次自动备份失败，下次启动重试"
    }
}
