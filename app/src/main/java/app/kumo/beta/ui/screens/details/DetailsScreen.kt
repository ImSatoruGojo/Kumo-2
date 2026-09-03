package app.kumo.beta.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import app.kumo.beta.ui.components.typeLabel
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoCard
import app.kumo.beta.ui.theme.KumoPurple
import app.kumo.beta.ui.theme.KumoTextSecondary

@Composable
fun DetailsScreen(
    title: Title,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KumoBlack)
    ) {
        item {
            // Top bar + cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(KumoPurple.copy(0.55f), KumoBlack)
                        )
                    )
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = title.title,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(typeLabel(title.type))
                            title.year?.let { append("  •  $it") }
                            if (title.genres.isNotEmpty()) {
                                append("  •  ")
                                append(title.genres.take(3).joinToString(", "))
                            }
                        },
                        color = KumoTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {
            Text(
                text = title.description,
                color = Color.White.copy(0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            // Play button placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .background(KumoPurple, RoundedCornerShape(10.dp))
                    .clickable { /* player later */ },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (title.type == MediaType.MANGA) "Read" else "Play",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        if (title.episodes.isNotEmpty()) {
            item {
                Text(
                    text = "Episodes",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(title.episodes, key = { it.id }) { ep ->
                EpisodeRow(
                    number = ep.number,
                    title = ep.title ?: "Episode ${ep.number}"
                )
            }
        }

        if (title.chapters.isNotEmpty()) {
            item {
                Text(
                    text = "Chapters",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(title.chapters, key = { it.id }) { ch ->
                EpisodeRow(
                    number = ch.number,
                    title = ch.title ?: "Chapter ${ch.number}"
                )
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun EpisodeRow(number: Int, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(KumoCard, RoundedCornerShape(10.dp))
            .clickable { }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$number",
            color = KumoPurple,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
