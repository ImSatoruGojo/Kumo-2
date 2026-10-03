package app.kumo.beta.data.local

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(context: Context) {
    private val appContext = context.applicationContext
    private val preferenceNames = listOf(
        "kumo_settings",
        "kumo_library",
        "kumo_downloads",
        "kumo_library_prefs",
        "kumo_repositories",
        "kumo_continue_watching_prefs",
        "kumo_provider_health",
        "kumo_saved_titles",
        "kumo_reader_progress"
    )

    fun exportTo(uri: Uri): Result<Unit> = runCatching {
        val root = JSONObject()
            .put("format", "kumo_backup")
            .put("version", 1)
            .put("createdAt", System.currentTimeMillis())

        val preferences = JSONObject()
        preferenceNames.forEach { name ->
            val source = appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
            preferences.put(name, JSONObject().apply {
                source.all.forEach { (key, value) ->
                    put(key, when (value) {
                        is Set<*> -> JSONArray(value.map { it.toString() })
                        else -> value
                    })
                }
            })
        }
        root.put("preferences", preferences)

        appContext.contentResolver.openOutputStream(uri, "w")?.use { output ->
            output.writer(Charsets.UTF_8).use { writer ->
                writer.write(root.toString())
            }
        } ?: error("Unable to open backup destination")
    }

    fun importFrom(uri: Uri): Result<Unit> = runCatching {
        val raw = appContext.contentResolver.openInputStream(uri)?.use {
            it.reader(Charsets.UTF_8).readText()
        } ?: error("Unable to open backup file")

        val root = JSONObject(raw)
        require(root.optString("format") == "kumo_backup") { "This is not a Kumo backup file" }
        require(root.optInt("version", -1) == 1) { "Unsupported Kumo backup version" }

        val preferences = root.optJSONObject("preferences") ?: error("Backup has no preferences")
        preferenceNames.forEach { name ->
            val encoded = preferences.optJSONObject(name) ?: return@forEach
            val target = appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
            val editor = target.edit().clear()
            val keys = encoded.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = encoded.opt(key)
                when (value) {
                    is JSONArray -> editor.putStringSet(
                        key,
                        buildSet {
                            for (i in 0 until value.length()) add(value.optString(i))
                        }
                    )
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is String -> editor.putString(key, value)
                    else -> Unit
                }
            }
            editor.apply()
        }
    }

    companion object {
        const val FILE_NAME = "kumo-backup.json"
    }
}
