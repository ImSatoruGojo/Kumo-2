package app.kumo.beta.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.local.StorageLocationManager
import app.kumo.beta.data.local.SettingsPreferencesStore
import app.kumo.beta.repository.ExtensionInfo
import app.kumo.beta.repository.ExtensionInstaller
import app.kumo.beta.repository.Repository
import app.kumo.beta.repository.RepositoryManager
import app.kumo.beta.repository.RepositoryResult
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoCard
import app.kumo.beta.ui.theme.KumoPurple
import app.kumo.beta.ui.theme.KumoTextSecondary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repositoryManager = remember { RepositoryManager(context) }
    val extensionInstaller = remember { ExtensionInstaller(context) }
    val storageManager = remember { StorageLocationManager(context) }
    val settingsStore = remember { SettingsPreferencesStore(context) }
    var settings by remember { mutableStateOf(settingsStore.get()) }

    var repositories by remember { mutableStateOf(repositoryManager.getRepositories()) }
    var extensions by remember { mutableStateOf(repositoryManager.getAllExtensions()) }
    var repositoryUrl by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var storageName by remember { mutableStateOf(storageManager.displayName()) }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            storageManager.setTreeUri(uri, flags)
            storageName = storageManager.displayName() ?: "Selected folder"
            status = "Download folder connected"
        }
    }

    fun refreshUi() {
        repositories = repositoryManager.getRepositories()
        extensions = repositoryManager.getAllExtensions().map { extension ->
            val current = extensionInstaller.installed().firstOrNull { it.id == extension.id }
            extension.copy(
                installed = current != null,
                enabled = current?.enabled ?: extension.enabled,
                version = current?.version ?: extension.version,
                versionCode = current?.versionCode ?: extension.versionCode
            )
        }
        storageName = storageManager.displayName()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(KumoBlack).padding(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Settings", color = MaterialTheme.colorScheme.onSurface, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Playback, storage and extensions", color = KumoTextSecondary, fontSize = 13.sp)
        }

        item {
            SettingsGroup("Storage") {
                SettingsItem("Download folder", storageName ?: "Not connected")
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { folderPicker.launch(null) }) { Text("Choose folder") }
                    if (storageName != null) {
                        OutlinedButton(onClick = {
                            storageManager.clearTreeUri()
                            refreshUi()
                            status = "Download folder disconnected"
                        }) { Text("Disconnect") }
                    }
                }
                Text(
                    "Completed direct video downloads are saved in the folder you choose",
                    color = KumoTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        item {
            SettingsGroup("General") {
                ChoiceItem("Theme", settings.theme, listOf("Dark", "Light", "System")) { settingsStore.setTheme(it); settings = settingsStore.get() }
                ChoiceItem("App language", settings.appLanguage, listOf("System", "English")) { settingsStore.setLanguage(it); settings = settingsStore.get() }
                ChoiceItem("Cache limit", "${settings.cacheLimitMb} MB", listOf("256 MB", "512 MB", "1024 MB", "2048 MB")) { settingsStore.setCacheLimitMb(it.removeSuffix(" MB").toInt()); settings = settingsStore.get() }
                SwitchItem("Reduce animations", "Useful on lower end devices", settings.reduceAnimations) { settingsStore.setReduceAnimations(it); settings = settingsStore.get() }
            }
        }

        item {
            SettingsGroup("Player") {
                ChoiceItem("Default quality", settings.defaultQuality, listOf("Auto", "1080p", "720p", "480p", "360p")) { settingsStore.setQuality(it); settings = settingsStore.get() }
                ChoiceItem("Playback speed", "${settings.playbackSpeed}x", listOf("0.75x", "1x", "1.25x", "1.5x", "2x")) { settingsStore.setSpeed(it.removeSuffix("x").toFloat()); settings = settingsStore.get() }
                ChoiceItem("Double tap seek", "${settings.doubleTapSeekSeconds} seconds", listOf("5 seconds", "10 seconds", "15 seconds", "30 seconds")) { settingsStore.setSeekSeconds(it.removeSuffix(" seconds").toInt()); settings = settingsStore.get() }
                SwitchItem("Autoplay next", "Start the next episode automatically", settings.autoplayNext) { settingsStore.setAutoplayNext(it); settings = settingsStore.get() }
                SwitchItem("Skip opening", "Skip when supported by the source", settings.skipOpening) { settingsStore.setSkipOpening(it); settings = settingsStore.get() }
                ChoiceItem("Default audio", settings.defaultAudio, listOf("Auto", "Dub", "Sub")) { settingsStore.setAudio(it); settings = settingsStore.get() }
                ChoiceItem("Default subtitles", settings.defaultSubtitle, listOf("Auto", "On", "Off")) { settingsStore.setSubtitle(it); settings = settingsStore.get() }
                ChoiceItem("Anime language", settings.animeLanguage, listOf("Dub", "Sub", "Auto")) { settingsStore.setAnimeLanguage(it); settings = settingsStore.get() }
                ChoiceItem("Movie language", settings.movieLanguage, listOf("Dub", "Sub", "Auto")) { settingsStore.setMovieLanguage(it); settings = settingsStore.get() }
            }
        }

        item {
            SettingsGroup("Playback & History") {
                SwitchItem("Continue watching", "Keep unfinished titles on Home", settings.continueWatching) { settingsStore.setContinueWatching(it); settings = settingsStore.get() }
                SwitchItem("Auto mark watched", "Mark episodes after playback completes", settings.autoMarkWatched) { settingsStore.setAutoMarkWatched(it); settings = settingsStore.get() }
            }
        }

        item {
            SettingsGroup("Downloads") {
                SwitchItem("Wi Fi only", "Only start downloads on unmetered Wi Fi", settings.wifiOnlyDownloads) { settingsStore.setWifiOnlyDownloads(it); settings = settingsStore.get() }
                SwitchItem("Confirm downloads", "Ask before starting a download", settings.confirmDownloads) { settingsStore.setConfirmDownloads(it); settings = settingsStore.get() }
            }
        }

        item {
            Text("Repositories", color = MaterialTheme.colorScheme.onSurface, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = repositoryUrl,
                onValueChange = { repositoryUrl = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Repository URL") },
                placeholder = { Text("https://example.com/repo.json") }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Button(enabled = repositoryUrl.isNotBlank() && !busy, onClick = {
                    runCatching { repositoryManager.addRepository(repositoryUrl) }
                        .onSuccess {
                            repositoryUrl = ""
                            refreshUi()
                            status = "Repository added"
                        }
                        .onFailure { status = it.message ?: "Invalid repository URL" }
                }) { Text("Add") }

                OutlinedButton(
                    enabled = !busy && repositories.any { it.enabled },
                    onClick = {
                        busy = true
                        scope.launch {
                            val results = repositoryManager.refreshAllRepositories()
                            refreshUi()
                            busy = false
                            status = if (results.values.any { it is RepositoryResult.Success }) {
                                "Repositories refreshed"
                            } else {
                                "No repository could be refreshed"
                            }
                        }
                    }
                ) { Text(if (busy) "Refreshing…" else "Refresh all") }
            }
            status?.let {
                Text(it, color = KumoTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }

        items(repositories, key = { it.id }) { repo ->
            RepositoryRow(
                repository = repo,
                onToggle = {
                    repositoryManager.setEnabled(repo.id, !repo.enabled)
                    refreshUi()
                },
                onRefresh = {
                    busy = true
                    scope.launch {
                        val result = repositoryManager.refreshRepository(repo.id)
                        refreshUi()
                        busy = false
                        status = when (result) {
                            is RepositoryResult.Success -> "Loaded " + result.extensions.size + " extensions"
                            is RepositoryResult.Failure -> result.message
                        }
                    }
                },
                onRemove = {
                    repositoryManager.removeRepository(repo.id)
                    refreshUi()
                }
            )
        }

        if (extensions.isNotEmpty()) {
            item {
                Text("Available extensions", color = MaterialTheme.colorScheme.onSurface, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text("Install, enable, disable or remove installed extensions", color = KumoTextSecondary, fontSize = 12.sp)
            }
            items(extensions, key = { it.id }) { extension ->
                ExtensionRow(extension, extensionInstaller, { refreshUi() }, { status = it })
            }
        }

        item {
            SettingsGroup("Appearance") {
                ChoiceItem("Accent color", settings.accentColor, listOf("White", "Orange", "Purple", "Blue", "Green")) { settingsStore.setAccentColor(it); settings = settingsStore.get() }
                SwitchItem("AMOLED black", "Use a pure black background when supported", settings.amoledMode) { settingsStore.setAmoledMode(it); settings = settingsStore.get() }
                SwitchItem("Data saving", "Reduce unnecessary artwork and network work", settings.dataSaving) { settingsStore.setDataSaving(it); settings = settingsStore.get() }
            }
        }

        item {
            SettingsGroup("Home") {
                ChoiceItem("Startup page", settings.startupPage, listOf("Home", "Library")) { settingsStore.setStartupPage(it); settings = settingsStore.get() }
                ChoiceItem("Items per row", settings.itemsPerRow.toString(), listOf("2", "3", "4", "5")) { settingsStore.setItemsPerRow(it.toInt()); settings = settingsStore.get() }
                SwitchItem("Continue Watching section", "Show unfinished titles on Home", settings.showContinueWatching) { settingsStore.setShowContinueWatching(it); settings = settingsStore.get() }
                SwitchItem("Popular section", "Show popular titles on Home", settings.showPopular) { settingsStore.setShowPopular(it); settings = settingsStore.get() }
                SwitchItem("Trending section", "Show trending titles on Home", settings.showTrending) { settingsStore.setShowTrending(it); settings = settingsStore.get() }
                SwitchItem("Top rated section", "Show top rated titles on Home", settings.showTopRated) { settingsStore.setShowTopRated(it); settings = settingsStore.get() }
                SwitchItem("New releases section", "Show new releases when available", settings.showNewReleases) { settingsStore.setShowNewReleases(it); settings = settingsStore.get() }
                SwitchItem("Recommended section", "Show recommendations when available", settings.showRecommended) { settingsStore.setShowRecommended(it); settings = settingsStore.get() }
            }
        }

        item {
            SettingsGroup("Providers") {
                SwitchItem("Automatic fallback", "Try another enabled provider when the current one fails", settings.providerFallback) { settingsStore.setProviderFallback(it); settings = settingsStore.get() }
            }
        }

        item {
            SettingsGroup("Player controls") {
                ChoiceItem("Decoder", settings.playerDecoder, listOf("Hardware", "Software")) { settingsStore.setPlayerDecoder(it); settings = settingsStore.get() }
                SwitchItem("Volume gestures", "Allow player swipe gestures to control volume", settings.gestureVolume) { settingsStore.setGestureVolume(it); settings = settingsStore.get() }
                SwitchItem("Brightness gestures", "Allow player swipe gestures to control brightness", settings.gestureBrightness) { settingsStore.setGestureBrightness(it); settings = settingsStore.get() }
                SwitchItem("Picture in Picture", "Allow the player to enter Android PiP", settings.pipEnabled) { settingsStore.setPipEnabled(it); settings = settingsStore.get() }
                SwitchItem("Keep screen on", "Prevent the display from sleeping during playback", settings.keepScreenOn) { settingsStore.setKeepScreenOn(it); settings = settingsStore.get() }
                ChoiceItem("Screen rotation", settings.screenRotation, listOf("Free", "Portrait", "Landscape")) { settingsStore.setScreenRotation(it); settings = settingsStore.get() }
            }
        }

        item {
            SettingsGroup("Downloads & Network") {
                ChoiceItem("Concurrent downloads", settings.maxConcurrentDownloads.toString(), listOf("1", "2", "3", "4")) { settingsStore.setMaxConcurrentDownloads(it.toInt()); settings = settingsStore.get() }
                ChoiceItem("Network timeout", settings.networkTimeoutSeconds.toString() + " seconds", listOf("10 seconds", "15 seconds", "30 seconds", "60 seconds")) { settingsStore.setNetworkTimeout(it.removeSuffix(" seconds").toInt()); settings = settingsStore.get() }
            }
        }

        item {
            SettingsGroup("Reader") {
                ChoiceItem("Reading mode", settings.readingMode, listOf("Vertical", "Paged", "Webtoon")) { settingsStore.setReadingMode(it); settings = settingsStore.get() }
                ChoiceItem("Reading direction", settings.readingDirection, listOf("Right to Left", "Left to Right")) { settingsStore.setReadingDirection(it); settings = settingsStore.get() }
            }
        }

        item {
            SettingsGroup("History") {
                ChoiceItem("History size", settings.historySize.toString(), listOf("20", "50", "100", "200")) { settingsStore.setHistorySize(it.toInt()); settings = settingsStore.get() }
            }
        }

        item {
            var showResetDialog by remember { mutableStateOf(false) }
            SettingsGroup("Advanced") {
                SwitchItem("Remember last screen", "Restore the last selected page when supported", settings.rememberLastScreen) { settingsStore.setRememberLastScreen(it); settings = settingsStore.get() }
                TextButton(onClick = { showResetDialog = true }, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text("Reset all settings", color = MaterialTheme.colorScheme.error)
                }
            }
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Reset all settings") },
                    text = { Text("This restores Kumo preferences to their defaults") },
                    confirmButton = {
                        TextButton(onClick = {
                            settingsStore.resetAll()
                            settings = settingsStore.get()
                            showResetDialog = false
                            status = "Settings reset"
                        }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel") } }
                )
            }
        }

        item {
            SettingsGroup("About") {
                SettingsItem("Version", "0.1.0-beta")
                SettingsItem("Build", "Core implementation")
            }
        }
    }
}

@Composable
private fun RepositoryRow(repository: Repository, onToggle: () -> Unit, onRefresh: () -> Unit, onRemove: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(KumoCard, RoundedCornerShape(14.dp)).padding(14.dp)) {
        Text(repository.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Text(repository.url, color = KumoTextSecondary, fontSize = 12.sp, maxLines = 2)
        repository.lastRefreshStatus?.let { Text("Status: " + it, color = KumoTextSecondary, fontSize = 12.sp) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onToggle) { Text(if (repository.enabled) "Disable" else "Enable", color = KumoPurple) }
            TextButton(onClick = onRefresh, enabled = repository.enabled) { Text("Refresh", color = KumoPurple) }
            TextButton(onClick = onRemove) { Text("Remove", color = KumoTextSecondary) }
        }
    }
}

@Composable
private fun ExtensionRow(
    extension: ExtensionInfo,
    installer: ExtensionInstaller,
    onChanged: () -> Unit,
    onError: (String) -> Unit
) {
    var installing by remember(extension.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxWidth().background(KumoCard, RoundedCornerShape(14.dp)).padding(14.dp)) {
        Text(extension.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Text(extension.type.name, color = KumoTextSecondary, fontSize = 12.sp)
        extension.version?.let { Text("Version " + it, color = KumoTextSecondary, fontSize = 12.sp) }

        if (extension.installed) {
            Text(
                if (extension.enabled) "Installed and enabled" else "Installed and disabled",
                color = KumoTextSecondary,
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    installer.setEnabled(extension.id, !extension.enabled)
                    onChanged()
                }) { Text(if (extension.enabled) "Disable" else "Enable") }
                TextButton(onClick = {
                    installer.removeInstalled(extension.id)
                    onChanged()
                }) { Text("Uninstall", color = KumoTextSecondary) }
            }
        } else {
            Button(
                enabled = !installing,
                onClick = {
                    installing = true
                    scope.launch {
                        installer.install(extension)
                            .onSuccess { onChanged() }
                            .onFailure { onError(it.message ?: "Installation failed") }
                        installing = false
                    }
                }
            ) { Text(if (installing) "Installing…" else "Install") }
        }
    }
}

@Composable
private fun ChoiceItem(label: String, value: String, options: List<String>, onSelected: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
        TextButton(onClick = { open = true }) { Text(value, color = KumoPurple) }
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(label) },
            text = { Column { options.forEach { option -> TextButton(onClick = { open = false; onSelected(option) }, modifier = Modifier.fillMaxWidth()) { Text(option) } } } },
            confirmButton = {}
        )
    }
}

@Composable
private fun SwitchItem(label: String, description: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
            Text(description, color = KumoTextSecondary, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, color = KumoTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
    Column(Modifier.fillMaxWidth().background(KumoCard, RoundedCornerShape(14.dp)).padding(vertical = 4.dp), content = content)
}

@Composable
private fun SettingsItem(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
        Text(value, color = KumoTextSecondary, fontSize = 14.sp, maxLines = 1)
    }
}
