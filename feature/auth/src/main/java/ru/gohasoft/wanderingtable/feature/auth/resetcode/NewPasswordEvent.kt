package ru.gohasoft.wanderingtable.feature.auth.resetcode

internal sealed interface NewPasswordEvent {
    data class OnPasswordChanged(val value: String) : NewPasswordEvent
    data class OnConfirmPasswordChanged(val value: String) : NewPasswordEvent
    data object OnSubmitClick : NewPasswordEvent
}
