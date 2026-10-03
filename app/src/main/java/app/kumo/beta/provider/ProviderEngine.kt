package app.kumo.beta.provider

import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class ProviderEngine(private val registry: ProviderRegistry, private val health: ProviderHealthStore? = null) {
    suspend fun search(query: String, type: MediaType? = null): List<KumoSearchResult> = withContext(Dispatchers.IO) {
        coroutineScope {
            registry.getEnabledProviders()
                .filter { type == null || type in it.supportedTypes }
                .filter { health?.canTry(it.id) != false }
                .map { provider ->
                    async {
                        val result = withTimeoutOrNull(12_000L) {
                            runCatching {
                                if (type == null) provider.search(query) else provider.search(query, type)
                            }
                        }
                        when {
                            result == null -> {
                                health?.markFailure(provider.id)
                                emptyList()
                            }
                            result.isSuccess -> {
                                health?.markSuccess(provider.id)
                                result.getOrDefault(emptyList())
                            }
                            else -> {
                                health?.markFailure(provider.id)
                                emptyList()
                            }
                        }
                    }
                }
                .awaitAll()
                .flatten()
                .let(::mergeTitles)
        }
    }

    suspend fun catalog(section: String, type: MediaType? = null): List<KumoSearchResult> = withContext(Dispatchers.IO) {
        coroutineScope {
            registry.getEnabledProviders()
                .filter { type == null || type in it.supportedTypes }
                .filter { health?.canTry(it.id) != false }
                .map { provider ->
                    async {
                        val result = withTimeoutOrNull(12_000L) {
                            runCatching {
                                if (type == null) provider.getCatalog(section) else provider.getCatalog(section, type)
                            }
                        }
                        when {
                            result == null -> {
                                health?.markFailure(provider.id)
                                emptyList()
                            }
                            result.isSuccess -> {
                                health?.markSuccess(provider.id)
                                result.getOrDefault(emptyList())
                            }
                            else -> {
                                health?.markFailure(provider.id)
                                emptyList()
                            }
                        }
                    }
                }.awaitAll().flatten().let(::mergeTitles)
        }
    }

    suspend fun load(title: Title): Title = withContext(Dispatchers.IO) {
        val providers = registry.getEnabledProviders()
            .filter { title.type in it.supportedTypes }
            .filter { title.providerIds.isEmpty() || it.id in title.providerIds }

        for (provider in providers) {
            if (health?.canTry(provider.id) == false) continue
            val providerTitleId = title.providerTitleIds[provider.id] ?: title.id
            val scopedTitle = title.copy(id = providerTitleId)
            val result = withTimeoutOrNull(12_000L) {
                runCatching { provider.load(scopedTitle) }
            }
            val loaded = result?.getOrNull()?.takeIf { it.title.isNotBlank() }
            if (loaded != null) {
                health?.markSuccess(provider.id)
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
            health?.markFailure(provider.id)
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
                    val loadedResult = withTimeoutOrNull(12_000L) {
                        runCatching { provider.load(scopedTitle) }
                    }
                    val loaded = loadedResult?.getOrNull() ?: scopedTitle
                    val episodesResult = withTimeoutOrNull(12_000L) {
                        runCatching { provider.getEpisodes(loaded) }
                    }
                    when {
                        loadedResult?.isSuccess == true || episodesResult?.isSuccess == true -> health?.markSuccess(provider.id)
                        else -> health?.markFailure(provider.id)
                    }
                    provider to episodesResult?.getOrDefault(emptyList()).orEmpty()
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
            .groupBy { (it.seasonNumber ?: 1) to it.number }
            .mapNotNull { (_, sameNumber) ->
                val best = sameNumber.firstOrNull { !it.title.isNullOrBlank() } ?: sameNumber.firstOrNull()
                best?.copy(
                    providerIds = sameNumber.flatMap { it.providerIds }.distinct(),
                    providerEpisodeIds = sameNumber
                        .flatMap { it.providerEpisodeIds.entries }
                        .associate { it.key to it.value }
                )
            }
            .sortedWith(compareBy({ it.seasonNumber ?: 1 }, { it.number }))

        val bestLoaded = loadedTitles
            .map { it.first }
            .maxByOrNull { score(it) }
            ?: title

        val finalEpisodes = if (mergedEpisodes.isNotEmpty()) mergedEpisodes else bestLoaded.episodes
        val finalSeasons = if (finalEpisodes.any { it.seasonNumber != null }) {
            finalEpisodes
                .groupBy { it.seasonNumber ?: 1 }
                .toSortedMap()
                .map { (season, eps) ->
                    app.kumo.beta.model.Season(
                        seasonNumber = season,
                        name = "Season " + season,
                        status = "Not Started",
                        episodes = eps.sortedBy { it.number }
                    )
                }
        } else {
            bestLoaded.seasons
        }

        bestLoaded.copy(
            id = title.id,
            providerIds = title.providerIds.ifEmpty { listOf(loadedTitles.maxByOrNull { score(it.first) }?.first?.id ?: bestLoaded.id) },
            providerTitleIds = title.providerTitleIds,
            episodes = finalEpisodes,
            seasons = finalSeasons
        )
    }

    private fun mergeTitles(results: List<KumoSearchResult>): List<KumoSearchResult> {
        val grouped = results.groupBy { result ->
            val normalizedTitle = normalize(result.title.title)
            val identity = normalizedTitle.ifBlank {
                result.providerId + ":" + result.title.id
            }
            result.title.type.name + ":" + identity
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

    private fun normalize(value: String): String =
        value.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")

    private fun score(title: Title) =
        (title.rating?.times(10f)?.toInt() ?: 0) + if (title.posterUrl != null) 5 else 0
}
