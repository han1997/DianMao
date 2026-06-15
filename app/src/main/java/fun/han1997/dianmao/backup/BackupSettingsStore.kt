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

    private companion object {
        const val KEY_URL = "webdav_url"
        const val KEY_USERNAME = "webdav_username"
        const val KEY_PASSWORD = "webdav_password"
    }
}
