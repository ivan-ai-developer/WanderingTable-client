package ru.gohasoft.wanderingtable.feature.main.createrequest

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
import ru.gohasoft.wanderingtable.feature.main.fake.FakeGameEventRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeGameRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter
import ru.gohasoft.wanderingtable.feature.main.fake.TestGameEvents

class CreateRequestViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var router: FakeRouter
    private lateinit var gameRepository: FakeGameRepository
    private lateinit var gameEventRepository: FakeGameEventRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        router = FakeRouter()
        gameRepository = FakeGameRepository().apply { games = listOf(TestGameEvents.game()) }
        gameEventRepository = FakeGameEventRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitting without a game shows an error and posts nothing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CreateRequestEvent.OnSubmitClick)

        assertThat(gameEventRepository.createdRequests).isEmpty()
        assertThat(viewModel.state.value.formError).isNotNull()
    }

    /**
     * The design's skill and table controls have no server field. This pins that they never leak
     * into the request — the note is the only free text that travels.
     */
    @Test
    fun `skill level and table stay on the device`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(CreateRequestEvent.OnGameSelected("g1"))
        viewModel.onEvent(CreateRequestEvent.OnSkillSelected(SkillLevelUi.EXPERT))
        viewModel.onEvent(CreateRequestEvent.OnTablePicked("Table 4"))
        viewModel.onEvent(CreateRequestEvent.OnNoteChanged("Beginners welcome"))

        viewModel.onEvent(CreateRequestEvent.OnSubmitClick)

        val posted = gameEventRepository.createdRequests.single()
        assertThat(posted.gameId).isEqualTo("g1")
        assertThat(posted.description).isEqualTo("Beginners welcome")
        assertThat(posted.title).isEqualTo("Settlers of Catan")
    }

    @Test
    fun `the seat count posted covers the players needed plus the host`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(CreateRequestEvent.OnGameSelected("g1"))
        viewModel.onEvent(CreateRequestEvent.OnPlayersIncrement)

        viewModel.onEvent(CreateRequestEvent.OnSubmitClick)

        assertThat(gameEventRepository.createdRequests.single().maxParticipants).isEqualTo(3)
    }

    @Test
    fun `a seat count the game cannot hold is rejected before the request goes out`() = runTest {
        gameRepository.games = listOf(TestGameEvents.game(minPlayers = 2, maxPlayers = 2))

        val viewModel = createViewModel()
        viewModel.onEvent(CreateRequestEvent.OnGameSelected("g1"))
        repeat(times = 3) { viewModel.onEvent(CreateRequestEvent.OnPlayersIncrement) }

        viewModel.onEvent(CreateRequestEvent.OnSubmitClick)

        assertThat(gameEventRepository.createdRequests).isEmpty()
        assertThat(viewModel.state.value.formError).isNotNull()
    }

    @Test
    fun `the players stepper never drops below one`() = runTest {
        val viewModel = createViewModel()

        repeat(times = 3) { viewModel.onEvent(CreateRequestEvent.OnPlayersDecrement) }

        assertThat(viewModel.state.value.playersNeeded).isEqualTo(1)
    }

    @Test
    fun `the draft is mirrored into saved state so it survives process death`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.onEvent(CreateRequestEvent.OnGameSelected("g1"))
        viewModel.onEvent(CreateRequestEvent.OnNoteChanged("Bring snacks"))

        val restored = createViewModel(savedStateHandle)

        assertThat(restored.state.value.selectedGameId).isEqualTo("g1")
        assertThat(restored.state.value.note).isEqualTo("Bring snacks")
    }

    @Test
    fun `a posted request clears the draft`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.onEvent(CreateRequestEvent.OnGameSelected("g1"))
        viewModel.onEvent(CreateRequestEvent.OnNoteChanged("Bring snacks"))

        viewModel.onEvent(CreateRequestEvent.OnSubmitClick)

        assertThat(savedStateHandle.get<String>("draft_note")).isNull()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = CreateRequestViewModel(
        savedStateHandle = savedStateHandle,
        router = router,
        gameRepository = gameRepository,
        gameEventRepository = gameEventRepository,
    )
}
