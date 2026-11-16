package ru.gohasoft.wanderingtable

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import ru.gohasoft.wanderingtable.core.uikit.R as UiKitR
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import ru.gohasoft.wanderingtable.core.presentation.navigation.nav3.NavigationHost
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.nav3.Navigation3Router
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableLightColorScheme
import ru.gohasoft.wanderingtable.core.uikit.theme.WanderingTableTheme
import ru.gohasoft.wanderingtable.gate.AppGateViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var router: Navigation3Router

    private var hasLocalNetworkAccess by mutableStateOf(false)

    private val requestLocalNetworkAccess = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocalNetworkAccess = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate: it swaps the launch theme for the app theme.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        hasLocalNetworkAccess = canAccessLocalNetwork()
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
                if (hasLocalNetworkAccess) {
                    val gateViewModel = hiltViewModel<AppGateViewModel>()
                    val startDestination by gateViewModel.startDestination.collectAsStateWithLifecycle()
                    val resolvedStartDestination = startDestination
                    if (resolvedStartDestination == null) {
                        LoadingIntro()
                    } else {
                        NavigationHost(router = router, startScreen = resolvedStartDestination)
                    }
                } else {
                    LocalNetworkPermissionContent(onOpenSettings = ::openAppSettings)
                }
            }
        }
        if (!hasLocalNetworkAccess && savedInstanceState == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                requestLocalNetworkAccess.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasLocalNetworkAccess = canAccessLocalNetwork()
    }

    private fun canAccessLocalNetwork(): Boolean =
        Build.VERSION.SDK_INT < 37 ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            ) == PackageManager.PERMISSION_GRANTED

    private fun openAppSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        )
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

    @Composable
    private fun LocalNetworkPermissionContent(onOpenSettings: () -> Unit) {
        Box(
            modifier = Modifier
                .background(color = WanderingTableLightColorScheme.primary)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.local_network_permission_message),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onOpenSettings) {
                    Text(stringResource(R.string.local_network_permission_settings))
                }
            }
        }
    }
}
