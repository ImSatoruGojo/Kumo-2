package app.kumo.beta.model

enum class MediaType {
    ANIME, MOVIE, SHOW, CARTOON, MANGA
}

val MediaType.typeLabel: String
    get() = when (this) {
        MediaType.ANIME -> "Anime"
        MediaType.MOVIE -> "Movie"
        MediaType.SHOW -> "TV Show"
        MediaType.CARTOON -> "Cartoon"
        MediaType.MANGA -> "Manga"
    }

data class Title(
    val id: String,
    val title: String,
    val altTitles: List<String> = emptyList(),
    val type: MediaType,
    val description: String,
    val genres: List<String> = emptyList(),
    val year: Int? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val rating: Float? = null,
    val status: String = "Ongoing",
    val seasons: List<Season> = emptyList(),
    val episodes: List<Episode> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val relatedTitles: List<String> = emptyList()
)

data class Season(
    val seasonNumber: Int,
    val name: String = "Season $seasonNumber",
    val status: String = "Not Started", // "Completed", "Currently Watching", "Not Started"
    val episodes: List<Episode> = emptyList()
)

data class Episode(
    val id: String,
    val number: Int,
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val durationMs: Long? = null,
    val isWatched: Boolean = false
)

data class Chapter(
    val id: String,
    val number: Int,
    val title: String? = null
)

data class Progress(
    val contentId: String,
    val episodeId: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

// PROVIDER INTERFACES & CAPABILITY ARCHITECTURE

interface BaseProvider {
    val id: String
    val name: String
    val baseUrl: String
    val lang: String
    val logoUrl: String?
    val version: Int
    val supportedTypes: List<MediaType>

    suspend fun search(query: String): List<Title>
    suspend fun getDetails(titleId: String): Title?
}

interface AnimeProvider : BaseProvider {
    suspend fun getStreamUrls(episodeId: String): List<VideoSource>
}

interface MovieProvider : BaseProvider {
    suspend fun getStreamUrls(mediaId: String): List<VideoSource>
}

interface TVProvider : BaseProvider {
    suspend fun getStreamUrls(season: Int, episode: Int): List<VideoSource>
}

interface CartoonProvider : BaseProvider {
    suspend fun getStreamUrls(episodeId: String): List<VideoSource>
}

interface MangaProvider : BaseProvider {
    suspend fun getChapterPages(chapterId: String): List<String>
}

data class VideoSource(
    val url: String,
    val quality: String,
    val format: String = "m3u8",
    val headers: Map<String, String> = emptyMap()
)
