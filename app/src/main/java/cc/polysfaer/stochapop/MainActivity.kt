package cc.polysfaer.stochapop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cc.polysfaer.stochapop.controller.NotificationChannels
import cc.polysfaer.stochapop.ui.StochaPopApp
import cc.polysfaer.stochapop.ui.theme.BackgroundColor
import cc.polysfaer.stochapop.ui.theme.StochaPopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        enableEdgeToEdge()

        // (we should enable dark mode if we want to force it to false)
        // window.isNavigationBarContrastEnforced = false

        NotificationChannels.initialize(this)

        setContent {
            StochaPopTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundColor, //MaterialTheme.colorScheme.background,
                ) {
                    StochaPopApp()
                }
            }
        }
    }
}
