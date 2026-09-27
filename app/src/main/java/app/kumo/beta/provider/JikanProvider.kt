package app.kumo.beta.provider

import app.kumo.beta.model.Episode
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class JikanProvider : KumoProvider {
    override val id = "jikan"
    override val name = "Jikan"
    override val language: String? = "en"
    override val supportedTypes: Set<MediaType> = setOf(MediaType.ANIME, MediaType.MANGA)

    override suspend fun getCatalog(section: String): List<KumoSearchResult> =
        getCatalog(section, MediaType.ANIME)

    override suspend fun getCatalog(section: String, type: MediaType): List<KumoSearchResult> {
        val mediaPath = if (type == MediaType.MANGA) "manga" else "anime"
        val endpoint = when {
            type == MediaType.MANGA && section.lowercase() in setOf("popular", "trending") ->
                "https://api.jikan.moe/v4/top/manga?filter=bypopularity&limit=20"
            type == MediaType.MANGA && section.lowercase() in setOf("top", "top_rated") ->
                "https://api.jikan.moe/v4/top/manga?limit=20"
            type == MediaType.MANGA && section.lowercase() in setOf("new", "new_releases") ->
                "https://api.jikan.moe/v4/manga?order_by=published&sort=desc&limit=20"
            type == MediaType.ANIME && section.lowercase() in setOf("popular", "trending") ->
                "https://api.jikan.moe/v4/top/anime?filter=bypopularity&limit=20"
            type == MediaType.ANIME && section.lowercase() in setOf("top", "top_rated") ->
                "https://api.jikan.moe/v4/top/anime?limit=20"
            type == MediaType.ANIME && section.lowercase() in setOf("new", "new_releases") ->
                "https://api.jikan.moe/v4/anime?order_by=aired&sort=desc&limit=20"
            else -> return emptyList()
        }
        val array = request(endpoint).optJSONArray("data") ?: return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            array.optJSONObject(i)?.toTitle(type)?.let { KumoSearchResult(it, id) }
        }
    }

    override suspend fun search(query: String): List<KumoSearchResult> =
        search(query, MediaType.ANIME)

    override suspend fun search(query: String, type: MediaType): List<KumoSearchResult> {
        val endpoint = when (type) {
            MediaType.ANIME -> "https://api.jikan.moe/v4/anime?q=" + URLEncoder.encode(query, "UTF-8") + "&sfw=true&limit=20"
            MediaType.MANGA -> "https://api.jikan.moe/v4/manga?q=" + URLEncoder.encode(query, "UTF-8") + "&limit=20"
            else -> return emptyList()
        }
        return request(endpoint).optJSONArray("data")?.let { array ->
            (0 until array.length()).mapNotNull { i ->
                array.optJSONObject(i)?.toTitle(type)?.let { KumoSearchResult(it, id) }
            }
        }.orEmpty()
    }

    override suspend fun load(title: Title): Title {
        val malId = title.id.removePrefix("mal:")
        val path = if (title.type == MediaType.MANGA) "manga" else "anime"
        return request("https://api.jikan.moe/v4/" + path + "/" + malId + "/full").optJSONObject("data")
            ?.toTitle(title.type, title)
            ?: title
    }

    override suspend fun getEpisodes(title: Title): List<Episode> {
        val malId = title.id.removePrefix("mal:")
        val result = mutableListOf<Episode>()
        var page = 1
        var hasNext = true
        while (hasNext && page <= 20) {
            val root = request("https://api.jikan.moe/v4/anime/" + malId + "/episodes?limit=100&page=" + page)
            val data = root.optJSONArray("data") ?: break
            for (i in 0 until data.length()) {
                data.optJSONObject(i)?.let { ep ->
                    result += Episode(
                        id = "jikan:" + malId + ":" + ep.optInt("mal_id", result.size + 1),
                        number = ep.optInt("mal_id", result.size + 1),
                        title = ep.optString("title").takeIf { it.isNotBlank() }
                    )
                }
            }
            hasNext = root.optJSONObject("pagination")?.optBoolean("has_next_page", false) == true
            page++
        }
        return result.distinctBy { it.number }.sortedBy { it.number }
    }

    override suspend fun getSources(episode: Episode): List<KumoStreamSource> = emptyList()
    override suspend fun getSubtitles(source: KumoStreamSource): List<KumoSubtitle> = emptyList()

    private fun request(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 15000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "Kumo/0.2")
        return connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
    }

    private fun JSONObject.toTitle(type: MediaType = MediaType.ANIME, existing: Title? = null): Title {
        val malId = optInt("mal_id")
        val images = optJSONObject("images")?.optJSONObject("jpg")
        val genres = optJSONArray("genres")
        val genreList = if (genres == null) emptyList() else (0 until genres.length()).mapNotNull {
            genres.optJSONObject(it)?.optString("name")?.takeIf(String::isNotBlank)
        }
        return (existing ?: Title(
            id = "mal:" + malId,
            title = optString("title").ifBlank { optString("title_english") },
            type = type,
            description = optString("synopsis")
        )).copy(
            title = optString("title_english").ifBlank { optString("title") },
            description = optString("synopsis"),
            genres = genreList,
            year = (optJSONObject(if (type == MediaType.MANGA) "published" else "aired")?.optString("from")?.take(4)?.toIntOrNull()),
            posterUrl = images?.optString("large_image_url")?.takeIf(String::isNotBlank)
                ?: images?.optString("image_url")?.takeIf(String::isNotBlank),
            rating = optDouble("score").takeIf { it > 0 }?.toFloat()
        )
    }
}