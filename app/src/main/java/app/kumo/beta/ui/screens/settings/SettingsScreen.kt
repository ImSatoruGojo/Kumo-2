package app.kumo.beta.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var repositories by remember { mutableStateOf(repositoryManager.getRepositories()) }
    var extensions by remember { mutableStateOf(repositoryManager.getAllExtensions()) }
    var repositoryUrl by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    fun refreshUi() { repositories = repositoryManager.getRepositories(); extensions = repositoryManager.getAllExtensions() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(KumoBlack).padding(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text("Settings", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
        }
        item {
            SettingsGroup("General") { SettingsItem("Theme", "Dark"); SettingsItem("App language", "System"); SettingsItem("Cache limit", "512 MB") }
            Spacer(Modifier.height(16.dp))
        }
        item {
            SettingsGroup("Player") { SettingsItem("Default quality", "Auto"); SettingsItem("Playback speed", "1x"); SettingsItem("Autoplay next", "Off"); SettingsItem("Double tap seek", "10 seconds") }
            Spacer(Modifier.height(16.dp))
        }
        item {
            Text("Repositories", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = repositoryUrl, onValueChange = { repositoryUrl = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Repository URL") }, placeholder = { Text("https://example.com/repo.json") })
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = repositoryUrl.isNotBlank() && !busy, onClick = {
                    runCatching { repositoryManager.addRepository(repositoryUrl) }.onSuccess { repositoryUrl = ""; refreshUi(); status = "Repository added" }.onFailure { status = it.message ?: "Invalid repository URL" }
                }) { Text("Add") }
                OutlinedButton(enabled = !busy && repositories.any { it.enabled }, onClick = {
                    busy = true
                    scope.launch {
                        val results = repositoryManager.refreshAllRepositories()
                        refreshUi(); busy = false
                        status = if (results.values.any { it is RepositoryResult.Success }) "Repositories refreshed" else "No repository could be refreshed"
                    }
                }) { Text(if (busy) "Refreshing…" else "Refresh all") }
            }
            status?.let { Text(it, color = KumoTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
            Spacer(Modifier.height(12.dp))
        }
        items(repositories, key = { it.id }) { repo ->
            RepositoryRow(repo,
                onRefresh = {
                    busy = true
                    scope.launch { val result = repositoryManager.refreshRepository(repo.id); refreshUi(); busy = false; status = when (result) { is RepositoryResult.Success -> "Loaded " + result.extensions.size + " extensions"; is RepositoryResult.Failure -> result.message } }
                },
                onRemove = { repositoryManager.removeRepository(repo.id); refreshUi() })
            Spacer(Modifier.height(8.dp))
        }
        if (extensions.isNotEmpty()) {
            item { Text("Available extensions", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(8.dp)) }
            items(extensions, key = { it.id }) { extension ->
                ExtensionRow(extension, extensionInstaller, onInstalled = { status = "Installed " + extension.name }, onError = { status = it })
                Spacer(Modifier.height(8.dp))
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            SettingsGroup("About") { SettingsItem("Version", "0.1.0-beta"); SettingsItem("Build", "Core implementation") }
        }
    }
}

@Composable
private fun RepositoryRow(repository: Repository, onRefresh: () -> Unit, onRemove: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(KumoCard, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(repository.name, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(repository.url, color = KumoTextSecondary, fontSize = 12.sp)
        repository.lastRefreshStatus?.let { Text("Status: " + it, color = KumoTextSecondary, fontSize = 12.sp) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = onRefresh) { Text("Refresh", color = KumoPurple) }; TextButton(onClick = onRemove) { Text("Remove", color = KumoTextSecondary) } }
    }
}

@Composable
private fun ExtensionRow(extension: ExtensionInfo, installer: ExtensionInstaller, onInstalled: () -> Unit, onError: (String) -> Unit) {
    var installing by remember(extension.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxWidth().background(KumoCard, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(extension.name, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(extension.type.name, color = KumoTextSecondary, fontSize = 12.sp)
        extension.version?.let { Text("Version " + it, color = KumoTextSecondary, fontSize = 12.sp) }
        Button(enabled = !installing, onClick = { installing = true; scope.launch { installer.install(extension).onSuccess { onInstalled() }.onFailure { onError(it.message ?: "Installation failed") }; installing = false } }) { Text(if (installing) "Installing…" else "Install") }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, color = KumoTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
    Column(Modifier.fillMaxWidth().background(KumoCard, RoundedCornerShape(12.dp)).padding(vertical = 4.dp), content = content)
}

@Composable
private fun SettingsItem(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontSize = 15.sp)
        Text(value, color = KumoTextSecondary, fontSize = 14.sp)
    }
}