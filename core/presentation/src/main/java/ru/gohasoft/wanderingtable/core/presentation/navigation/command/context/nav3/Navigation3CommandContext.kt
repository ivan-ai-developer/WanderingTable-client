package ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.nav3

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.NavigationCommandContext
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig

class Navigation3CommandContext(
    val backStack: NavBackStack<NavKey>,
    private val snackbarHostState: SnackbarHostState,
    private val context: Context
) : NavigationCommandContext {

    suspend fun showSnackbar(config: SnackbarScreenConfig): SnackbarResult =
        snackbarHostState.showSnackbar(
            message = config.message.getText(context),
            actionLabel = config.actionButton?.text?.getText(context),
            withDismissAction = config.withDismissAction,
            duration = config.duration.toSnackbarDuration(),
        )

    private fun SnackbarScreenConfig.Duration.toSnackbarDuration(): SnackbarDuration = when (this) {
        SnackbarScreenConfig.Duration.SHORT -> SnackbarDuration.Short
        SnackbarScreenConfig.Duration.LONG -> SnackbarDuration.Long
        SnackbarScreenConfig.Duration.INDEFINITE -> SnackbarDuration.Indefinite
    }
}