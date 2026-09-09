package ru.gohasoft.wanderingtable.feature.main.profile

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.push.PushTokenProvider
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.domain.repository.CacheRepository
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
    private val cacheRepository: CacheRepository,
    private val deviceRepository: DeviceRepository,
    private val pushTokenProvider: PushTokenProvider,
) : MviViewModel<ProfileState, ProfileEvent, Unit>() {

    private val _state = MutableStateFlow(ProfileState())
    override val state: StateFlow<ProfileState> = _state.asStateFlow()

    /** The offline-first read never completes, so a retry replaces the collection rather than
     *  stacking a second one on top of it. */
    private var loadJob: Job? = null

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

    /**
     * The profile is offline-first: the cached copy paints immediately and the refresh replaces it
     * when it lands. Only an empty cache with a failed refresh is an error state — being offline
     * with a stored profile shows the stored one, stale statistics and all.
     */
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            userRepository.getProfile().collect { result ->
                val profile = result.data
                _state.update { current ->
                    when {
                        profile != null -> current.copy(
                            isLoading = false,
                            error = null,
                            profile = profile.toProfileUi(),
                        )

                        else -> current.copy(
                            isLoading = result is Result.Loading,
                            error = (result as? Result.Error)?.error?.toLoadError(),
                            profile = null,
                        )
                    }
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
            // The cached profile, schedule and feed belong to the account signing out; without
            // this the next account on the device opens on the previous one's data.
            loadJob?.cancel()
            cacheRepository.clear().firstSuccessOrErrorResult()
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
