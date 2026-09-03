package app.kumo.beta.data.local

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode {
    DARK, AMOLED, LIGHT, SYSTEM
}

enum class AccentColorOption(val title: String, val hexColor: Long) {
    PURPLE("Kumo Purple", 0xFF6D4AFF),
    BLUE("Ocean Blue", 0xFF2196F3),
    CYAN("Neon Cyan", 0xFF00E5FF),
    RED("Crimson Red", 0xFFFF2D55),
    GREEN("Emerald Green", 0xFF00E676),
    ORANGE("Sunset Orange", 0xFFFF9100),
    PINK("Sakura Pink", 0xFFFF4081)
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kumo_settings", Context.MODE_PRIVATE)

    var appTheme: AppThemeMode
        get() {
            val name = prefs.getString("app_theme", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
            return try { AppThemeMode.valueOf(name) } catch (e: Exception) { AppThemeMode.DARK }
        }
        set(value) {
            prefs.edit().putString("app_theme", value.name).apply()
        }

    var themeMode: AppThemeMode
        get() = appTheme
        set(value) { appTheme = value }

    var accentColor: AccentColorOption
        get() {
            val name = prefs.getString("accent_color", AccentColorOption.PURPLE.name) ?: AccentColorOption.PURPLE.name
            return try { AccentColorOption.valueOf(name) } catch (e: Exception) { AccentColorOption.PURPLE }
        }
        set(value) {
            prefs.edit().putString("accent_color", value.name).apply()
        }

    var customHexColor: String
        get() = prefs.getString("custom_hex_color", "#7C4DFF") ?: "#7C4DFF"
        set(value) {
            prefs.edit().putString("custom_hex_color", value).apply()
        }

    var useCustomHex: Boolean
        get() = prefs.getBoolean("use_custom_hex", false)
        set(value) {
            prefs.edit().putBoolean("use_custom_hex", value).apply()
        }

    var appLanguage: String
        get() = prefs.getString("app_language", "English") ?: "English"
        set(value) {
            prefs.edit().putString("app_language", value).apply()
        }

    var dataSavingEnabled: Boolean
        get() = prefs.getBoolean("data_saving", false)
        set(value) {
            prefs.edit().putBoolean("data_saving", value).apply()
        }

    var showContinueWatching: Boolean
        get() = prefs.getBoolean("show_continue_watching", true)
        set(value) {
            prefs.edit().putBoolean("show_continue_watching", value).apply()
        }

    var showPopular: Boolean
        get() = prefs.getBoolean("show_popular", true)
        set(value) {
            prefs.edit().putBoolean("show_popular", value).apply()
        }

    var showTrending: Boolean
        get() = prefs.getBoolean("show_trending", true)
        set(value) {
            prefs.edit().putBoolean("show_trending", value).apply()
        }

    var showTopRated: Boolean
        get() = prefs.getBoolean("show_top_rated", true)
        set(value) {
            prefs.edit().putBoolean("show_top_rated", value).apply()
        }

    var showNewReleases: Boolean
        get() = prefs.getBoolean("show_new_releases", true)
        set(value) {
            prefs.edit().putBoolean("show_new_releases", value).apply()
        }

    var incognitoMode: Boolean
        get() = prefs.getBoolean("incognito_mode", false)
        set(value) {
            prefs.edit().putBoolean("incognito_mode", value).apply()
        }

    var autoUpdates: Boolean
        get() = prefs.getBoolean("auto_updates", true)
        set(value) {
            prefs.edit().putBoolean("auto_updates", value).apply()
        }

    var useHlsPlayer: Boolean
        get() = prefs.getBoolean("use_hls_player", true)
        set(value) {
            prefs.edit().putBoolean("use_hls_player", value).apply()
        }

    var autoSkipIntro: Boolean
        get() = prefs.getBoolean("auto_skip_intro", false)
        set(value) {
            prefs.edit().putBoolean("auto_skip_intro", value).apply()
        }

    var autoplayNext: Boolean
        get() = prefs.getBoolean("autoplay_next", true)
        set(value) {
            prefs.edit().putBoolean("autoplay_next", value).apply()
        }

    var playerScreenLock: Boolean
        get() = prefs.getBoolean("player_screen_lock", true)
        set(value) {
            prefs.edit().putBoolean("player_screen_lock", value).apply()
        }

    var nsfwEnabled: Boolean
        get() = prefs.getBoolean("nsfw_enabled", false)
        set(value) {
            prefs.edit().putBoolean("nsfw_enabled", value).apply()
        }

    var showSpoilerWarnings: Boolean
        get() = prefs.getBoolean("show_spoiler_warnings", true)
        set(value) {
            prefs.edit().putBoolean("show_spoiler_warnings", value).apply()
        }

    var preferredQuality: String
        get() = prefs.getString("preferred_quality", "1080p") ?: "1080p"
        set(value) {
            prefs.edit().putString("preferred_quality", value).apply()
        }

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
