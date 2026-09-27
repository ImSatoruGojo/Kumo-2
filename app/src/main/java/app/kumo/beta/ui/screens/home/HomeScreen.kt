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
import app.kumo.beta.data.CatalogStore
import app.kumo.beta.data.LibraryStore
import app.kumo.beta.data.local.SavedTitleStore
import app.kumo.beta.data.local.SettingsPreferencesStore
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import app.kumo.beta.provider.ProviderEngine
import app.kumo.beta.ui.components.ContinueWatchingCard
import app.kumo.beta.ui.components.TitleCard
import coil.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun HomeScreen(
    onNavigateToDetails: (String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSearchWithFilter: () -> Unit = {},
    onTitleClick: (Title) -> Unit = {},
    onVoiceSearch: () -> Unit = {},
    providerEngine: ProviderEngine? = null
) {
    val context = LocalContext.current
    val libraryStore = remember { LibraryStore(context) }
    val settingsStore = remember { SettingsPreferencesStore(context) }
    val settings = settingsStore.get()
    val savedTitleStore = remember { SavedTitleStore(context) }
    val continueWatchingList = remember { libraryStore.getContinueWatching() }

    var popularTitles by remember { mutableStateOf<List<Title>>(emptyList()) }
    var trendingTitles by remember { mutableStateOf<List<Title>>(emptyList()) }
    var topTitles by remember { mutableStateOf<List<Title>>(emptyList()) }
    var newTitles by remember { mutableStateOf<List<Title>>(emptyList()) }
    var movieTitles by remember { mutableStateOf<List<Title>>(emptyList()) }
    var featuredTitle by remember { mutableStateOf<Title?>(null) }
    var loading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshNonce by remember { mutableIntStateOf(0) }

    LaunchedEffect(providerEngine, refreshNonce) {
        val engine = providerEngine ?: return@LaunchedEffect
        loading = true
        loadError = null
        val loaded = coroutineScope {
            val popular = async { runCatching { engine.catalog("popular", MediaType.ANIME) }.getOrDefault(emptyList()) }
            val trending = async { runCatching { engine.catalog("trending", MediaType.ANIME) }.getOrDefault(emptyList()) }
            val top = async { runCatching { engine.catalog("top_rated", MediaType.ANIME) }.getOrDefault(emptyList()) }
            val newest = async { runCatching { engine.catalog("new_releases", MediaType.ANIME) }.getOrDefault(emptyList()) }
            val movies = async { runCatching { engine.catalog("popular", MediaType.MOVIE) }.getOrDefault(emptyList()) }
            listOf(popular.await(), trending.await(), top.await(), newest.await(), movies.await())
        }
        popularTitles = loaded[0].map { it.title }.distinctBy { it.id }
        trendingTitles = loaded[1].map { it.title }.distinctBy { it.id }
        topTitles = loaded[2].map { it.title }.distinctBy { it.id }
        newTitles = loaded[3].map { it.title }.distinctBy { it.id }
        movieTitles = loaded[4].map { it.title }.distinctBy { it.id }
        featuredTitle = popularTitles.firstOrNull()
        loading = false
        if (popularTitles.isEmpty() && trendingTitles.isEmpty() && topTitles.isEmpty() && newTitles.isEmpty() && movieTitles.isEmpty()) {
            loadError = "No catalog data is available from the enabled providers"
        }
    }

    val popularAnimeState = rememberLazyListState()
    val popularAnime = popularTitles.filter { it.type == MediaType.ANIME }

    LaunchedEffect(popularAnimeState, popularAnime.size) {
        if (popularAnime.size < 2) return@LaunchedEffect
        while (isActive) {
            delay(3500)
            val next = (popularAnimeState.firstVisibleItemIndex + 1) % popularAnime.size
            popularAnimeState.animateScrollToItem(next)
        }
    }

    fun openTitle(title: Title) {
        CatalogStore.put(title)
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
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(
                onClick = onVoiceSearch,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
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
                    AsyncImage(
                        model = title.backdropUrl,
                        contentDescription = title.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        Color(0xFF0F0F18)
                                    )
                                )
                            )
                    )
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 100f
                            )
                        )
                )
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "KUMO BETA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        title.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (title.genres.isNotEmpty()) {
                        Text(
                            title.genres.joinToString(" • "),
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { openTitle(title) },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Open", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (loading && popularAnime.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!loading && loadError != null && popularTitles.isEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(loadError!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = {
                    refreshNonce++
                }) {
                    Text("Retry")
                }
            }
        }

        SectionTitle("Popular Anime")        LazyRow(
            state = popularAnimeState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(popularAnime, key = { it.id }) { title ->
                TitleCard(title = title, onClick = { openTitle(title) })
            }
        }

        if (settings.showContinueWatching && settings.continueWatching && continueWatchingList.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("Continue Watching")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(continueWatchingList, key = { it.contentId + "|" + it.episodeId }) { prog ->
                    val item = CatalogStore.get(prog.contentId) ?: savedTitleStore.get(prog.contentId)?.also(CatalogStore::put)
                    if (item != null) {
                        val epNum = prog.episodeId.substringAfterLast(":").toIntOrNull()
                            ?: prog.episodeId.substringAfterLast("-").toIntOrNull()
                            ?: 1
                        val percent = if (prog.durationMs > 0L) {
                            (prog.positionMs.toFloat() / prog.durationMs).coerceIn(0f, 1f)
                        } else {
                            0.5f
                        }
                        ContinueWatchingCard(item, epNum, percent) { openTitle(item) }
                    }
                }
            }
        }

        if (settings.showPopular) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("Popular Right Now")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(popularTitles, key = { it.id }) { title ->
                    TitleCard(title = title, onClick = { openTitle(title) })
                }
            }
        }

        if (settings.showTrending && trendingTitles.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("Trending")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trendingTitles, key = { it.id }) { title ->
                    TitleCard(title = title, onClick = { openTitle(title) })
                }
            }
        }

        if (settings.showTopRated && topTitles.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("Top Rated")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(topTitles, key = { it.id }) { title ->
                    TitleCard(title = title, onClick = { openTitle(title) })
                }
            }
        }

        if (settings.showNewReleases && newTitles.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("New Releases")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newTitles, key = { it.id }) { title ->
                    TitleCard(title = title, onClick = { openTitle(title) })
                }
            }
        }

        if (movieTitles.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("Movies")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movieTitles, key = { it.id }) { title ->
                    TitleCard(title = title, onClick = { openTitle(title) })
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
