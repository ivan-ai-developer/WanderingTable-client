package ru.gohasoft.wanderingtable.feature.auth.screen.newpassword

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
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.ShowSnackbar
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.auth.R
import ru.gohasoft.wanderingtable.feature.auth.screen.login.LoginScreen
import ru.gohasoft.wanderingtable.feature.auth.validation.AuthValidator
import ru.gohasoft.wanderingtable.feature.auth.mapper.toFieldError
import ru.gohasoft.wanderingtable.feature.auth.mapper.toNewPasswordFieldError

/**
 * The reset token arrives as a screen argument and is never persisted: unlike the other auth forms
 * nothing here goes into [androidx.lifecycle.SavedStateHandle], because both fields hold a password
 * and saved state is written to disk.
 *
 * Success ends on the login screen rather than in the app — the server revokes every session as
 * part of the reset, so there is no signed-in state to continue into.
 */
@HiltViewModel(assistedFactory = NewPasswordViewModel.Factory::class)
internal class NewPasswordViewModel @AssistedInject constructor(
    @Assisted private val screen: NewPasswordScreen,
    private val router: Router,
    private val authRepository: AuthRepository,
) : MviViewModel<NewPasswordState, NewPasswordEvent, Unit>() {

    @AssistedFactory
    interface Factory {
        fun create(screen: NewPasswordScreen): NewPasswordViewModel
    }

    private val _state = MutableStateFlow(NewPasswordState())
    override val state: StateFlow<NewPasswordState> = _state.asStateFlow()

    override fun onEvent(event: NewPasswordEvent) {
        when (event) {
            is NewPasswordEvent.OnPasswordChanged -> updateState(
                password = event.value,
                passwordError = null,
                generalError = null
            )
            is NewPasswordEvent.OnConfirmPasswordChanged -> updateState(
                confirmPassword = event.value,
                confirmPasswordError = null,
                generalError = null
            )
            NewPasswordEvent.OnSubmitClick -> savePassword()
        }
    }

    private fun savePassword() {
        val password = state.value.password
        val confirmPassword = state.value.confirmPassword

        val passwordError = AuthValidator.validatePassword(password)
        val confirmPasswordError = if (password != confirmPassword) {
            StringResource(R.string.signup_error_password_mismatch)
        } else {
            null
        }
        if (passwordError != null || confirmPasswordError != null) {
            updateState(
                passwordError = passwordError?.toFieldError(),
                confirmPasswordError = confirmPasswordError,
            )
            return
        }

        viewModelScope.launch {
            val result = authRepository.resetPassword(screen.resetToken, password)
                .firstSuccessOrErrorResult()
                .orUnknownErrorResult()
            when (result) {
                is Result.Loading ->
                    updateState(
                        isLoading = true,
                        generalError = null
                    )
                is Result.Success -> {
                    updateState(isLoading = false)
                    navigateToLogin()
                }
                is Result.Error ->
                    updateState(
                        isLoading = false,
                        generalError = result.error.toNewPasswordFieldError()
                    )
            }
        }
    }

    private fun navigateToLogin() {
        router.execute(
            ShowSnackbar(
                SnackbarScreenConfig {
                    message(StringResource(R.string.new_password_updated_message))
                }
            ),
            BackTo<LoginScreen>(),
        )
    }

    private fun updateState(
        password: String = state.value.password,
        confirmPassword: String = state.value.confirmPassword,
        passwordError: TextResource? = state.value.passwordError,
        confirmPasswordError: TextResource? = state.value.confirmPasswordError,
        generalError: TextResource? = state.value.generalError,
        isLoading: Boolean = state.value.isLoading,
    ) {
        _state.update {
            it.copy(
                password = password,
                confirmPassword = confirmPassword,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                generalError = generalError,
                isLoading = isLoading
            )
        }
    }
}
