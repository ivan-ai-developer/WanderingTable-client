package ru.gohasoft.wanderingtable.feature.main.home

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
import ru.gohasoft.wanderingtable.core.domain.model.news.NewsItem
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.domain.repository.GameEventRepository
import ru.gohasoft.wanderingtable.core.domain.repository.GameRepository
import ru.gohasoft.wanderingtable.core.domain.repository.NewsRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.main.gamedetail.GameDetailScreen
import ru.gohasoft.wanderingtable.feature.main.home.HomeEvent.OnNewsClick
import ru.gohasoft.wanderingtable.feature.main.home.HomeEvent.OnNextGameClick
import ru.gohasoft.wanderingtable.feature.main.home.HomeEvent.OnRetryClick
import ru.gohasoft.wanderingtable.feature.main.home.HomeEvent.OnSeeAllNewsClick
import ru.gohasoft.wanderingtable.feature.main.mapper.byId
import ru.gohasoft.wanderingtable.feature.main.mapper.toGameEventUi
import ru.gohasoft.wanderingtable.feature.main.mapper.toLoadError
import ru.gohasoft.wanderingtable.feature.main.mapper.toNewsItemUi
import ru.gohasoft.wanderingtable.feature.main.newsdetail.NewsDetailScreen

/**
 * Home is a read-only digest: the soonest play the user is in, and the club news feed.
 *
 * The three reads are issued together rather than chained, because none of them depends on the
 * others once the session is known.
 */
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository,
    private val gameEventRepository: GameEventRepository,
    private val newsRepository: NewsRepository,
) : MviViewModel<HomeState, HomeEvent, Unit>() {

    private val _state = MutableStateFlow(HomeState())
    override val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        load()
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            OnNextGameClick -> navigateToNextGame()
            is OnNewsClick -> router.execute(Forward(NewsDetailScreen(newsId = event.newsId)))
            OnSeeAllNewsClick -> _state.update { it.copy(showAllNews = true) }
            OnRetryClick -> load()
        }
    }

    private fun navigateToNextGame() {
        val eventId = _state.value.nextGame?.id ?: return
        router.execute(Forward(GameDetailScreen(eventId = eventId)))
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val userId = authRepository.getSession().firstSuccessOrErrorData()?.user?.id
            val (games, myGames, news) = coroutineScope {
                val gamesTask = async { gameRepository.getGames().firstSuccessOrErrorResult() }
                val myGamesTask = async {
                    userId?.let { gameEventRepository.getUserGames(it).firstSuccessOrErrorResult() }
                }
                val newsTask = async { newsRepository.getNews().firstSuccessOrErrorResult() }
                Triple(gamesTask.await(), myGamesTask.await(), newsTask.await())
            }

            // The feed is the screen's reason to exist, so only a failed feed is an error state;
            // a missing catalogue or schedule just leaves the hero card out.
            if (news is Result.Error) {
                _state.update { it.copy(isLoading = false, error = news.error.toLoadError()) }
                return@launch
            }

            val catalogue = games?.data.orEmpty().byId()
            val nextGame = myGames?.data.orEmpty().toNextGame()
            _state.update { current ->
                current.copy(
                    isLoading = false,
                    error = null,
                    nextGame = nextGame?.toGameEventUi(
                        games = catalogue,
                        currentUserId = userId,
                        joinedEventIds = setOf(nextGame.id),
                    ),
                    news = news?.data.orEmpty().map { item: NewsItem ->
                        item.toNewsItemUi(currentUserId = userId)
                    },
                )
            }
        }
    }

    /** `GET /users/{id}/games` comes back newest-first; Home wants the soonest one still ahead. */
    private fun List<GameEvent>.toNextGame(now: Instant = Instant.now()): GameEvent? = this
        .filter { it.status == GameEventStatus.PLANNED }
        .filter { event -> event.startsAt?.isAfter(now) ?: false }
        .minByOrNull { requireNotNull(it.startsAt) }
}
