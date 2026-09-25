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
    override val supportedTypes: Set<MediaType> = setOf(MediaType.ANIME)

    override suspend fun search(query: String): List<KumoSearchResult> =
        request("https://api.jikan.moe/v4/anime?q=" + URLEncoder.encode(query, "UTF-8") + "&sfw=true&limit=20")
            .optJSONArray("data")?.let { array ->
                (0 until array.length()).mapNotNull { i ->
                    array.optJSONObject(i)?.toTitle()?.let { KumoSearchResult(it, id) }
                }
            }.orEmpty()

    override suspend fun load(title: Title): Title {
        val malId = title.id.removePrefix("mal:")
        return request("https://api.jikan.moe/v4/anime/" + malId + "/full").optJSONObject("data")?.toTitle(title) ?: title
    }

    override suspend fun getEpisodes(title: Title): List<Episode> {
        val malId = title.id.removePrefix("mal:")
        val data = request("https://api.jikan.moe/v4/anime/" + malId + "/episodes?limit=100").optJSONArray("data") ?: return emptyList()
        return (0 until data.length()).mapNotNull { i ->
            data.optJSONObject(i)?.let { ep ->
                Episode("jikan:" + malId + ":" + ep.optInt("mal_id", i + 1), ep.optInt("mal_id", i + 1), ep.optString("title").takeIf { it.isNotBlank() })
            }
        }
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

    private fun JSONObject.toTitle(existing: Title? = null): Title {
        val malId = optInt("mal_id")
        val images = optJSONObject("images")?.optJSONObject("jpg")
        val genres = optJSONArray("genres")
        val genreList = if (genres == null) emptyList() else (0 until genres.length()).mapNotNull {
            genres.optJSONObject(it)?.optString("name")?.takeIf(String::isNotBlank)
        }
        return (existing ?: Title(\n                id = "mal:" + malId,\n                title = optString("title").ifBlank { optString("title_english") },\n                type = MediaType.ANIME,\n                description = optString("synopsis")\n            )).copy(
            title = optString("title_english").ifBlank { optString("title") },
            description = optString("synopsis"),
            genres = genreList,
            year = optJSONObject("aired")?.optString("from")?.take(4)?.toIntOrNull(),
            posterUrl = images?.optString("large_image_url")?.takeIf(String::isNotBlank) ?: images?.optString("image_url")?.takeIf(String::isNotBlank),
            rating = optDouble("score").takeIf { it > 0 }?.toFloat()
        )
    }
}
