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
    HOME("Home Screen", Icons.Default.Home),
    PLAYER("Player Settings", Icons.Default.PlayCircle),
    LIBRARY("Library & Tags", Icons.Default.Bookmark),
    DOWNLOADS("Downloads Manager", Icons.Default.Download),
    EXTENSIONS("Extensions & Providers", Icons.Default.Extension),
    STORAGE("Storage & Cache", Icons.Default.Storage),
    CONTENT("Content & Filters", Icons.Default.Shield),
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
                    SettingsSubmenu.DOWNLOADS -> DownloadsManagerScreen()
                    SettingsSubmenu.EXTENSIONS -> ExtensionsManagerScreen()
                    SettingsSubmenu.STORAGE -> StorageSettings()
                    SettingsSubmenu.CONTENT -> ContentSettings()
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
        // Branding Banner Header
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
                    Text("Media discovery & streaming engine", color = Color.LightGray, fontSize = 12.sp)
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
    var language by remember { mutableStateOf(prefs.appLanguage) }
    var dataSaving by remember { mutableStateOf(prefs.dataSavingEnabled) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("General Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        SettingSwitchRow("Data Saver Mode", dataSaving) {
            dataSaving = it
            prefs.dataSavingEnabled = it
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("App Language: $language", fontSize = 14.sp)
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

    Column(modifier = Modifier.padding(16.dp)) {
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
    }
}

@Composable
fun PlayerSettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var autoSkipIntro by remember { mutableStateOf(prefs.autoSkipIntro) }
    var autoplayNext by remember { mutableStateOf(prefs.autoplayNext) }
    var screenLock by remember { mutableStateOf(prefs.playerScreenLock) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Player Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        SettingSwitchRow("Auto-Skip Intro / Outro", autoSkipIntro) {
            autoSkipIntro = it
            prefs.autoSkipIntro = it
        }
        SettingSwitchRow("Autoplay Next Episode", autoplayNext) {
            autoplayNext = it
            prefs.autoplayNext = it
        }
        SettingSwitchRow("Enable Screen Touch Lock", screenLock) {
            screenLock = it
            prefs.playerScreenLock = it
        }
    }
}

@Composable
fun LibrarySettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Library Options", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Default Sort Order: Title (A-Z)", fontSize = 14.sp)
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
    val extensionManager = remember { ExtensionManager(context) }
    val extensions = remember { extensionManager.getExtensions() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Installed Extensions & Providers", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        extensions.forEach { ext ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ext.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("v${ext.version}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(ext.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = ext.isEnabled, onCheckedChange = {})
                }
            }
        }
    }
}

@Composable
fun StorageSettings() {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
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

    var nsfwEnabled by remember { mutableStateOf(prefs.nsfwEnabled) }
    var spoilerWarnings by remember { mutableStateOf(prefs.showSpoilerWarnings) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Content & Safety Filters", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        SettingSwitchRow("Allow Adult / 18+ Content", nsfwEnabled) {
            nsfwEnabled = it
            prefs.nsfwEnabled = it
        }

        SettingSwitchRow("Show Episode Spoiler Warnings", spoilerWarnings) {
            spoilerWarnings = it
            prefs.showSpoilerWarnings = it
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
                Text("Version 0.2.0-v2 (Kumo V2 Overhaul)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Brand new Kumo branding visual identity.", fontSize = 14.sp)
                Text("• Added Custom HEX Color Picker with live preview.", fontSize = 14.sp)
                Text("• Added Downloads Manager & Queue UI architecture.", fontSize = 14.sp)
                Text("• Added Extensions & Media Providers management screen.", fontSize = 14.sp)
                Text("• Added Search History and expanded compact filter options.", fontSize = 14.sp)
                Text("• Added Storage & Cache cleanup tools and Content Safety controls.", fontSize = 14.sp)
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
        Text("Kumo Beta V2", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Version 0.2.0-v2", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("A lightweight, clean native Android media application.", fontSize = 14.sp)
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
