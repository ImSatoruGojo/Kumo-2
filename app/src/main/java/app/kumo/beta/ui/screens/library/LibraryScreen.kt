package app.kumo.beta.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.CatalogStore
import app.kumo.beta.data.local.LibraryCategory
import app.kumo.beta.data.local.LibraryManager
import app.kumo.beta.data.local.SavedTitleStore
import app.kumo.beta.data.local.SettingsPreferencesStore
import app.kumo.beta.model.Title
import app.kumo.beta.ui.components.PosterCard

@Composable
fun LibraryScreen(
    onNavigateToDetails: (String) -> Unit = {},
    onTitleClick: (Title) -> Unit = {}
) {
    val context = LocalContext.current
    val libraryManager = remember { LibraryManager(context) }
    val savedTitleStore = remember { SavedTitleStore(context) }
    val settingsStore = remember { SettingsPreferencesStore(context) }
    val settings = settingsStore.get()
    var selectedTab by remember { mutableIntStateOf(0) }

    val categories = LibraryCategory.entries
    val currentCategory = categories.getOrNull(selectedTab) ?: LibraryCategory.FAVORITES
    val titleIds = libraryManager.getTitlesInCategory(currentCategory).toList()
    val titlesInCategory = titleIds.mapNotNull { id ->
        CatalogStore.get(id) ?: savedTitleStore.get(id)?.also(CatalogStore::put)
    }.let { titles ->
        when (settings.librarySortOrder) {
            "Recently added" -> titles.sortedByDescending { it.id }
            "Rating" -> titles.sortedByDescending { it.rating ?: -1f }
            "Year" -> titles.sortedByDescending { it.year ?: 0 }
            else -> titles.sortedBy { it.title.lowercase() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Library",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    currentCategory.displayName + " • " + titlesInCategory.size + " titles",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                val next = when (settings.librarySortOrder) {
                    "Title A to Z" -> "Recently added"
                    "Recently added" -> "Rating"
                    "Rating" -> "Year"
                    else -> "Title A to Z"
                }
                settingsStore.setLibrarySortOrder(next)
            }) {
                Text("Sort: " + settings.librarySortOrder)
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            category.displayName,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (titlesInCategory.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Nothing here yet",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Add titles from their details page",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(settings.itemsPerRow.coerceIn(2, 5)),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(titlesInCategory, key = { it.id }) { title ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        PosterCard(
                            title = title,
                            onClick = {
                                CatalogStore.put(title)
                                onNavigateToDetails(title.id)
                                onTitleClick(title)
                            }
                        )
                    }
                }
            }
        }
    }
}
