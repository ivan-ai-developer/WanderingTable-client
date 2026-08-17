package ru.gohasoft.wanderingtable.feature.auth.signup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gohasoft.wanderingtable.core.domain.Result
import ru.gohasoft.wanderingtable.core.domain.exception.UnknownException
import ru.gohasoft.wanderingtable.core.domain.firstSuccessOrErrorResult
import ru.gohasoft.wanderingtable.core.domain.repository.AuthRepository
import ru.gohasoft.wanderingtable.core.presentation.navigation.AppEntryScreens
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.Back
import ru.gohasoft.wanderingtable.core.presentation.navigation.command.NewRoot
import ru.gohasoft.wanderingtable.core.presentation.navigation.router.Router
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.viewmodel.MviViewModel
import ru.gohasoft.wanderingtable.feature.auth.R
import ru.gohasoft.wanderingtable.feature.auth.signup.SignUpEvent.OnConfirmPasswordChanged
import ru.gohasoft.wanderingtable.feature.auth.signup.SignUpEvent.OnEmailChanged
import ru.gohasoft.wanderingtable.feature.auth.signup.SignUpEvent.OnLoginClick
import ru.gohasoft.wanderingtable.feature.auth.signup.SignUpEvent.OnNameChanged
import ru.gohasoft.wanderingtable.feature.auth.signup.SignUpEvent.OnPasswordChanged
import ru.gohasoft.wanderingtable.feature.auth.signup.SignUpEvent.OnSignUpClick
import ru.gohasoft.wanderingtable.feature.auth.validation.AuthValidator
import ru.gohasoft.wanderingtable.feature.auth.validation.toFieldError
import ru.gohasoft.wanderingtable.feature.auth.validation.toSignUpFieldError

@HiltViewModel
internal class SignUpViewModel @Inject constructor(
    private val router: Router,
    private val authRepository: AuthRepository,
    private val appEntryScreens: AppEntryScreens,
) : MviViewModel<SignUpState, SignUpEvent, Unit>() {

    private val _state = MutableStateFlow(SignUpState())
    override val state: StateFlow<SignUpState> = _state.asStateFlow()

    override fun onEvent(event: SignUpEvent) {
        when (event) {
            is OnNameChanged -> updateState(
                name = event.value,
                nameError = null,
                generalError = null
            )
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
            is OnConfirmPasswordChanged -> _state.update {
                it.copy(
                    confirmPassword = event.value,
                    confirmPasswordError = null,
                    generalError = null
                )
            }
            OnSignUpClick -> tryToSignUp()
            OnLoginClick -> navigateBack()
        }
    }

    private fun tryToSignUp() {
        val name = state.value.name
        val email = state.value.email
        val password = state.value.password
        val confirmPassword = state.value.confirmPassword

        val nameError = AuthValidator.validateName(name)
        val emailError = AuthValidator.validateEmail(email)
        val passwordError = AuthValidator.validatePassword(password)
        val confirmPasswordError = if (password != confirmPassword) {
            TextResource.StringResource(R.string.signup_error_password_mismatch)
        } else {
            null
        }
        if (nameError != null ||
            emailError != null ||
            passwordError != null ||
            confirmPasswordError != null
        ) {
            updateState(
                nameError = nameError?.toFieldError(),
                emailError = emailError?.toFieldError(),
                passwordError = passwordError?.toFieldError(),
                confirmPasswordError = confirmPasswordError,
            )
            return
        }

        viewModelScope.launch {
            val result = authRepository.signUp(
                name = name,
                email = email,
                password = password
            ).firstSuccessOrErrorResult()
                ?: Result.Error(UnknownException())
            when (result) {
                is Result.Loading ->
                    updateState(
                        isLoading = true,
                        generalError = null
                    )
                is Result.Success ->
                    router.execute(NewRoot(appEntryScreens.home()))
                is Result.Error ->
                    updateState(
                        isLoading = false,
                        generalError = result.error.toSignUpFieldError()
                    )
            }
        }
    }

    private fun updateState(
        name: String = state.value.name,
        email: String = state.value.email,
        password: String = state.value.password,
        confirmPassword: String = state.value.confirmPassword,
        nameError: TextResource? = state.value.nameError,
        emailError: TextResource? = state.value.emailError,
        passwordError: TextResource? = state.value.passwordError,
        confirmPasswordError: TextResource? = state.value.confirmPasswordError,
        generalError: TextResource? = state.value.generalError,
        isLoading: Boolean = state.value.isLoading,
    ) {
        _state.update {
            it.copy(
                name = name,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                generalError = generalError,
                isLoading = isLoading
            )
        }
    }

    private fun navigateBack() {
        router.execute(Back())
    }
}
