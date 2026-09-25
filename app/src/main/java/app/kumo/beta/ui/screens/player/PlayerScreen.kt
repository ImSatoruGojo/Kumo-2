package app.kumo.beta.ui.screens.player

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import app.kumo.beta.data.LibraryStore
import app.kumo.beta.data.PlaybackPreferencesStore
import app.kumo.beta.model.Episode
import app.kumo.beta.model.Progress
import app.kumo.beta.provider.KumoStreamSource
import app.kumo.beta.provider.SourceResolver
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoPurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    contentId: String,
    episode: Episode,
    sourceResolver: SourceResolver,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val libraryStore = remember { LibraryStore(context) }
    val playbackPreferences = remember { PlaybackPreferencesStore(context) }

    var sources by remember(episode.id) { mutableStateOf<List<KumoStreamSource>>(emptyList()) }
    var selected by remember(episode.id) { mutableStateOf<KumoStreamSource?>(null) }
    var error by remember(episode.id) { mutableStateOf<String?>(null) }
    var locked by remember(episode.id) { mutableStateOf(false) }
    var showSourceMenu by remember(episode.id) { mutableStateOf(false) }

    val preferences = playbackPreferences.get()
    val savedProgress = remember(contentId, episode.id) {
        libraryStore.getProgress().firstOrNull {
            it.contentId == contentId && it.episodeId == episode.id
        }
    }

    LaunchedEffect(episode.id) {
        val result = withContext(Dispatchers.IO) { runCatching { sourceResolver.resolve(episode) } }
        result.onSuccess { sources = it; selected = it.firstOrNull() }
            .onFailure { error = it.message ?: "Unable to resolve a stream" }
    }

    val source = selected
    val player = remember(source?.url) {
        source?.let {
            val headers = buildMap {
                putAll(it.headers)
                it.referer?.let { ref -> put("Referer", ref) }
            }
            val httpFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setDefaultRequestProperties(headers)
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(context).setDataSourceFactory(httpFactory)
                )
                .build()
                .apply {
                    setMediaItem(
                        MediaItem.Builder()
                            .setUri(Uri.parse(it.url))
                            .setMimeType(it.mimeType)
                            .build()
                    )
                    prepare()
                    savedProgress?.positionMs?.takeIf { position -> position > 0L }?.let(::seekTo)
                    setPlaybackSpeed(preferences.playbackSpeed)
                    playWhenReady = true
                }
        }
    }

    LaunchedEffect(player) {
        while (player != null) {
            delay(5000)
            val duration = player.duration.takeIf { it > 0L } ?: episode.durationMs ?: 0L
            if (duration > 0L) {
                libraryStore.saveProgress(
                    Progress(
                        contentId = contentId,
                        episodeId = episode.id,
                        positionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = duration
                    )
                )
            }
        }
    }

    DisposableEffect(player) {
        onDispose {
            player?.let {
                val duration = it.duration.takeIf { value -> value > 0L } ?: episode.durationMs ?: 0L
                if (duration > 0L) {
                    libraryStore.saveProgress(
                        Progress(
                            contentId = contentId,
                            episodeId = episode.id,
                            positionMs = it.currentPosition.coerceAtLeast(0L),
                            durationMs = duration
                        )
                    )
                }
                it.release()
            }
        }
    }

    Column(Modifier.fillMaxSize().background(KumoBlack)) {
        if (!locked) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text("Episode ${episode.number}", color = Color.White, modifier = Modifier.weight(1f))
                if (sources.size > 1) {
                    TextButton(onClick = { showSourceMenu = true }) {
                        Text("Source", color = KumoPurple)
                    }
                }
            }
        }

        if (player != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = !locked
                    }
                },
                update = { it.useController = !locked },
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            )
        } else {
            Box(
                Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    error ?: "No playable source is available yet",
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(24.dp)
                )
            }
        }

        if (!locked && player != null) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = {
                    player.seekTo((player.currentPosition - preferences.seekSeconds * 1000L).coerceAtLeast(0L))
                }) { Text("-${preferences.seekSeconds}s") }

                OutlinedButton(onClick = {
                    val target = player.currentPosition + preferences.seekSeconds * 1000L
                    player.seekTo(target.coerceAtMost(player.duration.takeIf { it > 0 } ?: target))
                }) { Text("+${preferences.seekSeconds}s") }

                var speedMenu by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { speedMenu = true }) {
                        Text("${preferences.playbackSpeed}x")
                    }
                    DropdownMenu(expanded = speedMenu, onDismissRequest = { speedMenu = false }) {
                        listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${preferences.playbackSpeed}x".replace("${preferences.playbackSpeed}", speed.toString())) },
                                onClick = {
                                    player.setPlaybackSpeed(speed)
                                    playbackPreferences.set(preferences.copy(playbackSpeed = speed))
                                    speedMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                OutlinedButton(onClick = { locked = true }) {
                    Text("Lock")
                }
            }
        } else if (locked) {
            Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                OutlinedButton(onClick = { locked = false }) { Text("Unlock") }
            }
        }

        if (!locked && sources.isNotEmpty()) {
            Text(
                "Available sources: ${sources.size}",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (showSourceMenu) {
        AlertDialog(
            onDismissRequest = { showSourceMenu = false },
            title = { Text("Sources") },
            text = {
                Column {
                    sources.forEach { item ->
                        TextButton(
                            onClick = {
                                selected = item
                                showSourceMenu = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                listOfNotNull(
                                    item.providerId,
                                    item.quality?.let { "itp" },
                                    item.language,
                                    item.audioType
                                ).joinToString(" • ")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourceMenu = false }) { Text("Close") }
            }
        )
    }
}
