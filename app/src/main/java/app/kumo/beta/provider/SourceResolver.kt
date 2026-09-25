package app.kumo.beta.provider

import app.kumo.beta.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SourceResolver(private val registry: ProviderRegistry) {
    suspend fun resolve(episode: Episode): List<KumoStreamSource> = withContext(Dispatchers.IO) {
        coroutineScope {
            registry.getEnabledProviders()
                .map { provider ->
                    async {
                        withTimeoutOrNull(12_000L) { provider.getSources(episode) }.orEmpty()
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
}
