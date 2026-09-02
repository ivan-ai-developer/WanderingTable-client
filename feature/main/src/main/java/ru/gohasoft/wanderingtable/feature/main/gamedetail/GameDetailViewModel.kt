package ru.gohasoft.wanderingtable.feature.main.gamedetail

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorData
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.domain.repository.GameEventRepository
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.gamedetail.GameDetailEvent.OnBackClick
import ru.gohasoft.wanderingtable.feature.main.gamedetail.GameDetailEvent.OnPrimaryActionClick
import ru.gohasoft.wanderingtable.feature.main.gamedetail.GameDetailEvent.OnRetryClick
import ru.gohasoft.wanderingtable.feature.main.mapper.toCancelError
import ru.gohasoft.wanderingtable.feature.main.mapper.toDateTimeLabel
import ru.gohasoft.wanderingtable.feature.main.mapper.toJoinError
import ru.gohasoft.wanderingtable.feature.main.mapper.toLeaveError
import ru.gohasoft.wanderingtable.feature.main.mapper.toLoadError

/**
 * One play in full. Unlike the list, `GET /events/{id}` fills in `participants`, which is what
 * lets this screen tell a member from a stranger and pick the right call to action.
 */
@HiltViewModel(assistedFactory = GameDetailViewModel.Factory::class)
internal class GameDetailViewModel @AssistedInject constructor(
    @Assisted private val screen: GameDetailScreen,
    private val router: Router,
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository,
    private val gameEventRepository: GameEventRepository,
) : MviViewModel<GameDetailState, GameDetailEvent, Unit>() {

    @AssistedFactory
    interface Factory {
        fun create(screen: GameDetailScreen): GameDetailViewModel
    }

    private val _state = MutableStateFlow(GameDetailState())
    override val state: StateFlow<GameDetailState> = _state.asStateFlow()

    init {
        load()
    }

    override fun onEvent(event: GameDetailEvent) {
        when (event) {
            OnBackClick -> router.execute(Back())
            OnPrimaryActionClick -> runPrimaryAction()
            OnRetryClick -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getSession().firstSuccessOrErrorData()?.user?.id
            val result = gameEventRepository.getEvent(screen.eventId).firstSuccessOrErrorResult()
            val event = result?.data
            if (result is Result.Error || event == null) {
                val error = (result as? Result.Error)?.error
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        error = error?.toLoadError() ?: StringResource(R.string.main_error_not_found),
                    )
                }
                return@launch
            }
            val gameName = gameRepository.getGame(event.gameId).firstSuccessOrErrorData()?.name
            _state.value = event.toDetailState(userId = userId, gameName = gameName)
        }
    }

    private fun GameEvent.toDetailState(userId: String?, gameName: String?): GameDetailState {
        val isCreator = creatorId == userId
        val isParticipant = userId != null && participants?.contains(userId) == true
        val isOpen = status == GameEventStatus.PLANNED
        return GameDetailState(
            isLoading = false,
            title = gameName ?: title,
            hostLine = if (isCreator) {
                StringResource(R.string.games_hosted_by_you)
            } else {
                StringResource(R.string.games_hosted_by_member)
            },
            // Ids are all the server gives for other members, so avatars stay anonymous.
            hostInitials = if (isCreator) "ME" else "?",
            skillLabel = StringResource(R.string.games_skill_any),
            dateTimeLabel = (startsAt ?: createdAt).toDateTimeLabel(),
            // The server models no table or venue for a play; the club is the only location.
            locationLabel = StringResource(R.string.game_detail_location_club),
            playersLabel = StringResource(
                R.string.game_detail_players_joined,
                listOf(participantsCount.toString(), maxParticipants.toString()),
            ),
            participantInitials = List(participantsCount) { "?" },
            note = description?.takeIf(String::isNotBlank),
            action = when {
                !isOpen -> GameDetailAction.NONE
                isCreator -> GameDetailAction.CANCEL
                isParticipant -> GameDetailAction.LEAVE
                hasFreeSeats -> GameDetailAction.JOIN
                else -> GameDetailAction.NONE
            },
        )
    }

    private fun runPrimaryAction() {
        val current = _state.value
        if (current.isActionInProgress) return
        val action = current.action
        if (action == GameDetailAction.NONE) return

        _state.update { it.copy(isActionInProgress = true) }
        viewModelScope.launch {
            val result = when (action) {
                GameDetailAction.JOIN -> gameEventRepository.join(screen.eventId)
                GameDetailAction.LEAVE -> gameEventRepository.leave(screen.eventId)
                GameDetailAction.CANCEL -> gameEventRepository.cancel(screen.eventId)
                GameDetailAction.NONE -> return@launch
            }.firstSuccessOrErrorResult()

            _state.update { it.copy(isActionInProgress = false) }
            if (result is Result.Error) {
                showSnackbar(
                    when (action) {
                        GameDetailAction.JOIN -> result.error.toJoinError()
                        GameDetailAction.LEAVE -> result.error.toLeaveError()
                        else -> result.error.toCancelError()
                    }
                )
                return@launch
            }
            showSnackbar(action.successMessage())
            load()
        }
    }

    private fun GameDetailAction.successMessage(): TextResource = when (this) {
        GameDetailAction.JOIN -> StringResource(R.string.games_joined_message)
        GameDetailAction.LEAVE -> StringResource(R.string.game_detail_left_message)
        else -> StringResource(R.string.game_detail_cancelled_message)
    }

    private fun showSnackbar(message: TextResource) {
        router.execute(ShowSnackbar(SnackbarScreenConfig { message(message) }))
    }
}
