package app.kumo.beta.data.source

import app.kumo.beta.model.MediaType

data class SourceSearchResult(
    val id: String,
    val sourceId: String,
    val title: String,
    val posterUrl: String? = null,
    val type: MediaType = MediaType.ANIME,
    val year: Int? = null,
    val rating: Float? = null,
    val genres: List<String> = emptyList()
)

data class SourceEpisode(
    val id: String,
    val sourceId: String,
    val episodeNumber: Int,
    val title: String? = null,
    val thumbnailUrl: String? = null
)

data class StreamLink(
    val sourceName: String,
    val url: String,
    val quality: String,
    val format: String = "m3u8",
    val headers: Map<String, String> = emptyMap()
)

data class UnifiedAnime(
    val normalizedTitle: String,
    val primaryTitle: String,
    val posterUrl: String?,
    val type: MediaType,
    val genres: List<String>,
    val year: Int?,
    val rating: Float?,
    val matchedSources: List<SourceSearchResult>,
    val aggregatedEpisodes: Map<Int, List<StreamLink>>
)
