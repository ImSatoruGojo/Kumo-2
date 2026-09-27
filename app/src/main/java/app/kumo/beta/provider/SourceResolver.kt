package app.kumo.beta.provider

import app.kumo.beta.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SourceResolver(private val registry: ProviderRegistry) {

    suspend fun resolve(
        episode: Episode,
        allowFallback: Boolean = true
    ): List<KumoStreamSource> = withContext(Dispatchers.IO) {
        val providers = registry.getEnabledProviders()
        if (providers.isEmpty()) return@withContext emptyList()

        val selectedProviders = if (allowFallback) providers else providers.take(1)

        val rawSources = coroutineScope {
            selectedProviders.flatMap { provider ->
                val sources = withTimeoutOrNull(12_000L) {
                    runCatching { provider.getSources(episode) }.getOrDefault(emptyList())
                }.orEmpty()
                sources.map { source ->
                    val providerId = source.providerId.ifBlank { provider.id }
                    source.copy(providerId = providerId)
                }
            }
        }

        val enrichedSources = coroutineScope {
            rawSources.map { source ->
                async {
                    val providerIds = source.providerId.split(" + ").filter { it.isNotBlank() }
                    val provider = providerIds.asSequence()
                        .mapNotNull(registry::getProvider)
                        .firstOrNull()
                    if (provider == null || source.subtitles.isNotEmpty()) {
                        source
                    } else {
                        val subtitles = withTimeoutOrNull(8_000L) {
                            runCatching { provider.getSubtitles(source) }.getOrDefault(emptyList())
                        }.orEmpty()
                        source.copy(subtitles = subtitles)
                    }
                }
            }.awaitAll()
        }

        return@withContext enrichedSources
            .groupBy { source -> source.url.trim() }
            .map { (_, sameUrl) -> combine(sameUrl) }
            .sortedWith(
                compareByDescending<KumoStreamSource> { it.audioType.equals("Dub", ignoreCase = true) }
                    .thenByDescending { it.quality ?: 0 }
                    .thenBy { it.language ?: "" }
                    .thenBy { it.providerId }
            )
    }

    private fun combine(sources: List<KumoStreamSource>): KumoStreamSource {
        val first = sources.first()
        val audioTracks = sources
            .flatMap { it.audioTracks }
            .distinctBy { listOf(it.id, it.language, it.label).joinToString("|") }

        val subtitles = sources
            .flatMap { it.subtitles }
            .distinctBy { listOf(it.url, it.language, it.format).joinToString("|") }

        val headers = buildMap {
            sources.forEach { putAll(it.headers) }
        }

        return first.copy(
            quality = sources.mapNotNull { it.quality }.maxOrNull() ?: first.quality,
            language = sources.firstNotNullOfOrNull { it.language } ?: first.language,
            audioType = sources.firstOrNull { it.audioType.equals("Dub", ignoreCase = true) }?.audioType
                ?: sources.firstNotNullOfOrNull { it.audioType }
                ?: first.audioType,
            audioTracks = audioTracks,
            subtitles = subtitles,
            headers = headers,
            referer = sources.firstNotNullOfOrNull { it.referer },
            mimeType = sources.firstNotNullOfOrNull { it.mimeType },
            providerId = sources.map { it.providerId }.distinct().joinToString(" + ")
        )
    }
}
