package app.kumo.beta.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.kumo.beta.data.local.AccentColorOption
import app.kumo.beta.data.local.AppThemeMode

// Base Background & Card Colors
val KumoBlack = Color(0xFF0B0B10)
val KumoAmoled = Color(0xFF000000)
val KumoSurface = Color(0xFF14141C)
val KumoCard = Color(0xFF181822)

// Legacy accent color compatibility
val KumoPurple = Color(0xFF6D4AFF)

// Text Colors
val KumoText = Color(0xFFFFFFFF)
val KumoTextSecondary = Color(0xFFAAAAAA)
val KumoBeta = Color(0xFFFFB020)

@Composable
fun KumoTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentOption: AccentColorOption = AccentColorOption.PURPLE,
    content: @Composable () -> Unit
) {
    val accentColor = Color(accentOption.hexColor)
    val isSystemDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemDark
    }

    val bgColor = if (themeMode == AppThemeMode.AMOLED) KumoAmoled else if (isDark) KumoBlack else Color(0xFFF4F4F8)
    val surfaceColor = if (themeMode == AppThemeMode.AMOLED) Color(0xFF08080C) else if (isDark) KumoSurface else Color(0xFFFFFFFF)
    val cardColor = if (themeMode == AppThemeMode.AMOLED) Color(0xFF101018) else if (isDark) KumoCard else Color(0xFFEBEBF0)
    val textColor = if (isDark) Color.White else Color(0xFF101015)
    val textSecondaryColor = if (isDark) KumoTextSecondary else Color(0xFF666670)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            secondary = accentColor,
            background = bgColor,
            surface = surfaceColor,
            onBackground = textColor,
            onSurface = textColor,
            surfaceVariant = cardColor,
            onSurfaceVariant = textSecondaryColor
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            secondary = accentColor,
            background = bgColor,
            surface = surfaceColor,
            onBackground = textColor,
            onSurface = textColor,
            surfaceVariant = cardColor,
            onSurfaceVariant = textSecondaryColor
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
