package ru.gohasoft.wanderingtable.feature.main.settings

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.gohasoft.wanderingtable.feature.main.fake.FakeGameRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeNotificationSettingsRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter
import ru.gohasoft.wanderingtable.feature.main.fake.TestGameEvents

class NotificationSettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var router: FakeRouter
    private lateinit var settingsRepository: FakeNotificationSettingsRepository
    private lateinit var gameRepository: FakeGameRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        router = FakeRouter()
        settingsRepository = FakeNotificationSettingsRepository()
        gameRepository = FakeGameRepository().apply {
            games = listOf(
                TestGameEvents.game(id = "g1", name = "Settlers of Catan"),
                TestGameEvents.game(id = "g2", name = "Chess"),
            )
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a toggle is written through immediately`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(NotificationSettingsEvent.OnClubNewsChanged(false))

        assertThat(settingsRepository.settings.value.clubNews).isFalse()
        assertThat(viewModel.state.value.clubNews).isFalse()
    }

    /** Each switch writes the whole record, so one must not reset the others to their defaults. */
    @Test
    fun `changing one preference leaves the rest alone`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(NotificationSettingsEvent.OnClubNewsChanged(false))
        viewModel.onEvent(NotificationSettingsEvent.OnGameRemindersChanged(false))

        assertThat(settingsRepository.settings.value.clubNews).isFalse()
        assertThat(settingsRepository.settings.value.gameReminders).isFalse()
        assertThat(settingsRepository.settings.value.gameInvites).isTrue()
    }

    @Test
    fun `a watched game moves out of the picker and into the list`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(NotificationSettingsEvent.OnGamePicked("g1"))

        assertThat(viewModel.state.value.watchedGames.map { it.name })
            .containsExactly("Settlers of Catan")
        assertThat(viewModel.state.value.pickableGames.map { it.name }).containsExactly("Chess")
        assertThat(viewModel.state.value.isGamePickerVisible).isFalse()
    }

    @Test
    fun `removing a watched game puts it back on offer`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(NotificationSettingsEvent.OnGamePicked("g1"))

        viewModel.onEvent(NotificationSettingsEvent.OnWatchedGameRemoved("g1"))

        assertThat(viewModel.state.value.watchedGames).isEmpty()
        assertThat(settingsRepository.settings.value.watchedGameIds).isEmpty()
    }

    private fun createViewModel() = NotificationSettingsViewModel(
        router = router,
        settingsRepository = settingsRepository,
        gameRepository = gameRepository,
    )
}
