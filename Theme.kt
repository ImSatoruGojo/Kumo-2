package app.kumo.beta.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KumoBlack = Color(0xFF0B0B10)
val KumoSurface = Color(0xFF14141C)
val KumoCard = Color(0xFF181822)
val KumoPurple = Color(0xFF6D4AFF)
val KumoPurpleDark = Color(0xFF4A2FC7)
val KumoText = Color(0xFFFFFFFF)
val KumoTextSecondary = Color(0xFFAAAAAA)
val KumoBeta = Color(0xFFFFB020)

private val DarkColorScheme = darkColorScheme(
    primary = KumoPurple,
    onPrimary = Color.White,
    secondary = KumoPurpleDark,
    background = KumoBlack,
    surface = KumoSurface,
    onBackground = KumoText,
    onSurface = KumoText,
    surfaceVariant = KumoCard,
    onSurfaceVariant = KumoTextSecondary
)

@Composable
fun KumoTheme(
    darkTheme: Boolean = true, // always dark for now
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
