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
import app.kumo.beta.model.Episode
import app.kumo.beta.provider.KumoStreamSource
import app.kumo.beta.provider.SourceResolver
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoPurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(episode: Episode, sourceResolver: SourceResolver, onBack: () -> Unit) {
    val context = LocalContext.current
    var sources by remember(episode.id) { mutableStateOf<List<KumoStreamSource>>(emptyList()) }
    var selected by remember(episode.id) { mutableStateOf<KumoStreamSource?>(null) }
    var error by remember(episode.id) { mutableStateOf<String?>(null) }

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
                .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(httpFactory))
                .build()
                .apply {
                    setMediaItem(MediaItem.Builder().setUri(Uri.parse(it.url)).setMimeType(it.mimeType).build())
                    prepare()
                    playWhenReady = true
                }
        }
    }

    DisposableEffect(player) { onDispose { player?.release() } }

    Column(Modifier.fillMaxSize().background(KumoBlack)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text("Episode ${episode.number}", color = Color.White, modifier = Modifier.weight(1f))
            if (sources.size > 1) TextButton(onClick = {
                val index = sources.indexOfFirst { it.url == selected?.url }
                selected = sources[(index + 1).mod(sources.size)]
            }) { Text("Source", color = KumoPurple) }
        }
        if (player != null) {
            AndroidView(factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true } }, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        } else {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f), contentAlignment = Alignment.Center) {
                Text(error ?: "No playable source is available yet", color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(24.dp))
            }
        }
        if (sources.isNotEmpty()) Text("Available sources: ${sources.size}", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp))
    }
}
