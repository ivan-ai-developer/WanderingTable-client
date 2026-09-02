package ru.gohasoft.wanderingtable.feature.main.games

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
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
import ru.gohasoft.wanderingtable.core.domain.model.event.GameEventType
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.feature.main.fake.FakeAuthRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeGameEventRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeGameRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter
import ru.gohasoft.wanderingtable.feature.main.fake.TestGameEvents

class GamesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var router: FakeRouter
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var gameRepository: FakeGameRepository
    private lateinit var gameEventRepository: FakeGameEventRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        router = FakeRouter()
        authRepository = FakeAuthRepository().apply {
            getSessionResult = Result.Success(
                Session(User(id = ME, name = "Me", email = "me@example.com"))
            )
        }
        gameRepository = FakeGameRepository().apply { games = listOf(TestGameEvents.game()) }
        gameEventRepository = FakeGameEventRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tournament events are not shown among club games`() = runTest {
        gameEventRepository.events = listOf(
            TestGameEvents.event(id = "regular"),
            TestGameEvents.event(id = "tournament", type = GameEventType.SINGLE_TOURNAMENT),
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.games.map { it.id }).containsExactly("regular")
    }

    @Test
    fun `a card takes the catalogue name over the free-form event title`() = runTest {
        gameEventRepository.events = listOf(TestGameEvents.event())

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.games.single().title).isEqualTo("Settlers of Catan")
    }

    @Test
    fun `open requests exclude the games you host and the ones you already joined`() = runTest {
        gameEventRepository.events = listOf(
            TestGameEvents.event(id = "mine", creatorId = ME),
            TestGameEvents.event(id = "joined"),
            TestGameEvents.event(id = "open"),
            TestGameEvents.event(id = "full", participantsCount = 2, maxParticipants = 2),
        )
        gameEventRepository.userGames = listOf(TestGameEvents.event(id = "joined"))

        val viewModel = createViewModel()
        viewModel.onEvent(GamesEvent.OnFilterSelected(GamesFilter.OPEN_REQUESTS))

        assertThat(viewModel.state.value.visibleGames.map { it.id }).containsExactly("open")
    }

    @Test
    fun `my games covers both hosting and having joined`() = runTest {
        gameEventRepository.events = listOf(
            TestGameEvents.event(id = "mine", creatorId = ME),
            TestGameEvents.event(id = "joined"),
            TestGameEvents.event(id = "other"),
        )
        gameEventRepository.userGames = listOf(TestGameEvents.event(id = "joined"))

        val viewModel = createViewModel()
        viewModel.onEvent(GamesEvent.OnFilterSelected(GamesFilter.MY_GAMES))

        assertThat(viewModel.state.value.visibleGames.map { it.id })
            .containsExactly("mine", "joined")
    }

    @Test
    fun `joining reloads the list and reports success`() = runTest {
        gameEventRepository.events = listOf(TestGameEvents.event(id = "open"))

        val viewModel = createViewModel()
        viewModel.onEvent(GamesEvent.OnJoinClick("open"))

        assertThat(gameEventRepository.joinCallCount).isEqualTo(1)
        assertThat(router.executedCommands).isNotNull()
        assertThat(viewModel.state.value.joiningEventId).isEqualTo(null)
    }

    @Test
    fun `a 409 on join leaves the screen usable`() = runTest {
        gameEventRepository.events = listOf(TestGameEvents.event(id = "open"))
        gameEventRepository.joinResult = Result.Error(NetworkException.Conflict())

        val viewModel = createViewModel()
        viewModel.onEvent(GamesEvent.OnJoinClick("open"))

        assertThat(viewModel.state.value.joiningEventId).isEqualTo(null)
        assertThat(viewModel.state.value.games.single().canJoin).isTrue()
    }

    @Test
    fun `a failed schedule read surfaces as an error rather than an empty list`() = runTest {
        gameEventRepository.getEventsResult = Result.Error(NetworkException.NoInternet())

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.error).isNotNull()
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    private fun createViewModel() = GamesViewModel(
        router = router,
        authRepository = authRepository,
        gameRepository = gameRepository,
        gameEventRepository = gameEventRepository,
    )

    private companion object {
        const val ME = "me"
    }
}
