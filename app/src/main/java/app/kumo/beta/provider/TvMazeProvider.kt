package app.kumo.beta.provider

import app.kumo.beta.model.Episode
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Season
import app.kumo.beta.model.Title
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class TvMazeProvider : KumoProvider {
    override val id = "tvmaze"
    override val name = "TVmaze"
    override val language: String? = "en"
    override val supportedTypes: Set<MediaType> = setOf(MediaType.SHOW, MediaType.CARTOON)

    override suspend fun search(query: String): List<KumoSearchResult> =
        search(query, MediaType.SHOW)

    override suspend fun search(query: String, type: MediaType): List<KumoSearchResult> {
        if (type != MediaType.SHOW && type != MediaType.CARTOON) return emptyList()
        val encoded = URLEncoder.encode(query, "UTF-8")
        val array = request("https://api.tvmaze.com/search/shows?q=" + encoded)
        return (0 until array.length()).mapNotNull { i ->
            array.optJSONObject(i)?.optJSONObject("show")?.toTitle()?.takeIf { it.type == type }?.let {
                KumoSearchResult(it, id)
            }
        }
    }

    override suspend fun getCatalog(section: String): List<KumoSearchResult> =
        getCatalog(section, MediaType.SHOW)

    override suspend fun getCatalog(section: String, type: MediaType): List<KumoSearchResult> {
        if (type != MediaType.SHOW && type != MediaType.CARTOON) return emptyList()
        val page = when (section.lowercase()) {
            "popular", "trending", "top_rated", "new_releases", "new" -> 0
            else -> 0
        }
        val array = request("https://api.tvmaze.com/shows?page=" + page)
        return (0 until array.length())
            .mapNotNull { array.optJSONObject(it)?.toTitle() }
            .filter { it.type == type }
            .let { list ->
                if (section.lowercase() == "top_rated") {
                    list.sortedByDescending { it.rating ?: -1f }
                } else {
                    list.sortedByDescending { it.year ?: 0 }
                }
            }
            .take(20)
            .map { KumoSearchResult(it, id) }
    }

    override suspend fun load(title: Title): Title {
        val mazeId = title.id.removePrefix("tvmaze:")
        val show = requestObject("https://api.tvmaze.com/shows/" + mazeId + "?embed=episodes")
        return show.toTitle(title.type, title)
    }

    override suspend fun getEpisodes(title: Title): List<Episode> {
        val mazeId = title.id.removePrefix("tvmaze:")
        val show = requestObject("https://api.tvmaze.com/shows/" + mazeId + "?embed=episodes")
        val array = show.optJSONObject("_embedded")?.optJSONArray("episodes") ?: return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            val episode = array.optJSONObject(i) ?: return@mapNotNull null
            val number = episode.optInt("number", 0)
            if (number <= 0) return@mapNotNull null
            val season = episode.optInt("season", 1).coerceAtLeast(1)
            val episodeId = episode.optInt("id", i + 1)
            Episode(
                id = "tvmaze:" + mazeId + ":" + episodeId,
                number = number,
                title = episode.optString("name").takeIf(String::isNotBlank),
                seasonNumber = season,
                durationMs = episode.optLong("runtime", 0L).takeIf { it > 0L }?.times(60_000L),
                thumbnailUrl = episode.optJSONObject("image")?.optString("original")
                    ?.takeIf(String::isNotBlank),
                description = episode.optString("summary").stripHtml().takeIf(String::isNotBlank),
                providerIds = listOf(id),
                providerEpisodeIds = mapOf(id to ("tvmaze:" + mazeId + ":" + episodeId))
            )
        }.sortedWith(compareBy({ it.seasonNumber ?: 1 }, { it.number }))
    }

    override suspend fun getSources(episode: Episode): List<KumoStreamSource> = emptyList()

    override suspend fun getSubtitles(source: KumoStreamSource): List<KumoSubtitle> = emptyList()

    private fun JSONObject.toTitle(
        typeOverride: MediaType? = null,
        existing: Title? = null
    ): Title {
        val name = optString("name").ifBlank { existing?.title.orEmpty() }
        val genres = optJSONArray("genres")
        val genreList = if (genres == null) emptyList() else {
            (0 until genres.length()).mapNotNull { genres.optString(it).takeIf(String::isNotBlank) }
        }
        val mappedType = typeOverride ?: if (
            genreList.any { it.equals("Animation", true) || it.equals("Children", true) }
        ) MediaType.CARTOON else MediaType.SHOW
        val premiered = optString("premiered")
        return (existing ?: Title(
            id = "tvmaze:" + optInt("id"),
            title = name,
            type = mappedType,
            description = optString("summary").stripHtml()
        )).copy(
            id = existing?.id ?: ("tvmaze:" + optInt("id")),
            title = name,
            type = mappedType,
            description = optString("summary").stripHtml(),
            genres = genreList,
            year = premiered.take(4).toIntOrNull(),
            releaseDate = premiered.takeIf(String::isNotBlank),
            posterUrl = optJSONObject("image")?.optString("original")?.takeIf(String::isNotBlank)
                ?: optJSONObject("image")?.optString("medium")?.takeIf(String::isNotBlank),
            rating = optJSONObject("rating")?.optDouble("average", 0.0)?.takeIf { it > 0 }?.toFloat(),
            runtimeMinutes = optInt("averageRuntime", optInt("runtime", 0)).takeIf { it > 0 },
            status = optString("status").ifBlank { "Unknown" },
            contentAgeRating = null,
            cast = emptyList(),
            seasons = embeddedSeasons()
        )
    }

    private fun JSONObject.embeddedSeasons(): List<Season> {
        val episodes = optJSONObject("_embedded")?.optJSONArray("episodes") ?: return emptyList()
        val grouped = mutableMapOf<Int, MutableList<Episode>>()
        for (i in 0 until episodes.length()) {
            val ep = episodes.optJSONObject(i) ?: continue
            val n = ep.optInt("number", 0)
            val s = ep.optInt("season", 1)
            val id = ep.optInt("id", i + 1)
            if (n > 0) {
                grouped.getOrPut(s) { mutableListOf() }.add(
                    Episode(
                        id = "tvmaze:" + optInt("id") + ":" + id,
                        number = n,
                        title = ep.optString("name").takeIf(String::isNotBlank),
                        seasonNumber = s
                    )
                )
            }
        }
        return grouped.toSortedMap().map { (season, eps) ->
            Season(
                seasonNumber = season,
                name = "Season " + season,
                status = "Not Started",
                episodes = eps.sortedBy { it.number }
            )
        }
    }

    private fun request(url: String): org.json.JSONArray {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("User-Agent", "Kumo/0.2")
        return try {
            require(connection.responseCode in 200..299) { "TVmaze HTTP " + connection.responseCode }
            connection.inputStream.bufferedReader().use { org.json.JSONArray(it.readText()) }
        } finally {
            connection.disconnect()
        }
    }

    private fun requestObject(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("User-Agent", "Kumo/0.2")
        return try {
            require(connection.responseCode in 200..299) { "TVmaze HTTP " + connection.responseCode }
            connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
        } finally {
            connection.disconnect()
        }
    }

    private fun String.stripHtml(): String =
        replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&quot;", """)
            .replace("&#39;", "'")
            .trim()
}
