package ru.gohasoft.wanderingtable.feature.main.creategame

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.isEmpty
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
import ru.gohasoft.wanderingtable.core.domain.model.game.GameResultType
import ru.gohasoft.wanderingtable.feature.main.fake.FakeGameRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter

class CreateGameViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var router: FakeRouter
    private lateinit var gameRepository: FakeGameRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        router = FakeRouter()
        gameRepository = FakeGameRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitting without a name shows an error and posts nothing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CreateGameEvent.OnSubmitClick)

        assertThat(gameRepository.createdGames).isEmpty()
        assertThat(viewModel.state.value.formError).isNotNull()
    }

    @Test
    fun `a blank description is sent as null rather than an empty string`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(CreateGameEvent.OnNameChanged("  Wingspan  "))
        viewModel.onEvent(CreateGameEvent.OnDescriptionChanged("   "))

        viewModel.onEvent(CreateGameEvent.OnSubmitClick)

        val posted = gameRepository.createdGames.single()
        assertThat(posted.name).isEqualTo("Wingspan")
        assertThat(posted.description).isNull()
    }

    @Test
    fun `the chosen result type reaches the request`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(CreateGameEvent.OnNameChanged("Chess"))
        viewModel.onEvent(CreateGameEvent.OnResultTypeSelected(GameResultType.WIN_LOSS))

        viewModel.onEvent(CreateGameEvent.OnSubmitClick)

        assertThat(gameRepository.createdGames.single().resultType)
            .isEqualTo(GameResultType.WIN_LOSS)
    }

    /** The server rejects `maxPlayers < minPlayers` with a 400; the steppers make it unreachable. */
    @Test
    fun `raising the floor above the ceiling pushes the ceiling up`() = runTest {
        val viewModel = createViewModel()

        repeat(times = 4) { viewModel.onEvent(CreateGameEvent.OnMinPlayersIncrement) }

        assertThat(viewModel.state.value.minPlayers).isEqualTo(6)
        assertThat(viewModel.state.value.maxPlayers).isEqualTo(6)
    }

    @Test
    fun `lowering the ceiling below the floor pulls the floor down`() = runTest {
        val viewModel = createViewModel()

        repeat(times = 3) { viewModel.onEvent(CreateGameEvent.OnMaxPlayersDecrement) }

        assertThat(viewModel.state.value.maxPlayers).isEqualTo(1)
        assertThat(viewModel.state.value.minPlayers).isEqualTo(1)
    }

    @Test
    fun `the player counts never leave their limits`() = runTest {
        val viewModel = createViewModel()

        repeat(times = 20) { viewModel.onEvent(CreateGameEvent.OnMaxPlayersIncrement) }
        repeat(times = 20) { viewModel.onEvent(CreateGameEvent.OnMinPlayersDecrement) }

        assertThat(viewModel.state.value.maxPlayers)
            .isEqualTo(CreateGameState.MAX_PLAYERS_LIMIT)
        assertThat(viewModel.state.value.minPlayers)
            .isEqualTo(CreateGameState.MIN_PLAYERS_LIMIT)
    }

    @Test
    fun `a duplicate name surfaces as a form error, not a crash`() = runTest {
        gameRepository.createGameResult = Result.Error(NetworkException.Conflict())

        val viewModel = createViewModel()
        viewModel.onEvent(CreateGameEvent.OnNameChanged("Chess"))
        viewModel.onEvent(CreateGameEvent.OnSubmitClick)

        assertThat(viewModel.state.value.formError).isNotNull()
        assertThat(viewModel.state.value.isSubmitting).isEqualTo(false)
    }

    @Test
    fun `the draft survives process death`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.onEvent(CreateGameEvent.OnNameChanged("Ark Nova"))
        viewModel.onEvent(CreateGameEvent.OnResultTypeSelected(GameResultType.PLACEMENT))

        val restored = createViewModel(savedStateHandle)

        assertThat(restored.state.value.name).isEqualTo("Ark Nova")
        assertThat(restored.state.value.resultType).isEqualTo(GameResultType.PLACEMENT)
    }

    @Test
    fun `a successful submit clears the draft`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.onEvent(CreateGameEvent.OnNameChanged("Ark Nova"))

        viewModel.onEvent(CreateGameEvent.OnSubmitClick)

        assertThat(savedStateHandle.get<String>("draft_name")).isNull()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = CreateGameViewModel(
        savedStateHandle = savedStateHandle,
        router = router,
        gameRepository = gameRepository,
    )
}
