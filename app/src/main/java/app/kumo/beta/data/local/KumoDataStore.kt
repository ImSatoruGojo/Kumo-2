package app.kumo.beta.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kumo_preferences")

class KumoDataStore(private val context: Context) {

    companion object {
        val APP_THEME = stringPreferencesKey("app_theme")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val CUSTOM_HEX_COLOR = stringPreferencesKey("custom_hex_color")
        val USE_CUSTOM_HEX = booleanPreferencesKey("use_custom_hex")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val DATA_SAVING = booleanPreferencesKey("data_saving")
        val SHOW_CONTINUE_WATCHING = booleanPreferencesKey("show_continue_watching")
        val SHOW_POPULAR = booleanPreferencesKey("show_popular")
        val SHOW_TRENDING = booleanPreferencesKey("show_trending")
        val SHOW_TOP_RATED = booleanPreferencesKey("show_top_rated")
        val SHOW_NEW_RELEASES = booleanPreferencesKey("show_new_releases")
        val AUTO_SKIP_INTRO = booleanPreferencesKey("auto_skip_intro")
        val AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next")
        val PLAYER_SCREEN_LOCK = booleanPreferencesKey("player_screen_lock")
        val NSFW_ENABLED = booleanPreferencesKey("nsfw_enabled")
        val SHOW_SPOILER_WARNINGS = booleanPreferencesKey("show_spoiler_warnings")
        val PREFERRED_QUALITY = stringPreferencesKey("preferred_quality")
        val WIFI_ONLY_DOWNLOADS = booleanPreferencesKey("wifi_only_downloads")
        val MAX_CONCURRENT_DOWNLOADS = intPreferencesKey("max_concurrent_downloads")
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[APP_THEME] ?: AppThemeMode.DARK.name
    }

    val accentColorFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ACCENT_COLOR] ?: AccentColorOption.PURPLE.name
    }

    val customHexFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_HEX_COLOR] ?: "#7C4DFF"
    }

    val useCustomHexFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[USE_CUSTOM_HEX] ?: false
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { prefs -> prefs[APP_THEME] = theme }
    }

    suspend fun saveAccentColor(accent: String) {
        context.dataStore.edit { prefs -> prefs[ACCENT_COLOR] = accent }
    }

    suspend fun saveCustomHex(hex: String) {
        context.dataStore.edit { prefs -> prefs[CUSTOM_HEX_COLOR] = hex }
    }

    suspend fun saveUseCustomHex(useCustom: Boolean) {
        context.dataStore.edit { prefs -> prefs[USE_CUSTOM_HEX] = useCustom }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}
