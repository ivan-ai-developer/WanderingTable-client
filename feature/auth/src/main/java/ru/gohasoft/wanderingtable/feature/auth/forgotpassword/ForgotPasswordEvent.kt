package ru.gohasoft.wanderingtable.feature.auth.forgotpassword

internal sealed interface ForgotPasswordEvent {
    data class OnEmailChanged(val value: String) : ForgotPasswordEvent
    data object OnSubmitClick : ForgotPasswordEvent
    data object OnBackToLoginClick : ForgotPasswordEvent
}
