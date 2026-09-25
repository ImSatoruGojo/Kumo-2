package app.kumo.beta.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.LibraryStore
import app.kumo.beta.data.local.SettingsPreferencesStore
import app.kumo.beta.data.local.LibraryCategory
import app.kumo.beta.data.local.LibraryManager
import app.kumo.beta.model.Progress
import app.kumo.beta.model.Title
import app.kumo.beta.model.typeLabel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    title: Title,
    onBack: () -> Unit,
    onEpisodeClick: (app.kumo.beta.model.Episode) -> Unit = {},
) {
    val context = LocalContext.current
    val libManager = remember { LibraryManager(context) }
    val libraryStore = remember { LibraryStore(context) }

    var selectedCategory by remember { mutableStateOf(libManager.getCategoryForTitle(title.id)) }
    var isFavorite by remember { mutableStateOf(libManager.isFavorite(title.id)) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var pendingDownload by remember { mutableStateOf<app.kumo.beta.model.Episode?>(null) }
    val settings = remember { SettingsPreferencesStore(context).get() }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 40.dp)
    ) {
        // BACKDROP HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color(0xFF14141E))
        ) {
            if (!title.backdropUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = title.backdropUrl,
                    contentDescription = title.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Top Bar Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        val newFav = !isFavorite
                        isFavorite = newFav
                        libManager.setFavorite(title.id, newFav)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            }
        }

        // CONTENT SUMMARY HEADER
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = title.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = title.type.typeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                title.year?.let { y ->
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "$y", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                title.rating?.let { r ->
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "★ %.1f".format(r),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB020)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ACTION BUTTONS (Add to Library / Resume)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showCategoryMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (selectedCategory != null) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = selectedCategory?.displayName ?: "Add to Library")
                    }

                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        LibraryCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat
                                    libManager.setCategoryForTitle(title.id, cat)
                                    showCategoryMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Remove from Library", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                selectedCategory = null
                                libManager.setCategoryForTitle(title.id, null)
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GENRE CHIPS
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                title.genres.forEach { genre ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(genre, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DESCRIPTION
            Text(
                text = "Synopsis",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { isDescriptionExpanded = !isDescriptionExpanded }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // CLOUDSTREAM-STYLE SEASON SELECTOR & EPISODE SYSTEM
            var selectedSeasonIndex by remember { mutableIntStateOf(0) }
            var showSeasonMenu by remember { mutableStateOf(false) }
            var episodeQuery by remember { mutableStateOf("") }
            var sortAscending by remember { mutableStateOf(true) }

            val currentSeasons = title.seasons.ifEmpty {
                listOf(app.kumo.beta.model.Season(seasonNumber = 1, name = "Season 1", status = "Currently Watching", episodes = title.episodes))
            }
            val activeSeason = currentSeasons.getOrNull(selectedSeasonIndex) ?: currentSeasons.first()

            val displayedEpisodes = remember(activeSeason, episodeQuery, sortAscending) {
                var list = activeSeason.episodes
                if (episodeQuery.isNotBlank()) {
                    list = list.filter {
                        (it.title ?: "").contains(episodeQuery, ignoreCase = true) ||
                                "Episode ${it.number}".contains(episodeQuery, ignoreCase = true)
                    }
                }
                if (!sortAscending) {
                    list = list.reversed()
                }
                list
            }

            if (currentSeasons.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { showSeasonMenu = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeSeason.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Status: ${activeSeason.status} • ${activeSeason.episodes.size} Episodes",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Season", tint = Color.White)
                        }
                    }

                    DropdownMenu(
                        expanded = showSeasonMenu,
                        onDismissRequest = { showSeasonMenu = false }
                    ) {
                        currentSeasons.forEachIndexed { index, season ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(season.name, fontWeight = FontWeight.Bold)
                                        Text("${season.status} • ${season.episodes.size} Episodes", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedSeasonIndex = index
                                    showSeasonMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // EPISODE SEARCH & SORT BAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = episodeQuery,
                        onValueChange = { episodeQuery = it },
                        placeholder = { Text("Search episode...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    IconButton(
                        onClick = { sortAscending = !sortAscending },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (sortAscending) Icons.AutoMirrored.Filled.Sort else Icons.Default.FilterList,
                            contentDescription = "Sort Order",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                var activeEpisodeToPlay by remember { mutableStateOf<app.kumo.beta.model.Episode?>(null) }

                displayedEpisodes.forEach { ep ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                libraryStore.saveProgress(
                                    Progress(
                                        contentId = title.id,
                                        episodeId = ep.id,
                                        positionMs = 0,
                                        durationMs = ep.durationMs ?: 0
                                    )
                                )
                                onEpisodeClick(ep)
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ep.title ?: "Episode ${ep.number}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(text = ep.durationMs?.let { formatDuration(it) } ?: "Duration unavailable", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { if (settings.confirmDownloads) pendingDownload = ep else onDownloadEpisode(ep) }) {
                                        Icon(Icons.Default.Download, contentDescription = "Download", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (title.chapters.isNotEmpty()) {
                Text(
                    text = "Chapters (${title.chapters.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                var activeChapterToRead by remember { mutableStateOf<app.kumo.beta.model.Chapter?>(null) }

                title.chapters.forEach { ch ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { activeChapterToRead = ch },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Read",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ch.title ?: "Chapter ${ch.number}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(text = "Chapter ${ch.number}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                activeChapterToRead?.let { ch ->
                    var readingMode by remember { mutableStateOf("Webtoon (Vertical)") }

                    AlertDialog(
                        onDismissRequest = { activeChapterToRead = null },
                        title = { Text("Reading ${title.title} - ${ch.title ?: "Chapter ${ch.number}"}") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = "Manga Page",
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text("Page 1 / 42 • Mode: $readingMode", color = Color.White, fontSize = 12.sp)
                                    }
                                }

                                Text("Reading Mode Selection", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Webtoon", "RTL Single", "LTR Single", "Continuous").forEach { mode ->
                                        FilterChip(
                                            selected = readingMode == mode,
                                            onClick = { readingMode = mode },
                                            label = { Text(mode, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { activeChapterToRead = null }) {
                                Text("Close Reader")
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalMinutes = (durationMs / 60_000L).toInt().coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) hours.toString() + "h " + minutes + "m" else minutes.toString() + "m"

    pendingDownload?.let { episode ->
        AlertDialog(
            onDismissRequest = { pendingDownload = null },
            title = { Text("Download episode?") },
            text = { Text("Save ${episode.title ?: "Episode " + episode.number} to your selected download folder") },
            confirmButton = {
                TextButton(onClick = { pendingDownload = null; onDownloadEpisode(episode) }) { Text("Download") }
            },
            dismissButton = { TextButton(onClick = { pendingDownload = null }) { Text("Cancel") } }
        )
    }
}
