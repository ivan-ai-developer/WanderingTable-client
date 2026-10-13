package ru.gohasoft.wanderingtable.core.presentation.navigation.command

import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.NavigationCommandContext
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.context.nav3.Navigation3CommandContext
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig

/**
 * Show a snackbar described by [config]. Suspends until dismissed, so a subsequent
 * command in the same execute(...) batch runs after it. [onAction] fires when the
 * user taps the snackbar's action button.
 */
class ShowSnackbar(
    private val config: SnackbarScreenConfig,
    private val onAction: (suspend (NavigationCommandContext) -> Unit)? = null,
) : Command {
    override suspend fun execute(context: Navigation3CommandContext) {
        withContext(Dispatchers.IO) {
            launch {
                val result = context.showSnackbar(config)
                if (result == SnackbarResult.ActionPerformed) {
                    onAction?.invoke(context)
                }
            }
        }
    }
}