package ru.gohasoft.wanderingtable.feature.main.welcome

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.AppEntryScreens
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.NewRoot
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel

@HiltViewModel
internal class WelcomeViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository,
    private val appEntryScreens: AppEntryScreens,
) : MviViewModel<WelcomeState, WelcomeEvent, Unit>() {

    private val _state = MutableStateFlow(WelcomeState())
    override val state: StateFlow<WelcomeState> = _state.asStateFlow()

    init {
        observeSession()
    }

    override fun onEvent(event: WelcomeEvent) {
        when (event) {
            WelcomeEvent.OnLogoutClick -> logOut()
        }
    }

    /** Defensive belt-and-braces: this screen should only ever be reached with a live session. */
    private fun observeSession() {
        viewModelScope.launch {
            authRepository.getSession().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val session = result.data
                        if (session == null) {
                            router.execute(NewRoot(appEntryScreens.login()))
                        } else {
                            _state.update { it.copy(userEmail = session.user.email) }
                        }
                    }
                    is Result.Error -> router.execute(NewRoot(appEntryScreens.login()))
                    is Result.Loading -> Unit
                }
            }
        }
    }

    private fun logOut() {
        _state.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            authRepository.logOut().collect { result ->
                if (result is Result.Success) {
                    router.execute(NewRoot(appEntryScreens.login()))
                }
            }
        }
    }
}
