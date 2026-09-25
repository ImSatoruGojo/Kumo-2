package app.kumo.beta.data.local

import android.content.Context

data class SettingsPreferences(
    val theme: String = "Dark",
    val appLanguage: String = "System",
    val cacheLimitMb: Int = 512,
    val defaultQuality: String = "Auto",
    val playbackSpeed: Float = 1f,
    val autoplayNext: Boolean = false,
    val doubleTapSeekSeconds: Int = 10,
    val skipOpening: Boolean = false,
    val defaultAudio: String = "Auto",
    val defaultSubtitle: String = "Auto",
    val animeLanguage: String = "Dub",
    val movieLanguage: String = "Dub",
    val autoMarkWatched: Boolean = true,
    val continueWatching: Boolean = true,
    val wifiOnlyDownloads: Boolean = true,
    val confirmDownloads: Boolean = true,
    val showAdultContent: Boolean = false,
    val reduceAnimations: Boolean = false
)

class SettingsPreferencesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kumo_settings", Context.MODE_PRIVATE)

    fun get(): SettingsPreferences = SettingsPreferences(
        theme = prefs.getString("theme", "Dark") ?: "Dark",
        appLanguage = prefs.getString("language", "System") ?: "System",
        cacheLimitMb = prefs.getInt("cache_limit_mb", 512),
        defaultQuality = prefs.getString("quality", "Auto") ?: "Auto",
        playbackSpeed = prefs.getFloat("speed", 1f),
        autoplayNext = prefs.getBoolean("autoplay_next", false),
        doubleTapSeekSeconds = prefs.getInt("seek_seconds", 10),
        skipOpening = prefs.getBoolean("skip_opening", false),
        defaultAudio = prefs.getString("audio", "Auto") ?: "Auto",
        defaultSubtitle = prefs.getString("subtitle", "Auto") ?: "Auto",
        animeLanguage = prefs.getString("anime_language", "Dub") ?: "Dub",
        movieLanguage = prefs.getString("movie_language", "Dub") ?: "Dub",
        autoMarkWatched = prefs.getBoolean("auto_mark_watched", true),
        continueWatching = prefs.getBoolean("continue_watching", true),
        wifiOnlyDownloads = prefs.getBoolean("wifi_only_downloads", true),
        confirmDownloads = prefs.getBoolean("confirm_downloads", true),
        showAdultContent = prefs.getBoolean("show_adult_content", false),
        reduceAnimations = prefs.getBoolean("reduce_animations", false)
    )

    fun setTheme(value: String) = prefs.edit().putString("theme", value).apply()
    fun setLanguage(value: String) = prefs.edit().putString("language", value).apply()
    fun setCacheLimitMb(value: Int) = prefs.edit().putInt("cache_limit_mb", value).apply()
    fun setQuality(value: String) = prefs.edit().putString("quality", value).apply()
    fun setSpeed(value: Float) = prefs.edit().putFloat("speed", value).apply()
    fun setAutoplayNext(value: Boolean) = prefs.edit().putBoolean("autoplay_next", value).apply()
    fun setSeekSeconds(value: Int) = prefs.edit().putInt("seek_seconds", value).apply()
    fun setSkipOpening(value: Boolean) = prefs.edit().putBoolean("skip_opening", value).apply()
    fun setAudio(value: String) = prefs.edit().putString("audio", value).apply()
    fun setSubtitle(value: String) = prefs.edit().putString("subtitle", value).apply()
    fun setAnimeLanguage(value: String) = prefs.edit().putString("anime_language", value).apply()
    fun setMovieLanguage(value: String) = prefs.edit().putString("movie_language", value).apply()
    fun setAutoMarkWatched(value: Boolean) = prefs.edit().putBoolean("auto_mark_watched", value).apply()
    fun setContinueWatching(value: Boolean) = prefs.edit().putBoolean("continue_watching", value).apply()
    fun setWifiOnlyDownloads(value: Boolean) = prefs.edit().putBoolean("wifi_only_downloads", value).apply()
    fun setConfirmDownloads(value: Boolean) = prefs.edit().putBoolean("confirm_downloads", value).apply()
    fun setShowAdultContent(value: Boolean) = prefs.edit().putBoolean("show_adult_content", value).apply()
    fun setReduceAnimations(value: Boolean) = prefs.edit().putBoolean("reduce_animations", value).apply()
}