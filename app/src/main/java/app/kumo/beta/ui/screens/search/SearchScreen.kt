package app.kumo.beta.ui.screens.search

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.DemoData
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import app.kumo.beta.provider.ProviderEngine
import app.kumo.beta.ui.components.TitleCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    providerEngine: ProviderEngine,
    onTitleClick: (Title) -> Unit,
    openFiltersInitially: Boolean = false
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(DemoData.allTitles) }
    var selectedType by remember { mutableStateOf<MediaType?>(null) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var showFilters by remember { mutableStateOf(openFiltersInitially) }
    var searching by remember { mutableStateOf(false) }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) query = spoken
        }
    }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = DemoData.allTitles
            return@LaunchedEffect
        }
        delay(250)
        searching = true
        val providerResults = runCatching { providerEngine.search(query) }.getOrDefault(emptyList()).map { it.title }
        val localResults = DemoData.search(query)
        results = (providerResults + localResults)
            .distinctBy { it.id }
        searching = false
    }

    val filteredResults = results.filter { title ->
        (selectedType == null || title.type == selectedType) &&
        (selectedGenre == null || title.genres.any { it.equals(selectedGenre, ignoreCase = true) })
    }

    val genres = remember(results) {
        results.flatMap { it.genres }.distinct().sorted()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search anime, movies, manga...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { showFilters = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.FilterList, "Filter", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(
                onClick = {
                    voiceLauncher.launch(
                        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        }
                    )
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Mic, "Voice search", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (selectedType != null || selectedGenre != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedType?.let { FilterChip(selected = true, onClick = { selectedType = null }, label = { Text(it.name) }) }
                selectedGenre?.let { FilterChip(selected = true, onClick = { selectedGenre = null }, label = { Text(it) }) }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (searching) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Text(
            text = if (query.isBlank()) "Browse" else "Search Results",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(10.dp))

        if (filteredResults.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredResults, key = { it.id }) { title ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TitleCard(title = title, width = 110.dp, height = 150.dp, onClick = { onTitleClick(title) })
                        Column(Modifier.weight(1f)) {
                            Text(title.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(title.type.name, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            title.year?.let { Text(it.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
                            if (title.genres.isNotEmpty()) {
                                Text(title.genres.take(3).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Filters", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Type", fontWeight = FontWeight.SemiBold)
                MediaType.entries.forEach { type ->
                    FilterChip(selected = selectedType == type, onClick = { selectedType = if (selectedType == type) null else type }, label = { Text(type.name) })
                }
                if (genres.isNotEmpty()) {
                    Text("Genre", fontWeight = FontWeight.SemiBold)
                    genres.take(20).forEach { genre ->
                        FilterChip(selected = selectedGenre == genre, onClick = { selectedGenre = if (selectedGenre == genre) null else genre }, label = { Text(genre) })
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
