package app.kumo.beta.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.DemoData
import app.kumo.beta.data.local.LibraryCategory
import app.kumo.beta.data.local.LibraryManager
import app.kumo.beta.ui.components.PosterCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToDetails: (String) -> Unit
) {
    val context = LocalContext.current
    val libManager = remember { LibraryManager(context) }

    var selectedTab by remember { mutableStateOf(0) }
    val categories = LibraryCategory.entries

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    val customLists by remember { mutableStateOf(libManager.getAllCustomLists()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(onClick = { showAddCustomDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add List", tint = MaterialTheme.colorScheme.primary)
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp
        ) {
            categories.forEachIndexed { index, cat ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = cat.displayName,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        val currentCat = categories[selectedTab]
        val titleIds = remember(selectedTab) { libManager.getTitlesInCategory(currentCat) }
        val titlesInCat = remember(titleIds) {
            titleIds.mapNotNull { DemoData.getById(it) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (titlesInCat.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No content in ${currentCat.displayName}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(titlesInCat) { item ->
                    PosterCard(title = item, onClick = { onNavigateToDetails(item.id) })
                }
            }
        }
    }

    if (showAddCustomDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("Create Custom List") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    placeholder = { Text("List Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newListName.isNotBlank()) {
                        libManager.addCustomList(newListName.trim())
                        newListName = ""
                        showAddCustomDialog = false
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
