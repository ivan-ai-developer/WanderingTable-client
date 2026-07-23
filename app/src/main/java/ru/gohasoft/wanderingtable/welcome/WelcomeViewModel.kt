package ru.gohasoft.wanderingtable.welcome

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.gohasoft.wanderingtable.R
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel

@HiltViewModel
internal class WelcomeViewModel @Inject constructor(
    private val router: Router,
) : MviViewModel<WelcomeState, WelcomeEvent, Unit>() {

    override val state: StateFlow<WelcomeState> = MutableStateFlow(WelcomeState()).asStateFlow()

    override fun onEvent(event: WelcomeEvent) {
        when (event) {
            WelcomeEvent.OnShowSnackbarClick -> showTestSnackbar()
        }
    }

    private fun showTestSnackbar() {
        router.execute(
            ShowSnackbar(
                SnackbarScreenConfig {
                    message(TextResource.StringResource(R.string.welcome_snackbar_message))
                    withDismissAction(true)
                }
            )
        )
    }
}
