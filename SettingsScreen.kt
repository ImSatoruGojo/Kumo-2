package app.kumo.beta.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoCard
import app.kumo.beta.ui.theme.KumoTextSecondary

@Composable
fun SettingsScreen() {
    Column(
import androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(KumoBlack)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
import androidx.compose.ui.Modifier

        SettingsGroup("General") {
            SettingsItem("Theme", "Dark (default)")
            SettingsItem("App language", "System")
            SettingsItem("Cache limit", "512 MB")
        }

import androidx.compose.ui.Modifier
        SettingsGroup("Player") {
            SettingsItem("Default quality", "Auto")
            SettingsItem("Playback speed", "1x")
            SettingsItem("Autoplay next", "Off")
            SettingsItem("Double-tap seek", "10 seconds")
        }

import androidx.compose.ui.Modifier
        SettingsGroup("About") {
            SettingsItem("Version", "0.1.0-beta")
            SettingsItem("Build", "Milestone 1")
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        color = KumoTextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
import androidx.compose.ui.Modifier
    )
    Column(
import androidx.compose.ui.Modifier
            .fillMaxWidth()
            .background(KumoCard, RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        content = content
    )
}

@Composable
private fun SettingsItem(label: String, value: String) {
    Row(
import androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 15.sp)
        Text(text = value, color = KumoTextSecondary, fontSize = 14.sp)
    }
}
