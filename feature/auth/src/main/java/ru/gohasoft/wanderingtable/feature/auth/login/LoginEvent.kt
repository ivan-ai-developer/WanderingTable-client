package ru.gohasoft.wanderingtable.feature.auth.login

internal sealed interface LoginEvent {
    data class OnEmailChanged(val value: String) : LoginEvent
    data class OnPasswordChanged(val value: String) : LoginEvent
    data object OnLoginClick : LoginEvent
    data object OnSignUpClick : LoginEvent
    data object OnForgotPasswordClick : LoginEvent
}
