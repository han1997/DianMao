package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.backup.AUTO_BACKUP_INTERVAL_MILLIS
import `fun`.han1997.dianmao.backup.AutoBackupSettings
import `fun`.han1997.dianmao.backup.autoBackupStatusText
import `fun`.han1997.dianmao.backup.shouldAutoBackupNow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoBackupPolicyTest {
    private val now = 1_800_000_000_000L

    @Test
    fun disabledOrUnconfiguredNeverBacksUp() {
        assertFalse(
            shouldAutoBackupNow(
                enabled = false,
                webDavConfigured = true,
                lastAutoBackupAtMillis = 0L,
                lastAutoBackupSucceeded = true,
                nowMillis = now
            )
        )
        assertFalse(
            shouldAutoBackupNow(
                enabled = true,
                webDavConfigured = false,
                lastAutoBackupAtMillis = 0L,
                lastAutoBackupSucceeded = true,
                nowMillis = now
            )
        )
    }

    @Test
    fun firstStartWithSwitchOnBacksUpImmediately() {
        assertTrue(
            shouldAutoBackupNow(
                enabled = true,
                webDavConfigured = true,
                lastAutoBackupAtMillis = 0L,
                lastAutoBackupSucceeded = true,
                nowMillis = now
            )
        )
    }

    @Test
    fun withinIntervalDoesNotRepeat() {
        assertFalse(
            shouldAutoBackupNow(
                enabled = true,
                webDavConfigured = true,
                lastAutoBackupAtMillis = now - AUTO_BACKUP_INTERVAL_MILLIS / 2,
                lastAutoBackupSucceeded = true,
                nowMillis = now
            )
        )
    }

    @Test
    fun intervalElapsedBacksUp() {
        assertTrue(
            shouldAutoBackupNow(
                enabled = true,
                webDavConfigured = true,
                lastAutoBackupAtMillis = now - AUTO_BACKUP_INTERVAL_MILLIS - 1,
                lastAutoBackupSucceeded = true,
                nowMillis = now
            )
        )
    }

    @Test
    fun failedAttemptRetriesOnNextStart() {
        assertTrue(
            shouldAutoBackupNow(
                enabled = true,
                webDavConfigured = true,
                lastAutoBackupAtMillis = now - 60_000L,
                lastAutoBackupSucceeded = false,
                nowMillis = now
            )
        )
    }

    @Test
    fun statusTextReflectsSettingsAndConfig() {
        val unconfigured = autoBackupStatusText(AutoBackupSettings(enabled = true), webDavConfigured = false)
        assertEquals("需先配置 WebDAV 地址", unconfigured)

        val disabled = autoBackupStatusText(AutoBackupSettings(enabled = false), webDavConfigured = true)
        assertEquals("开启后每次启动自动上传，最多每天一次", disabled)

        val waiting = autoBackupStatusText(AutoBackupSettings(enabled = true), webDavConfigured = true)
        assertEquals("等待下次启动时首次上传", waiting)

        val succeeded = autoBackupStatusText(
            AutoBackupSettings(enabled = true, lastAutoBackupAtMillis = now, lastAutoBackupSucceeded = true),
            webDavConfigured = true
        )
        assertTrue(succeeded.startsWith("上次自动备份："))

        val failed = autoBackupStatusText(
            AutoBackupSettings(enabled = true, lastAutoBackupAtMillis = now, lastAutoBackupSucceeded = false),
            webDavConfigured = true
        )
        assertEquals("上次自动备份失败，下次启动重试", failed)
    }
}
