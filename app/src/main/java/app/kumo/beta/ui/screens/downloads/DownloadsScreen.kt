package app.kumo.beta.ui.screens.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.kumo.beta.data.local.DownloadItem
import app.kumo.beta.data.local.DownloadManager

@Composable
fun DownloadsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val manager = remember { DownloadManager(context) }
    var downloads by remember { mutableStateOf(manager.getDownloads()) }
    LaunchedEffect(Unit) {
        while (true) {
            downloads = manager.getDownloads()
            kotlinx.coroutines.delay(1000)
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Downloads", style = MaterialTheme.typography.headlineSmall)
        Text("Offline files saved in your selected storage folder", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
        if (downloads.isEmpty()) {
            Text("No downloads yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(downloads, key = { it.id }) { item ->
                    DownloadRow(item) {
                        manager.deleteDownload(item.id)
                        downloads = manager.getDownloads()
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(item: DownloadItem, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(item.episodeTitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.status.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() } + " • " + item.quality, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
            if (item.totalBytes > 0L) {
                Text(item.downloadedBytes.toString() + " / " + item.totalBytes + " bytes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}