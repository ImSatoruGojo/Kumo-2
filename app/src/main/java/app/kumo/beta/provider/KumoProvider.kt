package app.kumo.beta.provider

import app.kumo.beta.model.Episode
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title

data class KumoAudioTrack(
    val id: String,
    val language: String? = null,
    val label: String? = null,
    val isDefault: Boolean = false
)

data class KumoSearchResult(val title: Title, val providerId: String)

data class KumoStreamSource(
    val url: String,
    val quality: Int? = null,
    val language: String? = null,
    val audioType: String? = null,
    val audioTracks: List<KumoAudioTrack> = emptyList(),
    val subtitles: List<KumoSubtitle> = emptyList(),
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
    suspend fun getCatalog(section: String): List<KumoSearchResult> = emptyList()
}