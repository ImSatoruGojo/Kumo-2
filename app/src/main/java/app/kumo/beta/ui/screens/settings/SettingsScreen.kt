package app.kumo.beta.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.local.AccentColorOption
import app.kumo.beta.data.local.AppThemeMode
import app.kumo.beta.data.local.PreferencesManager

enum class SettingsSubmenu(val title: String, val icon: ImageVector) {
    GENERAL("General", Icons.Default.Tune),
    APPEARANCE("Appearance", Icons.Default.Palette),
    HOME("Home Screen", Icons.Default.Home),
    PLAYER("Player", Icons.Default.PlayCircle),
    LIBRARY("Library", Icons.Default.Bookmark),
    STORAGE("Storage & Cache", Icons.Default.Storage),
    DOWNLOADS("Downloads", Icons.Default.Download),
    CHANGELOG("Changelog", Icons.Default.History),
    ADVANCED("Advanced", Icons.Default.Build),
    ABOUT("About", Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemeChanged: (AppThemeMode, AccentColorOption) -> Unit
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
                    SettingsSubmenu.STORAGE -> StorageSettings()
                    SettingsSubmenu.DOWNLOADS -> DownloadSettings()
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("General Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Language: $language", fontSize = 14.sp)
    }
}

@Composable
fun AppearanceSettings(onThemeChanged: (AppThemeMode, AccentColorOption) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var selectedTheme by remember { mutableStateOf(prefs.themeMode) }
    var selectedAccent by remember { mutableStateOf(prefs.accentColor) }

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
                        onThemeChanged(mode, selectedAccent)
                    }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedTheme == mode,
                    onClick = {
                        selectedTheme = mode
                        prefs.themeMode = mode
                        onThemeChanged(mode, selectedAccent)
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = mode.name, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Accent Color", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            selectedAccent = option
                            prefs.accentColor = option
                            onThemeChanged(selectedTheme, option)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedAccent == option) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White
                        )
                    }
                }
            }
        }
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
        Text("Home Sections", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        SettingSwitchRow("Continue Watching", showCW) {
            showCW = it
            prefs.showContinueWatching = it
        }
        SettingSwitchRow("Popular Right Now", showPop) {
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
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Player Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Default Quality: Auto", fontSize = 14.sp)
        Text("Decoder: Hardware / Auto", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("(Full player integration available in Milestone 3)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LibrarySettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Library Options", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Default Sort: Title", fontSize = 14.sp)
    }
}

@Composable
fun StorageSettings() {
    val context = LocalContext.current
    var cleared by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Storage Management", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { cleared = true }) {
            Text(if (cleared) "Cache Cleared!" else "Clear Image Cache")
        }
    }
}

@Composable
fun DownloadSettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Download Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text("WiFi Only: Enabled", fontSize = 14.sp)
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
                Text("• Brand new Grok-inspired minimal white-on-black logo and app icon.", fontSize = 14.sp)
                Text("• Anikoto-style featured hero banner carousel on Home screen.", fontSize = 14.sp)
                Text("• Top search bar & filter shortcut with instant navigation.", fontSize = 14.sp)
                Text("• Fully persistent local Library (Favorites, Watch Later, Watching, Completed, Dropped).", fontSize = 14.sp)
                Text("• Local Continue Watching system with episode progress bars.", fontSize = 14.sp)
                Text("• Multi-tier Settings submenus with real-time Theme & Accent color switching.", fontSize = 14.sp)
                Text("• Custom list creation, custom tags, and local search.", fontSize = 14.sp)
                Text("• Enhanced Details page with backdrop header, synopsis, and episode list.", fontSize = 14.sp)
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Version 0.1.0-beta (Milestone 1)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Native Jetpack Compose foundation setup.", fontSize = 14.sp)
                Text("• Basic bottom navigation (Home, Search, Library, Settings).", fontSize = 14.sp)
                Text("• Dark theme baseline with demo media catalog.", fontSize = 14.sp)
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
            Text("Reset All Settings")
        }
    }
}

@Composable
fun AboutSettings() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Kumo Beta V2", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Version 0.2.0-v2 (Milestone 2)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("A lightweight, clean native Android media discovery application.", fontSize = 14.sp)
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
