package app.kumo.beta.repository

import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

object RepositoryParser {
    fun parse(url: String, body: String): RepositoryResult {
        val obj = runCatching { JSONObject(body) }.getOrNull()
        if (obj != null) {
            if (obj.has("pluginLists")) return parseCloudStreamRepo(url, obj)
            if (obj.has("pkg") && obj.has("apk")) return parseAniyomi(url, JSONArray().put(obj))
        }
        val array = runCatching { JSONArray(body) }.getOrNull()
            ?: return RepositoryResult.Failure(RepositoryResult.Reason.INVALID_JSON, "Invalid repository JSON")
        if (array.length() == 0)
            return RepositoryResult.Failure(RepositoryResult.Reason.EMPTY_REPOSITORY, "Repository returned no extensions")
        return when {
            looksCloudStream(array) -> RepositoryResult.Success(
                Repository(url.hashCode().toString(), url, type = RepositoryType.CLOUDSTREAM_PLUGIN_LIST),
                cloudStreamEntries(url, url, array)
            )
            looksAniyomi(array) -> parseAniyomi(url, array)
            else -> RepositoryResult.Failure(RepositoryResult.Reason.UNSUPPORTED_FORMAT, "Unsupported repository format")
        }
    }

    private fun parseCloudStreamRepo(url: String, root: JSONObject): RepositoryResult {
        val lists = root.optJSONArray("pluginLists") ?: JSONArray()
        val extensions = mutableListOf<ExtensionInfo>()
        for (i in 0 until lists.length()) {
            val listUrl = resolve(url, lists.optString(i))
            val list = runCatching {
                val connection = URI(listUrl).toURL().openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 12000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Kumo/0.2")
                try {
                    require(connection.responseCode in 200..299) { "HTTP " + connection.responseCode }
                    connection.inputStream.bufferedReader().use { it.readText() }
                } finally {
                    connection.disconnect()
                }
            }.getOrNull() ?: continue

            val array = runCatching { JSONArray(list) }.getOrNull() ?: continue
            extensions += cloudStreamEntries(url, listUrl, array)
        }
        return RepositoryResult.Success(
            Repository(
                id = url.hashCode().toString(),
                url = url,
                name = root.optString("name", url),
                description = root.optString("description"),
                type = RepositoryType.CLOUDSTREAM
            ),
            extensions.distinctBy { it.id }
        )
    }

    private fun cloudStreamEntries(repoUrl: String, baseUrl: String, array: JSONArray): List<ExtensionInfo> =
        buildList {
            for (i in 0 until array.length()) {
                val o = array.optJSONObject(i) ?: continue
                val download = o.optString("url")
                if (download.isBlank()) continue
                val name = o.optString("name", o.optString("internalName", download))
                val internal = o.optString("internalName").takeIf { it.isNotBlank() }
                add(
                    ExtensionInfo(
                        id = "cloudstream:" + (internal ?: name.lowercase().replace(Regex("[^a-z0-9]+"), "-")),
                        name = name,
                        internalName = internal,
                        repositoryId = repoUrl.hashCode().toString(),
                        repositoryUrl = repoUrl,
                        type = RepositoryType.CLOUDSTREAM_PLUGIN_LIST,
                        downloadUrl = resolve(baseUrl, download),
                        version = o.optString("version").takeIf { it.isNotBlank() },
                        language = o.optString("language").takeIf { it.isNotBlank() },
                        description = o.optString("description").takeIf { it.isNotBlank() },
                        iconUrl = o.optString("iconUrl").takeIf { it.isNotBlank() },
                        fileSize = if (o.has("fileSize")) o.optLong("fileSize") else null,
                        fileHash = o.optString("fileHash").takeIf { it.isNotBlank() }
                    )
                )
            }
        }

    private fun parseAniyomi(url: String, array: JSONArray): RepositoryResult {
        val repoId = url.hashCode().toString()
        val extensions = buildList {
            for (i in 0 until array.length()) {
                val o = array.optJSONObject(i) ?: continue
                val pkg = o.optString("pkg")
                val apk = o.optString("apk")
                if (pkg.isBlank() || apk.isBlank()) continue
                add(
                    ExtensionInfo(
                        id = "aniyomi:" + pkg,
                        name = o.optString("name", pkg),
                        packageName = pkg,
                        repositoryId = repoId,
                        repositoryUrl = url,
                        type = RepositoryType.ANIYOMI,
                        downloadUrl = resolve(url, apk),
                        language = o.optString("lang").takeIf { it.isNotBlank() },
                        versionCode = if (o.has("code")) o.optLong("code") else null,
                        version = o.optString("version").takeIf { it.isNotBlank() },
                        nsfw = o.optBoolean("nsfw", false)
                    )
                )
            }
        }
        if (extensions.isEmpty())
            return RepositoryResult.Failure(RepositoryResult.Reason.EMPTY_REPOSITORY, "No Aniyomi extensions found")
        return RepositoryResult.Success(Repository(repoId, url, url, type = RepositoryType.ANIYOMI), extensions)
    }

    private fun looksCloudStream(a: JSONArray): Boolean {
        val o = a.optJSONObject(0) ?: return false
        return o.has("url") || o.has("internalName")
    }

    private fun looksAniyomi(a: JSONArray): Boolean {
        val o = a.optJSONObject(0) ?: return false
        return o.has("pkg") && o.has("apk")
    }

    private fun resolve(base: String, value: String): String =
        runCatching { URI(base).resolve(value).toString() }.getOrDefault(value)
}
