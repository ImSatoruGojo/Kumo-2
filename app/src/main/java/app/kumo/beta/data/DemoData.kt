package app.kumo.beta.data

import app.kumo.beta.model.Chapter
import app.kumo.beta.model.Episode
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Season
import app.kumo.beta.model.Title

object DemoData {

    private fun eps(count: Int, prefix: String = "Episode"): List<Episode> =
        (1..count).map {
            Episode(
                id = "$prefix-$it",
                number = it,
                title = "$prefix $it",
                description = "Full HD episode stream with multi-sub & dub audio tracks.",
                thumbnailUrl = "https://picsum.photos/300/180?random=$it"
            )
        }

    private fun chs(count: Int): List<Chapter> =
        (1..count).map { Chapter(id = "ch-$it", number = it, title = "Chapter $it") }

    val allTitles: List<Title> = listOf(
        // Anime with Seasons
        Title(
            id = "kumo:title:skybound-hero",
            title = "Skybound Hero",
            englishTitle = "Skybound Hero",
            japaneseTitle = "天空の勇者",
            romanizedTitle = "Tenkuu no Yuusha",
            altTitles = listOf("Tenkuu no Yuusha", "Skybound Legend"),
            type = MediaType.ANIME,
            description = "A young warrior discovers an ancient power that can reshape the skies. Action-packed journey through floating kingdoms.",
            genres = listOf("Action", "Fantasy", "Adventure"),
            year = 2024,
            releaseDate = "Jan 12, 2024",
            rating = 4.8f,
            ratingCount = 14250,
            contentAgeRating = "TV-14",
            runtimeMinutes = 24,
            status = "Ongoing",
            studio = "Kumo Animation",
            director = "Satoru Gojo",
            cast = listOf("Hero A", "Heroine B", "Rival C"),
            seasons = listOf(
                Season(seasonNumber = 1, name = "Season 1: Skybound Origin", status = "Completed", episodes = eps(12, "S1 Ep")),
                Season(seasonNumber = 2, name = "Season 2: Floating Realm War", status = "Currently Watching", episodes = eps(12, "S2 Ep"))
            ),
            episodes = eps(12, "S2 Ep")
        ),
        Title(
            id = "kumo:title:magic-academy",
            title = "Magic Academy",
            altTitles = listOf("Mahou Gakuen", "Sorcery High"),
            type = MediaType.ANIME,
            description = "Students at the elite Magic Academy face trials that test more than just their spells.",
            genres = listOf("Fantasy", "Adventure", "School"),
            year = 2023,
            rating = 4.6f,
            status = "Completed",
            seasons = listOf(
                Season(seasonNumber = 1, name = "Season 1", status = "Completed", episodes = eps(12, "S1 Ep")),
                Season(seasonNumber = 2, name = "Season 2", status = "Completed", episodes = eps(12, "S2 Ep"))
            ),
            episodes = eps(24)
        ),
        Title(
            id = "kumo:title:another-realm",
            title = "Another Realm",
            type = MediaType.ANIME,
            description = "Suddenly transported to another world, an ordinary student must adapt to survive.",
            genres = listOf("Isekai", "Fantasy", "Action"),
            year = 2025,
            episodes = eps(12)
        ),
        Title(
            id = "kumo:title:one-piece-demo",
            title = "One Piece",
            type = MediaType.ANIME,
            description = "Follow Monkey D. Luffy and his pirate crew in their search for the ultimate treasure.",
            genres = listOf("Action", "Adventure", "Comedy"),
            year = 1999,
            episodes = eps(20)
        ),

        // Movies
        Title(
            id = "kumo:title:night-city",
            title = "Night City",
            type = MediaType.MOVIE,
            description = "In a neon-soaked metropolis, a lone operative uncovers a conspiracy that reaches the highest levels.",
            genres = listOf("Action", "Sci-Fi"),
            year = 2024
        ),
        Title(
            id = "kumo:title:last-horizon",
            title = "Last Horizon",
            type = MediaType.MOVIE,
            description = "An expedition to the edge of the known world discovers something that should have stayed buried.",
            genres = listOf("Adventure", "Mystery"),
            year = 2023
        ),

        // TV Shows
        Title(
            id = "kumo:title:the-investigation",
            title = "The Investigation",
            type = MediaType.SHOW,
            description = "A brilliant detective and a reluctant partner solve cases that no one else can.",
            genres = listOf("Mystery", "Crime", "Drama"),
            year = 2022,
            episodes = eps(10, "Episode")
        ),
        Title(
            id = "kumo:title:kingdom-roads",
            title = "Kingdom Roads",
            type = MediaType.SHOW,
            description = "Political intrigue and personal rivalries unfold across a fractured kingdom.",
            genres = listOf("Drama", "Historical"),
            year = 2021,
            episodes = eps(8)
        ),

        // Cartoons
        Title(
            id = "kumo:title:pixel-planet",
            title = "Pixel Planet",
            type = MediaType.CARTOON,
            description = "A group of friends explore a world made entirely of pixels and code.",
            genres = listOf("Comedy", "Adventure", "Kids"),
            year = 2020,
            episodes = eps(16)
        ),
        Title(
            id = "kumo:title:robot-rangers",
            title = "Robot Rangers",
            type = MediaType.CARTOON,
            description = "Five teenagers pilot giant robots to defend Earth from interdimensional threats.",
            genres = listOf("Action", "Adventure", "Mecha"),
            year = 2019,
            episodes = eps(22)
        ),

        // Manga
        Title(
            id = "kumo:title:blade-chronicle",
            title = "Blade Chronicle",
            type = MediaType.MANGA,
            description = "A legendary sword chooses a new wielder in a world of warring clans.",
            genres = listOf("Action", "Fantasy"),
            year = 2022,
            chapters = chs(45)
        ),
        Title(
            id = "kumo:title:zero-kingdom",
            title = "Zero Kingdom",
            type = MediaType.MANGA,
            description = "In a world where magic has been outlawed, a young mage fights to restore balance.",
            genres = listOf("Adventure", "Magic"),
            year = 2023,
            chapters = chs(30)
        )
    )

    fun byType(type: MediaType): List<Title> = allTitles.filter { it.type == type }

    fun search(query: String): List<Title> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return allTitles
            .map { title ->
                val score = when {
                    title.title.lowercase() == q -> 100
                    title.title.lowercase().startsWith(q) -> 80
                    title.title.lowercase().contains(q) -> 60
                    title.genres.any { it.lowercase().contains(q) } -> 40
                    else -> 0
                }
                title to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    fun getById(id: String): Title? = allTitles.find { it.id == id }
}
