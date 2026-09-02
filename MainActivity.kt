package app.kumo.beta

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
}
