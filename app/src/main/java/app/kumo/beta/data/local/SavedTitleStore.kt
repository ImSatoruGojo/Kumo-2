package app.kumo.beta.data.local

import android.content.Context
import app.kumo.beta.model.Chapter
import app.kumo.beta.model.Episode
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Season
import app.kumo.beta.model.Title
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists the metadata needed to keep Library entries usable after an app restart
 * while the full provider payload remains cached separately
 */
class SavedTitleStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kumo_saved_titles", Context.MODE_PRIVATE)

    fun get(id: String): Title? =
        prefs.getString(key(id), null)?.let { decode(runCatching { JSONObject(it) }.getOrNull()) }

    fun getAll(): List<Title> =
        prefs.all.values.mapNotNull { value ->
            if (value !is String) null else decode(runCatching { JSONObject(value) }.getOrNull())
        }.distinctBy { it.id }

    fun put(title: Title) {
        prefs.edit().putString(key(title.id), encode(title).toString()).apply()
    }

    fun putAll(titles: Iterable<Title>) {
        val editor = prefs.edit()
        titles.forEach { editor.putString(key(it.id), encode(it).toString()) }
        editor.apply()
    }

    fun remove(id: String) {
        prefs.edit().remove(key(id)).apply()
    }

    private fun key(id: String) = "title_" + id

    private fun encode(title: Title): JSONObject = JSONObject().apply {
        put("id", title.id)
        put("title", title.title)
        put("originalTitle", title.originalTitle ?: "")
        put("englishTitle", title.englishTitle ?: "")
        put("romanizedTitle", title.romanizedTitle ?: "")
        put("japaneseTitle", title.japaneseTitle ?: "")
        put("altTitles", JSONArray(title.altTitles))
        put("type", title.type.name)
        put("description", title.description)
        put("genres", JSONArray(title.genres))
        title.year?.let { put("year", it) }
        put("releaseDate", title.releaseDate ?: "")
        put("posterUrl", title.posterUrl ?: "")
        put("backdropUrl", title.backdropUrl ?: "")
        put("bannerUrl", title.bannerUrl ?: "")
        put("logoUrl", title.logoUrl ?: "")
        title.rating?.let { put("rating", it.toDouble()) }
        title.ratingCount?.let { put("ratingCount", it) }
        put("contentAgeRating", title.contentAgeRating ?: "")
        title.runtimeMinutes?.let { put("runtimeMinutes", it) }
        put("status", title.status)
        put("studio", title.studio ?: "")
        put("director", title.director ?: "")
        put("cast", JSONArray(title.cast))
        put("seasons", JSONArray().apply { title.seasons.forEach { put(encodeSeason(it)) } })
        put("episodes", JSONArray().apply { title.episodes.forEach { put(encodeEpisode(it)) } })
        put("chapters", JSONArray().apply {
            title.chapters.forEach {
                put(JSONObject().apply {
                    put("id", it.id)
                    put("number", it.number)
                    put("title", it.title ?: "")
                })
            }
        })
        put("relatedTitles", JSONArray(title.relatedTitles))
        put("providerIds", JSONArray(title.providerIds))
        put("providerTitleIds", JSONObject(title.providerTitleIds))
    }

    private fun encodeSeason(season: Season): JSONObject = JSONObject().apply {
        put("seasonNumber", season.seasonNumber)
        put("name", season.name)
        put("status", season.status)
        put("episodes", JSONArray().apply { season.episodes.forEach { put(encodeEpisode(it)) } })
    }

    private fun encodeEpisode(episode: Episode): JSONObject = JSONObject().apply {
        put("id", episode.id)
        put("number", episode.number)
        episode.seasonNumber?.let { put("seasonNumber", it) }
        put("title", episode.title ?: "")
        put("description", episode.description ?: "")
        put("thumbnailUrl", episode.thumbnailUrl ?: "")
        episode.durationMs?.let { put("durationMs", it) }
        put("isWatched", episode.isWatched)
        put("providerIds", JSONArray(episode.providerIds))
        put("providerEpisodeIds", JSONObject(episode.providerEpisodeIds))
    }

    private fun decode(root: JSONObject?): Title? {
        if (root == null) return null
        return runCatching {
            val type = MediaType.valueOf(root.optString("type", MediaType.ANIME.name))
            val episodes = decodeEpisodes(root.optJSONArray("episodes"))
            val seasonsArray = root.optJSONArray("seasons")
            val seasons = if (seasonsArray == null) emptyList() else buildList {
                for (i in 0 until seasonsArray.length()) {
                    val season = seasonsArray.optJSONObject(i) ?: continue
                    add(
                        Season(
                            seasonNumber = season.optInt("seasonNumber", i + 1),
                            name = season.optString("name", "Season " + (i + 1)),
                            status = season.optString("status", "Not Started"),
                            episodes = decodeEpisodes(season.optJSONArray("episodes"))
                        )
                    )
                }
            }
            val chaptersArray = root.optJSONArray("chapters")
            val chapters = if (chaptersArray == null) emptyList() else buildList {
                for (i in 0 until chaptersArray.length()) {
                    val chapter = chaptersArray.optJSONObject(i) ?: continue
                    add(
                        Chapter(
                            id = chapter.optString("id"),
                            number = chapter.optInt("number", i + 1),
                            title = chapter.optString("title").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
            Title(
                id = root.getString("id"),
                title = root.optString("title"),
                originalTitle = root.optString("originalTitle").takeIf { it.isNotBlank() },
                englishTitle = root.optString("englishTitle").takeIf { it.isNotBlank() },
                romanizedTitle = root.optString("romanizedTitle").takeIf { it.isNotBlank() },
                japaneseTitle = root.optString("japaneseTitle").takeIf { it.isNotBlank() },
                altTitles = stringList(root.optJSONArray("altTitles")),
                type = type,
                description = root.optString("description"),
                genres = stringList(root.optJSONArray("genres")),
                year = root.optInt("year", 0).takeIf { it != 0 },
                releaseDate = root.optString("releaseDate").takeIf { it.isNotBlank() },
                posterUrl = root.optString("posterUrl").takeIf { it.isNotBlank() },
                backdropUrl = root.optString("backdropUrl").takeIf { it.isNotBlank() },
                bannerUrl = root.optString("bannerUrl").takeIf { it.isNotBlank() },
                logoUrl = root.optString("logoUrl").takeIf { it.isNotBlank() },
                rating = root.optDouble("rating", Double.NaN).takeUnless(Double::isNaN)?.toFloat(),
                ratingCount = root.optInt("ratingCount", 0).takeIf { it != 0 },
                contentAgeRating = root.optString("contentAgeRating").takeIf { it.isNotBlank() },
                runtimeMinutes = root.optInt("runtimeMinutes", 0).takeIf { it != 0 },
                status = root.optString("status", "Ongoing"),
                studio = root.optString("studio").takeIf { it.isNotBlank() },
                director = root.optString("director").takeIf { it.isNotBlank() },
                cast = stringList(root.optJSONArray("cast")),
                seasons = seasons,
                episodes = episodes,
                chapters = chapters,
                relatedTitles = stringList(root.optJSONArray("relatedTitles")),
                providerIds = stringList(root.optJSONArray("providerIds")),
                providerTitleIds = stringMap(root.optJSONObject("providerTitleIds"))
            )
        }.getOrNull()
    }

    private fun decodeEpisodes(array: JSONArray?): List<Episode> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val episode = array.optJSONObject(i) ?: continue
                add(
                    Episode(
                        id = episode.optString("id"),
                        number = episode.optInt("number", i + 1),
                        title = episode.optString("title").takeIf { it.isNotBlank() },
                        seasonNumber = episode.optInt("seasonNumber", 0).takeIf { it != 0 },
                        description = episode.optString("description").takeIf { it.isNotBlank() },
                        thumbnailUrl = episode.optString("thumbnailUrl").takeIf { it.isNotBlank() },
                        durationMs = episode.optLong("durationMs", 0L).takeIf { it != 0L },
                        isWatched = episode.optBoolean("isWatched", false),
                        providerIds = stringList(episode.optJSONArray("providerIds")),
                        providerEpisodeIds = stringMap(episode.optJSONObject("providerEpisodeIds"))
                    )
                )
            }
        }
    }

    private fun stringMap(obj: JSONObject?): Map<String, String> =
        if (obj == null) emptyMap() else obj.keys().asSequence().associateWith { obj.optString(it) }.filterValues { it.isNotBlank() }

    private fun stringList(array: JSONArray?): List<String> =
        if (array == null) emptyList() else (0 until array.length())
            .mapNotNull { i -> array.optString(i).takeIf { it.isNotBlank() } }
}
