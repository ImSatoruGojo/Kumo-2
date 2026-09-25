package app.kumo.beta.provider

import app.kumo.beta.model.Episode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SourceResolver(private val registry: ProviderRegistry) {
    suspend fun resolve(episode: Episode): List<KumoStreamSource> = coroutineScope {
        registry.getEnabledProviders()
            .map { provider ->
                async {
                    runCatching { provider.getSources(episode) }.getOrDefault(emptyList())
                }
            }
            .awaitAll()
            .flatten()
            .distinctBy {
                listOf(it.url, it.quality, it.language, it.audioType).joinToString("|")
            }
            .sortedWith(
                compareByDescending<KumoStreamSource> { it.quality ?: 0 }
                    .thenBy { it.language ?: "" }
            )
    }
}
