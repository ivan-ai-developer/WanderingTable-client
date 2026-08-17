package ru.gohasoft.wanderingtable.feature.auth.forgotpassword

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
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
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.feature.auth.R
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeAuthRepository
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeRouter

class ForgotPasswordViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var router: FakeRouter
    private lateinit var viewModel: ForgotPasswordViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        router = FakeRouter()
        viewModel = ForgotPasswordViewModel(router, authRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `an invalid email blocks the request`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.OnEmailChanged("not-an-email"))
        viewModel.onEvent(ForgotPasswordEvent.OnSubmitClick)

        assertThat(authRepository.requestPasswordResetCallCount).isEqualTo(0)
        assertThat(viewModel.state.value.emailError).isNotNull()
        assertThat(viewModel.state.value.isLinkSent).isFalse()
    }

    @Test
    fun `a valid email is forwarded and switches to the confirmation`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(ForgotPasswordEvent.OnSubmitClick)

        assertThat(authRepository.requestPasswordResetCallCount).isEqualTo(1)
        assertThat(authRepository.lastPasswordResetEmail).isEqualTo("player@example.com")
        assertThat(viewModel.state.value.isLinkSent).isTrue()
        assertThat(viewModel.state.value.generalError).isNull()
    }

    /** The endpoint is not implemented server-side yet, so 404 is today's expected answer. */
    @Test
    fun `a 404 surfaces the unavailable message without navigating`() = runTest {
        authRepository.requestPasswordResetResult = Result.Error(NetworkException.NotFound())

        viewModel.onEvent(ForgotPasswordEvent.OnEmailChanged("player@example.com"))
        viewModel.onEvent(ForgotPasswordEvent.OnSubmitClick)

        assertThat(viewModel.state.value.generalError)
            .isEqualTo(TextResource.StringResource(R.string.auth_error_password_reset_unavailable))
        assertThat(viewModel.state.value.isLinkSent).isFalse()
        assertThat(router.executedCommands).isEmpty()
    }

    @Test
    fun `back to login navigates back`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.OnBackToLoginClick)

        assertThat(router.executedCommands.last()).isInstanceOf(Back::class)
    }
}
