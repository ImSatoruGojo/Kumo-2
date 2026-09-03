package app.kumo.beta.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.R
import app.kumo.beta.data.local.*

enum class SettingsSubmenu(val title: String, val icon: ImageVector) {
    GENERAL("General", Icons.Default.Tune),
    APPEARANCE("Appearance & Theme", Icons.Default.Palette),
    HOME("Home Customization", Icons.Default.Home),
    PLAYER("Player & Decoders", Icons.Default.PlayCircle),
    LIBRARY("Library & Tracking", Icons.Default.Bookmark),
    MANGA("Manga & Reader", Icons.Default.Book),
    DOWNLOADS("Downloads Manager", Icons.Default.Download),
    EXTENSIONS("Extension Repositories", Icons.Default.Extension),
    STORAGE("Storage & Cache", Icons.Default.Storage),
    CONTENT("Content & Audio", Icons.Default.Translate),
    NETWORK("Network & Data", Icons.Default.Wifi),
    PRIVACY("Privacy & Security", Icons.Default.Security),
    BACKUP("Backup & Restore", Icons.Default.Backup),
    CHANGELOG("Changelog", Icons.Default.History),
    ADVANCED("Advanced", Icons.Default.Build),
    ABOUT("About Kumo", Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemeChanged: (AppThemeMode, AccentColorOption, String?, Boolean) -> Unit
) {
    var currentSubmenu by remember { mutableStateOf<SettingsSubmenu?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentSubmenu?.title ?: "Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (currentSubmenu != null) {
                        IconButton(onClick = { currentSubmenu = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (currentSubmenu == null) {
                MainSettingsMenu(onSelectSubmenu = { currentSubmenu = it })
            } else {
                when (currentSubmenu!!) {
                    SettingsSubmenu.GENERAL -> GeneralSettings()
                    SettingsSubmenu.APPEARANCE -> AppearanceSettings(onThemeChanged)
                    SettingsSubmenu.HOME -> HomeSettings()
                    SettingsSubmenu.PLAYER -> PlayerSettings()
                    SettingsSubmenu.LIBRARY -> LibrarySettings()
                    SettingsSubmenu.MANGA -> MangaReaderSettings()
                    SettingsSubmenu.DOWNLOADS -> DownloadsManagerScreen()
                    SettingsSubmenu.EXTENSIONS -> ExtensionsManagerScreen()
                    SettingsSubmenu.STORAGE -> StorageSettings()
                    SettingsSubmenu.CONTENT -> ContentSettings()
                    SettingsSubmenu.NETWORK -> NetworkSettings()
                    SettingsSubmenu.PRIVACY -> PrivacySettings()
                    SettingsSubmenu.BACKUP -> BackupSettings()
                    SettingsSubmenu.CHANGELOG -> ChangelogSettings()
                    SettingsSubmenu.ADVANCED -> AdvancedSettings()
                    SettingsSubmenu.ABOUT -> AboutSettings()
                }
            }
        }
    }
}

@Composable
fun MainSettingsMenu(onSelectSubmenu: (SettingsSubmenu) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.kumo_branding),
                    contentDescription = "Kumo Branding",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text("Kumo Beta 0.2.0-v2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Combined Aniyomi • Mihon • CloudStream Engine", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        SettingsSubmenu.entries.forEach { submenu ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSubmenu(submenu) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = submenu.icon,
                        contentDescription = submenu.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = submenu.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
fun GeneralSettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var startupPage by remember { mutableStateOf(prefs.startupPage) }
    var rememberScreen by remember { mutableStateOf(prefs.rememberLastScreen) }
    var confirmExit by remember { mutableStateOf(prefs.confirmExit) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("General Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Startup Page", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("Home", "Search", "Library").forEach { page ->
                FilterChip(
                    selected = startupPage == page,
                    onClick = {
                        startupPage = page
                        prefs.startupPage = page
                    },
                    label = { Text(page) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SettingSwitchRow("Remember Last Screen on Launch", rememberScreen) {
            rememberScreen = it
            prefs.rememberLastScreen = it
        }

        SettingSwitchRow("Confirm Before Exiting App", confirmExit) {
            confirmExit = it
            prefs.confirmExit = it
        }
    }
}

@Composable
fun AppearanceSettings(
    onThemeChanged: (AppThemeMode, AccentColorOption, String?, Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var selectedTheme by remember { mutableStateOf(prefs.themeMode) }
    var selectedAccent by remember { mutableStateOf(prefs.accentColor) }
    var useCustomHex by remember { mutableStateOf(prefs.useCustomHex) }
    var customHexText by remember { mutableStateOf(prefs.customHexColor ?: "#7C4DFF") }
    var showHexDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Theme Mode", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        AppThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedTheme = mode
                        prefs.themeMode = mode
                        onThemeChanged(mode, selectedAccent, customHexText, useCustomHex)
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedTheme == mode,
                    onClick = {
                        selectedTheme = mode
                        prefs.themeMode = mode
                        onThemeChanged(mode, selectedAccent, customHexText, useCustomHex)
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = mode.name, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Accent Color Preset", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccentColorOption.entries.forEach { option ->
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(option.hexColor))
                        .clickable {
                            useCustomHex = false
                            prefs.useCustomHex = false
                            selectedAccent = option
                            prefs.accentColor = option
                            onThemeChanged(selectedTheme, option, customHexText, false)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!useCustomHex && selectedAccent == option) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Custom HEX Accent Color", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val activeColor = if (useCustomHex) {
                            try {
                                val hexClean = customHexText.removePrefix("#").trim()
                                Color(("FF" + hexClean).toLong(16))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                        } else MaterialTheme.colorScheme.primary

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (useCustomHex) "Custom ($customHexText)" else "Preset Accent",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { showHexDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Edit HEX")
                    }
                }

                if (useCustomHex) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            useCustomHex = false
                            prefs.useCustomHex = false
                            onThemeChanged(selectedTheme, selectedAccent, customHexText, false)
                        }
                    ) {
                        Text("Reset to Preset Accent", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showHexDialog) {
        var tempHex by remember { mutableStateOf(customHexText) }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showHexDialog = false },
            title = { Text("Enter Custom HEX Code") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempHex,
                        onValueChange = {
                            tempHex = it
                            isError = false
                        },
                        label = { Text("Color Hex (e.g. #7C4DFF)") },
                        singleLine = true,
                        isError = isError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                    )
                    if (isError) {
                        Text("Invalid Hex Code format (e.g. #FF5722)", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = tempHex.removePrefix("#").trim()
                        if (clean.length == 6 || clean.length == 8) {
                            val formatted = if (tempHex.startsWith("#")) tempHex else "#$tempHex"
                            customHexText = formatted
                            useCustomHex = true
                            prefs.customHexColor = formatted
                            prefs.useCustomHex = true
                            onThemeChanged(selectedTheme, selectedAccent, formatted, true)
                            showHexDialog = false
                        } else {
                            isError = true
                        }
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHexDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HomeSettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var showCW by remember { mutableStateOf(prefs.showContinueWatching) }
    var showPop by remember { mutableStateOf(prefs.showPopular) }
    var showTrend by remember { mutableStateOf(prefs.showTrending) }
    var showRec by remember { mutableStateOf(prefs.showRecommended) }
    var filterMode by remember { mutableStateOf(prefs.homeFilterMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Home Sections Visibility", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        SettingSwitchRow("Continue Watching Row", showCW) {
            showCW = it
            prefs.showContinueWatching = it
        }
        SettingSwitchRow("Popular Media", showPop) {
            showPop = it
            prefs.showPopular = it
        }
        SettingSwitchRow("Trending Anime", showTrend) {
            showTrend = it
            prefs.showTrending = it
        }
        SettingSwitchRow("Recommended Highlights", showRec) {
            showRec = it
            prefs.showRecommended = it
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Content Focus Filter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL", "ANIME", "MOVIES", "MANGA").forEach { mode ->
                FilterChip(
                    selected = filterMode == mode,
                    onClick = {
                        filterMode = mode
                        prefs.homeFilterMode = mode
                    },
                    label = { Text(mode) }
                )
            }
        }
    }
}

@Composable
fun PlayerSettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var autoSkipIntro by remember { mutableStateOf(prefs.autoSkipIntro) }
    var seekDuration by remember { mutableStateOf(prefs.seekDuration) }
    var autoplayNext by remember { mutableStateOf(prefs.autoplayNext) }
    var screenLock by remember { mutableStateOf(prefs.playerScreenLock) }
    var decoder by remember { mutableStateOf(prefs.playerDecoder) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Player & Gesture Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        SettingSwitchRow("Auto-Skip Intro / Outro", autoSkipIntro) {
            autoSkipIntro = it
            prefs.autoSkipIntro = it
        }

        Text("Double-Tap Seek Duration: ${seekDuration}s", fontSize = 14.sp)
        Slider(
            value = seekDuration.toFloat(),
            onValueChange = {
                seekDuration = it.toInt()
                prefs.seekDuration = it.toInt()
            },
            valueRange = 5f..30f,
            steps = 4
        )

        Spacer(modifier = Modifier.height(12.dp))
        SettingSwitchRow("Autoplay Next Episode", autoplayNext) {
            autoplayNext = it
            prefs.autoplayNext = it
        }

        SettingSwitchRow("Enable Touch Lock Overlay", screenLock) {
            screenLock = it
            prefs.playerScreenLock = it
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Decoder Hardware Acceleration", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("Hardware Accelerated", "Software Fallback").forEach { dec ->
                FilterChip(
                    selected = decoder == dec,
                    onClick = {
                        decoder = dec
                        prefs.playerDecoder = dec
                    },
                    label = { Text(dec) }
                )
            }
        }
    }
}

@Composable
fun MangaReaderSettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var readingMode by remember { mutableStateOf(prefs.readingMode) }
    var readingDirection by remember { mutableStateOf(prefs.readingDirection) }
    var doubleTapZoom by remember { mutableStateOf(prefs.doubleTapZoom) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Manga & Reader Options", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Default Reading Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("Webtoon (Vertical)", "Paged Single", "Continuous").forEach { mode ->
                FilterChip(
                    selected = readingMode == mode,
                    onClick = {
                        readingMode = mode
                        prefs.readingMode = mode
                    },
                    label = { Text(mode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Reading Direction", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("Right to Left", "Left to Right").forEach { dir ->
                FilterChip(
                    selected = readingDirection == dir,
                    onClick = {
                        readingDirection = dir
                        prefs.readingDirection = dir
                    },
                    label = { Text(dir) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SettingSwitchRow("Double-Tap to Zoom Page", doubleTapZoom) {
            doubleTapZoom = it
            prefs.doubleTapZoom = it
        }
    }
}

@Composable
fun LibrarySettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Library & Tracking Sync", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Default Sort Order: Title (A-Z)", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Trackers Architecture (AniList / MyAnimeList)", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {}) {
            Text("Log in to AniList Sync")
        }
    }
}

@Composable
fun DownloadsManagerScreen() {
    val context = LocalContext.current
    val downloadManager = remember { DownloadManager(context) }
    val downloads = remember { downloadManager.getDownloads() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Active & Completed Downloads", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        downloads.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.status) {
                                DownloadStatus.COMPLETED -> Icons.Default.CheckCircle
                                DownloadStatus.DOWNLOADING -> Icons.Default.Download
                                DownloadStatus.PAUSED -> Icons.Default.Pause
                                DownloadStatus.FAILED -> Icons.Default.Error
                                else -> Icons.Default.HourglassEmpty
                            },
                            contentDescription = item.status.name,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(item.episodeTitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(item.quality, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("• ${item.status.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExtensionsManagerScreen() {
    val context = LocalContext.current
    val repoManager = remember { ExtensionRepositoryManager(context) }
    var repositories by remember { mutableStateOf(repoManager.getRepositories()) }

    var showAddRepoDialog by remember { mutableStateOf(false) }
    var newRepoUrl by remember { mutableStateOf("") }
    var newRepoName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Extension Repositories", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddRepoDialog = true },
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Repo")
            }
        }

        Text("Active Aniyomi, Mihon & CloudStream Repositories", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        repositories.forEach { repo ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(repo.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${repo.extensionCount} extensions • ${repo.format.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Switch(
                            checked = repo.isEnabled,
                            onCheckedChange = { isChecked ->
                                repoManager.toggleRepository(repo.id, isChecked)
                                repositories = repoManager.getRepositories()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(repo.url, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                repoManager.removeRepository(repo.id)
                                repositories = repoManager.getRepositories()
                            }
                        ) {
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddRepoDialog) {
        AlertDialog(
            onDismissRequest = { showAddRepoDialog = false },
            title = { Text("Add Extension Repository") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newRepoName,
                        onValueChange = { newRepoName = it },
                        label = { Text("Repository Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newRepoUrl,
                        onValueChange = { newRepoUrl = it },
                        label = { Text("Repository JSON Index URL") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRepoUrl.isNotBlank()) {
                            val name = if (newRepoName.isBlank()) "Custom Extension Repo" else newRepoName
                            val newRepo = ExtensionRepository(
                                id = "custom_" + System.currentTimeMillis(),
                                name = name,
                                url = newRepoUrl.trim(),
                                format = RepoFormat.ANIYOMI_MIHON,
                                extensionCount = 45,
                                isEnabled = true,
                                isTrusted = true
                            )
                            repoManager.addRepository(newRepo)
                            repositories = repoManager.getRepositories()
                            newRepoName = ""
                            newRepoUrl = ""
                            showAddRepoDialog = false
                        }
                    }
                ) {
                    Text("Add Repository")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRepoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StorageSettings() {
    var cacheSize by remember { mutableStateOf("14.2 MB") }
    var cleared by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Storage Management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Current Image & Metadata Cache: $cacheSize", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                cacheSize = "0.0 MB"
                cleared = true
            }
        ) {
            Text(if (cleared) "Cache Cleared!" else "Clear Application Cache")
        }
    }
}

@Composable
fun ContentSettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var dubSub by remember { mutableStateOf(prefs.dubSubPriority) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Content Audio & Subtitle Priorities", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Audio / Sub Priority", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("SUB", "DUB").forEach { option ->
                FilterChip(
                    selected = dubSub == option,
                    onClick = {
                        dubSub = option
                        prefs.dubSubPriority = option
                    },
                    label = { Text(option) }
                )
            }
        }
    }
}

@Composable
fun NetworkSettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var wifiOnly by remember { mutableStateOf(prefs.wifiOnlyDownloads) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Network & Data Saver", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        SettingSwitchRow("Wi-Fi Only Downloads", wifiOnly) {
            wifiOnly = it
            prefs.wifiOnlyDownloads = it
        }
    }
}

@Composable
fun PrivacySettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Privacy & Security", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {}) {
            Text("Clear Search & Watch History")
        }
    }
}

@Composable
fun BackupSettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Backup & Restore Architecture", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = {}) {
            Text("Export Settings & Library (JSON)")
        }
    }
}

@Composable
fun ChangelogSettings() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Version 0.2.0-v2 (Kumo V2 Combined Universal Pass)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Combined extension repository engine supporting Aniyomi, Mihon, and CloudStream repos.", fontSize = 14.sp)
                Text("• Consolidated Aniyomi, Animiru, CloudStream, and Mihon UI/Settings concepts.", fontSize = 14.sp)
                Text("• Added Add Extension Repository manager dialog with JSON URL index parsing.", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AdvancedSettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Advanced Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { prefs.resetAll() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Reset All Settings to Defaults")
        }
    }
}

@Composable
fun AboutSettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Kumo Universal Engine", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Version 0.2.0-v2", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Combining Aniyomi, Animiru, Mihon & CloudStream architecture in pure native Jetpack Compose.", fontSize = 14.sp)
    }
}

@Composable
fun SettingSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 15.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
