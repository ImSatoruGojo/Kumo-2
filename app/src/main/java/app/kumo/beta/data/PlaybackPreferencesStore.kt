package app.kumo.beta.data

import android.content.Context
import app.kumo.beta.player.PlaybackPreferences

class PlaybackPreferencesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("playback_preferences", Context.MODE_PRIVATE)

    fun get(): PlaybackPreferences = PlaybackPreferences(
        defaultQuality = prefs.getInt("preferredQuality", 0).takeIf { it > 0 },
        playbackSpeed = prefs.getFloat("playbackSpeed", 1f),
        autoplayNext = prefs.getBoolean("autoPlayNext", true),
        seekSeconds = prefs.getInt("seekSeconds", 10),
        preferredAudio = prefs.getString("preferredAudio", null),
        preferredSubtitle = prefs.getString("preferredSubtitle", null)
    )

    fun set(preferences: PlaybackPreferences) {
        prefs.edit()
            .putInt("preferredQuality", preferences.defaultQuality ?: 0)
            .putFloat("playbackSpeed", preferences.playbackSpeed)
            .putBoolean("autoPlayNext", preferences.autoplayNext)
            .putInt("seekSeconds", preferences.seekSeconds)
            .putString("preferredAudio", preferences.preferredAudio)
            .putString("preferredSubtitle", preferences.preferredSubtitle)
            .apply()
    }
}
