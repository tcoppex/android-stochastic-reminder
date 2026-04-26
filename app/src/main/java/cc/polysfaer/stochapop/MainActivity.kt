package cc.polysfaer.stochapop

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cc.polysfaer.stochapop.ui.StochaPopApp
import cc.polysfaer.stochapop.ui.theme.StochaPopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        enableEdgeToEdge()

        // As we currently force LightMode, we need enable enforced contrast on it.
        val isNightMode = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_YES) != 0
        window.isNavigationBarContrastEnforced = isNightMode

        setContent {
            StochaPopTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    StochaPopApp()
                }
            }
        }
    }
}
