package `fun`.han1997.dianmao.appearance

import android.content.Context

/**
 * Prototype-first: the confirmed prototype exposes a three-way appearance
 * setting (follow system / light / dark) on the two-level settings hub. The
 * app previously only honoured [androidx.compose.foundation.isSystemInDarkTheme],
 * so this preference persists the user's explicit choice locally.
 *
 * The value intentionally lives in its own SharedPreferences file rather than
 * the backup store: the prototype states "主题偏好保存在本机,不随备份上传",
 * so it must never travel with a backup payload.
 */
enum class ThemeMode {
    System,
    Light,
    Dark;

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: System
    }
}

class AppearanceSettingsStore(context: Context) {
    private val preferences =
        context.getSharedPreferences("appearance_settings", Context.MODE_PRIVATE)

    fun loadThemeMode(): ThemeMode =
        ThemeMode.fromKey(preferences.getString(KEY_THEME_MODE, null))

    fun saveThemeMode(mode: ThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}