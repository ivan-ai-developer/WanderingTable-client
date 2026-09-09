package ru.gohasoft.wanderingtable.feature.auth.resetcode

internal sealed interface EnterResetCodeEvent {
    data class OnCodeChanged(val value: String) : EnterResetCodeEvent
    data object OnSubmitClick : EnterResetCodeEvent
    data object OnResendClick : EnterResetCodeEvent
    data object OnBackToLoginClick : EnterResetCodeEvent
}
