package app.kumo.beta.provider

import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class ProviderEngine(private val registry: ProviderRegistry) {
    suspend fun search(query: String, type: MediaType? = null): List<KumoSearchResult> = withContext(Dispatchers.IO) {
        coroutineScope {
            registry.getEnabledProviders()
                .filter { type == null || type in it.supportedTypes }
                .map { provider ->
                    async {
                        runCatching { provider.search(query) }.getOrDefault(emptyList())
                    }
                }
                .awaitAll()
                .flatten()
                .let(::mergeTitles)
        }
    }

    suspend fun load(title: Title): Title = withContext(Dispatchers.IO) {
        for (provider in registry.getEnabledProviders().filter { title.type in it.supportedTypes }) {
            runCatching { provider.load(title) }
                .getOrNull()
                ?.takeIf { it.title.isNotBlank() }
                ?.let { return@withContext it }
        }
        title
    }

    suspend fun episodes(title: Title): Title = withContext(Dispatchers.IO) {
        val providers = registry.getEnabledProviders().filter { title.type in it.supportedTypes }
        if (providers.isEmpty()) return@withContext title

        val loadedTitles = coroutineScope {
            providers.map { provider ->
                async {
                    val loaded = runCatching { provider.load(title) }.getOrNull() ?: title
                    val episodes = runCatching { provider.getEpisodes(loaded) }.getOrDefault(emptyList())
                    loaded to episodes
                }
            }.awaitAll()
        }

        val mergedEpisodes = loadedTitles
            .flatMap { it.second }
            .groupBy { it.number }
            .mapNotNull { (_, sameNumber) ->
                sameNumber.firstOrNull { !it.title.isNullOrBlank() } ?: sameNumber.firstOrNull()
            }
            .sortedBy { it.number }

        val bestLoaded = loadedTitles
            .map { it.first }
            .maxByOrNull { score(it) }
            ?: title

        bestLoaded.copy(
            episodes = if (mergedEpisodes.isNotEmpty()) mergedEpisodes else bestLoaded.episodes
        )
    }

    private fun mergeTitles(results: List<KumoSearchResult>): List<KumoSearchResult> {
        val merged = LinkedHashMap<String, KumoSearchResult>()
        results.forEach { result ->
            val key = normalize(result.title.title)
            val old = merged[key]
            if (old == null || score(result.title) > score(old.title)) {
                merged[key] = result
            }
        }
        return merged.values.toList()
    }

    private fun normalize(value: String) =
        value.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    private fun score(title: Title) =
        (title.rating?.times(10f)?.toInt() ?: 0) + if (title.posterUrl != null) 5 else 0
}
