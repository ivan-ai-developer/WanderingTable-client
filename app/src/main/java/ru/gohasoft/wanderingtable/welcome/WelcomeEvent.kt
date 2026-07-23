package ru.gohasoft.wanderingtable.welcome

sealed interface WelcomeEvent {
    data object OnShowSnackbarClick : WelcomeEvent
}