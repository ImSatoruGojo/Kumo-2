package app.kumo.beta.provider

import app.kumo.beta.model.Episode
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title

data class KumoSearchResult(val title: Title, val providerId: String)

data class KumoStreamSource(
    val url: String,
    val quality: Int? = null,
    val language: String? = null,
    val audioType: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val referer: String? = null,
    val mimeType: String? = null,
    val providerId: String
)

data class KumoSubtitle(
    val url: String,
    val language: String? = null,
    val format: String? = null
)

interface KumoProvider {
    val id: String
    val name: String
    val language: String?
    val supportedTypes: Set<MediaType>

    suspend fun search(query: String): List<KumoSearchResult>
    suspend fun load(title: Title): Title
    suspend fun getEpisodes(title: Title): List<Episode>
    suspend fun getSources(episode: Episode): List<KumoStreamSource>
    suspend fun getSubtitles(source: KumoStreamSource): List<KumoSubtitle>
}
