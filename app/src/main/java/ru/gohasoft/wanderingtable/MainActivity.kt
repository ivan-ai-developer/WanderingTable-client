package ru.gohasoft.wanderingtable

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import ru.gohasoft.wanderingtable.core.uikit.R as UiKitR
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import ru.gohasoft.wanderingtable.core.presentation.navigation.nav3.NavigationHost
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.nav3.Navigation3Router
import ru.gohasoft.wanderingtable.core.uikit.components.background.GradientBackground
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableLightColorScheme
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.gate.AppGateViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var router: Navigation3Router

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate: it swaps the launch theme for the app theme.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Every screen so far sits on the dark purple gradient, so the bar icons are
        // forced light. Move this to per-screen control once light screens land.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT,
            ),
        )
        setContent {
            WanderingTableTheme {
                val gateViewModel = hiltViewModel<AppGateViewModel>()
                val startDestination by gateViewModel.startDestination.collectAsStateWithLifecycle()
                val resolvedStartDestination = startDestination
                if (resolvedStartDestination == null) {
                    LoadingIntro()
                } else {
                    NavigationHost(router = router, startScreen = resolvedStartDestination)
                }
            }
        }
    }

    @Composable
    private fun LoadingIntro() {
        Box(
            modifier = Modifier
                .background(color = WanderingTableLightColorScheme.primary)
                .fillMaxSize()
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(160.dp),
                painter = painterResource(UiKitR.drawable.ic_main_logo),
                tint = Color.White,
                contentDescription = null
            )
        }
    }
}
