package ru.gohasoft.wanderingtable.feature.auth.resetcode

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
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.BackTo
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.feature.auth.R
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeAuthRepository
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeRouter
import ru.gohasoft.wanderingtable.feature.auth.screen.newpassword.NewPasswordEvent
import ru.gohasoft.wanderingtable.feature.auth.screen.newpassword.NewPasswordScreen
import ru.gohasoft.wanderingtable.feature.auth.screen.newpassword.NewPasswordViewModel

class NewPasswordViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var router: FakeRouter
    private lateinit var viewModel: NewPasswordViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        router = FakeRouter()
        viewModel = NewPasswordViewModel(
            screen = NewPasswordScreen(RESET_TOKEN),
            router = router,
            authRepository = authRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a weak password never reaches the network`() = runTest {
        submit(password = "short", confirmPassword = "short")

        assertThat(authRepository.resetPasswordCallCount).isEqualTo(0)
        assertThat(viewModel.state.value.passwordError).isNotNull()
    }

    @Test
    fun `a mismatched confirmation blocks the request`() = runTest {
        submit(password = NEW_PASSWORD, confirmPassword = "Password9")

        assertThat(authRepository.resetPasswordCallCount).isEqualTo(0)
        assertThat(viewModel.state.value.confirmPasswordError)
            .isEqualTo(TextResource.StringResource(R.string.signup_error_password_mismatch))
    }

    /** The server revokes every session, so the flow can only end back on the login screen. */
    @Test
    fun `a saved password announces itself and returns to login`() = runTest {
        submit(password = NEW_PASSWORD, confirmPassword = NEW_PASSWORD)

        assertThat(authRepository.resetPasswordCallCount).isEqualTo(1)
        assertThat(authRepository.lastResetToken).isEqualTo(RESET_TOKEN)
        assertThat(authRepository.lastNewPassword).isEqualTo(NEW_PASSWORD)
        assertThat(router.executedCommands.first()).isInstanceOf(ShowSnackbar::class)
        assertThat(router.executedCommands.last()).isInstanceOf(BackTo::class)
    }

    @Test
    fun `an expired reset token surfaces its own message`() = runTest {
        authRepository.resetPasswordResult = Result.Error(NetworkException.Unauthorized())

        submit(password = NEW_PASSWORD, confirmPassword = NEW_PASSWORD)

        assertThat(viewModel.state.value.generalError)
            .isEqualTo(TextResource.StringResource(R.string.auth_error_reset_request_expired))
        assertThat(router.executedCommands).isEmpty()
    }

    private fun submit(password: String, confirmPassword: String) {
        viewModel.onEvent(NewPasswordEvent.OnPasswordChanged(password))
        viewModel.onEvent(NewPasswordEvent.OnConfirmPasswordChanged(confirmPassword))
        viewModel.onEvent(NewPasswordEvent.OnSubmitClick)
    }

    private companion object {
        const val RESET_TOKEN = "reset-token"
        const val NEW_PASSWORD = "Password1"
    }
}
