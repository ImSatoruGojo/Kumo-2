package app.kumo.beta.data.local

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class ExtensionRepository(
    val id: String,
    val name: String,
    val url: String,
    val format: RepoFormat,
    val extensionCount: Int = 0,
    val isEnabled: Boolean = true,
    val isTrusted: Boolean = true
)

enum class RepoFormat {
    ANIYOMI_MIHON, CLOUDSTREAM, UNIVERSAL_JSON
}

class ExtensionRepositoryManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kumo_extension_repos", Context.MODE_PRIVATE)

    companion object {
        val PRESET_REPOSITORIES = listOf(
            ExtensionRepository(
                id = "preset_aniyomi_official",
                name = "Aniyomi Extensions Repository",
                url = "https://raw.githubusercontent.com/aniyomiorg/aniyomi-extensions/repo/index.json",
                format = RepoFormat.ANIYOMI_MIHON,
                extensionCount = 142,
                isEnabled = true,
                isTrusted = true
            ),
            ExtensionRepository(
                id = "preset_mihon_official",
                name = "Mihon Extensions Index",
                url = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.json",
                format = RepoFormat.ANIYOMI_MIHON,
                extensionCount = 280,
                isEnabled = true,
                isTrusted = true
            ),
            ExtensionRepository(
                id = "preset_cloudstream_plugins",
                name = "CloudStream Repository Hub",
                url = "https://raw.githubusercontent.com/recloudstream/cloudstream-extensions/master/repo.json",
                format = RepoFormat.CLOUDSTREAM,
                extensionCount = 95,
                isEnabled = true,
                isTrusted = true
            )
        )
    }

    fun getRepositories(): List<ExtensionRepository> {
        val jsonStr = prefs.getString("custom_repos", null)
        if (jsonStr.isNullOrEmpty()) {
            // Save presets initially
            saveRepositories(PRESET_REPOSITORIES)
            return PRESET_REPOSITORIES
        }

        val list = mutableListOf<ExtensionRepository>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ExtensionRepository(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        url = obj.getString("url"),
                        format = try { RepoFormat.valueOf(obj.getString("format")) } catch (e: Exception) { RepoFormat.ANIYOMI_MIHON },
                        extensionCount = obj.optInt("extensionCount", 0),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        isTrusted = obj.optBoolean("isTrusted", true)
                    )
                )
            }
        } catch (e: Exception) {
            return PRESET_REPOSITORIES
        }
        return list
    }

    fun addRepository(repo: ExtensionRepository) {
        val current = getRepositories().toMutableList()
        current.removeAll { it.url == repo.url || it.id == repo.id }
        current.add(0, repo)
        saveRepositories(current)
    }

    fun removeRepository(id: String) {
        val current = getRepositories().toMutableList()
        current.removeAll { it.id == id }
        saveRepositories(current)
    }

    fun toggleRepository(id: String, enabled: Boolean) {
        val current = getRepositories().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(isEnabled = enabled)
            saveRepositories(current)
        }
    }

    private fun saveRepositories(repos: List<ExtensionRepository>) {
        val array = JSONArray()
        repos.forEach { repo ->
            val obj = JSONObject().apply {
                put("id", repo.id)
                put("name", repo.name)
                put("url", repo.url)
                put("format", repo.format.name)
                put("extensionCount", repo.extensionCount)
                put("isEnabled", repo.isEnabled)
                put("isTrusted", repo.isTrusted)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_repos", array.toString()).apply()
    }
}
