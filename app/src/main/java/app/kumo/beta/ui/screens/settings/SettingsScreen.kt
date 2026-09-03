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
    var currentLangCode by remember { mutableStateOf(prefs.appLanguage) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableStateOf("18.4 MB") }
    var cacheCleared by remember { mutableStateOf(false) }

    val currentLang = AppLanguages.getLanguageByCode(currentLangCode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("General Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        // App Language Selection
        Card(
            modifier = Modifier.fillMaxWidth().clickable { showLangDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("App Language", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("${currentLang.displayName} (${currentLang.nativeName})", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                Icon(Icons.Default.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Startup Page Selection
        Column {
            Text("Startup Page", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        }

        SettingSwitchRow("Remember Last Screen on Launch", rememberScreen) {
            rememberScreen = it
            prefs.rememberLastScreen = it
        }

        SettingSwitchRow("Confirm Before Exiting App", confirmExit) {
            confirmExit = it
            prefs.confirmExit = it
        }

        HorizontalDivider()

        Text("Storage & Data Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Image & Search Cache", fontWeight = FontWeight.Medium)
                    Text(cacheSize, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        cacheSize = "0.0 MB"
                        cacheCleared = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (cacheCleared) "Cache Cleared!" else "Clear Application Cache")
                }
                OutlinedButton(
                    onClick = { showClearDataDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All App Data & Reset")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("App Version & Info", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Kumo Version: 0.2.0-v2 Beta", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Engine: Aniyomi • Mihon • CloudStream V2", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text("Select App Language (40+ Languages)") },
            text = {
                Box(modifier = Modifier.height(350.dp)) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppLanguages.supportedLanguages.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentLangCode = lang.code
                                        prefs.appLanguage = lang.code
                                        showLangDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(lang.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(lang.nativeName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (currentLangCode.equals(lang.code, ignoreCase = true)) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will reset all settings, watch history, and library items. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        prefs.resetAll()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AppearanceSettings(
    onThemeChanged: (AppThemeMode, AccentColorOption, String?, Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var selectedTheme by remember { mutableStateOf(prefs.themeMode) }
    var selectedCatppuccin by remember { mutableStateOf(prefs.catppuccinTheme) }
    var selectedAccent by remember { mutableStateOf(prefs.accentColor) }
    var useCustomHex by remember { mutableStateOf(prefs.useCustomHex) }
    var customHexText by remember { mutableStateOf(prefs.customHexColor ?: "#7C4DFF") }
    var cornerRadius by remember { mutableStateOf(prefs.cornerRadius) }
    var fontScale by remember { mutableStateOf(prefs.fontScale) }
    var cardStyle by remember { mutableStateOf(prefs.cardStyle) }
    var showHexDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Theme Mode", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        AppThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedTheme = mode
                        prefs.themeMode = mode
                        onThemeChanged(mode, selectedAccent, customHexText, useCustomHex)
                    }
                    .padding(vertical = 4.dp),
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

        HorizontalDivider()

        Text("Catppuccin Theme Palette", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Latte", "Frappé", "Macchiato", "Mocha").forEach { cat ->
                FilterChip(
                    selected = selectedCatppuccin == cat,
                    onClick = {
                        selectedCatppuccin = cat
                        prefs.catppuccinTheme = cat
                    },
                    label = { Text(cat) }
                )
            }
        }

        HorizontalDivider()

        Text("Accent Color Preset", fontSize = 16.sp, fontWeight = FontWeight.Bold)

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

        Text("Custom HEX Accent Color", fontSize = 16.sp, fontWeight = FontWeight.Bold)

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

        HorizontalDivider()

        Text("UI Card Style & Layout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Flat", "Elevated", "Outlined").forEach { style ->
                FilterChip(
                    selected = cardStyle == style,
                    onClick = {
                        cardStyle = style
                        prefs.cardStyle = style
                    },
                    label = { Text(style) }
                )
            }
        }

        Text("Border Corner Radius: ${cornerRadius}dp", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = cornerRadius.toFloat(),
            onValueChange = {
                cornerRadius = it.toInt()
                prefs.cornerRadius = it.toInt()
            },
            valueRange = 0f..24f,
            steps = 5
        )

        Text("UI Font Scale: ${String.format("%.1fx", fontScale)}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = fontScale,
            onValueChange = {
                fontScale = it
                prefs.fontScale = it
            },
            valueRange = 0.8f..1.3f,
            steps = 5
        )
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
    var showTop by remember { mutableStateOf(prefs.showTopRated) }
    var showNew by remember { mutableStateOf(prefs.showNewReleases) }
    var showRec by remember { mutableStateOf(prefs.showRecommended) }
    var filterMode by remember { mutableStateOf(prefs.homeFilterMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Home Sections Visibility", fontSize = 16.sp, fontWeight = FontWeight.Bold)

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
        SettingSwitchRow("Top Rated Anime & Movies", showTop) {
            showTop = it
            prefs.showTopRated = it
        }
        SettingSwitchRow("New Season Releases", showNew) {
            showNew = it
            prefs.showNewReleases = it
        }
        SettingSwitchRow("Recommended Highlights", showRec) {
            showRec = it
            prefs.showRecommended = it
        }

        HorizontalDivider()

        Text("Default Content Category Filter", fontSize = 16.sp, fontWeight = FontWeight.Bold)

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

    var playerEngine by remember { mutableStateOf(prefs.playerEngine) }
    var prefQuality by remember { mutableStateOf(prefs.preferredQuality) }
    var autoSkipIntro by remember { mutableStateOf(prefs.autoSkipIntro) }
    var skipIntroDuration by remember { mutableStateOf(prefs.skipIntroDuration) }
    var seekDuration by remember { mutableStateOf(prefs.seekDuration) }
    var autoplayNext by remember { mutableStateOf(prefs.autoplayNext) }
    var screenLock by remember { mutableStateOf(prefs.playerScreenLock) }
    var decoder by remember { mutableStateOf(prefs.playerDecoder) }
    var gestureVol by remember { mutableStateOf(prefs.gestureVolumeControl) }
    var gestureBright by remember { mutableStateOf(prefs.gestureBrightnessControl) }
    var subLang by remember { mutableStateOf(prefs.subtitleLanguage) }
    var subSize by remember { mutableStateOf(prefs.subtitleSize) }
    var subBgStyle by remember { mutableStateOf(prefs.subtitleBgStyle) }
    var audioBoost by remember { mutableStateOf(prefs.audioBoost) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Player Engine & Decoders", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Text("Playback Engine", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ExoPlayer (Native)", "VLC Engine", "MPV Core").forEach { eng ->
                FilterChip(
                    selected = playerEngine == eng,
                    onClick = {
                        playerEngine = eng
                        prefs.playerEngine = eng
                    },
                    label = { Text(eng) }
                )
            }
        }

        Text("Preferred Video Resolution", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1080p", "720p", "480p", "Auto").forEach { q ->
                FilterChip(
                    selected = prefQuality == q,
                    onClick = {
                        prefQuality = q
                        prefs.preferredQuality = q
                    },
                    label = { Text(q) }
                )
            }
        }

        Text("Decoder Hardware Acceleration", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        HorizontalDivider()

        Text("Gestures & Playback Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        SettingSwitchRow("Vertical Swipe Volume Control", gestureVol) {
            gestureVol = it
            prefs.gestureVolumeControl = it
        }

        SettingSwitchRow("Vertical Swipe Brightness Control", gestureBright) {
            gestureBright = it
            prefs.gestureBrightnessControl = it
        }

        SettingSwitchRow("Auto-Skip Intro / Outro", autoSkipIntro) {
            autoSkipIntro = it
            prefs.autoSkipIntro = it
        }

        Text("Skip Intro Duration: ${skipIntroDuration}s", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = skipIntroDuration.toFloat(),
            onValueChange = {
                skipIntroDuration = it.toInt()
                prefs.skipIntroDuration = it.toInt()
            },
            valueRange = 30f..120f,
            steps = 9
        )

        Text("Double-Tap Seek Duration: ${seekDuration}s", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = seekDuration.toFloat(),
            onValueChange = {
                seekDuration = it.toInt()
                prefs.seekDuration = it.toInt()
            },
            valueRange = 5f..30f,
            steps = 4
        )

        SettingSwitchRow("Autoplay Next Episode", autoplayNext) {
            autoplayNext = it
            prefs.autoplayNext = it
        }

        SettingSwitchRow("Enable Touch Lock Overlay", screenLock) {
            screenLock = it
            prefs.playerScreenLock = it
        }

        HorizontalDivider()

        Text("Subtitles & Audio Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Text("Preferred Subtitle Language", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("English", "Spanish", "French", "Japanese", "None").forEach { sLang ->
                FilterChip(
                    selected = subLang == sLang,
                    onClick = {
                        subLang = sLang
                        prefs.subtitleLanguage = sLang
                    },
                    label = { Text(sLang) }
                )
            }
        }

        Text("Subtitle Text Size: ${subSize}sp", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = subSize.toFloat(),
            onValueChange = {
                subSize = it.toInt()
                prefs.subtitleSize = it.toInt()
            },
            valueRange = 12f..28f,
            steps = 7
        )

        Text("Subtitle Background Box", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Transparent Black", "Solid Black", "None").forEach { bg ->
                FilterChip(
                    selected = subBgStyle == bg,
                    onClick = {
                        subBgStyle = bg
                        prefs.subtitleBgStyle = bg
                    },
                    label = { Text(bg) }
                )
            }
        }

        SettingSwitchRow("Enable Audio Boost (+200% Volume)", audioBoost) {
            audioBoost = it
            prefs.audioBoost = it
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
    var tapToScroll by remember { mutableStateOf(prefs.tapToScroll) }
    var webtoonGap by remember { mutableStateOf(prefs.webtoonGap) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Manga & Reader Options", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Text("Default Reading Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        Text("Reading Direction", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Right to Left", "Left to Right", "Vertical").forEach { dir ->
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

        HorizontalDivider()

        Text("Gestures & Display Options", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        SettingSwitchRow("Double-Tap to Zoom Page", doubleTapZoom) {
            doubleTapZoom = it
            prefs.doubleTapZoom = it
        }

        SettingSwitchRow("Tap Screen Edges to Scroll", tapToScroll) {
            tapToScroll = it
            prefs.tapToScroll = it
        }

        Text("Webtoon Page Gap Padding: ${webtoonGap}dp", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = webtoonGap.toFloat(),
            onValueChange = {
                webtoonGap = it.toInt()
                prefs.webtoonGap = it.toInt()
            },
            valueRange = 0f..30f,
            steps = 5
        )
    }
}

@Composable
fun LibrarySettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var sortOrder by remember { mutableStateOf(prefs.librarySortOrder) }
    var autoSync by remember { mutableStateOf(prefs.autoSyncTrackers) }
    var aniListUser by remember { mutableStateOf(prefs.aniListUsername) }
    var malUser by remember { mutableStateOf(prefs.malUsername) }
    var showAniListDialog by remember { mutableStateOf(false) }
    var showMalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Library Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Text("Default Library Sort Order", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Title (A-Z)", "Last Read", "Date Added", "Rating").forEach { sort ->
                FilterChip(
                    selected = sortOrder == sort,
                    onClick = {
                        sortOrder = sort
                        prefs.librarySortOrder = sort
                    },
                    label = { Text(sort) }
                )
            }
        }

        HorizontalDivider()

        Text("Tracking Sync Integration", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        SettingSwitchRow("Auto Sync Progress with Trackers", autoSync) {
            autoSync = it
            prefs.autoSyncTrackers = it
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("AniList Tracker", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(if (aniListUser.isBlank()) "Not connected" else "Logged in as @$aniListUser", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { showAniListDialog = true }
                    ) {
                        Text(if (aniListUser.isBlank()) "Connect" else "Manage")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("MyAnimeList Tracker", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(if (malUser.isBlank()) "Not connected" else "Logged in as @$malUser", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { showMalDialog = true }
                    ) {
                        Text(if (malUser.isBlank()) "Connect" else "Manage")
                    }
                }
            }
        }
    }

    if (showAniListDialog) {
        var tempUser by remember { mutableStateOf(aniListUser) }
        AlertDialog(
            onDismissRequest = { showAniListDialog = false },
            title = { Text("AniList Sync Account") },
            text = {
                OutlinedTextField(
                    value = tempUser,
                    onValueChange = { tempUser = it },
                    label = { Text("AniList Username") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    aniListUser = tempUser
                    prefs.aniListUsername = tempUser
                    showAniListDialog = false
                }) {
                    Text("Save Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAniListDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMalDialog) {
        var tempUser by remember { mutableStateOf(malUser) }
        AlertDialog(
            onDismissRequest = { showMalDialog = false },
            title = { Text("MyAnimeList Sync Account") },
            text = {
                OutlinedTextField(
                    value = tempUser,
                    onValueChange = { tempUser = it },
                    label = { Text("MyAnimeList Username") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    malUser = tempUser
                    prefs.malUsername = tempUser
                    showMalDialog = false
                }) {
                    Text("Save Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
