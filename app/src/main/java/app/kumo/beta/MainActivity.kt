package app.kumo.beta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.kumo.beta.ui.navigation.KumoNavGraph
import androidx.compose.runtime.*
import app.kumo.beta.data.local.PreferencesManager
import app.kumo.beta.ui.theme.KumoBlack
import app.kumo.beta.ui.theme.KumoTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        applyLocalePreference()

        setContent {
            KumoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KumoBlack
                ) {
                    KumoNavGraph()
                }
            }
        }
    }

    private fun applyLocalePreference() {
        try {
            val prefs = PreferencesManager(this)
            val langCode = prefs.appLanguage
            val locale = if (langCode.contains("-")) {
                val parts = langCode.split("-")
                Locale(parts[0], parts[1])
            } else {
                Locale(langCode)
            }
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
