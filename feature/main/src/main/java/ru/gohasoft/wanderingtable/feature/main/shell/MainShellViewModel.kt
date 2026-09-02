package ru.gohasoft.wanderingtable.feature.main.shell

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorData
import ru.gohasoft.wanderingtable.core.domain.model.notification.Notification
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.domain.push.PushTokenProvider
import ru.gohasoft.wanderingtable.core.domain.repository.DeviceRepository
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationRepository
import ru.gohasoft.wanderingtable.core.domain.repository.UserRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.creategame.CreateGameScreen
import ru.gohasoft.wanderingtable.feature.main.createnews.CreateNewsScreen
import ru.gohasoft.wanderingtable.feature.main.createrequest.CreateRequestScreen
import ru.gohasoft.wanderingtable.feature.main.games.GamesFilter
import ru.gohasoft.wanderingtable.feature.main.notifications.NotificationsScreen
import ru.gohasoft.wanderingtable.feature.main.shell.MainShellEvent.OnBellClick
import ru.gohasoft.wanderingtable.feature.main.shell.MainShellEvent.OnCreateGameClick
import ru.gohasoft.wanderingtable.feature.main.shell.MainShellEvent.OnCreateSheetDismissed
import ru.gohasoft.wanderingtable.feature.main.shell.MainShellEvent.OnFindOpponentClick
import ru.gohasoft.wanderingtable.feature.main.shell.MainShellEvent.OnGamesFilterConsumed
import ru.gohasoft.wanderingtable.feature.main.shell.MainShellEvent.OnNavItemSelected
import ru.gohasoft.wanderingtable.feature.main.shell.MainShellEvent.OnOpenGames
import ru.gohasoft.wanderingtable.feature.main.shell.MainShellEvent.OnPostClubNewsClick

/**
 * Owns the bottom bar: which tab is showing, whether the Create sheet is open, and the two
 * signals the chrome needs — the unread dot and whether this account may post news.
 *
 * The tabs themselves are composables inside this one screen, so switching between them never
 * touches the back stack; only the detail screens this ViewModel pushes do.
 */
@HiltViewModel
internal class MainShellViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val router: Router,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val deviceRepository: DeviceRepository,
    private val pushTokenProvider: PushTokenProvider,
) : MviViewModel<MainShellState, MainShellEvent, Unit>() {

    private val _state = MutableStateFlow(
        MainShellState(tab = savedStateHandle.get<String>(KEY_TAB)?.let(::tabOrHome) ?: MainTab.HOME)
    )
    override val state: StateFlow<MainShellState> = _state.asStateFlow()

    init {
        refreshRoles()
        observeUnreadNotifications()
        registerPushToken()
    }

    override fun onEvent(event: MainShellEvent) {
        when (event) {
            is OnNavItemSelected -> selectNavItem(event.navIndex)
            OnCreateSheetDismissed -> setCreateSheetVisible(false)
            OnPostClubNewsClick -> navigateToCreateNews()
            OnCreateGameClick -> navigateToCreateGame()
            OnFindOpponentClick -> navigateToCreateRequest()
            OnBellClick -> navigateToNotifications()
            is OnOpenGames -> openGames(event.filter)
            OnGamesFilterConsumed -> _state.update { it.copy(pendingGamesFilter = null) }
        }
    }

    private fun selectNavItem(navIndex: Int) {
        if (navIndex == MainTab.CREATE_NAV_INDEX) {
            // Re-read first: a manager can grant themselves GAME_CREATOR from Club Administration
            // and come straight back here, and the sheet must not still be hiding the option.
            refreshRoles()
            setCreateSheetVisible(true)
            return
        }
        val tab = MainTab.fromNavIndex(navIndex) ?: return
        savedStateHandle[KEY_TAB] = tab.name
        _state.update { it.copy(tab = tab) }
    }

    private fun openGames(filter: GamesFilter) {
        savedStateHandle[KEY_TAB] = MainTab.GAMES.name
        _state.update { it.copy(tab = MainTab.GAMES, pendingGamesFilter = filter) }
    }

    private fun setCreateSheetVisible(visible: Boolean) {
        _state.update { it.copy(isCreateSheetVisible = visible) }
    }

    private fun navigateToCreateNews() {
        setCreateSheetVisible(false)
        router.execute(Forward(CreateNewsScreen))
    }

    private fun navigateToCreateGame() {
        setCreateSheetVisible(false)
        router.execute(Forward(CreateGameScreen))
    }

    private fun navigateToCreateRequest() {
        setCreateSheetVisible(false)
        router.execute(Forward(CreateRequestScreen))
    }

    private fun navigateToNotifications() {
        router.execute(Forward(NotificationsScreen))
    }

    /**
     * Roles are re-read rather than cached: the server drops a revoked role immediately, so the
     * sheet must stop offering an option without waiting for a new token — and must start
     * offering one the moment it is granted.
     *
     * Each flag tests the exact role its endpoint demands. `CLUB_MANAGER` is deliberately not
     * treated as a superset — it may manage any event and delete any news, but creating news or
     * games still needs the specific role, which a manager grants themselves in Club
     * Administration.
     */
    private fun refreshRoles() {
        viewModelScope.launch {
            userRepository.getProfile().collect { result ->
                val roles = (result as? Result.Success)?.data?.user?.roles ?: return@collect
                _state.update {
                    it.copy(
                        canPostNews = Role.NEWS_CREATOR in roles,
                        canCreateGames = Role.GAME_CREATOR in roles,
                    )
                }
            }
        }
    }

    private fun observeUnreadNotifications() {
        viewModelScope.launch {
            notificationRepository.getNotifications().collect { result ->
                val notifications = result.data ?: return@collect
                val hasUnread = notifications.any { notification: Notification -> !notification.isRead }
                _state.update { it.copy(hasUnreadNotifications = hasUnread) }
            }
        }
    }

    /**
     * The shell only exists once a session does, which makes it the natural place to bind this
     * device to the account. Registration is idempotent server-side, so running it on every
     * launch costs one request and covers the case where a rotated token was never delivered.
     * Failure is silent: push is a nice-to-have, not a precondition for using the app.
     */
    private fun registerPushToken() {
        viewModelScope.launch {
            val token = pushTokenProvider.currentToken() ?: return@launch
            deviceRepository.registerPushToken(token).firstSuccessOrErrorData()
        }
    }

    private fun tabOrHome(name: String): MainTab =
        MainTab.entries.firstOrNull { it.name == name } ?: MainTab.HOME

    private companion object {
        const val KEY_TAB = "selected_tab"
    }
}
