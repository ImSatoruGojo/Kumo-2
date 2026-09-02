package app.kumo.beta.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoTextSecondary

@Composable
fun LibraryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KumoBlack)
            .padding(16.dp)
    ) {
        Text(
            text = "Library",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Continue Watching & Watchlist",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Your progress will appear here once you start watching.",
                    color = KumoTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}
