package ru.gohasoft.wanderingtable.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.AppEntryScreens
import ru.gohasoft.wanderingtable.core.presentation.navigation.screen.compose.ComposableScreen

/**
 * Resolves the very first screen the app shows, before [ru.gohasoft.wanderingtable.MainActivity]
 * mounts `NavigationHost` — a one-time bootstrap decision, not a navigable screen, so it lives
 * here rather than as a `ComposableScreen`/`Command`.
 */
@HiltViewModel
internal class AppGateViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appEntryScreens: AppEntryScreens,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<ComposableScreen?>(null)
    val startDestination: StateFlow<ComposableScreen?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getSession().collect { result ->
                when (result) {
                    is Result.Success if result.data != null ->
                        _startDestination.value = appEntryScreens.home()
                    is Result.Success, is Result.Error ->
                        _startDestination.value = appEntryScreens.login()
                    is Result.Loading -> Unit
                }
            }
        }
    }
}
