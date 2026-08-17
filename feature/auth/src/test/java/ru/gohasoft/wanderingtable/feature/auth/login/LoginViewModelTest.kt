package ru.gohasoft.wanderingtable.feature.auth.login

import assertk.assertThat
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
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.NewRoot
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeAppEntryScreens
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeAuthRepository
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeRouter

class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var router: FakeRouter
    private lateinit var viewModel: LoginViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        router = FakeRouter()
        viewModel = LoginViewModel(router, authRepository, FakeAppEntryScreens())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invalid email blocks submit without calling the repository`() = runTest {
        viewModel.onEvent(LoginEvent.OnEmailChanged("not-an-email"))
        viewModel.onEvent(LoginEvent.OnPasswordChanged("whatever"))
        viewModel.onEvent(LoginEvent.OnLoginClick)

        assertThat(authRepository.logInCallCount).isEqualTo(0)
        assertThat(viewModel.state.value.emailError).isNotNull()
    }

    @Test
    fun `valid credentials and repository success navigate home`() = runTest {
        viewModel.onEvent(LoginEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(LoginEvent.OnPasswordChanged("anything"))
        viewModel.onEvent(LoginEvent.OnLoginClick)

        assertThat(authRepository.logInCallCount).isEqualTo(1)
        assertThat(router.executedCommands.last()).isInstanceOf(NewRoot::class)
    }

    @Test
    fun `wrong credentials surface an inline error without navigating`() = runTest {
        authRepository.logInResult = Result.Error(NetworkException.Unauthorized())

        viewModel.onEvent(LoginEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(LoginEvent.OnPasswordChanged("wrong-password"))
        viewModel.onEvent(LoginEvent.OnLoginClick)

        assertThat(viewModel.state.value.generalError).isNotNull()
        assertThat(router.executedCommands).isEmpty()
    }

    @Test
    fun `sign up click forwards to the sign up screen`() = runTest {
        viewModel.onEvent(LoginEvent.OnSignUpClick)

        assertThat(router.executedCommands.last()).isInstanceOf(Forward::class)
    }

    @Test
    fun `forgot password click forwards to the reset screen`() = runTest {
        viewModel.onEvent(LoginEvent.OnForgotPasswordClick)

        assertThat(router.executedCommands.last()).isInstanceOf(Forward::class)
    }
}
