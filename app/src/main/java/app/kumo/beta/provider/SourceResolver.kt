package app.kumo.beta.provider

import app.kumo.beta.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SourceResolver(private val registry: ProviderRegistry, private val health: ProviderHealthStore? = null) {

    suspend fun resolve(
        episode: Episode,
        allowFallback: Boolean = true
    ): List<KumoStreamSource> = withContext(Dispatchers.IO) {
        val providers = registry.getEnabledProviders()
        if (providers.isEmpty()) return@withContext emptyList()

        val matchingProviders = if (episode.providerIds.isEmpty()) providers else {
            providers.sortedByDescending { if (it.id in episode.providerIds) 1 else 0 }
        }
        val selectedProviders = (if (allowFallback) matchingProviders else matchingProviders.take(1))
            .filter { health?.canTry(it.id) != false }

        val rawSources = coroutineScope {
            selectedProviders.map { provider ->
                async {
                    val providerEpisodeId = episode.providerEpisodeIds[provider.id] ?: episode.id
                    val result = withTimeoutOrNull(12_000L) {
                        runCatching { provider.getSources(episode.copy(id = providerEpisodeId)) }
                    }
                    when {
                        result == null -> {
                            health?.markFailure(provider.id)
                            provider to emptyList<KumoStreamSource>()
                        }
                        result.isSuccess -> {
                            health?.markSuccess(provider.id)
                            provider to result.getOrDefault(emptyList())
                        }
                        else -> {
                            health?.markFailure(provider.id)
                            provider to emptyList()
                        }
                    }
                }
            }.awaitAll().flatMap { (provider, sources) ->
                sources.map { source ->
                    source.copy(providerId = source.providerId.ifBlank { provider.id })
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
            introStartMs = sources.firstNotNullOfOrNull { it.introStartMs },
            introEndMs = sources.firstNotNullOfOrNull { it.introEndMs },
            providerId = sources.map { it.providerId }.distinct().joinToString(" + ")
        )
    }
}
