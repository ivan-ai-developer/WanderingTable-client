package ru.gohasoft.wanderingtable.feature.auth.forgotpassword

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.orUnknownErrorResult
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.auth.forgotpassword.ForgotPasswordEvent.OnBackToLoginClick
import ru.gohasoft.wanderingtable.feature.auth.forgotpassword.ForgotPasswordEvent.OnEmailChanged
import ru.gohasoft.wanderingtable.feature.auth.forgotpassword.ForgotPasswordEvent.OnSubmitClick
import ru.gohasoft.wanderingtable.feature.auth.validation.AuthValidator
import ru.gohasoft.wanderingtable.feature.auth.validation.toFieldError
import ru.gohasoft.wanderingtable.feature.auth.validation.toPasswordResetFieldError

/**
 * The reset request is expected to fail today — the server has no `auth/forgot-password` endpoint,
 * so the 404 surfaces as an inline "not available yet" message. When the endpoint lands, only the
 * [toPasswordResetFieldError] mapping needs to change.
 */
@HiltViewModel
internal class ForgotPasswordViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository,
) : MviViewModel<ForgotPasswordState, ForgotPasswordEvent, Unit>() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    override val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    override fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is OnEmailChanged -> updateState(
                email = event.value,
                emailError = null,
                generalError = null
            )
            OnSubmitClick -> requestResetLink()
            OnBackToLoginClick -> navigateBack()
        }
    }

    private fun navigateBack() {
        router.execute(Back())
    }

    private fun requestResetLink() {
        val email = _state.value.email
        val emailError = AuthValidator.validateEmail(email)
        if (emailError != null) {
            updateState(emailError = emailError.toFieldError())
            return
        }
        viewModelScope.launch {
            val result = authRepository.requestPasswordReset(email)
                .firstSuccessOrErrorResult()
                .orUnknownErrorResult()
            when (result) {
                is Result.Loading ->
                    updateState(
                        isLoading = true,
                        generalError = null
                    )
                is Result.Success ->
                    updateState(
                        isLoading = false,
                        isLinkSent = true
                    )
                is Result.Error ->
                    updateState(
                        isLoading = false,
                        generalError = result.error.toPasswordResetFieldError()
                    )
            }
        }
    }

    private fun updateState(
        email: String = state.value.email,
        emailError: TextResource? = state.value.emailError,
        generalError: TextResource? = state.value.generalError,
        isLoading: Boolean = state.value.isLoading,
        isLinkSent: Boolean = state.value.isLinkSent,
    ) {
        _state.update {
            it.copy(
                email = email,
                emailError = emailError,
                generalError = generalError,
                isLoading = isLoading,
                isLinkSent = isLinkSent
            )
        }
    }
}
