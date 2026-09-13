package `fun`.han1997.dianmao.backup

import android.content.Context

data class WebDavSettings(
    val url: String = "",
    val username: String = "",
    val password: String = ""
) {
    val isConfigured: Boolean
        get() = url.isNotBlank()
}

data class AutoBackupSettings(
    val enabled: Boolean = false,
    val lastAutoBackupAtMillis: Long = 0L,
    val lastAutoBackupSucceeded: Boolean = true
)

class BackupSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("backup_settings", Context.MODE_PRIVATE)

    fun loadWebDavSettings(): WebDavSettings {
        return WebDavSettings(
            url = preferences.getString(KEY_URL, "").orEmpty(),
            username = preferences.getString(KEY_USERNAME, "").orEmpty(),
            password = preferences.getString(KEY_PASSWORD, "").orEmpty()
        )
    }

    fun saveWebDavSettings(settings: WebDavSettings) {
        preferences.edit()
            .putString(KEY_URL, settings.url.trim())
            .putString(KEY_USERNAME, settings.username.trim())
            .putString(KEY_PASSWORD, settings.password)
            .apply()
    }

    fun loadAutoBackupSettings(): AutoBackupSettings {
        return AutoBackupSettings(
            enabled = preferences.getBoolean(KEY_AUTO_BACKUP_ENABLED, false),
            lastAutoBackupAtMillis = preferences.getLong(KEY_LAST_AUTO_BACKUP_AT, 0L),
            lastAutoBackupSucceeded = preferences.getBoolean(KEY_LAST_AUTO_BACKUP_OK, true)
        )
    }

    fun saveAutoBackupEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply()
    }

    fun recordAutoBackupResult(atMillis: Long, succeeded: Boolean) {
        preferences.edit()
            .putLong(KEY_LAST_AUTO_BACKUP_AT, atMillis)
            .putBoolean(KEY_LAST_AUTO_BACKUP_OK, succeeded)
            .apply()
    }

    private companion object {
        const val KEY_URL = "webdav_url"
        const val KEY_USERNAME = "webdav_username"
        const val KEY_PASSWORD = "webdav_password"
        const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        const val KEY_LAST_AUTO_BACKUP_AT = "last_auto_backup_at"
        const val KEY_LAST_AUTO_BACKUP_OK = "last_auto_backup_succeeded"
    }
}
