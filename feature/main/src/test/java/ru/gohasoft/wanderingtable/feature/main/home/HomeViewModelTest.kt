package ru.gohasoft.wanderingtable.feature.main.home

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.domain.model.Session
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventStatus
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.feature.main.fake.FakeAuthRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeGameEventRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeGameRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeNewsRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter
import ru.gohasoft.wanderingtable.feature.main.fake.TestGameEvents

class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var router: FakeRouter
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var gameRepository: FakeGameRepository
    private lateinit var gameEventRepository: FakeGameEventRepository
    private lateinit var newsRepository: FakeNewsRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        router = FakeRouter()
        authRepository = FakeAuthRepository().apply {
            getSessionResult = Result.Success(
                Session(User(id = "me", name = "Me", email = "me@example.com"))
            )
        }
        gameRepository = FakeGameRepository().apply { games = listOf(TestGameEvents.game()) }
        gameEventRepository = FakeGameEventRepository()
        newsRepository = FakeNewsRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the hero card shows the soonest upcoming play, not the newest one`() = runTest {
        gameEventRepository.userGames = listOf(
            TestGameEvents.event(id = "later", startsAt = TestGameEvents.NOW.plusSeconds(7200)),
            TestGameEvents.event(id = "sooner", startsAt = TestGameEvents.NOW.plusSeconds(3600)),
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.nextGame?.id).isEqualTo("sooner")
    }

    @Test
    fun `plays already in the past are not offered as the next game`() = runTest {
        gameEventRepository.userGames = listOf(
            TestGameEvents.event(id = "past", startsAt = TestGameEvents.NOW.minusSeconds(3600)),
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.nextGame).isNull()
    }

    @Test
    fun `a cancelled play is not the next game`() = runTest {
        gameEventRepository.userGames = listOf(
            TestGameEvents.event(id = "cancelled", status = GameEventStatus.CANCELLED),
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.nextGame).isNull()
    }

    @Test
    fun `only three posts show until see all is tapped`() = runTest {
        newsRepository.news = List(5) { index -> TestGameEvents.news(id = "n$index") }

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.visibleNews).hasSize(3)

        viewModel.onEvent(HomeEvent.OnSeeAllNewsClick)

        assertThat(viewModel.state.value.visibleNews).hasSize(5)
    }

    /** The schedule is a nice-to-have on Home; only a broken feed turns the screen into an error. */
    @Test
    fun `a failed schedule still renders the news feed`() = runTest {
        newsRepository.news = listOf(TestGameEvents.news())
        gameEventRepository.getUserGamesResult = Result.Error(NetworkException.ServerError())

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.nextGame).isNull()

        assertThat(viewModel.state.value.error).isNull()
        assertThat(viewModel.state.value.news).hasSize(1)
    }

    @Test
    fun `a failed feed is an error state`() = runTest {
        newsRepository.getNewsResult = Result.Error(NetworkException.NoInternet())

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.error).isNotNull()
    }

    private fun createViewModel() = HomeViewModel(
        router = router,
        authRepository = authRepository,
        gameRepository = gameRepository,
        gameEventRepository = gameEventRepository,
        newsRepository = newsRepository,
    )
}
