package app.kumo.beta.provider

import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class ProviderEngine(private val registry: ProviderRegistry) {
    suspend fun search(query: String, type: MediaType? = null): List<KumoSearchResult> = withContext(Dispatchers.IO) {
        coroutineScope {
            registry.getEnabledProviders()
                .filter { type == null || type in it.supportedTypes }
                .map { provider ->
                    async {
                        withTimeoutOrNull(12_000L) {
                            if (type == null) provider.search(query) else provider.search(query, type)
                        }.orEmpty()
                    }
                }
                .awaitAll()
                .flatten()
                .let(::mergeTitles)
        }
    }

    suspend fun catalog(section: String, type: MediaType? = null): List<KumoSearchResult> = withContext(Dispatchers.IO) {
        coroutineScope {
            registry.getEnabledProviders().filter { type == null || type in it.supportedTypes }.map { provider ->
                async {
                    withTimeoutOrNull(12_000L) {
                        if (type == null) provider.getCatalog(section) else provider.getCatalog(section, type)
                    }.orEmpty()
                }
            }.awaitAll().flatten().let(::mergeTitles)
        }
    }

    suspend fun load(title: Title): Title = withContext(Dispatchers.IO) {
        val providers = registry.getEnabledProviders()
            .filter { title.type in it.supportedTypes }
            .filter { title.providerIds.isEmpty() || it.id in title.providerIds }

        for (provider in providers) {
            val providerTitleId = title.providerTitleIds[provider.id] ?: title.id
            val scopedTitle = title.copy(id = providerTitleId)
            runCatching { provider.load(scopedTitle) }
                .getOrNull()
                ?.takeIf { it.title.isNotBlank() }
                ?.let { loaded ->
                    return@withContext loaded.copy(
                        id = title.id,
                        providerIds = title.providerIds.ifEmpty { listOf(provider.id) },
                        providerTitleIds = if (title.providerTitleIds.isEmpty()) {
                            mapOf(provider.id to providerTitleId)
                        } else {
                            title.providerTitleIds
                        }
                    )
                }
        }
        title
    }

    suspend fun episodes(title: Title): Title = withContext(Dispatchers.IO) {
        val providers = registry.getEnabledProviders().filter { title.type in it.supportedTypes }
        if (providers.isEmpty()) return@withContext title

        val loadedTitles = coroutineScope {
            providers.map { provider ->
                async {
                    if (title.providerIds.isNotEmpty() && provider.id !in title.providerIds) {
                        return@async provider to emptyList()
                    }
                    val providerTitleId = title.providerTitleIds[provider.id] ?: title.id
                    val scopedTitle = title.copy(id = providerTitleId)
                    val loaded = withTimeoutOrNull(12_000L) { provider.load(scopedTitle) } ?: scopedTitle
                    val episodes = withTimeoutOrNull(12_000L) { provider.getEpisodes(loaded) }.orEmpty()
                    provider to episodes
                }
            }.awaitAll()
        }

        val mergedEpisodes = loadedTitles
            .flatMap { (provider, episodes) ->
                episodes.map { episode ->
                    episode.copy(
                        providerIds = (episode.providerIds + provider.id).distinct(),
                        providerEpisodeIds = episode.providerEpisodeIds + (provider.id to episode.id)
                    )
                }
            }
            .groupBy { it.number }
            .mapNotNull { (_, sameNumber) ->
                val best = sameNumber.firstOrNull { !it.title.isNullOrBlank() } ?: sameNumber.firstOrNull()
                best?.copy(
                    providerIds = sameNumber.flatMap { it.providerIds }.distinct(),
                    providerEpisodeIds = sameNumber
                        .flatMap { it.providerEpisodeIds.entries }
                        .associate { it.key to it.value }
                )
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
        val grouped = results.groupBy {
            it.title.type.name + ":" + normalize(it.title.title)
        }
        return grouped.values.mapNotNull { group ->
            val best = group.maxByOrNull { score(it.title) } ?: return@mapNotNull null
            val providerTitleIds = buildMap {
                group.forEach { put(it.providerId, it.title.id) }
            }
            val mergedTitle = best.title.copy(
                providerIds = group.map { it.providerId }.distinct(),
                providerTitleIds = providerTitleIds
            )
            KumoSearchResult(
                title = mergedTitle,
                providerId = best.providerId
            )
        }
    }

    private fun normalize(value: String) =
        value.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    private fun score(title: Title) =
        (title.rating?.times(10f)?.toInt() ?: 0) + if (title.posterUrl != null) 5 else 0
}
