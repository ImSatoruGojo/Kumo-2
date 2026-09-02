package app.kumo.beta.model

enum class MediaType {
    ANIME, MOVIE, SHOW, CARTOON, MANGA
}

data class Title(
    val id: String,
    val title: String,
    val type: MediaType,
    val description: String,
    val genres: List<String> = emptyList(),
    val year: Int? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val rating: Float? = null,
    val episodes: List<Episode> = emptyList(),
    val chapters: List<Chapter> = emptyList()
)

data class Episode(
    val id: String,
    val number: Int,
    val title: String? = null,
    val durationMs: Long? = null
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
