package app.kumo.beta.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.DemoData
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import app.kumo.beta.ui.components.PosterCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToDetails: (String) -> Unit = {},
    onTitleClick: (Title) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<MediaType?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val allTitles = remember { DemoData.allTitles }
    val allGenres = remember { allTitles.flatMap { it.genres }.distinct().sorted() }

    val searchResults by remember(query, selectedGenre, selectedType) {
        derivedStateOf {
            var list = if (query.isNotBlank()) DemoData.search(query) else allTitles
            selectedGenre?.let { g -> list = list.filter { it.genres.contains(g) } }
            selectedType?.let { t -> list = list.filter { it.type == t } }
            list
        }
    }

    val handleDetails = { title: Title ->
        onNavigateToDetails(title.id)
        onTitleClick(title)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search titles, genres...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = { showFilterSheet = true },
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(
                        if (selectedGenre != null || selectedType != null)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = Color.White
                )
            }
        }

        // Active Filter Chips
        if (selectedGenre != null || selectedType != null) {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedType?.let { type ->
                    FilterChip(
                        selected = true,
                        onClick = { selectedType = null },
                        label = { Text("Type: ${type.name}") },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                    )
                }
                selectedGenre?.let { genre ->
                    FilterChip(
                        selected = true,
                        onClick = { selectedGenre = null },
                        label = { Text("Genre: $genre") },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No results found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(searchResults) { item ->
                    PosterCard(title = item, onClick = { handleDetails(item) })
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(text = "Filter Content", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Media Type", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = if (selectedType == type) null else type },
                            label = { Text(type.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Genres", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    allGenres.chunked(3).forEach { rowGenres ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowGenres.forEach { genre ->
                                FilterChip(
                                    selected = selectedGenre == genre,
                                    onClick = { selectedGenre = if (selectedGenre == genre) null else genre },
                                    label = { Text(genre) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
