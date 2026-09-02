package ru.gohasoft.wanderingtable.feature.main.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorData
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationSettings
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository
import ru.gohasoft.wanderingtable.core.domain.repository.NotificationSettingsRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsEvent.OnAddGameClick
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsEvent.OnBackClick
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsEvent.OnClubNewsChanged
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsEvent.OnGameInvitesChanged
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsEvent.OnGamePicked
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsEvent.OnGamePickerDismissed
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsEvent.OnGameRemindersChanged
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsEvent.OnPushEnabledChanged
import ru.gohasoft.wanderingtable.feature.main.settings.NotificationSettingsEvent.OnWatchedGameRemoved

/**
 * Push preferences, kept on the device. The server has no notification settings endpoint and
 * sends whatever it knows about, so these switches decide what this install surfaces.
 *
 * Every change is written straight through — the screen has no save button, matching the design.
 */
@HiltViewModel
internal class NotificationSettingsViewModel @Inject constructor(
    private val router: Router,
    private val settingsRepository: NotificationSettingsRepository,
    private val gameRepository: GameRepository,
) : MviViewModel<NotificationSettingsState, NotificationSettingsEvent, Unit>() {

    private val _state = MutableStateFlow(NotificationSettingsState())
    override val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()

    /** The catalogue, kept so the watched list can show names for the ids that are stored. */
    private var catalogue: List<Game> = emptyList()

    init {
        load()
    }

    override fun onEvent(event: NotificationSettingsEvent) {
        when (event) {
            OnBackClick -> router.execute(Back())
            is OnPushEnabledChanged -> update { it.copy(pushEnabled = event.enabled) }
            is OnGameInvitesChanged -> update { it.copy(gameInvites = event.enabled) }
            is OnClubNewsChanged -> update { it.copy(clubNews = event.enabled) }
            is OnGameRemindersChanged -> update { it.copy(gameReminders = event.enabled) }
            OnAddGameClick -> _state.update { it.copy(isGamePickerVisible = true) }
            OnGamePickerDismissed -> _state.update { it.copy(isGamePickerVisible = false) }
            is OnGamePicked -> {
                _state.update { it.copy(isGamePickerVisible = false) }
                update { it.copy(watchedGameIds = it.watchedGameIds + event.gameId) }
            }

            is OnWatchedGameRemoved ->
                update { it.copy(watchedGameIds = it.watchedGameIds - event.gameId) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            catalogue = gameRepository.getGames().firstSuccessOrErrorData().orEmpty()
            settingsRepository.getSettings().collect { result ->
                val settings = result.data ?: return@collect
                _state.update { current -> current.applySettings(settings) }
            }
        }
    }

    /**
     * Read-modify-write against the stored value rather than the screen state, so a toggle flipped
     * before the first load lands cannot overwrite the rest of the preferences with defaults.
     */
    private fun update(transform: (NotificationSettings) -> NotificationSettings) {
        viewModelScope.launch {
            val stored = settingsRepository.getSettings().firstSuccessOrErrorData()
                ?: NotificationSettings()
            val updated = transform(stored)
            settingsRepository.updateSettings(updated).firstSuccessOrErrorData()
            _state.update { current -> current.applySettings(updated) }
        }
    }

    private fun NotificationSettingsState.applySettings(
        settings: NotificationSettings,
    ): NotificationSettingsState {
        val watched = catalogue.filter { it.id in settings.watchedGameIds }
        return copy(
            isLoading = false,
            pushEnabled = settings.pushEnabled,
            gameInvites = settings.gameInvites,
            clubNews = settings.clubNews,
            gameReminders = settings.gameReminders,
            watchedGames = watched.map { WatchedGameUi(id = it.id, name = it.name) },
            pickableGames = catalogue
                .filterNot { it.id in settings.watchedGameIds }
                .map { WatchedGameUi(id = it.id, name = it.name) },
        )
    }
}
