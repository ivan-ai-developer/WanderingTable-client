package ru.gohasoft.wanderingtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.nav3.Navigation3Router
import ru.gohasoft.wanderingtable.core.presentation.navigation.nav3.NavigationHost
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.welcome.WelcomeScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var router: Navigation3Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WanderingTableTheme {
                NavigationHost(router = router, startScreen = WelcomeScreen())
            }
        }
    }
}
