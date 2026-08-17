package ru.gohasoft.wanderingtable.feature.auth.signup

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
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.NewRoot
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.feature.auth.R
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeAppEntryScreens
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeAuthRepository
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeRouter

class SignUpViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var router: FakeRouter
    private lateinit var viewModel: SignUpViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        router = FakeRouter()
        viewModel = SignUpViewModel(router, authRepository, FakeAppEntryScreens())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `weak password blocks submit without calling the repository`() = runTest {
        viewModel.onEvent(SignUpEvent.OnNameChanged("Ivan"))
        viewModel.onEvent(SignUpEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(SignUpEvent.OnPasswordChanged("weak"))
        viewModel.onEvent(SignUpEvent.OnConfirmPasswordChanged("weak"))
        viewModel.onEvent(SignUpEvent.OnSignUpClick)

        assertThat(authRepository.signUpCallCount).isEqualTo(0)
        assertThat(viewModel.state.value.passwordError).isNotNull()
    }

    @Test
    fun `mismatched confirm password blocks submit`() = runTest {
        viewModel.onEvent(SignUpEvent.OnNameChanged("Ivan"))
        viewModel.onEvent(SignUpEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(SignUpEvent.OnPasswordChanged("Password1"))
        viewModel.onEvent(SignUpEvent.OnConfirmPasswordChanged("Password2"))
        viewModel.onEvent(SignUpEvent.OnSignUpClick)

        assertThat(authRepository.signUpCallCount).isEqualTo(0)
        assertThat(viewModel.state.value.confirmPasswordError).isNotNull()
    }

    @Test
    fun `blank name blocks submit without calling the repository`() = runTest {
        viewModel.onEvent(SignUpEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(SignUpEvent.OnPasswordChanged("Password1"))
        viewModel.onEvent(SignUpEvent.OnConfirmPasswordChanged("Password1"))
        viewModel.onEvent(SignUpEvent.OnSignUpClick)

        assertThat(authRepository.signUpCallCount).isEqualTo(0)
        assertThat(viewModel.state.value.nameError).isNotNull()
    }

    @Test
    fun `a name longer than 64 characters blocks submit`() = runTest {
        viewModel.onEvent(SignUpEvent.OnNameChanged("и".repeat(65)))
        viewModel.onEvent(SignUpEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(SignUpEvent.OnPasswordChanged("Password1"))
        viewModel.onEvent(SignUpEvent.OnConfirmPasswordChanged("Password1"))
        viewModel.onEvent(SignUpEvent.OnSignUpClick)

        assertThat(authRepository.signUpCallCount).isEqualTo(0)
        assertThat(viewModel.state.value.nameError).isNotNull()
    }

    @Test
    fun `valid input forwards the name to the repository`() = runTest {
        viewModel.onEvent(SignUpEvent.OnNameChanged("Ivan"))
        viewModel.onEvent(SignUpEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(SignUpEvent.OnPasswordChanged("Password1"))
        viewModel.onEvent(SignUpEvent.OnConfirmPasswordChanged("Password1"))
        viewModel.onEvent(SignUpEvent.OnSignUpClick)

        assertThat(authRepository.signUpCallCount).isEqualTo(1)
        assertThat(authRepository.lastSignUpName).isEqualTo("Ivan")
        assertThat(router.executedCommands.last()).isInstanceOf(NewRoot::class)
    }

    @Test
    fun `email already taken surfaces an inline error without navigating`() = runTest {
        authRepository.signUpResult = Result.Error(NetworkException.Conflict())

        viewModel.onEvent(SignUpEvent.OnNameChanged("Ivan"))
        viewModel.onEvent(SignUpEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(SignUpEvent.OnPasswordChanged("Password1"))
        viewModel.onEvent(SignUpEvent.OnConfirmPasswordChanged("Password1"))
        viewModel.onEvent(SignUpEvent.OnSignUpClick)

        assertThat(viewModel.state.value.generalError).isNotNull()
        assertThat(router.executedCommands).isEmpty()
    }

    /**
     * The mapper used to compare `this` against freshly built exception instances, which is
     * identity comparison for a plain class, so every validation failure fell through to the
     * generic message. Assert the specific string, not just that an error exists.
     */
    @Test
    fun `a validation failure maps to its own message, not the generic one`() = runTest {
        viewModel.onEvent(SignUpEvent.OnNameChanged("Ivan"))
        viewModel.onEvent(SignUpEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(SignUpEvent.OnPasswordChanged("Pass1"))
        viewModel.onEvent(SignUpEvent.OnConfirmPasswordChanged("Pass1"))
        viewModel.onEvent(SignUpEvent.OnSignUpClick)

        assertThat(viewModel.state.value.passwordError)
            .isEqualTo(TextResource.StringResource(R.string.auth_error_password_too_short))
    }

    @Test
    fun `login click navigates back`() = runTest {
        viewModel.onEvent(SignUpEvent.OnLoginClick)

        assertThat(router.executedCommands.last()).isInstanceOf(Back::class)
    }
}
