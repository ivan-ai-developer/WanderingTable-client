package ru.gohasoft.wanderingtable.feature.auth.screen.forgotpassword

internal sealed interface ForgotPasswordEvent {
    data class OnEmailChanged(val value: String) : ForgotPasswordEvent
    data object OnSubmitClick : ForgotPasswordEvent
    data object OnBackToLoginClick : ForgotPasswordEvent
}
