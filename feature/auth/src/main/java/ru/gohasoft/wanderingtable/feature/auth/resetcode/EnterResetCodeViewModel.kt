package ru.gohasoft.wanderingtable.feature.auth.resetcode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.orUnknownErrorResult
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.BackTo
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.auth.R
import ru.gohasoft.wanderingtable.feature.auth.login.LoginScreen
import ru.gohasoft.wanderingtable.feature.auth.resetcode.EnterResetCodeEvent.OnBackToLoginClick
import ru.gohasoft.wanderingtable.feature.auth.resetcode.EnterResetCodeEvent.OnCodeChanged
import ru.gohasoft.wanderingtable.feature.auth.resetcode.EnterResetCodeEvent.OnResendClick
import ru.gohasoft.wanderingtable.feature.auth.resetcode.EnterResetCodeEvent.OnSubmitClick
import ru.gohasoft.wanderingtable.feature.auth.validation.AuthValidator
import ru.gohasoft.wanderingtable.feature.auth.validation.toFieldError
import ru.gohasoft.wanderingtable.feature.auth.validation.toPasswordResetFieldError
import ru.gohasoft.wanderingtable.feature.auth.validation.toResetCodeFieldError

/**
 * The code is spent by the attempt whether or not it matched, so a rejected code cannot be retyped —
 * the screen offers a resend instead, and [toResetCodeFieldError] words the 401 that way.
 */
@HiltViewModel(assistedFactory = EnterResetCodeViewModel.Factory::class)
internal class EnterResetCodeViewModel @AssistedInject constructor(
    @Assisted private val screen: EnterResetCodeScreen,
    private val savedStateHandle: SavedStateHandle,
    private val router: Router,
    private val authRepository: AuthRepository,
) : MviViewModel<EnterResetCodeState, EnterResetCodeEvent, Unit>() {

    @AssistedFactory
    interface Factory {
        fun create(screen: EnterResetCodeScreen): EnterResetCodeViewModel
    }

    private val _state = MutableStateFlow(
        EnterResetCodeState(
            email = screen.email,
            code = savedStateHandle[KEY_CODE] ?: "",
        )
    )
    override val state: StateFlow<EnterResetCodeState> = _state.asStateFlow()

    override fun onEvent(event: EnterResetCodeEvent) {
        when (event) {
            is OnCodeChanged -> saveCode(event.value)
            OnSubmitClick -> verifyCode()
            OnResendClick -> resendCode()
            OnBackToLoginClick -> navigateToLogin()
        }
    }

    private fun saveCode(value: String) {
        savedStateHandle[KEY_CODE] = value
        updateState(
            code = value,
            codeError = null,
            generalError = null,
            isCodeResent = false
        )
    }

    /** Leaves the whole reset flow: the intermediate screens are of no use once it is abandoned. */
    private fun navigateToLogin() {
        router.execute(BackTo<LoginScreen>())
    }

    private fun verifyCode() {
        val code = _state.value.code
        val codeError = AuthValidator.validateResetCode(code)
        if (codeError != null) {
            updateState(codeError = codeError.toFieldError())
            return
        }
        viewModelScope.launch {
            val result = authRepository.verifyPasswordResetCode(_state.value.email, code)
                .firstSuccessOrErrorResult()
                .orUnknownErrorResult()
            when (result) {
                is Result.Loading ->
                    updateState(
                        isLoading = true,
                        generalError = null,
                        isCodeResent = false
                    )
                // A success without a token can only mean a response the client cannot read;
                // there is nothing to carry to the next screen, so it is reported as an error.
                is Result.Success -> when (val resetToken = result.data) {
                    null -> updateState(
                        isLoading = false,
                        generalError = StringResource(R.string.auth_error_generic)
                    )
                    else -> {
                        updateState(isLoading = false)
                        router.execute(Forward(NewPasswordScreen(resetToken)))
                    }
                }
                is Result.Error ->
                    updateState(
                        isLoading = false,
                        generalError = result.error.toResetCodeFieldError()
                    )
            }
        }
    }

    private fun resendCode() {
        viewModelScope.launch {
            val result = authRepository.requestPasswordReset(_state.value.email)
                .firstSuccessOrErrorResult()
                .orUnknownErrorResult()
            when (result) {
                is Result.Loading ->
                    updateState(
                        isLoading = true,
                        generalError = null,
                        isCodeResent = false
                    )
                is Result.Success -> {
                    // The previous code is dead server-side, so anything already typed is stale.
                    savedStateHandle[KEY_CODE] = ""
                    updateState(
                        code = "",
                        codeError = null,
                        isLoading = false,
                        isCodeResent = true
                    )
                }
                is Result.Error ->
                    updateState(
                        isLoading = false,
                        generalError = result.error.toPasswordResetFieldError()
                    )
            }
        }
    }

    private fun updateState(
        code: String = state.value.code,
        codeError: TextResource? = state.value.codeError,
        generalError: TextResource? = state.value.generalError,
        isLoading: Boolean = state.value.isLoading,
        isCodeResent: Boolean = state.value.isCodeResent,
    ) {
        _state.update {
            it.copy(
                code = code,
                codeError = codeError,
                generalError = generalError,
                isLoading = isLoading,
                isCodeResent = isCodeResent
            )
        }
    }

    private companion object {
        const val KEY_CODE = "reset_code"
    }
}
