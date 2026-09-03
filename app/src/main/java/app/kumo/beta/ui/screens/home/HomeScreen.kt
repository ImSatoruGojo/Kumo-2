package app.kumo.beta.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.data.DemoData
import app.kumo.beta.model.MediaType
import app.kumo.beta.model.Title
import app.kumo.beta.ui.components.BetaBadge
import app.kumo.beta.ui.components.HorizontalRail
import app.kumo.beta.ui.components.SectionTitle
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoPurple

@Composable
fun HomeScreen(
    onTitleClick: (Title) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KumoBlack)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Kumo",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            BetaBadge()
        }

        // Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(140.dp)
                .background(
                    Brush.linearGradient(listOf(KumoPurple.copy(0.6f), Color(0xFF151522))),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Everything in one place",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Anime, Movies, TV Shows, Cartoons & Manga",
                    color = Color.White.copy(0.8f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Sections
        SectionTitle("Popular Anime")
        HorizontalRail(
            titles = DemoData.byType(MediaType.ANIME),
            onTitleClick = onTitleClick
        )

        Spacer(Modifier.height(16.dp))
        SectionTitle("Movies")
        HorizontalRail(
            titles = DemoData.byType(MediaType.MOVIE),
            onTitleClick = onTitleClick
        )

        Spacer(Modifier.height(16.dp))
        SectionTitle("TV Shows")
        HorizontalRail(
            titles = DemoData.byType(MediaType.SHOW),
            onTitleClick = onTitleClick
        )

        Spacer(Modifier.height(16.dp))
        SectionTitle("Cartoons")
        HorizontalRail(
            titles = DemoData.byType(MediaType.CARTOON),
            onTitleClick = onTitleClick
        )

        Spacer(Modifier.height(16.dp))
        SectionTitle("Manga")
        HorizontalRail(
            titles = DemoData.byType(MediaType.MANGA),
            onTitleClick = onTitleClick
        )

        Spacer(Modifier.height(80.dp))
    }
}
