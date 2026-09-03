package app.kumo.beta.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.DemoData
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import app.kumo.beta.ui.components.PosterCard
import app.kumo.beta.ui.components.typeLabel
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoCard
import app.kumo.beta.ui.theme.KumoTextSecondary
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    onTitleClick: (Title) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Title>>(emptyList()) }

    // 250ms debounce
    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            return@LaunchedEffect
        }
        delay(250)
        results = DemoData.search(query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KumoBlack)
    ) {
        Text(
            text = "Search",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(KumoCard, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = KumoTextSecondary
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.White),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text("Search anime, movies, shows, manga…", color = KumoTextSecondary, fontSize = 15.sp)
                    }
                    inner()
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (query.isBlank()) {
            Text(
                text = "Start typing to search across all categories",
                color = KumoTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else if (results.isEmpty()) {
            Text(
                text = "No results for \"$query\"",
                color = KumoTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            // Group by type
            val grouped = results.groupBy { it.type }
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MediaType.entries.forEach { type ->
                    val list = grouped[type] ?: return@forEach
                    item {
                        Text(
                            text = typeLabel(type),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            list.take(4).forEach { title ->
                                PosterCard(
                                    title = title,
                                    onClick = { onTitleClick(title) },
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
