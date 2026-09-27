package app.kumo.beta.provider

import app.kumo.beta.model.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class ChapterResolver(private val registry: ProviderRegistry) {
    suspend fun resolve(chapter: Chapter): List<String> = withContext(Dispatchers.IO) {
        coroutineScope {
            registry.getProvidersForMediaType(app.kumo.beta.model.MediaType.MANGA)
                .map { provider ->
                    async {
                        withTimeoutOrNull(12_000L) {
                            runCatching { provider.getChapterPages(chapter) }.getOrDefault(emptyList())
                        }.orEmpty()
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
