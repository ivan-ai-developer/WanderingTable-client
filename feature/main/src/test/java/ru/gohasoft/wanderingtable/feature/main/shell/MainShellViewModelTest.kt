package ru.gohasoft.wanderingtable.feature.main.shell

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.gohasoft.wanderingtable.core.domain.model.notification.Notification
import ru.gohasoft.wanderingtable.core.domain.model.notification.NotificationType
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.feature.main.fake.FakeDeviceRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeNotificationRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakePushTokenProvider
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter
import ru.gohasoft.wanderingtable.feature.main.fake.FakeUserRepository
import ru.gohasoft.wanderingtable.feature.main.games.GamesFilter

class MainShellViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var router: FakeRouter
    private lateinit var userRepository: FakeUserRepository
    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var deviceRepository: FakeDeviceRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        router = FakeRouter()
        userRepository = FakeUserRepository()
        notificationRepository = FakeNotificationRepository()
        deviceRepository = FakeDeviceRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting a tab switches content without touching the back stack`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(MainShellEvent.OnNavItemSelected(MainTab.PROFILE.navIndex))

        assertThat(viewModel.state.value.tab).isEqualTo(MainTab.PROFILE)
        assertThat(router.executedCommands).containsExactly()
    }

    @Test
    fun `the create slot opens the sheet instead of becoming a tab`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(MainShellEvent.OnNavItemSelected(MainTab.CREATE_NAV_INDEX))

        assertThat(viewModel.state.value.isCreateSheetVisible).isTrue()
        assertThat(viewModel.state.value.tab).isEqualTo(MainTab.HOME)
    }

    @Test
    fun `choosing an option closes the sheet and navigates`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(MainShellEvent.OnNavItemSelected(MainTab.CREATE_NAV_INDEX))

        viewModel.onEvent(MainShellEvent.OnFindOpponentClick)

        assertThat(viewModel.state.value.isCreateSheetVisible).isFalse()
        assertThat(router.executedCommands.last()).isInstanceOf(Forward::class)
    }

    @Test
    fun `the selected tab survives process death`() = runTest {
        val savedStateHandle = SavedStateHandle()
        createViewModel(savedStateHandle)
            .onEvent(MainShellEvent.OnNavItemSelected(MainTab.GAMES.navIndex))

        val restored = createViewModel(savedStateHandle)

        assertThat(restored.state.value.tab).isEqualTo(MainTab.GAMES)
    }

    @Test
    fun `opening games from another tab carries the requested filter once`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(MainShellEvent.OnOpenGames(GamesFilter.MY_GAMES))

        assertThat(viewModel.state.value.tab).isEqualTo(MainTab.GAMES)
        assertThat(viewModel.state.value.pendingGamesFilter).isEqualTo(GamesFilter.MY_GAMES)

        viewModel.onEvent(MainShellEvent.OnGamesFilterConsumed)

        assertThat(viewModel.state.value.pendingGamesFilter).isEqualTo(null)
    }

    @Test
    fun `posting news is offered only to accounts that hold the role`() = runTest {
        assertThat(createViewModel().state.value.canPostNews).isFalse()

        userRepository.profile = FakeUserRepository.profile(roles = listOf(Role.NEWS_CREATOR))

        assertThat(createViewModel().state.value.canPostNews).isTrue()
    }

    @Test
    fun `adding a game is offered only to accounts that hold the role`() = runTest {
        assertThat(createViewModel().state.value.canCreateGames).isFalse()

        userRepository.profile = FakeUserRepository.profile(roles = listOf(Role.GAME_CREATOR))

        assertThat(createViewModel().state.value.canCreateGames).isTrue()
    }

    /**
     * A club manager grants roles rather than inheriting them: the server still demands the
     * specific role for `POST /notes` and `POST /games`, so the sheet must not offer either.
     */
    @Test
    fun `club manager alone unlocks neither catalogue option`() = runTest {
        userRepository.profile = FakeUserRepository.profile(roles = listOf(Role.CLUB_MANAGER))

        val state = createViewModel().state.value

        assertThat(state.canPostNews).isFalse()
        assertThat(state.canCreateGames).isFalse()
    }

    @Test
    fun `adding a game closes the sheet and navigates`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(MainShellEvent.OnNavItemSelected(MainTab.CREATE_NAV_INDEX))

        viewModel.onEvent(MainShellEvent.OnCreateGameClick)

        assertThat(viewModel.state.value.isCreateSheetVisible).isFalse()
        assertThat(router.executedCommands.last()).isInstanceOf(Forward::class)
    }

    @Test
    fun `an unread notification lights the bell`() = runTest {
        notificationRepository.notifications.value = listOf(
            Notification(
                id = "1",
                title = "Opponent found",
                message = "Someone joined",
                createdAt = Instant.parse("2026-07-10T12:00:00Z"),
                isRead = false,
                type = NotificationType.OPPONENT_FOUND,
            )
        )

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.hasUnreadNotifications).isTrue()
    }

    @Test
    fun `the device binds itself to the account on launch`() = runTest {
        createViewModel()

        assertThat(deviceRepository.registeredTokens).containsExactly("fcm-token")
    }

    @Test
    fun `a build without messaging registers nothing`() = runTest {
        createViewModel(pushToken = null)

        assertThat(deviceRepository.registeredTokens).containsExactly()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        pushToken: String? = "fcm-token",
    ) = MainShellViewModel(
        savedStateHandle = savedStateHandle,
        router = router,
        userRepository = userRepository,
        notificationRepository = notificationRepository,
        deviceRepository = deviceRepository,
        pushTokenProvider = FakePushTokenProvider(pushToken),
    )
}
