package ru.gohasoft.wanderingtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import ru.gohasoft.wanderingtable.core.uikit.R as UiKitR
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import ru.gohasoft.wanderingtable.core.presentation.navigation.nav3.NavigationHost
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.nav3.Navigation3Router
import ru.gohasoft.wanderingtable.core.uikit.components.background.GradientBackground
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.gate.AppGateViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var router: Navigation3Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        GradientBackground(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(UiKitR.drawable.ic_main_logo),
                tint = Color.White,
                contentDescription = null
            )
        }
    }
}
