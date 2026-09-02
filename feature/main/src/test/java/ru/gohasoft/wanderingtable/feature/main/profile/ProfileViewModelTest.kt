package ru.gohasoft.wanderingtable.feature.main.profile

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
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
import ru.gohasoft.wanderingtable.core.domain.model.user.Role
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.NewRoot
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.feature.main.fake.FakeAppEntryScreens
import ru.gohasoft.wanderingtable.feature.main.fake.FakeAuthRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeDeviceRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakePushTokenProvider
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter
import ru.gohasoft.wanderingtable.feature.main.fake.FakeUserRepository

class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var router: FakeRouter
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var deviceRepository: FakeDeviceRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        router = FakeRouter()
        authRepository = FakeAuthRepository()
        userRepository = FakeUserRepository()
        deviceRepository = FakeDeviceRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the profile loads into the header`() = runTest {
        val viewModel = createViewModel()

        val profile = viewModel.state.value.profile
        assertThat(profile).isNotNull()
        assertThat(profile?.initials).isEqualTo("AN")
        assertThat(profile?.wins).isEqualTo("19")
    }

    @Test
    fun `logging out unbinds this device before clearing the session`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.OnLogOutClick)

        assertThat(deviceRepository.unregisteredTokens).containsExactly("fcm-token")
        assertThat(authRepository.logOutCallCount).isEqualTo(1)
        assertThat(router.executedCommands.last()).isInstanceOf(NewRoot::class)
    }

    @Test
    fun `a build without messaging still logs out`() = runTest {
        val viewModel = createViewModel(pushToken = null)

        viewModel.onEvent(ProfileEvent.OnLogOutClick)

        assertThat(deviceRepository.unregisteredTokens).isEmpty()
        assertThat(authRepository.logOutCallCount).isEqualTo(1)
        assertThat(router.executedCommands.last()).isInstanceOf(NewRoot::class)
    }

    @Test
    fun `a second tap on log out does not sign out twice`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.OnLogOutClick)
        viewModel.onEvent(ProfileEvent.OnLogOutClick)

        assertThat(authRepository.logOutCallCount).isEqualTo(1)
    }

    /** The row is the only way into role management, so its flag must follow the actual role. */
    @Test
    fun `club administration is offered only to a club manager`() = runTest {
        assertThat(createViewModel().state.value.profile?.isClubManager).isEqualTo(false)

        userRepository.profile = FakeUserRepository.profile(roles = listOf(Role.CLUB_MANAGER))

        assertThat(createViewModel().state.value.profile?.isClubManager).isEqualTo(true)
    }

    @Test
    fun `club administration navigates`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.OnClubAdminClick)

        assertThat(router.executedCommands.last()).isInstanceOf(Forward::class)
    }

    @Test
    fun `notification settings is a real destination`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.OnNotificationSettingsClick)

        assertThat(router.executedCommands.last()).isInstanceOf(Forward::class)
    }

    @Test
    fun `undesigned rows say so instead of navigating nowhere`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.OnAccountSettingsClick)

        assertThat(router.executedCommands.last()).isInstanceOf(ShowSnackbar::class)
    }

    @Test
    fun `a failed profile read shows an error`() = runTest {
        userRepository.getProfileResult = Result.Error(NetworkException.NoInternet())

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.error).isNotNull()
    }

    private fun createViewModel(pushToken: String? = "fcm-token") = ProfileViewModel(
        router = router,
        appEntryScreens = FakeAppEntryScreens(),
        authRepository = authRepository,
        userRepository = userRepository,
        deviceRepository = deviceRepository,
        pushTokenProvider = FakePushTokenProvider(pushToken),
    )
}
