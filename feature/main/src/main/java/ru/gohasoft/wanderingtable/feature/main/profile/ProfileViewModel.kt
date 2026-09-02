package ru.gohasoft.wanderingtable.feature.main.profile

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.push.PushTokenProvider
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.domain.repository.DeviceRepository
import ru.gohasoft.wanderingtable.core.domain.repository.UserRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.AppEntryScreens
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.NewRoot
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.mapper.toLoadError
import ru.gohasoft.wanderingtable.feature.main.mapper.toProfileUi
import ru.gohasoft.wanderingtable.feature.main.profile.ProfileEvent.OnAccountSettingsClick
import ru.gohasoft.wanderingtable.feature.main.profile.ProfileEvent.OnClubAdminClick
import ru.gohasoft.wanderingtable.feature.main.profile.ProfileEvent.OnClubMembershipClick
import ru.gohasoft.wanderingtable.feature.main.profile.ProfileEvent.OnLogOutClick
import ru.gohasoft.wanderingtable.feature.main.profile.ProfileEvent.OnNotificationSettingsClick
import ru.gohasoft.wanderingtable.feature.main.profile.ProfileEvent.OnRetryClick
import ru.gohasoft.wanderingtable.feature.main.clubadmin.ClubAdminScreen
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsScreen

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val router: Router,
    private val appEntryScreens: AppEntryScreens,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val pushTokenProvider: PushTokenProvider,
) : MviViewModel<ProfileState, ProfileEvent, Unit>() {

    private val _state = MutableStateFlow(ProfileState())
    override val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        load()
    }

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            OnNotificationSettingsClick -> router.execute(Forward(NotificationSettingsScreen))
            OnClubAdminClick -> router.execute(Forward(ClubAdminScreen))
            OnAccountSettingsClick, OnClubMembershipClick -> showNotAvailableYet()
            OnLogOutClick -> logOut()
            OnRetryClick -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = userRepository.getProfile().firstSuccessOrErrorResult()
            _state.update { current ->
                when (result) {
                    is Result.Error -> current.copy(
                        isLoading = false,
                        error = result.error.toLoadError(),
                    )

                    else -> current.copy(
                        isLoading = false,
                        error = null,
                        profile = result?.data?.toProfileUi(),
                    )
                }
            }
        }
    }

    /**
     * Unbinding the push token first, so a signed-out phone stops receiving this account's
     * notifications. It is best effort: a failed unbind must not keep the user signed in, and the
     * session is cleared locally whatever the server answers.
     */
    private fun logOut() {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            pushTokenProvider.currentToken()?.let { token ->
                deviceRepository.unregisterPushToken(token).firstSuccessOrErrorResult()
            }
            authRepository.logOut().firstSuccessOrErrorResult()
            router.execute(NewRoot(appEntryScreens.login()))
        }
    }

    private fun showNotAvailableYet() {
        router.execute(
            ShowSnackbar(
                SnackbarScreenConfig { message(StringResource(R.string.profile_not_available_yet)) }
            )
        )
    }
}
