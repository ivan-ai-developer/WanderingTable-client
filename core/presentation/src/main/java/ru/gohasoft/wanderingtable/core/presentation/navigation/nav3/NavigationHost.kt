package ru.gohasoft.wanderingtable.core.presentation.navigation.nav3

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.nav3.Navigation3CommandContext
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.nav3.Navigation3Router

@Composable
fun NavigationHost(
    router: Navigation3Router,
    startScreen: ComposableScreen,
) {
    // Survives process death: NavKey + @Serializable screens make this possible.
    val backStack = rememberNavBackStack(startScreen)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val commandContext = remember(backStack, snackbarHostState, context) {
        Navigation3CommandContext(backStack, snackbarHostState, context)
    }

    // Apply commands sequentially on the UI scope.
    LaunchedEffect(router, commandContext) {
        router.commands.collect { batch ->
            batch.forEach { command -> command.execute(commandContext) }
        }
    }

    // No content insets: screens draw edge to edge and apply their own
    // safeDrawingPadding/imePadding, so backgrounds reach under the system bars.
    // The snackbar therefore has to inset itself.
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.safeDrawingPadding(),
            )
        },
    ) { contentPadding ->
        NavDisplay(
            // Zero today — this Scaffold has no bars and no content insets — but applied rather
            // than dropped so the host still behaves if a bar is ever added here. Window insets
            // are deliberately not part of it; screens apply those themselves.
            modifier = Modifier.padding(contentPadding),
            backStack = backStack,
            onBack = { router.execute(Back()) },
            entryDecorators = listOf(
                // Order matters. Both are REQUIRED for process-death safety:
                // 1) per-entry rememberSaveable persistence,
                // 2) per-entry ViewModelStore (each screen gets its own VM,
                //    cleared when popped).
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = { key ->
                // Screens carry their own UI, so no central route registry is
                // needed: feature modules add screens with zero changes here.
                NavEntry(key) { (key as ComposableScreen).Content() }
            },
        )
    }
}
