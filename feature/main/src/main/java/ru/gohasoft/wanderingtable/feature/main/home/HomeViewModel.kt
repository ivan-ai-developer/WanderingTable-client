package ru.gohasoft.wanderingtable.feature.main.home

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
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
import ru.gohasoft.wanderingtable.core.domain.model.game.Game
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
 * The three reads are combined rather than chained, because none of them depends on the others
 * once the session is known. Each is offline-first, so the screen paints from cache on the first
 * emission and repaints when the refresh lands — the spinner is only for a cold start, and a
 * failed refresh leaves the cached digest on screen instead of replacing it with an error.
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

    /** The offline-first reads never complete, so a retry replaces the collection rather than
     *  stacking a second one on top of it. */
    private var loadJob: Job? = null

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
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val userId = authRepository.getSession().firstSuccessOrErrorData()?.user?.id
            val myGames = userId
                ?.let { gameEventRepository.getUserGames(it) }
                ?: flowOf(Result.Success(emptyList()))

            combine(
                gameRepository.getGames(),
                myGames,
                newsRepository.getNews(),
            ) { games, mine, news -> Digest(games, mine, news) }
                .collect { digest -> render(userId, digest) }
        }
    }

    /**
     * The feed is the screen's reason to exist, so only a feed with nothing to show — no cache and
     * a failed refresh — is an error state; a missing catalogue or schedule just leaves the hero
     * card out.
     */
    private fun render(userId: String?, digest: Digest) {
        val news = digest.news.data
        if (news == null) {
            _state.update { current ->
                current.copy(
                    isLoading = digest.news is Result.Loading,
                    error = (digest.news as? Result.Error)?.error?.toLoadError(),
                )
            }
            return
        }

        val catalogue = digest.games.data.orEmpty().byId()
        val nextGame = digest.myGames.data.orEmpty().toNextGame()
        _state.update { current ->
            current.copy(
                isLoading = false,
                error = null,
                nextGame = nextGame?.toGameEventUi(
                    games = catalogue,
                    currentUserId = userId,
                    joinedEventIds = setOf(nextGame.id),
                ),
                news = news.map { item: NewsItem -> item.toNewsItemUi(currentUserId = userId) },
            )
        }
    }

    private data class Digest(
        val games: Result<List<Game>>,
        val myGames: Result<List<GameEvent>>,
        val news: Result<List<NewsItem>>,
    )

    /** `GET /users/{id}/games` comes back newest-first; Home wants the soonest one still ahead. */
    private fun List<GameEvent>.toNextGame(now: Instant = Instant.now()): GameEvent? = this
        .filter { it.status == GameEventStatus.PLANNED }
        .filter { event -> event.startsAt?.isAfter(now) ?: false }
        .minByOrNull { requireNotNull(it.startsAt) }
}
