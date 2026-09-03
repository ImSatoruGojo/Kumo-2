package app.kumo.beta.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import app.kumo.beta.data.local.ContinueWatchingManager
import app.kumo.beta.data.local.PreferencesManager
import app.kumo.beta.model.Title
import app.kumo.beta.ui.components.ContinueWatchingCard
import app.kumo.beta.ui.components.TitleCard
import coil.compose.AsyncImage

@Composable
fun HomeScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSearchWithFilter: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    val cwManager = remember { ContinueWatchingManager(context) }

    val allTitles = remember { DemoData.allTitles }
    val featuredTitle = remember { allTitles.firstOrNull() }
    val continueWatchingList = remember { cwManager.getAllContinueWatching() }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        // TOP BAR: Search Bar + Filter Shortcut
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
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search anime, movies, series...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = { onNavigateToSearchWithFilter() },
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // FEATURED BANNER CAROUSEL
        featuredTitle?.let { title ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onNavigateToDetails(title.id) }
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        Color(0xFF0F0F18)
                                    )
                                )
                            )
                    )
                }

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 100f
                            )
                        )
                )

                // Featured Title Details
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "FEATURED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = title.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = title.genres.joinToString(" • "),
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onNavigateToDetails(title.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Watch Now", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CONTINUE WATCHING SECTION
        if (prefs.showContinueWatching && continueWatchingList.isNotEmpty()) {
            HomeSectionHeader("Continue Watching")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(continueWatchingList) { prog ->
                    val item = DemoData.getById(prog.contentId)
                    if (item != null) {
                        val epNum = prog.episodeId.substringAfterLast("-").toIntOrNull() ?: 1
                        val percent = if (prog.durationMs > 0) prog.positionMs.toFloat() / prog.durationMs else 0.5f
                        ContinueWatchingCard(
                            title = item,
                            episodeNum = epNum,
                            progressPercent = percent,
                            onClick = { onNavigateToDetails(item.id) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // POPULAR RIGHT NOW
        if (prefs.showPopular) {
            HomeSectionHeader("Popular Right Now")
            HorizontalTitleList(allTitles.take(5), onNavigateToDetails)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // TRENDING
        if (prefs.showTrending) {
            HomeSectionHeader("Trending Anime")
            HorizontalTitleList(allTitles.filter { it.type == app.kumo.beta.model.MediaType.ANIME }, onNavigateToDetails)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // TOP RATED MOVIES
        if (prefs.showTopRated) {
            HomeSectionHeader("Featured Movies")
            HorizontalTitleList(allTitles.filter { it.type == app.kumo.beta.model.MediaType.MOVIE }, onNavigateToDetails)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // MANGA & OTHERS
        if (prefs.showNewReleases) {
            HomeSectionHeader("Top Manga & Books")
            HorizontalTitleList(allTitles.filter { it.type == app.kumo.beta.model.MediaType.MANGA }, onNavigateToDetails)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HomeSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun HorizontalTitleList(
    titles: List<Title>,
    onNavigateToDetails: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(titles) { item ->
            TitleCard(title = item, onClick = { onNavigateToDetails(item.id) })
        }
    }
}
