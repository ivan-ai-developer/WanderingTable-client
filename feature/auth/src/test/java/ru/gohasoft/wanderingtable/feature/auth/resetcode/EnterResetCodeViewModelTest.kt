package ru.gohasoft.wanderingtable.feature.auth.resetcode

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
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
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.BackTo
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.feature.auth.R
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeAuthRepository
import ru.gohasoft.wanderingtable.feature.auth.fake.FakeRouter

class EnterResetCodeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var router: FakeRouter
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: EnterResetCodeViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        router = FakeRouter()
        savedStateHandle = SavedStateHandle()
        viewModel = createViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the email argument reaches the state`() = runTest {
        assertThat(viewModel.state.value.email).isEqualTo(EMAIL)
    }

    @Test
    fun `a code that is not six digits never reaches the network`() = runTest {
        viewModel.onEvent(EnterResetCodeEvent.OnCodeChanged("12ab"))
        viewModel.onEvent(EnterResetCodeEvent.OnSubmitClick)

        assertThat(authRepository.verifyPasswordResetCodeCallCount).isEqualTo(0)
        assertThat(viewModel.state.value.codeError).isNotNull()
        assertThat(router.executedCommands).isEmpty()
    }

    @Test
    fun `a valid code is exchanged and forwards to the new password screen`() = runTest {
        viewModel.onEvent(EnterResetCodeEvent.OnCodeChanged("123456"))
        viewModel.onEvent(EnterResetCodeEvent.OnSubmitClick)

        assertThat(authRepository.verifyPasswordResetCodeCallCount).isEqualTo(1)
        assertThat(authRepository.lastVerifiedCode).isEqualTo("123456")
        assertThat(router.executedCommands.last()).isInstanceOf(Forward::class)
    }

    /** The server spends the code on the attempt, so the message has to point at a resend. */
    @Test
    fun `a rejected code stays on the screen and asks for a new one`() = runTest {
        authRepository.verifyPasswordResetCodeResult = Result.Error(NetworkException.Unauthorized())

        viewModel.onEvent(EnterResetCodeEvent.OnCodeChanged("123456"))
        viewModel.onEvent(EnterResetCodeEvent.OnSubmitClick)

        assertThat(viewModel.state.value.generalError)
            .isEqualTo(TextResource.StringResource(R.string.auth_error_reset_code_rejected))
        assertThat(router.executedCommands).isEmpty()
    }

    /** A success carrying no token means an unreadable response, not a usable reset. */
    @Test
    fun `a success without a token is reported as an error`() = runTest {
        authRepository.verifyPasswordResetCodeResult = Result.Success(null)

        viewModel.onEvent(EnterResetCodeEvent.OnCodeChanged("123456"))
        viewModel.onEvent(EnterResetCodeEvent.OnSubmitClick)

        assertThat(viewModel.state.value.generalError)
            .isEqualTo(TextResource.StringResource(R.string.auth_error_generic))
        assertThat(router.executedCommands).isEmpty()
    }

    @Test
    fun `resending clears the stale code and confirms`() = runTest {
        viewModel.onEvent(EnterResetCodeEvent.OnCodeChanged("123456"))

        viewModel.onEvent(EnterResetCodeEvent.OnResendClick)

        assertThat(authRepository.requestPasswordResetCallCount).isEqualTo(1)
        assertThat(authRepository.lastPasswordResetEmail).isEqualTo(EMAIL)
        assertThat(viewModel.state.value.code).isEqualTo("")
        assertThat(viewModel.state.value.isCodeResent).isTrue()
    }

    @Test
    fun `the typed code survives process death`() = runTest {
        viewModel.onEvent(EnterResetCodeEvent.OnCodeChanged("654321"))

        val restored = createViewModel()

        assertThat(restored.state.value.code).isEqualTo("654321")
        assertThat(restored.state.value.isCodeResent).isFalse()
    }

    @Test
    fun `back to login unwinds the whole reset flow`() = runTest {
        viewModel.onEvent(EnterResetCodeEvent.OnBackToLoginClick)

        assertThat(router.executedCommands.last()).isInstanceOf(BackTo::class)
    }

    private fun createViewModel() = EnterResetCodeViewModel(
        screen = EnterResetCodeScreen(EMAIL),
        savedStateHandle = savedStateHandle,
        router = router,
        authRepository = authRepository,
    )

    private companion object {
        const val EMAIL = "player@example.com"
    }
}
