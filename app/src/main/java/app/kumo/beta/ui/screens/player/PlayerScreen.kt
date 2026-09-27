package app.kumo.beta.ui.screens.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
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
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TrackSelectionDialogBuilder
import app.kumo.beta.data.LibraryStore
import app.kumo.beta.data.PlaybackPreferencesStore
import app.kumo.beta.data.local.SettingsPreferencesStore
import app.kumo.beta.model.Episode
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Progress
import app.kumo.beta.provider.KumoStreamSource
import app.kumo.beta.provider.KumoSubtitle
import app.kumo.beta.provider.SourceResolver
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoPurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    contentId: String,
    episode: Episode,
    mediaType: MediaType? = null,
    sourceResolver: SourceResolver,
    onBack: () -> Unit,
    onEpisodeEnded: () -> Unit = {}
) {
    val context = LocalContext.current
    val libraryStore = remember { LibraryStore(context) }
    val playbackPreferences = remember { PlaybackPreferencesStore(context) }
    val settingsStore = remember { SettingsPreferencesStore(context) }
    val settings = settingsStore.get()
    val effectiveAudio = when (mediaType) {
        MediaType.ANIME -> settings.animeLanguage
        MediaType.MOVIE, MediaType.TV_SHOW, MediaType.CARTOON -> settings.movieLanguage
        else -> settings.defaultAudio
    }.let { if (it == "Auto") settings.defaultAudio else it }

    var sources by remember(episode.id) { mutableStateOf<List<KumoStreamSource>>(emptyList()) }
    var selected by remember(episode.id) { mutableStateOf<KumoStreamSource?>(null) }
    var error by remember(episode.id) { mutableStateOf<String?>(null) }
    var locked by remember(episode.id) { mutableStateOf(false) }
    var showSourceMenu by remember(episode.id) { mutableStateOf(false) }
    var showQualityMenu by remember(episode.id) { mutableStateOf(false) }

    val preferences = playbackPreferences.get().copy(
        playbackSpeed = settings.playbackSpeed,
        seekSeconds = settings.doubleTapSeekSeconds,
        autoplayNext = settings.autoplayNext,
        preferredAudio = effectiveAudio.takeIf { it != "Auto" },
        preferredSubtitle = settings.defaultSubtitle.takeIf { it != "Auto" }
    )
    DisposableEffect(settings.screenRotation) {
        val activity = context as? Activity
        val previous = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = when (settings.screenRotation) {
            "Portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            "Landscape" -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            activity?.requestedOrientation = previous
        }
    }

    DisposableEffect(settings.keepScreenOn) {
        val activity = context as? android.app.Activity
        if (settings.keepScreenOn) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val savedProgress = remember(contentId, episode.id) {
        libraryStore.getProgress().firstOrNull {
            it.contentId == contentId && it.episodeId == episode.id
        }
    }

    LaunchedEffect(episode.id) {
        val result = withContext(Dispatchers.IO) { runCatching { sourceResolver.resolve(episode, allowFallback = settings.providerFallback) } }
        result.onSuccess { resolved ->
            val preferredAudio = effectiveAudio
            val preferredSubtitle = settings.defaultSubtitle
            val preferred = resolved.sortedByDescending { source ->
                var score = source.quality?.let { quality ->
                    when (settings.defaultQuality) {
                        "1080p" -> if (quality == 1080) 8 else 0
                        "720p" -> if (quality == 720) 8 else 0
                        "480p" -> if (quality == 480) 8 else 0
                        "360p" -> if (quality == 360) 8 else 0
                        else -> 0
                    }
                } ?: 0
                if (preferredAudio == "Dub" && source.audioType.equals("Dub", ignoreCase = true)) score += 4
                if (preferredAudio == "Sub" && source.audioType.equals("Sub", ignoreCase = true)) score += 4
                if (preferredSubtitle == "On" && !source.language.isNullOrBlank()) score += 1
                score
            }
            sources = preferred
            selected = preferred.firstOrNull()
        }
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
                    val subtitleConfigurations = if (settings.defaultSubtitle == "Off") {
                        emptyList()
                    } else {
                        it.subtitles.mapNotNull { subtitleConfiguration(it, settings.subtitleLanguage) }
                    }
                    setMediaItem(
                        MediaItem.Builder()
                            .setUri(Uri.parse(it.url))
                            .setMimeType(it.mimeType)
                            .setSubtitleConfigurations(subtitleConfigurations)
                            .build()
                    )
                    prepare()
                    savedProgress?.positionMs?.takeIf { position -> position > 0L }?.let(::seekTo)
                    setPlaybackSpeed(preferences.playbackSpeed)
                    playWhenReady = true
                }
        }
    }

    LaunchedEffect(player, source?.introStartMs, source?.introEndMs, settings.skipOpening) {
        val currentPlayer = player ?: return@LaunchedEffect
        val start = source?.introStartMs ?: return@LaunchedEffect
        val end = source?.introEndMs ?: return@LaunchedEffect
        if (!settings.skipOpening || end <= start) return@LaunchedEffect
        var skipped = false
        while (isActive && !skipped) {
            delay(250)
            val position = currentPlayer.currentPosition
            if (position in start..end) {
                currentPlayer.seekTo(end)
                skipped = true
            } else if (position > end) {
                skipped = true
            }
        }
    }

    DisposableEffect(player, source?.url) {
        val currentPlayer = player ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onPlayerError(playbackException: androidx.media3.common.PlaybackException) {
                val index = sources.indexOfFirst { it.url == source?.url }
                val fallback = sources.drop(index + 1).firstOrNull()
                if (fallback != null) {
                    error = null
                    selected = fallback
                } else {
                    error = playbackException.message ?: "Playback failed"
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED && settings.autoplayNext) {
                    onEpisodeEnded()
                }
            }
        }
        currentPlayer.addListener(listener)
        onDispose { currentPlayer.removeListener(listener) }
    }

    LaunchedEffect(player) {
        while (player != null) {
            delay(5000)
            val duration = player.duration.takeIf { it > 0L } ?: episode.durationMs ?: 0L
            if (duration > 0L) {
                if (settings.autoMarkWatched && (player?.currentPosition ?: 0L) >= (duration * 0.9f).toLong()) {
                    libraryStore.markWatched(contentId, episode.id)
                }
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
                if (settings.autoMarkWatched && (it.currentPosition >= (duration * 0.9f).toLong())) {
                    libraryStore.markWatched(contentId, episode.id)
                }
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
                if (settings.pipEnabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    TextButton(onClick = {
                        (context as? android.app.Activity)?.enterPictureInPictureMode()
                    }) { Text("PiP", color = KumoPurple) }
                }
                if (sources.mapNotNull { it.quality }.distinct().size > 1) {
                    Box {
                        TextButton(onClick = { showQualityMenu = true }) { Text("Quality", color = KumoPurple) }
                        DropdownMenu(expanded = showQualityMenu, onDismissRequest = { showQualityMenu = false }) {
                            sources.mapNotNull { it.quality }.distinct().sortedDescending().forEach { quality ->
                                DropdownMenuItem(
                                    text = { Text("${quality}p") },
                                    onClick = {
                                        sources.firstOrNull { it.quality == quality }?.let { selected = it }
                                        showQualityMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                if (player != null) {
                    TextButton(onClick = {
                        TrackSelectionDialogBuilder(context, "Audio", player, C.TRACK_TYPE_AUDIO)
                            .build()
                            .show()
                    }) {
                        Text("Audio", color = KumoPurple)
                    }
                    TextButton(onClick = {
                        TrackSelectionDialogBuilder(context, "Subtitles", player, C.TRACK_TYPE_TEXT)
                            .build()
                            .show()
                    }) {
                        Text("Subtitles", color = KumoPurple)
                    }
                }

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
                        val detector = GestureDetector(
                            ctx,
                            object : GestureDetector.SimpleOnGestureListener() {
                                override fun onDoubleTap(event: MotionEvent): Boolean {
                                    if (locked) return false
                                    val delta = preferences.seekSeconds * 1000L
                                    if (event.x < width / 2f) {
                                        player.seekTo((player.currentPosition - delta).coerceAtLeast(0L))
                                    } else {
                                        val target = player.currentPosition + delta
                                        player.seekTo(
                                            target.coerceAtMost(
                                                player.duration.takeIf { it > 0L } ?: target
                                            )
                                        )
                                    }
                                    return true
                                }

                                override fun onScroll(
                                    e1: MotionEvent?,
                                    e2: MotionEvent,
                                    distanceX: Float,
                                    distanceY: Float
                                ): Boolean {
                                    if (locked || e1 == null) return false
                                    if (kotlin.math.abs(distanceY) <= kotlin.math.abs(distanceX)) return false
                                    val ratio = (-distanceY / height.coerceAtLeast(1)).coerceIn(-0.12f, 0.12f)
                                    val isLeftSide = e1.x < width / 2f
                                    if (isLeftSide && settings.gestureBrightness) {
                                        val activity = ctx as? Activity
                                        if (activity != null) {
                                            val params = activity.window.attributes
                                            val current = if (params.screenBrightness in 0.01f..1f) params.screenBrightness else 0.5f
                                            params.screenBrightness = (current + ratio).coerceIn(0.02f, 1f)
                                            activity.window.attributes = params
                                        }
                                    } else if (!isLeftSide && settings.gestureVolume) {
                                        val audio = ctx.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
                                        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                                        val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                                        val next = (current + (ratio * max)).toInt().coerceIn(0, max)
                                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, next, 0)
                                    } else {
                                        return false
                                    }
                                    return true
                                }
                            }
                        )
                        setOnTouchListener { _, event ->
                            detector.onTouchEvent(event)
                            false
                        }
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
                        Text(speedLabel(preferences.playbackSpeed))
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
                                    item.quality?.let { "${it}p" },
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


private fun speedLabel(speed: Float): String =
    if (speed % 1f == 0f) speed.toInt().toString() + "x" else speed.toString() + "x"

private fun subtitleConfiguration(subtitle: KumoSubtitle, preferredLanguage: String): MediaItem.SubtitleConfiguration? {
    val cleanUrl = subtitle.url.substringBefore("?")
    val mime = when {
        subtitle.format.equals("vtt", ignoreCase = true) || cleanUrl.endsWith(".vtt", ignoreCase = true) -> MimeTypes.TEXT_VTT
        subtitle.format.equals("srt", ignoreCase = true) || cleanUrl.endsWith(".srt", ignoreCase = true) -> MimeTypes.APPLICATION_SUBRIP
        subtitle.format.equals("ttml", ignoreCase = true) || cleanUrl.endsWith(".ttml", ignoreCase = true) -> MimeTypes.APPLICATION_TTML
        else -> return null
    }
    return MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle.url))
        .setMimeType(mime)
        .setLanguage(subtitle.language)
        .setSelectionFlags(
            if (preferredLanguage.equals("Auto", ignoreCase = true) ||
                subtitle.language.equals(preferredLanguage, ignoreCase = true)
            ) MediaItem.SubtitleConfiguration.SELECTION_FLAG_DEFAULT else 0
        )
        .build()
}
