package app.kumo.beta.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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

data class ExtensionRepositoryItem(
    val id: String,
    val name: String,
    val version: String,
    val apkUrl: String,
    val iconUrl: String,
    val language: String,
    val nsfw: Boolean,
    val description: String = ""
)

class ExtensionRepositoryManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kumo_extension_repos", Context.MODE_PRIVATE)

    companion object {
        val PRESET_REPOSITORIES = listOf(
            ExtensionRepository(
                id = "repo_aniyomi_official",
                name = "Aniyomi Extensions Repository",
                url = "https://raw.githubusercontent.com/aniyomiorg/aniyomi-extensions/repo/index.json",
                format = RepoFormat.ANIYOMI_MIHON,
                extensionCount = 142,
                isEnabled = true,
                isTrusted = true
            ),
            ExtensionRepository(
                id = "repo_keiyoushi_official",
                name = "Keiyoushi Extensions Index",
                url = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.json",
                format = RepoFormat.ANIYOMI_MIHON,
                extensionCount = 280,
                isEnabled = true,
                isTrusted = true
            ),
            ExtensionRepository(
                id = "repo_cloudstream_official",
                name = "CloudStream Extensions Hub",
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

    suspend fun fetchAndSyncRepository(urlStr: String, name: String = ""): Result<ExtensionRepository> = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Kumo/2.0)")

            if (connection.responseCode in 200..299) {
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                val detectedFormat = if (content.contains("plugins") || content.contains("manifestVersion")) RepoFormat.CLOUDSTREAM else RepoFormat.ANIYOMI_MIHON
                val items = parseRepositoryIndexJson(content, detectedFormat)

                val repoName = if (name.isNotBlank()) name else try {
                    val root = JSONObject(content)
                    root.optString("name", "Custom Repository")
                } catch (e: Exception) {
                    "Custom Repository (${url.host})"
                }

                val repo = ExtensionRepository(
                    id = "repo_" + urlStr.hashCode(),
                    name = repoName,
                    url = urlStr,
                    format = detectedFormat,
                    extensionCount = items.size,
                    isEnabled = true,
                    isTrusted = true
                )

                addRepository(repo)

                // Sync items into ExtensionManager
                val extensionManager = ExtensionManager(context)
                items.forEach { item ->
                    val extType = when {
                        item.name.lowercase().contains("manga") || item.id.contains("manga") -> ExtensionType.MANGA
                        item.name.lowercase().contains("movie") || item.id.contains("movie") -> ExtensionType.MOVIE_TV
                        item.name.lowercase().contains("cartoon") -> ExtensionType.CARTOON
                        else -> ExtensionType.ANIME
                    }
                    extensionManager.installExtension(
                        ExtensionItem(
                            id = item.id,
                            name = item.name,
                            version = item.version,
                            type = extType,
                            iconUrl = item.iconUrl,
                            author = repo.name,
                            isInstalled = true,
                            isEnabled = true,
                            description = item.description.ifEmpty { "Provider from ${repo.name}" }
                        )
                    )
                }

                Result.success(repo)
            } else {
                Result.failure(Exception("HTTP Error ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseRepositoryIndexJson(jsonString: String, format: RepoFormat): List<ExtensionRepositoryItem> {
        val list = mutableListOf<ExtensionRepositoryItem>()
        try {
            val trimmed = jsonString.trim()
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ExtensionRepositoryItem(
                            id = obj.optString("id", obj.optString("pkg", "ext_$i")),
                            name = obj.optString("name", obj.optString("title", "Extension $i")),
                            version = obj.optString("version", "1.0.0"),
                            apkUrl = obj.optString("apk", obj.optString("url", "")),
                            iconUrl = obj.optString("icon", obj.optString("iconUrl", "")),
                            language = obj.optString("lang", "en"),
                            nsfw = obj.optBoolean("nsfw", false),
                            description = obj.optString("description", "")
                        )
                    )
                }
            } else if (trimmed.startsWith("{")) {
                val root = JSONObject(trimmed)
                val repos = root.optJSONArray("repos") ?: root.optJSONArray("extensions") ?: root.optJSONArray("plugins")
                if (repos != null) {
                    for (i in 0 until repos.length()) {
                        val obj = repos.getJSONObject(i)
                        list.add(
                            ExtensionRepositoryItem(
                                id = obj.optString("id", obj.optString("pkg", "ext_$i")),
                                name = obj.optString("name", obj.optString("title", "Extension $i")),
                                version = obj.optString("version", "1.0.0"),
                                apkUrl = obj.optString("apk", obj.optString("url", "")),
                                iconUrl = obj.optString("icon", obj.optString("iconUrl", "")),
                                language = obj.optString("lang", "en"),
                                nsfw = obj.optBoolean("nsfw", false),
                                description = obj.optString("description", "")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            /* Fallback parsing */
        }
        return list
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
