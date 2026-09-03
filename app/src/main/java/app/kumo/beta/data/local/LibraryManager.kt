package app.kumo.beta.data.local

import android.content.Context
import android.content.SharedPreferences
import app.kumo.beta.model.Progress
import org.json.JSONArray
import org.json.JSONObject

enum class LibraryCategory(val displayName: String) {
    FAVORITES("Favorites"),
    WATCH_LATER("Watch Later"),
    CURRENTLY_WATCHING("Watching"),
    COMPLETED("Completed"),
    DROPPED("Dropped")
}

class LibraryManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kumo_library_prefs", Context.MODE_PRIVATE)

    fun getCategoryForTitle(titleId: String): LibraryCategory? {
        val catName = prefs.getString("cat_$titleId", null) ?: return null
        return try { LibraryCategory.valueOf(catName) } catch (e: Exception) { null }
    }

    fun setCategoryForTitle(titleId: String, category: LibraryCategory?) {
        if (category == null) {
            prefs.edit().remove("cat_$titleId").apply()
        } else {
            prefs.edit().putString("cat_$titleId", category.name).apply()
        }
    }

    fun getTitlesInCategory(category: LibraryCategory): Set<String> {
        val result = mutableSetOf<String>()
        for ((key, value) in prefs.all) {
            if (key.startsWith("cat_") && value == category.name) {
                result.add(key.removePrefix("cat_"))
            }
        }
        return result
    }

    fun isFavorite(titleId: String): Boolean {
        return getCategoryForTitle(titleId) == LibraryCategory.FAVORITES ||
                prefs.getBoolean("fav_$titleId", false)
    }

    fun setFavorite(titleId: String, isFav: Boolean) {
        prefs.edit().putBoolean("fav_$titleId", isFav).apply()
    }

    fun getCustomTags(titleId: String): List<String> {
        val json = prefs.getString("tags_$titleId", "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    fun setCustomTags(titleId: String, tags: List<String>) {
        val array = JSONArray()
        tags.forEach { array.put(it) }
        prefs.edit().putString("tags_$titleId", array.toString()).apply()
    }

    fun getAllCustomLists(): List<String> {
        val json = prefs.getString("custom_lists", "[\"Anime Favorites\", \"Movie Night\"]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    fun addCustomList(listName: String) {
        val current = getAllCustomLists().toMutableList()
        if (!current.contains(listName)) {
            current.add(listName)
            val array = JSONArray()
            current.forEach { array.put(it) }
            prefs.edit().putString("custom_lists", array.toString()).apply()
        }
    }
}

class ContinueWatchingManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kumo_continue_watching_prefs", Context.MODE_PRIVATE)

    fun getProgress(titleId: String): Progress? {
        val jsonStr = prefs.getString("prog_$titleId", null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            Progress(
                contentId = obj.getString("contentId"),
                episodeId = obj.getString("episodeId"),
                positionMs = obj.getLong("positionMs"),
                durationMs = obj.getLong("durationMs"),
                updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveProgress(progress: Progress) {
        val obj = JSONObject().apply {
            put("contentId", progress.contentId)
            put("episodeId", progress.episodeId)
            put("positionMs", progress.positionMs)
            put("durationMs", progress.durationMs)
            put("updatedAt", progress.updatedAt)
        }
        prefs.edit().putString("prog_${progress.contentId}", obj.toString()).apply()
    }

    fun removeProgress(titleId: String) {
        prefs.edit().remove("prog_$titleId").apply()
    }

    fun getAllContinueWatching(): List<Progress> {
        val list = mutableListOf<Progress>()
        for ((key, value) in prefs.all) {
            if (key.startsWith("prog_") && value is String) {
                try {
                    val obj = JSONObject(value)
                    list.add(
                        Progress(
                            contentId = obj.getString("contentId"),
                            episodeId = obj.getString("episodeId"),
                            positionMs = obj.getLong("positionMs"),
                            durationMs = obj.getLong("durationMs"),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                } catch (e: Exception) { /* skip */ }
            }
        }
        return list.sortedByDescending { it.updatedAt }
    }
}
