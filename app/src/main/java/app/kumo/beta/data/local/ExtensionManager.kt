package app.kumo.beta.data.local

import android.content.Context
import android.content.SharedPreferences

enum class ExtensionType {
    ANIME, MOVIE_TV, MANGA, CARTOON
}

data class ExtensionItem(
    val id: String,
    val name: String,
    val version: String,
    val type: ExtensionType,
    val iconUrl: String,
    val author: String,
    val isInstalled: Boolean,
    val isEnabled: Boolean,
    val hasUpdate: Boolean = false,
    val description: String
)

class ExtensionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kumo_extensions", Context.MODE_PRIVATE)

    private val defaultExtensions = listOf(
        ExtensionItem(
            id = "ext_anikoto",
            name = "Anikoto Anime",
            version = "1.2.4",
            type = ExtensionType.ANIME,
            iconUrl = "https://picsum.photos/100/100?random=11",
            author = "Kumo Official",
            isInstalled = true,
            isEnabled = true,
            hasUpdate = false,
            description = "High quality subbed and dubbed anime provider."
        ),
        ExtensionItem(
            id = "ext_cinemahd",
            name = "Cinema HD",
            version = "2.0.1",
            type = ExtensionType.MOVIE_TV,
            iconUrl = "https://picsum.photos/100/100?random=12",
            author = "Community",
            isInstalled = true,
            isEnabled = true,
            hasUpdate = true,
            description = "Movies and TV shows streams with multi-source backup."
        ),
        ExtensionItem(
            id = "ext_mangacloud",
            name = "Manga Cloud",
            version = "0.9.5",
            type = ExtensionType.MANGA,
            iconUrl = "https://picsum.photos/100/100?random=13",
            author = "MangaDev",
            isInstalled = true,
            isEnabled = false,
            hasUpdate = false,
            description = "Manga reader extension with fast page loading."
        ),
        ExtensionItem(
            id = "ext_cartoonverse",
            name = "Cartoon Verse",
            version = "1.1.0",
            type = ExtensionType.CARTOON,
            iconUrl = "https://picsum.photos/100/100?random=14",
            author = "Kumo Official",
            isInstalled = false,
            isEnabled = false,
            hasUpdate = false,
            description = "Classic and modern animated cartoon catalog."
        )
    )

    fun getExtensions(): List<ExtensionItem> {
        return defaultExtensions
    }
}
