package app.kumo.beta.data.source

import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class UnifiedRepository(
    private val sources: List<AnimeSource> = emptyList()
) {

    private fun normalizeTitle(rawTitle: String): String {
        return rawTitle
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }

    suspend fun searchAndUnify(query: String): List<UnifiedAnime> = coroutineScope {
        if (query.isBlank()) return@coroutineScope emptyList()

        // Broadcast query concurrently across all active/installed sources
        val deferredResults = sources.map { source ->
            async {
                try {
                    source.searchAnime(query)
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        val allResults: List<SourceSearchResult> = deferredResults.awaitAll().flatten()

        // Group incoming raw results by normalized title string
        val grouped: Map<String, List<SourceSearchResult>> = allResults.groupBy { normalizeTitle(it.title) }

        // Collapse duplicate results into single UnifiedAnime domain objects
        grouped.map { (normKey, resultGroup) ->
            val first = resultGroup.first()
            UnifiedAnime(
                normalizedTitle = normKey,
                primaryTitle = first.title,
                posterUrl = resultGroup.mapNotNull { it.posterUrl }.firstOrNull(),
                type = first.type,
                genres = resultGroup.flatMap { it.genres }.distinct(),
                year = resultGroup.mapNotNull { it.year }.firstOrNull(),
                rating = resultGroup.mapNotNull { it.rating }.firstOrNull(),
                matchedSources = resultGroup,
                aggregatedEpisodes = emptyMap()
            )
        }
    }

    fun unifiedToTitle(unified: UnifiedAnime): Title {
        return Title(
            id = "unified:${unified.normalizedTitle}",
            title = unified.primaryTitle,
            type = unified.type,
            description = "Aggregated result from ${unified.matchedSources.size} extension sources.",
            genres = unified.genres,
            year = unified.year,
            posterUrl = unified.posterUrl,
            rating = unified.rating,
            status = "Ongoing"
        )
    }
}
