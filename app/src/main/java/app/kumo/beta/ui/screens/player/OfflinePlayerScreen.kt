package app.kumo.beta.ui.screens.player

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import app.kumo.beta.data.local.DownloadItem
import app.kumo.beta.data.local.SettingsPreferencesStore
import app.kumo.beta.ui.theme.KumoBlack

@Composable
fun OfflinePlayerScreen(
    item: DownloadItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settingsStore = remember { SettingsPreferencesStore(context) }
    val settings = settingsStore.get()
    var playbackError by remember { mutableStateOf<String?>(null) }
    var speedMenu by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(settings.playbackSpeed) }

    DisposableEffect(settings.keepScreenOn) {
        val window = (context as? android.app.Activity)?.window
        val wasKeepingScreenOn = window?.attributes?.let {
            (it.flags and android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
        } ?: false
        if (settings.keepScreenOn) window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            if (wasKeepingScreenOn) window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val player = remember(item.id, item.fileUri) {
        item.fileUri?.let { uri ->
            ExoPlayer.Builder(context)
                .build()
                .apply {
                    setMediaItem(MediaItem.fromUri(Uri.parse(uri)))
                    prepare()
                    setPlaybackSpeed(speed)
                    playWhenReady = true
                }
        }
    }

    DisposableEffect(player) {
        val current = player ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playbackError = error.message ?: "Unable to play downloaded file"
            }
        }
        current.addListener(listener)
        onDispose {
            current.removeListener(listener)
            current.release()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(KumoBlack)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                item.title + " • " + item.episodeTitle,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Box {
                TextButton(onClick = { speedMenu = true }) {
                    Text(if (speed % 1f == 0f) speed.toInt().toString() + "x" else speed.toString() + "x", color = Color.White)
                }
                DropdownMenu(expanded = speedMenu, onDismissRequest = { speedMenu = false }) {
                    listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { value ->
                        DropdownMenuItem(
                            text = { Text(if (value % 1f == 0f) value.toInt().toString() + "x" else value.toString() + "x") },
                            onClick = {
                                speed = value
                                player?.setPlaybackSpeed(value)
                                settingsStore.setSpeed(value)
                                speedMenu = false
                            }
                        )
                    }
                }
            }
        }

        if (player != null) {
            AndroidView(
                factory = { PlayerView(it).apply { this.player = player } },
                update = { it.player = player },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentAlignment = Alignment.Center
            ) {
                Text("Downloaded file is unavailable", color = Color.White)
            }
        }

        playbackError?.let {
            AlertDialog(
                onDismissRequest = { playbackError = null },
                title = { Text("Playback error") },
                text = { Text(it) },
                confirmButton = {
                    Button(onClick = { playbackError = null }) { Text("OK") }
                }
            )
        }
    }
}
