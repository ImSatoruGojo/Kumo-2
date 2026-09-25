package app.kumo.beta.data.local

import android.content.Context
import android.content.SharedPreferences

enum class ExtensionType {
    ANIME, MOVIE_TV, MANGA, CARTOON
}

data class ExtensionItem(
    val id: String,
    val name: String,
    val version: String,
    val type: ExtensionType,
    val iconUrl: String,
    val author: String,
    val isInstalled: Boolean,
    val isEnabled: Boolean,
    val hasUpdate: Boolean = false,
    val description: String
)

class ExtensionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kumo_extensions", Context.MODE_PRIVATE)

    private val defaultExtensions = listOf(
        ExtensionItem(
            id = "ext_anikoto",
            name = "Anikoto Anime",
            version = "1.2.4",
            type = ExtensionType.ANIME,
            iconUrl = "https://picsum.photos/100/100?random=11",
            author = "Kumo Official",
            isInstalled = true,
            isEnabled = true,
            hasUpdate = false,
            description = "High quality subbed and dubbed anime provider."
        ),
        ExtensionItem(
            id = "ext_cinemahd",
            name = "Cinema HD",
            version = "2.0.1",
            type = ExtensionType.MOVIE_TV,
            iconUrl = "https://picsum.photos/100/100?random=12",
            author = "Community",
            isInstalled = true,
            isEnabled = true,
            hasUpdate = true,
            description = "Movies and TV shows streams with multi-source backup."
        ),
        ExtensionItem(
            id = "ext_mangacloud",
            name = "Manga Cloud",
            version = "0.9.5",
            type = ExtensionType.MANGA,
            iconUrl = "https://picsum.photos/100/100?random=13",
            author = "MangaDev",
            isInstalled = true,
            isEnabled = false,
            hasUpdate = false,
            description = "Manga reader extension with fast page loading."
        ),
        ExtensionItem(
            id = "ext_cartoonverse",
            name = "Cartoon Verse",
            version = "1.1.0",
            type = ExtensionType.CARTOON,
            iconUrl = "https://picsum.photos/100/100?random=14",
            author = "Kumo Official",
            isInstalled = false,
            isEnabled = false,
            hasUpdate = false,
            description = "Classic and modern animated cartoon catalog."
        )
    )

    fun getExtensions(): List<ExtensionItem> {
        val installedJson = prefs.getString("installed_exts", null)
        if (installedJson.isNullOrEmpty()) {
            return defaultExtensions
        }
        val list = mutableListOf<ExtensionItem>()
        try {
            val array = org.json.JSONArray(installedJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ExtensionItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        version = obj.getString("version"),
                        type = try { ExtensionType.valueOf(obj.getString("type")) } catch (e: Exception) { ExtensionType.ANIME },
                        iconUrl = obj.optString("iconUrl", ""),
                        author = obj.optString("author", "Community"),
                        isInstalled = obj.optBoolean("isInstalled", true),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        hasUpdate = obj.optBoolean("hasUpdate", false),
                        description = obj.optString("description", "")
                    )
                )
            }
        } catch (e: Exception) {
            return defaultExtensions
        }
        return list
    }

    fun toggleExtension(id: String, enabled: Boolean) {
        val current = getExtensions().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(isEnabled = enabled)
            saveExtensions(current)
        }
    }

    fun installExtension(extension: ExtensionItem) {
        val current = getExtensions().toMutableList()
        current.removeAll { it.id == extension.id }
        current.add(extension.copy(isInstalled = true, isEnabled = true, hasUpdate = false))
        saveExtensions(current)
    }

    fun uninstallExtension(id: String) {
        val current = getExtensions().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(isInstalled = false, isEnabled = false)
            saveExtensions(current)
        }
    }

    fun getFallbackProvider(type: ExtensionType): ExtensionItem? {
        return getExtensions().firstOrNull { it.isInstalled && it.isEnabled && it.type == type }
    }

    private fun saveExtensions(list: List<ExtensionItem>) {
        val array = org.json.JSONArray()
        list.forEach { ext ->
            val obj = org.json.JSONObject().apply {
                put("id", ext.id)
                put("name", ext.name)
                put("version", ext.version)
                put("type", ext.type.name)
                put("iconUrl", ext.iconUrl)
                put("author", ext.author)
                put("isInstalled", ext.isInstalled)
                put("isEnabled", ext.isEnabled)
                put("hasUpdate", ext.hasUpdate)
                put("description", ext.description)
            }
            array.put(obj)
        }
        prefs.edit().putString("installed_exts", array.toString()).apply()
    }
}
