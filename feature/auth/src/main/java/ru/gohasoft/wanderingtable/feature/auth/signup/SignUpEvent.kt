package ru.gohasoft.wanderingtable.feature.auth.signup

internal sealed interface SignUpEvent {
    data class OnNameChanged(val value: String) : SignUpEvent
    data class OnEmailChanged(val value: String) : SignUpEvent
    data class OnPasswordChanged(val value: String) : SignUpEvent
    data class OnConfirmPasswordChanged(val value: String) : SignUpEvent
    data object OnSignUpClick : SignUpEvent
    data object OnLoginClick : SignUpEvent
}
