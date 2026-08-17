package ru.gohasoft.wanderingtable.feature.auth.login

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
import ru.gohasoft.wanderingtable.core.presentation.navigation.AppEntryScreens
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Forward
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.NewRoot
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.auth.forgotpassword.ForgotPasswordScreen
import ru.gohasoft.wanderingtable.feature.auth.login.LoginEvent.OnEmailChanged
import ru.gohasoft.wanderingtable.feature.auth.login.LoginEvent.OnForgotPasswordClick
import ru.gohasoft.wanderingtable.feature.auth.login.LoginEvent.OnLoginClick
import ru.gohasoft.wanderingtable.feature.auth.login.LoginEvent.OnPasswordChanged
import ru.gohasoft.wanderingtable.feature.auth.login.LoginEvent.OnSignUpClick
import ru.gohasoft.wanderingtable.feature.auth.signup.SignUpScreen
import ru.gohasoft.wanderingtable.feature.auth.validation.AuthValidator
import ru.gohasoft.wanderingtable.feature.auth.validation.toFieldError
import ru.gohasoft.wanderingtable.feature.auth.validation.toLoginFieldError

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository,
    private val appEntryScreens: AppEntryScreens,
) : MviViewModel<LoginState, LoginEvent, Unit>() {

    private val _state = MutableStateFlow(LoginState())
    override val state: StateFlow<LoginState> = _state.asStateFlow()

    override fun onEvent(event: LoginEvent) {
        when (event) {
            is OnEmailChanged -> updateState(
                email = event.value,
                emailError = null,
                generalError = null
            )
            is OnPasswordChanged -> updateState(
                password = event.value,
                passwordError = null,
                generalError = null
            )
            OnLoginClick -> tryToLogIn()
            OnSignUpClick -> navigateToSignUpScreen()
            OnForgotPasswordClick -> navigateToForgotPasswordScreen()
        }
    }

    private fun tryToLogIn() {
        val email = _state.value.email
        val password = _state.value.password
        val emailError = AuthValidator.validateEmail(email)
        val passwordError = AuthValidator.validateLoginPassword(password)
        if (emailError != null || passwordError != null) {
            updateState(
                emailError = emailError?.toFieldError(),
                passwordError = passwordError?.toFieldError(),
            )
            return
        }
        viewModelScope.launch {
            val result = authRepository.logIn(email, password)
                .firstSuccessOrErrorResult()
                .orUnknownErrorResult()
            when (result) {
                is Result.Loading ->
                    updateState(
                        isLoading = true,
                        generalError = null
                    )
                is Result.Success ->
                    navigateToHomeScreen()
                is Result.Error ->
                    updateState(
                        isLoading = false,
                        generalError = result.error.toLoginFieldError()
                    )
            }
        }
    }

    private fun navigateToHomeScreen() {
        router.execute(NewRoot(appEntryScreens.home()))
    }

    private fun navigateToSignUpScreen() {
        router.execute(Forward(SignUpScreen))
    }

    private fun navigateToForgotPasswordScreen() {
        router.execute(Forward(ForgotPasswordScreen))
    }

    private fun updateState(
        email: String = state.value.email,
        password: String = state.value.password,
        emailError: TextResource? = state.value.emailError,
        passwordError: TextResource? = state.value.passwordError,
        generalError: TextResource? = state.value.generalError,
        isLoading: Boolean = state.value.isLoading,
    ) {
        _state.update {
            it.copy(
                email = email,
                password = password,
                emailError = emailError,
                passwordError = passwordError,
                generalError = generalError,
                isLoading = isLoading
            )
        }
    }
}
