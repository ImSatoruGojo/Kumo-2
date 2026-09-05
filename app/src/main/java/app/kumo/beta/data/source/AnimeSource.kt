package app.kumo.beta.data.source

import app.kumo.beta.model.MediaType

interface AnimeSource {
    val id: String
    val name: String
    val lang: String
    val baseUrl: String
    val supportedTypes: List<MediaType>

    suspend fun searchAnime(query: String): List<SourceSearchResult>
    suspend fun fetchEpisodeList(animeId: String): List<SourceEpisode>
    suspend fun fetchVideoLinks(episodeId: String): List<StreamLink>
}
