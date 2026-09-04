package app.kumo.beta.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.DemoData
import app.kumo.beta.data.local.PreferencesManager
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import app.kumo.beta.ui.components.PosterCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToDetails: (String) -> Unit = {},
    onTitleClick: (Title) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var query by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<MediaType?>(null) }
    var selectedYear by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedSort by remember { mutableStateOf("Popularity") }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Search History List State
    val searchHistoryList = remember {
        mutableStateListOf("Solo Leveling", "Jujutsu Kaisen", "Attack on Titan", "Demon Slayer")
    }

    val allTitles = remember { DemoData.allTitles }
    val expandedGenres = listOf(
        "Action", "Adventure", "Fantasy", "Sci-Fi", "Supernatural",
        "Comedy", "Slice of Life", "Isekai", "Romance", "Shonen", "Seinen", "Thriller", "Mecha"
    )
    val years = listOf("2024", "2023", "2022", "2021", "2020 & Earlier")
    val sortOptions = listOf("Popularity", "Rating", "Title (A-Z)")

    val searchResults by remember(query, selectedGenre, selectedType, selectedYear, selectedStatus, selectedSort) {
        derivedStateOf {
            var list = if (query.isNotBlank()) DemoData.search(query) else allTitles
            selectedGenre?.let { g -> list = list.filter { it.genres.contains(g) } }
            selectedType?.let { t -> list = list.filter { it.type == t } }
            selectedYear?.let { y ->
                if (y == "2024") list = list.filter { it.year == 2024 }
                else if (y == "2023") list = list.filter { it.year == 2023 }
                else if (y == "2022") list = list.filter { it.year == 2022 }
            }
            if (selectedSort == "Rating") list = list.sortedByDescending { it.rating ?: 0f }
            else if (selectedSort == "Title (A-Z)") list = list.sortedBy { it.title }

            list
        }
    }

    val handleDetails = { title: Title ->
        if (query.isNotBlank() && !searchHistoryList.contains(query.trim())) {
            searchHistoryList.add(0, query.trim())
        }
        onNavigateToDetails(title.id)
        onTitleClick(title)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search titles, genres, year...") },
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
                        if (selectedGenre != null || selectedType != null || selectedYear != null || selectedStatus != null)
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

        // Search History Chips (Visible when query is empty)
        if (query.isEmpty() && searchHistoryList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Recent Searches",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Searches",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextTextButton(text = "Clear History") {
                    searchHistoryList.clear()
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(searchHistoryList) { histQuery ->
                    SuggestionChip(
                        onClick = { query = histQuery },
                        label = { Text(histQuery, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Active Filter Chips
        if (selectedGenre != null || selectedType != null || selectedYear != null || selectedStatus != null) {
            LazyRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedType?.let { type ->
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { selectedType = null },
                            label = { Text("Type: ${type.name}") },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                        )
                    }
                }
                selectedGenre?.let { genre ->
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { selectedGenre = null },
                            label = { Text("Genre: $genre") },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                        )
                    }
                }
                selectedYear?.let { year ->
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { selectedYear = null },
                            label = { Text("Year: $year") },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No results found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (query.isNotBlank()) {
            // ANIKAI-INSPIRED VERTICAL SUBMITTED SEARCH RESULTS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Search Results (${searchResults.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                searchResults.forEach { title ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { handleDetails(title) },
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
                                    .size(70.dp, 100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF222230))
                            ) {
                                if (!title.posterUrl.isNullOrEmpty()) {
                                    coil.compose.AsyncImage(
                                        model = title.posterUrl,
                                        contentDescription = title.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = title.type.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    title.rating?.let { r ->
                                        Text("⭐ %.1f".format(r), fontSize = 12.sp, color = Color(0xFFFFB020), fontWeight = FontWeight.Bold)
                                    }
                                    title.year?.let { y ->
                                        Text("• $y", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = title.genres.joinToString(" • "),
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { handleDetails(title) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Watch", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("My List", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
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
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(text = "Filter Content", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

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
                Text(text = "Sort By", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortOptions.forEach { sort ->
                        FilterChip(
                            selected = selectedSort == sort,
                            onClick = { selectedSort = sort },
                            label = { Text(sort) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Release Year", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    years.take(3).forEach { year ->
                        FilterChip(
                            selected = selectedYear == year,
                            onClick = { selectedYear = if (selectedYear == year) null else year },
                            label = { Text(year) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Genres & Tags", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    expandedGenres.chunked(3).forEach { rowGenres ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
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

@Composable
fun TextTextButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable { onClick() }
    )
}
