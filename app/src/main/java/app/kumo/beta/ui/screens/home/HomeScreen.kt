package app.kumo.beta.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import app.kumo.beta.data.DemoData
import app.kumo.beta.data.LibraryStore
import app.kumo.beta.data.local.SettingsPreferencesStore
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import app.kumo.beta.ui.components.ContinueWatchingCard
import app.kumo.beta.ui.components.TitleCard
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun HomeScreen(
    onNavigateToDetails: (String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSearchWithFilter: () -> Unit = {},
    onTitleClick: (Title) -> Unit = {},
    onVoiceSearch: () -> Unit = {}
) {
    val context = LocalContext.current
    val libraryStore = remember { LibraryStore(context) }
    val settingsStore = remember { SettingsPreferencesStore(context) }
    val settings = settingsStore.get()
    val allTitles = remember { DemoData.allTitles }
    val featuredTitle = remember { allTitles.firstOrNull() }
    val continueWatchingList = remember { libraryStore.getContinueWatching() }
    val popularAnimeState = rememberLazyListState()

    LaunchedEffect(popularAnimeState) {
        while (isActive) {
            delay(3500)
            val count = allTitles.count { it.type == MediaType.ANIME }
            if (count > 1) {
                val next = (popularAnimeState.firstVisibleItemIndex + 1) % count
                popularAnimeState.animateScrollToItem(next)
            }
        }
    }

    fun openTitle(title: Title) {
        onNavigateToDetails(title.id)
        onTitleClick(title)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onNavigateToSearch() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Text("Search anime, movies, manga...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onNavigateToSearchWithFilter,
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(23.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(
                onClick = onVoiceSearch,
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(23.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice search", tint = MaterialTheme.colorScheme.primary)
            }
        }

        featuredTitle?.let { title ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { openTitle(title) }
            ) {
                if (!title.backdropUrl.isNullOrEmpty()) {
                    AsyncImage(title.backdropUrl, title.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), Color(0xFF0F0F18)))))
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)), startY = 100f)))
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                        Text("KUMO V2 BUILD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(title.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(title.genres.joinToString(" • "), fontSize = 12.sp, color = Color.LightGray, maxLines = 1)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { openTitle(title) }, shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Open", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text("Popular Anime", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyRow(
            state = popularAnimeState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allTitles.filter { it.type == MediaType.ANIME }, key = { it.id }) { title ->
                TitleCard(title = title, onClick = { openTitle(title) })
            }
        }

        Spacer(Modifier.height(20.dp))

        if (settings.showContinueWatching && settings.continueWatching && continueWatchingList.isNotEmpty()) {
            Text("Continue Watching", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(continueWatchingList) { prog ->
                    DemoData.getById(prog.contentId)?.let { item ->
                        val epNum = prog.episodeId.substringAfterLast("-").toIntOrNull() ?: 1
                        val percent = if (prog.durationMs > 0) prog.positionMs.toFloat() / prog.durationMs else 0.5f
                        ContinueWatchingCard(item, epNum, percent) { openTitle(item) }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        if (settings.showPopular) {
        Text("Popular Right Now", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(allTitles) { title -> TitleCard(title = title, onClick = { openTitle(title) }) }
        }

        }

        Spacer(Modifier.height(20.dp))
        if (settings.showNewReleases) {
        Text("Movies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(allTitles.filter { it.type == MediaType.MOVIE }) { title -> TitleCard(title = title, onClick = { openTitle(title) }) }
        }        }

    }
}
