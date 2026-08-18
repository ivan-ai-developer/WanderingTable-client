package ru.gohasoft.wanderingtable.feature.main.welcome

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.AppEntryScreens
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.NewRoot
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.mutableStateIn
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel

@HiltViewModel
internal class WelcomeViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository,
    private val appEntryScreens: AppEntryScreens,
) : MviViewModel<WelcomeState, WelcomeEvent, Unit>() {

    private val _state = authRepository.getSession().map { result ->
        val oldState = state.value
        when (result) {
            is Result.Success -> {
                val session = result.data
                if (session == null) {
                    router.execute(NewRoot(appEntryScreens.login()))
                    oldState
                } else {
                    oldState.copy(userEmail = session.user.email)
                }
            }
            is Result.Error -> {
                router.execute(NewRoot(appEntryScreens.login()))
                oldState
            }
            is Result.Loading ->
                oldState
        }
    }.mutableStateIn(viewModelScope, WelcomeState())
    override val state: StateFlow<WelcomeState> = _state.asStateFlow()

    override fun onEvent(event: WelcomeEvent) {
        when (event) {
            WelcomeEvent.OnLogoutClick -> logOut()
        }
    }

    private fun logOut() {
        _state.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            authRepository.logOut()
                .firstSuccessOrErrorResult()
            router.execute(NewRoot(appEntryScreens.login()))
        }
    }
}
