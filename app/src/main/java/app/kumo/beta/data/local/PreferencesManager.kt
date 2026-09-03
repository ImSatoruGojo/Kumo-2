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

    var startupPage: String
        get() = prefs.getString("startup_page", "Home") ?: "Home"
        set(value) {
            prefs.edit().putString("startup_page", value).apply()
        }

    var rememberLastScreen: Boolean
        get() = prefs.getBoolean("remember_last_screen", true)
        set(value) {
            prefs.edit().putBoolean("remember_last_screen", value).apply()
        }

    var confirmExit: Boolean
        get() = prefs.getBoolean("confirm_exit", false)
        set(value) {
            prefs.edit().putBoolean("confirm_exit", value).apply()
        }

    var dataSavingEnabled: Boolean
        get() = prefs.getBoolean("data_saving", false)
        set(value) {
            prefs.edit().putBoolean("data_saving", value).apply()
        }

    // Home Screen Customization
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

    var showRecentlyAdded: Boolean
        get() = prefs.getBoolean("show_recently_added", true)
        set(value) {
            prefs.edit().putBoolean("show_recently_added", value).apply()
        }

    var showRecommended: Boolean
        get() = prefs.getBoolean("show_recommended", true)
        set(value) {
            prefs.edit().putBoolean("show_recommended", value).apply()
        }

    var homeFilterMode: String
        get() = prefs.getString("home_filter_mode", "ALL") ?: "ALL"
        set(value) {
            prefs.edit().putString("home_filter_mode", value).apply()
        }

    // Content Preferences
    var preferredAnimeLang: String
        get() = prefs.getString("pref_anime_lang", "Japanese (Sub)") ?: "Japanese (Sub)"
        set(value) {
            prefs.edit().putString("pref_anime_lang", value).apply()
        }

    var preferredMovieLang: String
        get() = prefs.getString("pref_movie_lang", "English") ?: "English"
        set(value) {
            prefs.edit().putString("pref_movie_lang", value).apply()
        }

    var dubSubPriority: String
        get() = prefs.getString("dub_sub_priority", "SUB") ?: "SUB"
        set(value) {
            prefs.edit().putString("dub_sub_priority", value).apply()
        }

    var providerPriority: String
        get() = prefs.getString("provider_priority", "Auto Select") ?: "Auto Select"
        set(value) {
            prefs.edit().putString("provider_priority", value).apply()
        }

    var autoProviderFallback: Boolean
        get() = prefs.getBoolean("auto_provider_fallback", true)
        set(value) {
            prefs.edit().putBoolean("auto_provider_fallback", value).apply()
        }

    // Player Preferences
    var autoSkipIntro: Boolean
        get() = prefs.getBoolean("auto_skip_intro", false)
        set(value) {
            prefs.edit().putBoolean("auto_skip_intro", value).apply()
        }

    var skipIntroDuration: Int
        get() = prefs.getInt("skip_intro_duration", 85)
        set(value) {
            prefs.edit().putInt("skip_intro_duration", value).apply()
        }

    var seekDuration: Int
        get() = prefs.getInt("seek_duration", 10)
        set(value) {
            prefs.edit().putInt("seek_duration", value).apply()
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

    var playerDecoder: String
        get() = prefs.getString("player_decoder", "Hardware Accelerated") ?: "Hardware Accelerated"
        set(value) {
            prefs.edit().putString("player_decoder", value).apply()
        }

    // Manga / Reader Preferences
    var readingMode: String
        get() = prefs.getString("reading_mode", "Webtoon (Vertical)") ?: "Webtoon (Vertical)"
        set(value) {
            prefs.edit().putString("reading_mode", value).apply()
        }

    var readingDirection: String
        get() = prefs.getString("reading_direction", "Right to Left") ?: "Right to Left"
        set(value) {
            prefs.edit().putString("reading_direction", value).apply()
        }

    var doubleTapZoom: Boolean
        get() = prefs.getBoolean("double_tap_zoom", true)
        set(value) {
            prefs.edit().putBoolean("double_tap_zoom", value).apply()
        }

    // Network & Data
    var wifiOnlyDownloads: Boolean
        get() = prefs.getBoolean("wifi_only_downloads", true)
        set(value) {
            prefs.edit().putBoolean("wifi_only_downloads", value).apply()
        }

    var maxConcurrentDownloads: Int
        get() = prefs.getInt("max_concurrent_downloads", 3)
        set(value) {
            prefs.edit().putInt("max_concurrent_downloads", value).apply()
        }

    var networkTimeout: Int
        get() = prefs.getInt("network_timeout", 15)
        set(value) {
            prefs.edit().putInt("network_timeout", value).apply()
        }

    // Content Safety
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
