package app.kumo.beta.data

import android.content.Context
import app.kumo.beta.model.Progress
import org.json.JSONArray
import org.json.JSONObject

class LibraryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kumo_library", Context.MODE_PRIVATE)
    fun isSaved(id: String): Boolean = prefs.getStringSet("saved", emptySet())?.contains(id) == true
    fun setSaved(id: String, saved: Boolean) {
        val current = prefs.getStringSet("saved", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (saved) current += id else current -= id
        prefs.edit().putStringSet("saved", current).apply()
    }
    fun getProgress(): List<Progress> {
        val array = JSONArray(prefs.getString("progress", "[]"))
        return (0 until array.length()).mapNotNull { i -> runCatching {
            val o=array.getJSONObject(i)
            Progress(o.getString("contentId"),o.getString("episodeId"),o.getLong("positionMs"),o.getLong("durationMs"),o.getLong("updatedAt"))
        }.getOrNull() }.sortedByDescending { it.updatedAt }
    }
    fun saveProgress(progress: Progress) {
        val items=getProgress().filterNot { it.contentId==progress.contentId && it.episodeId==progress.episodeId }.toMutableList()
        items += progress
        val array=JSONArray()
        items.take(100).forEach { p -> array.put(JSONObject().apply {
            put("contentId",p.contentId); put("episodeId",p.episodeId); put("positionMs",p.positionMs); put("durationMs",p.durationMs); put("updatedAt",p.updatedAt)
        }) }
        prefs.edit().putString("progress",array.toString()).apply()
    }
}
