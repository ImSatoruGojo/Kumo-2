package app.kumo.beta.data.local

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode {
    DARK, AMOLED, LIGHT, SYSTEM
}

enum class AccentColorOption(val title: String, val hexColor: Long) {
    WHITE("White Accent", 0xFFFFFFFF),
    ORANGE("Kumo Orange", 0xFFFF6B35),
    RED("Red", 0xFFFF0000),
    GREEN("Green", 0xFF00FF00),
    BLUE("Blue", 0xFF0000FF),
    YELLOW("Yellow", 0xFFFFFF00),
    MAGENTA("Magenta", 0xFFFF00FF),
    CYAN("Cyan", 0xFF00FFFF),
    PURPLE("Purple", 0xFF800080),
    PINK("Pink", 0xFFFFC0CB)
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
            val name = prefs.getString("accent_color", AccentColorOption.WHITE.name) ?: AccentColorOption.WHITE.name
            return try { AccentColorOption.valueOf(name) } catch (e: Exception) { AccentColorOption.WHITE }
        }
        set(value) {
            prefs.edit().putString("accent_color", value.name).apply()
        }

    var customHexColor: String
        get() = prefs.getString("custom_hex_color", "#FFFFFF") ?: "#FFFFFF"
        set(value) {
            prefs.edit().putString("custom_hex_color", value).apply()
        }

    var useCustomHex: Boolean
        get() = prefs.getBoolean("use_custom_hex", false)
        set(value) {
            prefs.edit().putBoolean("use_custom_hex", value).apply()
        }

    var appLanguage: String
        get() = prefs.getString("app_language", "en") ?: "en"
        set(value) {
            prefs.edit().putString("app_language", value).apply()
        }

    var startupPage: String
        get() = prefs.getString("startup_page", "Home") ?: "Home"
        set(value) {
            prefs.edit().putString("startup_page", value).apply()
        }

    var uiStyle: String
        get() = prefs.getString("ui_style", "NETFLIX") ?: "NETFLIX"
        set(value) {
            prefs.edit().putString("ui_style", value).apply()
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

    var amoledMode: Boolean
        get() = prefs.getBoolean("pref_amoled_mode", false)
        set(value) {
            prefs.edit().putBoolean("pref_amoled_mode", value).apply()
        }

    var dataSavingEnabled: Boolean
        get() = prefs.getBoolean("data_saving", false)
        set(value) {
            prefs.edit().putBoolean("data_saving", value).apply()
        }

    var catppuccinTheme: String
        get() = prefs.getString("catppuccin_theme", "Mocha") ?: "Mocha"
        set(value) {
            prefs.edit().putString("catppuccin_theme", value).apply()
        }

    var cornerRadius: Int
        get() = prefs.getInt("corner_radius", 12)
        set(value) {
            prefs.edit().putInt("corner_radius", value).apply()
        }

    var fontScale: Float
        get() = prefs.getFloat("font_scale", 1.0f)
        set(value) {
            prefs.edit().putFloat("font_scale", value).apply()
        }

    var cardStyle: String
        get() = prefs.getString("card_style", "Elevated") ?: "Elevated"
        set(value) {
            prefs.edit().putString("card_style", value).apply()
        }

    var themePreset: String
        get() = prefs.getString("pref_theme_preset", "Kumo Default") ?: "Kumo Default"
        set(value) {
            prefs.edit().putString("pref_theme_preset", value).apply()
        }

    var roundedCorners: Boolean
        get() = prefs.getBoolean("pref_rounded_corners", true)
        set(value) {
            prefs.edit().putBoolean("pref_rounded_corners", value).apply()
        }

    var brightnessFilter: Int
        get() = prefs.getInt("pref_brightness_filter", 100)
        set(value) {
            prefs.edit().putInt("pref_brightness_filter", value).apply()
        }

    var contrastFilter: Int
        get() = prefs.getInt("pref_contrast_filter", 100)
        set(value) {
            prefs.edit().putInt("pref_contrast_filter", value).apply()
        }

    var saturationFilter: Int
        get() = prefs.getInt("pref_saturation_filter", 100)
        set(value) {
            prefs.edit().putInt("pref_saturation_filter", value).apply()
        }

    var colorTemp: Int
        get() = prefs.getInt("pref_color_temp", 50)
        set(value) {
            prefs.edit().putInt("pref_color_temp", value).apply()
        }

    var invertColors: Boolean
        get() = prefs.getBoolean("pref_invert_colors", false)
        set(value) {
            prefs.edit().putBoolean("pref_invert_colors", value).apply()
        }

    var sepiaMode: Boolean
        get() = prefs.getBoolean("pref_sepia_mode", false)
        set(value) {
            prefs.edit().putBoolean("pref_sepia_mode", value).apply()
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

    var showRating: Boolean
        get() = prefs.getBoolean("pref_show_rating", true)
        set(value) {
            prefs.edit().putBoolean("pref_show_rating", value).apply()
        }

    var showStatus: Boolean
        get() = prefs.getBoolean("pref_show_status", true)
        set(value) {
            prefs.edit().putBoolean("pref_show_status", value).apply()
        }

    var itemsPerRow: Int
        get() = prefs.getInt("pref_items_per_row", 2)
        set(value) {
            prefs.edit().putInt("pref_items_per_row", value).apply()
        }

    var historySize: String
        get() = prefs.getString("pref_history_size", "50") ?: "50"
        set(value) {
            prefs.edit().putString("pref_history_size", value).apply()
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

    var playerEngine: String
        get() = prefs.getString("player_engine", "ExoPlayer (Native)") ?: "ExoPlayer (Native)"
        set(value) {
            prefs.edit().putString("player_engine", value).apply()
        }

    var gestureVolumeControl: Boolean
        get() = prefs.getBoolean("gesture_volume_control", true)
        set(value) {
            prefs.edit().putBoolean("gesture_volume_control", value).apply()
        }

    var gestureBrightnessControl: Boolean
        get() = prefs.getBoolean("gesture_brightness_control", true)
        set(value) {
            prefs.edit().putBoolean("gesture_brightness_control", value).apply()
        }

    var subtitleLanguage: String
        get() = prefs.getString("subtitle_language", "English") ?: "English"
        set(value) {
            prefs.edit().putString("subtitle_language", value).apply()
        }

    var subtitleSize: Int
        get() = prefs.getInt("subtitle_size", 16)
        set(value) {
            prefs.edit().putInt("subtitle_size", value).apply()
        }

    var subtitleBgStyle: String
        get() = prefs.getString("subtitle_bg_style", "Transparent Black") ?: "Transparent Black"
        set(value) {
            prefs.edit().putString("subtitle_bg_style", value).apply()
        }

    var audioBoost: Boolean
        get() = prefs.getBoolean("audio_boost", false)
        set(value) {
            prefs.edit().putBoolean("audio_boost", value).apply()
        }

    var skipOutroDuration: Int
        get() = prefs.getInt("pref_skip_outro", 30)
        set(value) {
            prefs.edit().putInt("pref_skip_outro", value).apply()
        }

    var autoplayThreshold: Int
        get() = prefs.getInt("pref_autoplay_threshold", 90)
        set(value) {
            prefs.edit().putInt("pref_autoplay_threshold", value).apply()
        }

    var pipEnabled: Boolean
        get() = prefs.getBoolean("pref_pip_enabled", true)
        set(value) {
            prefs.edit().putBoolean("pref_pip_enabled", value).apply()
        }

    var uiAutohideDuration: Int
        get() = prefs.getInt("pref_ui_autohide", 3000)
        set(value) {
            prefs.edit().putInt("pref_ui_autohide", value).apply()
        }

    var screenRotation: String
        get() = prefs.getString("pref_screen_rotation", "Free") ?: "Free"
        set(value) {
            prefs.edit().putString("pref_screen_rotation", value).apply()
        }

    var keepScreenOn: Boolean
        get() = prefs.getBoolean("pref_keep_screen_on", false)
        set(value) {
            prefs.edit().putBoolean("pref_keep_screen_on", value).apply()
        }

    var notchHandling: String
        get() = prefs.getString("pref_notch_handling", "Ignore") ?: "Ignore"
        set(value) {
            prefs.edit().putString("pref_notch_handling", value).apply()
        }

    // Library & Tracking Preferences
    var librarySortOrder: String
        get() = prefs.getString("library_sort_order", "Title (A-Z)") ?: "Title (A-Z)"
        set(value) {
            prefs.edit().putString("library_sort_order", value).apply()
        }

    var autoSyncTrackers: Boolean
        get() = prefs.getBoolean("auto_sync_trackers", true)
        set(value) {
            prefs.edit().putBoolean("auto_sync_trackers", value).apply()
        }

    var aniListUsername: String
        get() = prefs.getString("anilist_username", "") ?: ""
        set(value) {
            prefs.edit().putString("anilist_username", value).apply()
        }

    var malUsername: String
        get() = prefs.getString("mal_username", "") ?: ""
        set(value) {
            prefs.edit().putString("mal_username", value).apply()
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

    var tapToScroll: Boolean
        get() = prefs.getBoolean("tap_to_scroll", true)
        set(value) {
            prefs.edit().putBoolean("tap_to_scroll", value).apply()
        }

    var webtoonGap: Int
        get() = prefs.getInt("webtoon_gap", 0)
        set(value) {
            prefs.edit().putInt("webtoon_gap", value).apply()
        }

    var perMangaMode: Boolean
        get() = prefs.getBoolean("pref_per_manga_mode", true)
        set(value) {
            prefs.edit().putBoolean("pref_per_manga_mode", value).apply()
        }

    var readerBgColor: String
        get() = prefs.getString("pref_reader_bg", "Black") ?: "Black"
        set(value) {
            prefs.edit().putString("pref_reader_bg", value).apply()
        }

    var readerTextColor: String
        get() = prefs.getString("pref_reader_text_color", "#FFFFFF") ?: "#FFFFFF"
        set(value) {
            prefs.edit().putString("pref_reader_text_color", value).apply()
        }

    var cropBorders: Boolean
        get() = prefs.getBoolean("pref_crop_borders", false)
        set(value) {
            prefs.edit().putBoolean("pref_crop_borders", value).apply()
        }

    var scaleToFit: Boolean
        get() = prefs.getBoolean("pref_scale_to_fit", true)
        set(value) {
            prefs.edit().putBoolean("pref_scale_to_fit", value).apply()
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
