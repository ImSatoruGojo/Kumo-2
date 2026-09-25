package app.kumo.beta

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.kumo.beta.data.local.SettingsPreferencesStore
import app.kumo.beta.data.local.AppThemeMode
import app.kumo.beta.data.local.AccentColorOption
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.kumo.beta.ui.navigation.KumoNavGraph
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsStore = remember { SettingsPreferencesStore(this@MainActivity) }
            var settings by remember { mutableStateOf(settingsStore.get()) }
            DisposableEffect(settingsStore) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                    settings = settingsStore.get()
                }
                settingsStore.registerListener(listener)
                onDispose { settingsStore.unregisterListener(listener) }
            }
            val themeMode = when {
                settings.amoledMode -> AppThemeMode.AMOLED
                settings.theme == "Light" -> AppThemeMode.LIGHT
                settings.theme == "System" -> AppThemeMode.SYSTEM
                else -> AppThemeMode.DARK
            }
            val accent = when (settings.accentColor) {
                "Orange" -> AccentColorOption.ORANGE
                "Purple" -> AccentColorOption.PURPLE
                "Blue" -> AccentColorOption.BLUE
                "Green" -> AccentColorOption.GREEN
                else -> AccentColorOption.WHITE
            }
            KumoTheme(themeMode = themeMode, accentOption = accent) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KumoBlack
                ) {
                    KumoNavGraph()
                }
            }
        }
    }
}
