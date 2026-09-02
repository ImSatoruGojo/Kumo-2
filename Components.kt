package app.kumo.beta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import app.kumo.beta.ui.theme.KumoBeta
import app.kumo.beta.ui.theme.KumoCard
import app.kumo.beta.ui.theme.KumoPurple
import app.kumo.beta.ui.theme.KumoTextSecondary

@Composable
fun BetaBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(KumoBeta.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Kumo is still in beta — expect some bugs",
            color = KumoBeta,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun PosterCard(
    title: Title,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(KumoPurple.copy(alpha = 0.7f), KumoCard)
                    )
                ),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = title.title.take(1),
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = title.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = typeLabel(title.type),
            color = KumoTextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
fun HorizontalRail(
    titles: List<Title>,
    onTitleClick: (Title) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(titles, key = { it.id }) { title ->
            PosterCard(title = title, onClick = { onTitleClick(title) })
        }
    }
}

fun typeLabel(type: MediaType): String = when (type) {
    MediaType.ANIME -> "Anime"
    MediaType.MOVIE -> "Movie"
    MediaType.SHOW -> "TV Show"
    MediaType.CARTOON -> "Cartoon"
    MediaType.MANGA -> "Manga"
}
