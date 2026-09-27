package app.kumo.beta.provider

import app.kumo.beta.model.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class ChapterResolver(private val registry: ProviderRegistry, private val health: ProviderHealthStore? = null) {
    suspend fun resolve(chapter: Chapter): List<String> = withContext(Dispatchers.IO) {
        coroutineScope {
            registry.getProvidersForMediaType(app.kumo.beta.model.MediaType.MANGA)
                .filter { health?.canTry(it.id) != false }
                .map { provider ->
                    async {
                        val result = withTimeoutOrNull(12_000L) {
                            runCatching { provider.getChapterPages(chapter) }
                        }
                        when {
                            result == null -> { health?.markFailure(provider.id); emptyList() }
                            result.isSuccess -> { health?.markSuccess(provider.id); result.getOrDefault(emptyList()) }
                            else -> { health?.markFailure(provider.id); emptyList() }
                        }
                    }
                }
                .awaitAll()
                .flatten()
                .map(String::trim)
                .filter { it.startsWith("http://") || it.startsWith("https://") }
                .distinct()
        }
    }
}
