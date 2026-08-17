package ru.gohasoft.wanderingtable.feature.main.welcome

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.model.Session
import ru.gohasoft.wanderingtable.core.domain.model.user.User
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.NewRoot
import ru.gohasoft.wanderingtable.feature.main.fake.FakeAppEntryScreens
import ru.gohasoft.wanderingtable.feature.main.fake.FakeAuthRepository
import ru.gohasoft.wanderingtable.feature.main.fake.FakeRouter

class WelcomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var router: FakeRouter

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        router = FakeRouter()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `existing session populates the user's email`() = runTest {
        authRepository.getSessionResult =
            Result.Success(Session(User(id = "1", name = "", email = "player@example.com")))

        val viewModel = WelcomeViewModel(router, authRepository, FakeAppEntryScreens())

        assertThat(viewModel.state.value.userEmail).isEqualTo("player@example.com")
    }

    @Test
    fun `no session navigates back to login`() = runTest {
        authRepository.getSessionResult = Result.Success(null)

        WelcomeViewModel(router, authRepository, FakeAppEntryScreens())

        assertThat(router.executedCommands.last()).isInstanceOf(NewRoot::class)
    }

    @Test
    fun `logout navigates to login`() = runTest {
        authRepository.getSessionResult =
            Result.Success(Session(User(id = "1", name = "", email = "player@example.com")))
        val viewModel = WelcomeViewModel(router, authRepository, FakeAppEntryScreens())

        viewModel.onEvent(WelcomeEvent.OnLogoutClick)

        assertThat(authRepository.logOutCallCount).isEqualTo(1)
        assertThat(router.executedCommands.last()).isInstanceOf(NewRoot::class)
    }
}
