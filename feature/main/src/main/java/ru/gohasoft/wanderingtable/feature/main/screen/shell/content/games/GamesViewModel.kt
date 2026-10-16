package ru.gohasoft.wanderingtable.feature.main.screen.shell.content.games

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorData
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEvent
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventType
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.domain.repository.GameEventRepository
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.R
import ru.gohasoft.wanderingtable.feature.main.screen.gamedetail.GameDetailScreen
import ru.gohasoft.wanderingtable.feature.main.screen.shell.content.games.GamesEvent.OnFilterSelected
import ru.gohasoft.wanderingtable.feature.main.screen.shell.content.games.GamesEvent.OnGameClick
import ru.gohasoft.wanderingtable.feature.main.screen.shell.content.games.GamesEvent.OnJoinClick
import ru.gohasoft.wanderingtable.feature.main.screen.shell.content.games.GamesEvent.OnRetryClick
import ru.gohasoft.wanderingtable.feature.main.mapper.byId
import ru.gohasoft.wanderingtable.feature.main.mapper.toGameEventUi
import ru.gohasoft.wanderingtable.feature.main.mapper.toJoinError
import ru.gohasoft.wanderingtable.feature.main.mapper.toLoadError

/**
 * The club schedule, narrowed to ordinary plays: tournaments have their own lifecycle and no
 * screens in this design, so they are filtered out rather than shown as un-joinable cards.
 *
 * Membership cannot be read from the schedule — list responses omit `participants` — so the plays
 * the user is in are fetched separately and matched by id.
 *
 * All three reads are offline-first, so the list paints from cache on the first emission and
 * repaints when the refresh lands; the spinner is only for a cold start, and a failed refresh
 * leaves the cached schedule on screen rather than replacing it with an error.
 */
@HiltViewModel
internal class GamesViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository,
    private val gameEventRepository: GameEventRepository,
) : MviViewModel<GamesState, GamesEvent, Unit>() {

    private val _state = MutableStateFlow(GamesState())
    override val state: StateFlow<GamesState> = _state.asStateFlow()

    /** The offline-first reads never complete, so a retry replaces the collection rather than
     *  stacking a second one on top of it. */
    private var loadJob: Job? = null

    init {
        load()
    }

    override fun onEvent(event: GamesEvent) {
        when (event) {
            is OnFilterSelected -> _state.update { it.copy(filter = event.filter) }
            is OnGameClick -> router.execute(Forward(GameDetailScreen(eventId = event.eventId)))
            is OnJoinClick -> join(event.eventId)
            OnRetryClick -> load()
        }
    }

    /** Called by the tab when another screen asked for Games to open on a particular chip. */
    fun applyFilter(filter: GamesFilter) {
        _state.update { it.copy(filter = filter) }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val userId = authRepository.getSession().firstSuccessOrErrorData()?.user?.id
            val myGames = userId
                ?.let { gameEventRepository.getUserGames(it) }
                ?: flowOf(Result.Success(emptyList()))

            combine(
                gameRepository.getGames(),
                gameEventRepository.getEvents(GameEventStatus.PLANNED),
                myGames,
            ) { games, events, mine -> Schedule(games, events, mine) }
                .collect { schedule -> render(userId, schedule) }
        }
    }

    /**
     * Only a schedule with nothing to show — no cache and a failed refresh — is an error state;
     * a missing catalogue just leaves the cards without their game names.
     */
    private fun render(userId: String?, schedule: Schedule) {
        val events = schedule.events.data
        if (events == null) {
            _state.update { current ->
                current.copy(
                    isLoading = schedule.events is Result.Loading,
                    error = (schedule.events as? Result.Error)?.error?.toLoadError(),
                )
            }
            return
        }

        val catalogue = schedule.games.data.orEmpty().byId()
        val joinedEventIds = schedule.myGames.data.orEmpty().map(GameEvent::id).toSet()
        _state.update { current ->
            current.copy(
                isLoading = false,
                error = null,
                games = events
                    .filter { it.type == GameEventType.REGULAR_GAME }
                    .sortedBy { it.startsAt ?: it.createdAt }
                    .map { event ->
                        event.toGameEventUi(
                            games = catalogue,
                            currentUserId = userId,
                            joinedEventIds = joinedEventIds,
                        )
                    },
            )
        }
    }

    private data class Schedule(
        val games: Result<List<Game>>,
        val events: Result<List<GameEvent>>,
        val myGames: Result<List<GameEvent>>,
    )

    /**
     * The server rejects a second join with 409, so a double tap cannot create two seats; the
     * in-flight id only stops the card from inviting the second tap in the first place.
     */
    private fun join(eventId: String) {
        if (_state.value.joiningEventId != null) return
        _state.update { it.copy(joiningEventId = eventId) }
        viewModelScope.launch {
            val result = gameEventRepository.join(eventId).firstSuccessOrErrorResult()
            _state.update { it.copy(joiningEventId = null) }
            when (result) {
                is Result.Error -> showSnackbar(result.error.toJoinError())
                else -> {
                    showSnackbar(StringResource(R.string.games_joined_message))
                    load()
                }
            }
        }
    }

    private fun showSnackbar(message: TextResource) {
        router.execute(ShowSnackbar(SnackbarScreenConfig { message(message) }))
    }
}
