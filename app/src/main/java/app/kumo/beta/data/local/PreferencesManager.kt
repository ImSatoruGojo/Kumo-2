package app.kumo.beta.data.local

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode {
    DARK, LIGHT, SYSTEM, AMOLED
}

enum class AccentColorOption(val displayName: String, val hexColor: Long) {
    PURPLE("Kumo Purple", 0xFF6D4AFF),
    ELECTRIC_BLUE("Electric Blue", 0xFF00B2FF),
    CRIMSON_RED("Crimson Red", 0xFFFF2A55),
    EMERALD_GREEN("Emerald Green", 0xFF00E676),
    SUNSET_ORANGE("Sunset Orange", 0xFFFF6D00),
    SAKURA_PINK("Sakura Pink", 0xFFFF4081)
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kumo_preferences", Context.MODE_PRIVATE)

    // Appearance
    var themeMode: AppThemeMode
        get() {
            val name = prefs.getString("theme_mode", AppThemeMode.DARK.name)
            return try { AppThemeMode.valueOf(name ?: AppThemeMode.DARK.name) } catch (e: Exception) { AppThemeMode.DARK }
        }
        set(value) = prefs.edit().putString("theme_mode", value.name).apply()

    var accentColor: AccentColorOption
        get() {
            val name = prefs.getString("accent_color", AccentColorOption.PURPLE.name)
            return try { AccentColorOption.valueOf(name ?: AccentColorOption.PURPLE.name) } catch (e: Exception) { AccentColorOption.PURPLE }
        }
        set(value) = prefs.edit().putString("accent_color", value.name).apply()

    var isCompactMode: Boolean
        get() = prefs.getBoolean("compact_mode", false)
        set(value) = prefs.edit().putBoolean("compact_mode", value).apply()

    // General
    var appLanguage: String
        get() = prefs.getString("app_language", "System Default") ?: "System Default"
        set(value) = prefs.edit().putString("app_language", value).apply()

    var defaultHomeTab: String
        get() = prefs.getString("default_home_tab", "Home") ?: "Home"
        set(value) = prefs.edit().putString("default_home_tab", value).apply()

    var dataSavingEnabled: Boolean
        get() = prefs.getBoolean("data_saving", false)
        set(value) = prefs.edit().putBoolean("data_saving", value).apply()

    // Home Sections
    var showContinueWatching: Boolean
        get() = prefs.getBoolean("sec_continue_watching", true)
        set(value) = prefs.edit().putBoolean("sec_continue_watching", value).apply()

    var showPopular: Boolean
        get() = prefs.getBoolean("sec_popular", true)
        set(value) = prefs.edit().putBoolean("sec_popular", value).apply()

    var showTrending: Boolean
        get() = prefs.getBoolean("sec_trending", true)
        set(value) = prefs.edit().putBoolean("sec_trending", value).apply()

    var showRecentlyUpdated: Boolean
        get() = prefs.getBoolean("sec_recently_updated", true)
        set(value) = prefs.edit().putBoolean("sec_recently_updated", value).apply()

    var showTopRated: Boolean
        get() = prefs.getBoolean("sec_top_rated", true)
        set(value) = prefs.edit().putBoolean("sec_top_rated", value).apply()

    var showNewReleases: Boolean
        get() = prefs.getBoolean("sec_new_releases", true)
        set(value) = prefs.edit().putBoolean("sec_new_releases", value).apply()

    var showSeasonal: Boolean
        get() = prefs.getBoolean("sec_seasonal", true)
        set(value) = prefs.edit().putBoolean("sec_seasonal", value).apply()

    // Player Settings
    var defaultQuality: String
        get() = prefs.getString("player_quality", "Auto") ?: "Auto"
        set(value) = prefs.edit().putString("player_quality", value).apply()

    var playbackSpeed: String
        get() = prefs.getString("player_speed", "1.0x") ?: "1.0x"
        set(value) = prefs.edit().putString("player_speed", value).apply()

    var rememberPlaybackSpeed: Boolean
        get() = prefs.getBoolean("player_remember_speed", true)
        set(value) = prefs.edit().putBoolean("player_remember_speed", value).apply()

    var autoplayNext: Boolean
        get() = prefs.getBoolean("player_autoplay_next", true)
        set(value) = prefs.edit().putBoolean("player_autoplay_next", value).apply()

    var doubleTapSeekSeconds: Int
        get() = prefs.getInt("player_double_tap_seek", 10)
        set(value) = prefs.edit().putInt("player_double_tap_seek", value).apply()

    var preferredAudioLang: String
        get() = prefs.getString("player_audio_lang", "Japanese") ?: "Japanese"
        set(value) = prefs.edit().putString("player_audio_lang", value).apply()

    var preferredSubLang: String
        get() = prefs.getString("player_sub_lang", "English") ?: "English"
        set(value) = prefs.edit().putString("player_sub_lang", value).apply()

    var decoderType: String
        get() = prefs.getString("player_decoder", "Auto") ?: "Auto"
        set(value) = prefs.edit().putString("player_decoder", value).apply()

    // Library
    var defaultLibrarySort: String
        get() = prefs.getString("library_sort", "Title") ?: "Title"
        set(value) = prefs.edit().putString("library_sort", value).apply()

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
