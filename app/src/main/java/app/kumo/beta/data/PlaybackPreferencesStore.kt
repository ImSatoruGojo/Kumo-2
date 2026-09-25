package app.kumo.beta.data

import android.content.Context
import app.kumo.beta.player.PlaybackPreferences

class PlaybackPreferencesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("playback_preferences", Context.MODE_PRIVATE)

    fun get(): PlaybackPreferences = PlaybackPreferences(
        preferredLanguage = prefs.getString("preferredLanguage", null),
        preferredQuality = prefs.getInt("preferredQuality", 0).takeIf { it > 0 },
        autoPlayNext = prefs.getBoolean("autoPlayNext", true),
        skipIntroSeconds = prefs.getInt("skipIntroSeconds", 0)
    )

    fun set(preferences: PlaybackPreferences) {
        prefs.edit()
            .putString("preferredLanguage", preferences.preferredLanguage)
            .putInt("preferredQuality", preferences.preferredQuality ?: 0)
            .putBoolean("autoPlayNext", preferences.autoPlayNext)
            .putInt("skipIntroSeconds", preferences.skipIntroSeconds)
            .apply()
    }
}