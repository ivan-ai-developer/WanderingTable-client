package ru.gohasoft.wanderingtable.feature.main.gamedetail

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
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
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter
import ru.gohasoft.wanderingtable.feature.main.fake.TestGameEvents

class GameDetailViewModelTest {

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
    fun `the creator is offered cancel, never leave`() = runTest {
        gameEventRepository.getEventResult = Result.Success(
            TestGameEvents.event(creatorId = ME, participants = listOf(ME))
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.action).isEqualTo(GameDetailAction.CANCEL)
    }

    @Test
    fun `a participant who is not the creator is offered leave`() = runTest {
        gameEventRepository.getEventResult = Result.Success(
            TestGameEvents.event(participants = listOf("host", ME), participantsCount = 2)
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.action).isEqualTo(GameDetailAction.LEAVE)
    }

    @Test
    fun `an outsider is offered join while seats remain`() = runTest {
        gameEventRepository.getEventResult = Result.Success(
            TestGameEvents.event(participants = listOf("host"))
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.action).isEqualTo(GameDetailAction.JOIN)
    }

    @Test
    fun `a full game offers nothing`() = runTest {
        gameEventRepository.getEventResult = Result.Success(
            TestGameEvents.event(
                participants = listOf("host", "other"),
                participantsCount = 2,
                maxParticipants = 2,
            )
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.action).isEqualTo(GameDetailAction.NONE)
    }

    @Test
    fun `a finished game offers nothing even to its creator`() = runTest {
        gameEventRepository.getEventResult = Result.Success(
            TestGameEvents.event(
                creatorId = ME,
                status = GameEventStatus.FINISHED,
                participants = listOf(ME),
            )
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.action).isEqualTo(GameDetailAction.NONE)
    }

    @Test
    fun `a 409 on join clears the in-flight flag and keeps the screen loaded`() = runTest {
        gameEventRepository.getEventResult = Result.Success(
            TestGameEvents.event(participants = listOf("host"))
        )
        gameEventRepository.joinResult = Result.Error(NetworkException.Conflict())

        val viewModel = createViewModel()
        viewModel.onEvent(GameDetailEvent.OnPrimaryActionClick)

        assertThat(gameEventRepository.joinCallCount).isEqualTo(1)
        assertThat(viewModel.state.value.isActionInProgress).isEqualTo(false)
        assertThat(viewModel.state.value.title).isEqualTo("Settlers of Catan")
    }

    @Test
    fun `leaving calls leave, not cancel`() = runTest {
        gameEventRepository.getEventResult = Result.Success(
            TestGameEvents.event(participants = listOf("host", ME), participantsCount = 2)
        )

        val viewModel = createViewModel()
        viewModel.onEvent(GameDetailEvent.OnPrimaryActionClick)

        assertThat(gameEventRepository.leaveCallCount).isEqualTo(1)
        assertThat(gameEventRepository.cancelCallCount).isEqualTo(0)
    }

    @Test
    fun `a missing event shows an error instead of an empty screen`() = runTest {
        gameEventRepository.getEventResult = Result.Error(NetworkException.NotFound())

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.error).isNotNull()
        assertThat(viewModel.state.value.isLoaded).isEqualTo(false)
    }

    private fun createViewModel() = GameDetailViewModel(
        screen = GameDetailScreen(eventId = "e1"),
        router = router,
        authRepository = authRepository,
        gameRepository = gameRepository,
        gameEventRepository = gameEventRepository,
    )

    private companion object {
        const val ME = "me"
    }
}
